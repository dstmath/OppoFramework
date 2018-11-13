package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class IZatXTRADebugReport implements Parcelable {
    private static final int BDS_XTRA_DATA_AVAILABLE = 4;
    public static final Creator<IZatXTRADebugReport> CREATOR = new Creator<IZatXTRADebugReport>() {
        public IZatXTRADebugReport createFromParcel(Parcel source) {
            return new IZatXTRADebugReport(source);
        }

        public IZatXTRADebugReport[] newArray(int size) {
            return new IZatXTRADebugReport[size];
        }
    };
    private static final int GAL_XTRA_DATA_AVAILABLE = 8;
    private static final int GLONASS_XTRA_DATA_AVAILABLE = 2;
    private static final int GPS_XTRA_DATA_AVAILABLE = 1;
    private static final int QZSS_XTRA_DATA_AVAILABLE = 16;
    private static String TAG = "IZatXTRAReport";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private BdsXtraValidityInfo mBdsXtraValidityInfo;
    private GalXtraValidityInfo mGalXtraValidityInfo;
    private GlonassXtraValidityInfo mGlonassXtraValidityInfo;
    private GpsXtraValidityInfo mGpsXtraValidityInfo;
    private QzssXtraValidityInfo mQzssXtraValidityInfo;
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;
    private byte mValidityMask;

    public class BdsXtraValidityInfo {
        private int mBdsXtraAge;
        private long mBdsXtraValidity;

        public int getXtraAge() {
            return this.mBdsXtraAge;
        }

        public long getXtraValidity() {
            return this.mBdsXtraValidity;
        }
    }

    public class GalXtraValidityInfo {
        private int mGalXtraAge;
        private long mGalXtraValidity;

        public int getXtraAge() {
            return this.mGalXtraAge;
        }

        public long getXtraValidity() {
            return this.mGalXtraValidity;
        }
    }

    public class GlonassXtraValidityInfo {
        private int mGlonassXtraAge;
        private int mGlonassXtraValidity;

        public int getXtraAge() {
            return this.mGlonassXtraAge;
        }

        public int getXtraValidity() {
            return this.mGlonassXtraValidity;
        }
    }

    public class GpsXtraValidityInfo {
        private int mGpsXtraAge;
        private int mGpsXtraValidity;

        public int getXtraAge() {
            return this.mGpsXtraAge;
        }

        public int getGpsXtraValidity() {
            return this.mGpsXtraValidity;
        }
    }

    public class QzssXtraValidityInfo {
        private int mQzssXtraAge;
        private byte mQzssXtraValidity;

        public int getXtraAge() {
            return this.mQzssXtraAge;
        }

        public byte getXtraValidity() {
            return this.mQzssXtraValidity;
        }
    }

    public IZatXTRADebugReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, byte validityMask, int gpsXtraValidity, int gpsXtraAge, int glonassXtraValidity, int glonassXtraAge, long bdsXtraValidity, int bdsXtraAge, long galXtraValidity, int galXtraAge, byte qzssXtraValidity, int qzssXtraAge) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        this.mValidityMask = validityMask;
        if ((this.mValidityMask & 1) != 0) {
            this.mGpsXtraValidityInfo = new GpsXtraValidityInfo();
            this.mGpsXtraValidityInfo.mGpsXtraValidity = gpsXtraValidity;
            this.mGpsXtraValidityInfo.mGpsXtraAge = gpsXtraAge;
        }
        if ((this.mValidityMask & 2) != 0) {
            this.mGlonassXtraValidityInfo = new GlonassXtraValidityInfo();
            this.mGlonassXtraValidityInfo.mGlonassXtraValidity = glonassXtraValidity;
            this.mGlonassXtraValidityInfo.mGlonassXtraAge = glonassXtraAge;
        }
        if ((this.mValidityMask & 4) != 0) {
            this.mBdsXtraValidityInfo = new BdsXtraValidityInfo();
            this.mBdsXtraValidityInfo.mBdsXtraValidity = bdsXtraValidity;
            this.mBdsXtraValidityInfo.mBdsXtraAge = bdsXtraAge;
        }
        if ((this.mValidityMask & 8) != 0) {
            this.mGalXtraValidityInfo = new GalXtraValidityInfo();
            this.mGalXtraValidityInfo.mGalXtraValidity = galXtraValidity;
            this.mGalXtraValidityInfo.mGalXtraAge = galXtraAge;
        }
        if ((this.mValidityMask & 16) != 0) {
            this.mQzssXtraValidityInfo = new QzssXtraValidityInfo();
            this.mQzssXtraValidityInfo.mQzssXtraValidity = qzssXtraValidity;
            this.mQzssXtraValidityInfo.mQzssXtraAge = qzssXtraAge;
        }
    }

    public IZatXTRADebugReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mValidityMask = source.readByte();
        if ((this.mValidityMask & 1) != 0) {
            this.mGpsXtraValidityInfo = new GpsXtraValidityInfo();
            this.mGpsXtraValidityInfo.mGpsXtraValidity = source.readInt();
            this.mGpsXtraValidityInfo.mGpsXtraAge = source.readInt();
        }
        if ((this.mValidityMask & 2) != 0) {
            this.mGlonassXtraValidityInfo = new GlonassXtraValidityInfo();
            this.mGlonassXtraValidityInfo.mGlonassXtraValidity = source.readInt();
            this.mGlonassXtraValidityInfo.mGlonassXtraAge = source.readInt();
        }
        if ((this.mValidityMask & 4) != 0) {
            this.mBdsXtraValidityInfo = new BdsXtraValidityInfo();
            this.mBdsXtraValidityInfo.mBdsXtraValidity = source.readLong();
            this.mBdsXtraValidityInfo.mBdsXtraAge = source.readInt();
        }
        if ((this.mValidityMask & 8) != 0) {
            this.mGalXtraValidityInfo = new GalXtraValidityInfo();
            this.mGalXtraValidityInfo.mGalXtraValidity = source.readLong();
            this.mGalXtraValidityInfo.mGalXtraAge = source.readInt();
        }
        if ((this.mValidityMask & 16) != 0) {
            this.mQzssXtraValidityInfo = new QzssXtraValidityInfo();
            this.mQzssXtraValidityInfo.mQzssXtraValidity = source.readByte();
            this.mQzssXtraValidityInfo.mQzssXtraAge = source.readInt();
        }
    }

    public boolean hasGpsXtraInfo() {
        return (this.mValidityMask & 1) != 0;
    }

    public boolean hasGlonassXtraInfo() {
        return (this.mValidityMask & 2) != 0;
    }

    public boolean hasBdsXtraInfo() {
        return (this.mValidityMask & 4) != 0;
    }

    public boolean hasGalXtraInfo() {
        return (this.mValidityMask & 8) != 0;
    }

    public boolean hasQzssXtraInfo() {
        return (this.mValidityMask & 16) != 0;
    }

    public GpsXtraValidityInfo getXtraDataValidityForGPS() {
        return this.mGpsXtraValidityInfo;
    }

    public GlonassXtraValidityInfo getXtraDataValidityForGlonass() {
        return this.mGlonassXtraValidityInfo;
    }

    public BdsXtraValidityInfo getXtraDataValidityForBDS() {
        return this.mBdsXtraValidityInfo;
    }

    public GalXtraValidityInfo getXtraDataValidityForGal() {
        return this.mGalXtraValidityInfo;
    }

    public QzssXtraValidityInfo getXtraDataValidityForQzss() {
        return this.mQzssXtraValidityInfo;
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
        dest.writeByte(this.mValidityMask);
        if ((this.mValidityMask & 1) != 0) {
            dest.writeInt(this.mGpsXtraValidityInfo.mGpsXtraValidity);
            dest.writeInt(this.mGpsXtraValidityInfo.mGpsXtraAge);
        }
        if ((this.mValidityMask & 2) != 0) {
            dest.writeInt(this.mGlonassXtraValidityInfo.mGlonassXtraValidity);
            dest.writeInt(this.mGlonassXtraValidityInfo.mGlonassXtraAge);
        }
        if ((this.mValidityMask & 4) != 0) {
            dest.writeLong(this.mBdsXtraValidityInfo.mBdsXtraValidity);
            dest.writeInt(this.mBdsXtraValidityInfo.mBdsXtraAge);
        }
        if ((this.mValidityMask & 8) != 0) {
            dest.writeLong(this.mGalXtraValidityInfo.mGalXtraValidity);
            dest.writeInt(this.mGalXtraValidityInfo.mGalXtraAge);
        }
        if ((this.mValidityMask & 16) != 0) {
            dest.writeByte(this.mQzssXtraValidityInfo.mQzssXtraValidity);
            dest.writeInt(this.mQzssXtraValidityInfo.mQzssXtraAge);
        }
    }
}
