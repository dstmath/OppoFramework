package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class IZatEphmerisDebugReport implements Parcelable {
    public static final Creator<IZatEphmerisDebugReport> CREATOR = new Creator<IZatEphmerisDebugReport>() {
        public IZatEphmerisDebugReport createFromParcel(Parcel source) {
            return new IZatEphmerisDebugReport(source);
        }

        public IZatEphmerisDebugReport[] newArray(int size) {
            return new IZatEphmerisDebugReport[size];
        }
    };
    private static String TAG = "IZatEphmeris";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private long mBdsEphemrisDataValidity;
    private long mGalEphemrisDataValidity;
    private int mGlonassEphemrisDataValidity;
    private int mGpsEphemrisDataValidity;
    private byte mQzssEphemrisDataValidity;
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;

    public IZatEphmerisDebugReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, int gpsEphDataValidity, int glonassEphDataValidity, long bdsEphDataValidity, long galEphDataValidity, byte qzssEphDataValidity) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        this.mGpsEphemrisDataValidity = gpsEphDataValidity;
        this.mGlonassEphemrisDataValidity = glonassEphDataValidity;
        this.mBdsEphemrisDataValidity = bdsEphDataValidity;
        this.mGalEphemrisDataValidity = galEphDataValidity;
        this.mQzssEphemrisDataValidity = qzssEphDataValidity;
    }

    public IZatEphmerisDebugReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mGpsEphemrisDataValidity = source.readInt();
        this.mGlonassEphemrisDataValidity = source.readInt();
        this.mBdsEphemrisDataValidity = source.readLong();
        this.mGalEphemrisDataValidity = source.readLong();
        this.mQzssEphemrisDataValidity = source.readByte();
    }

    public int getEphmerisForGPS() {
        return this.mGpsEphemrisDataValidity;
    }

    public int getEphmerisForGlonass() {
        return this.mGlonassEphemrisDataValidity;
    }

    public long getEphmerisForBDS() {
        return this.mBdsEphemrisDataValidity;
    }

    public long getEphmerisForGal() {
        return this.mGalEphemrisDataValidity;
    }

    public byte getEphmerisForQzss() {
        return this.mQzssEphemrisDataValidity;
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
        dest.writeInt(this.mGpsEphemrisDataValidity);
        dest.writeInt(this.mGlonassEphemrisDataValidity);
        dest.writeLong(this.mBdsEphemrisDataValidity);
        dest.writeLong(this.mGalEphemrisDataValidity);
        dest.writeByte(this.mQzssEphemrisDataValidity);
    }
}
