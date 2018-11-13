package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IZatXoStateDebugReport implements Parcelable {
    public static final Creator<IZatXoStateDebugReport> CREATOR = new Creator<IZatXoStateDebugReport>() {
        public IZatXoStateDebugReport createFromParcel(Parcel source) {
            return new IZatXoStateDebugReport(source);
        }

        public IZatXoStateDebugReport[] newArray(int size) {
            return new IZatXoStateDebugReport[size];
        }
    };
    private IZatUtcSpec mUtcTimeLastReported;
    private IZatUtcSpec mUtcTimeLastUpdated;
    private IZatXoState mXoState;

    public enum IZatXoState {
        FAILED(0),
        NOT_CAL(1),
        FAC1(2),
        DEFAULT(3),
        WIDE_BINS(4),
        COARSE(5),
        IFC(6),
        FINE(7),
        FT1(8),
        OLD_RGS(9),
        INTERP(10),
        FT2(11),
        EXACT(12),
        RGS(13),
        RGS_RECENT(14),
        VCO_LAST(15);
        
        private final int mValue;

        private IZatXoState(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public IZatXoStateDebugReport(IZatUtcSpec utcTimeLastUpdated, IZatUtcSpec utcTimeLastReported, int xoState) {
        this.mUtcTimeLastUpdated = utcTimeLastUpdated;
        this.mUtcTimeLastReported = utcTimeLastReported;
        try {
            this.mXoState = IZatXoState.values()[xoState];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.mXoState = IZatXoState.FAILED;
        }
    }

    public IZatXoStateDebugReport(Parcel source) {
        this.mUtcTimeLastUpdated = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mUtcTimeLastReported = (IZatUtcSpec) source.readParcelable(IZatUtcSpec.class.getClassLoader());
        this.mXoState = IZatXoState.values()[source.readInt()];
    }

    public IZatUtcSpec getUTCTimestamp() {
        return this.mUtcTimeLastUpdated;
    }

    public IZatUtcSpec getLastReportedUTCTime() {
        return this.mUtcTimeLastReported;
    }

    public IZatXoState getXoState() {
        return this.mXoState;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mUtcTimeLastUpdated, 0);
        dest.writeParcelable(this.mUtcTimeLastReported, 0);
        dest.writeInt(this.mXoState.getValue());
    }
}
