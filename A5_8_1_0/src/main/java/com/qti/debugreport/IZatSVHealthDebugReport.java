package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class IZatSVHealthDebugReport implements Parcelable {
    public static final Creator<IZatSVHealthDebugReport> CREATOR = new Creator<IZatSVHealthDebugReport>() {
        public IZatSVHealthDebugReport createFromParcel(Parcel source) {
            return new IZatSVHealthDebugReport(source);
        }

        public IZatSVHealthDebugReport[] newArray(int size) {
            return new IZatSVHealthDebugReport[size];
        }
    };
    private static String TAG = "IZatSVHealthReport";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private long mBdsBadMask;
    private long mBdsGoodMask;
    private List<IzatSVHealthState> mBdsSVHealthState = new ArrayList();
    private long mBdsUnknownMask;
    private long mGalBadMask;
    private long mGalGoodMask;
    private List<IzatSVHealthState> mGalSVHealthState = new ArrayList();
    private long mGalUnknownMask;
    private int mGlonassBadMask;
    private int mGlonassGoodMask;
    private List<IzatSVHealthState> mGlonassSVHealthState = new ArrayList();
    private int mGlonassUnknownMask;
    private int mGpsBadMask;
    private int mGpsGoodMask;
    private List<IzatSVHealthState> mGpsSVHealthState = new ArrayList();
    private int mGpsUnknownMask;
    private byte mQzssBadMask;
    private byte mQzssGoodMask;
    private List<IzatSVHealthState> mQzssSVHealthState = new ArrayList();
    private byte mQzssUnknownMask;
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;

    public enum IzatSVHealthState {
        SV_HEALTH_UNKNOWN(0),
        SV_HEALTH_GOOD(1),
        SV_HEALTH_BAD(2);
        
        private final int mValue;

        private IzatSVHealthState(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public List<IzatSVHealthState> getSVHealthForGPS() {
        return this.mGpsSVHealthState;
    }

    public List<IzatSVHealthState> getSVHealthForGlonass() {
        return this.mGlonassSVHealthState;
    }

    public List<IzatSVHealthState> getSVHealthForBDS() {
        return this.mBdsSVHealthState;
    }

    public List<IzatSVHealthState> getSVHealthForGal() {
        return this.mGalSVHealthState;
    }

    public List<IzatSVHealthState> getSVHealthForQzss() {
        return this.mQzssSVHealthState;
    }

    public IZatSVHealthDebugReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, int gpsGoodMask, int gpsBadMask, int gpsUnknownMask, int glonassGoodMask, int glonassBadMask, int glonassUnknownMask, long bdsGoodMask, long bdsBadMask, long bdsUnknownMask, long galGoodMask, long galBadMask, long galUnknownMask, byte qzssGoodMask, byte qzssBadMask, byte qzssUnknownMask) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        this.mGpsGoodMask = gpsGoodMask;
        this.mGpsBadMask = gpsBadMask;
        this.mGpsUnknownMask = gpsUnknownMask;
        this.mGlonassGoodMask = glonassGoodMask;
        this.mGlonassBadMask = glonassBadMask;
        this.mGlonassUnknownMask = glonassUnknownMask;
        this.mBdsGoodMask = bdsGoodMask;
        this.mBdsBadMask = bdsBadMask;
        this.mBdsUnknownMask = bdsUnknownMask;
        this.mGalGoodMask = galGoodMask;
        this.mGalBadMask = galBadMask;
        this.mGalUnknownMask = galUnknownMask;
        this.mQzssGoodMask = qzssGoodMask;
        this.mQzssBadMask = qzssBadMask;
        this.mQzssUnknownMask = qzssUnknownMask;
        fillSVHealthList(this.mGpsSVHealthState, gpsGoodMask, gpsBadMask, gpsUnknownMask);
        fillSVHealthList(this.mGlonassSVHealthState, glonassGoodMask, glonassBadMask, glonassUnknownMask);
        fillSVHealthList(this.mBdsSVHealthState, bdsGoodMask, bdsBadMask, bdsUnknownMask);
        fillSVHealthList(this.mGalSVHealthState, galGoodMask, galBadMask, galUnknownMask);
        fillSVHealthList(this.mQzssSVHealthState, qzssGoodMask, qzssBadMask, qzssUnknownMask);
    }

    public IZatSVHealthDebugReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mGpsGoodMask = source.readInt();
        this.mGpsBadMask = source.readInt();
        this.mGpsUnknownMask = source.readInt();
        this.mGlonassGoodMask = source.readInt();
        this.mGlonassBadMask = source.readInt();
        this.mGlonassUnknownMask = source.readInt();
        this.mBdsGoodMask = source.readLong();
        this.mBdsBadMask = source.readLong();
        this.mBdsUnknownMask = source.readLong();
        this.mGalGoodMask = source.readLong();
        this.mGalBadMask = source.readLong();
        this.mGalUnknownMask = source.readLong();
        this.mQzssGoodMask = source.readByte();
        this.mQzssBadMask = source.readByte();
        this.mQzssUnknownMask = source.readByte();
        fillSVHealthList(this.mGpsSVHealthState, this.mGpsGoodMask, this.mGpsBadMask, this.mGpsUnknownMask);
        fillSVHealthList(this.mGlonassSVHealthState, this.mGlonassGoodMask, this.mGlonassBadMask, this.mGlonassUnknownMask);
        fillSVHealthList(this.mBdsSVHealthState, this.mBdsGoodMask, this.mBdsBadMask, this.mBdsUnknownMask);
        fillSVHealthList(this.mGalSVHealthState, this.mGalGoodMask, this.mGalBadMask, this.mGalUnknownMask);
        fillSVHealthList(this.mQzssSVHealthState, this.mQzssGoodMask, this.mQzssBadMask, this.mQzssUnknownMask);
    }

    private void fillSVHealthList(List<IzatSVHealthState> listSVHealth, int goodMask, int badMask, int unknowMask) {
        for (int i = 0; i < 32; i++) {
            if (((1 << i) & goodMask) != 0) {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_GOOD);
            } else if (((1 << i) & badMask) != 0) {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_BAD);
            } else {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_UNKNOWN);
            }
        }
    }

    private void fillSVHealthList(List<IzatSVHealthState> listSVHealth, long goodMask, long badMask, long unknowMask) {
        for (long i = 0; i < 64; i++) {
            if (((1 << ((int) i)) & goodMask) != 0) {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_GOOD);
            } else if (((1 << ((int) i)) & badMask) != 0) {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_BAD);
            } else {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_UNKNOWN);
            }
        }
    }

    private void fillSVHealthList(List<IzatSVHealthState> listSVHealth, byte goodMask, byte badMask, byte unknowMask) {
        for (byte i = (byte) 0; i < (byte) 8; i = (byte) (i + 1)) {
            if (((1 << i) & goodMask) != 0) {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_GOOD);
            } else if (((1 << i) & badMask) != 0) {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_BAD);
            } else {
                listSVHealth.add(IzatSVHealthState.SV_HEALTH_UNKNOWN);
            }
        }
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
        dest.writeInt(this.mGpsGoodMask);
        dest.writeInt(this.mGpsBadMask);
        dest.writeInt(this.mGpsUnknownMask);
        dest.writeInt(this.mGlonassGoodMask);
        dest.writeInt(this.mGlonassBadMask);
        dest.writeInt(this.mGlonassUnknownMask);
        dest.writeLong(this.mBdsGoodMask);
        dest.writeLong(this.mBdsBadMask);
        dest.writeLong(this.mBdsUnknownMask);
        dest.writeLong(this.mGalGoodMask);
        dest.writeLong(this.mGalBadMask);
        dest.writeLong(this.mGalUnknownMask);
        dest.writeByte(this.mQzssGoodMask);
        dest.writeByte(this.mQzssBadMask);
        dest.writeByte(this.mQzssUnknownMask);
    }
}
