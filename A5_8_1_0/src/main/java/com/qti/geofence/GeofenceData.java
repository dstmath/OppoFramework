package com.qti.geofence;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class GeofenceData implements Parcelable {
    public static final Creator<GeofenceData> CREATOR = new Creator<GeofenceData>() {
        public GeofenceData createFromParcel(Parcel source) {
            return new GeofenceData(source);
        }

        public GeofenceData[] newArray(int size) {
            return new GeofenceData[size];
        }
    };
    private static String TAG = "GeofenceData";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private Bundle mAppBundleData;
    private String mAppTextData;
    private GeofenceConfidence mConfidence;
    private int mDwellTime;
    private DwellTypes mDwellType;
    private int mGeofenceId;
    private double mLatitude;
    private double mLongitude;
    private int mNotifyResponsiveness;
    private double mRadius;
    private GeofenceTransitionTypes mTransitionType;

    public enum DwellTypes {
        UNKNOWN(0),
        DWELL_TYPE_INSIDE(1),
        DWELL_TYPE_OUTSIDE(2),
        DWELL_TYPE_INSIDE_OUTSIDE(3);
        
        private final int mValue;

        private DwellTypes(int val) {
            this.mValue = val;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public enum GeofenceConfidence {
        LOW(1),
        MEDIUM(2),
        HIGH(3);
        
        private final int mValue;

        private GeofenceConfidence(int val) {
            this.mValue = val;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public enum GeofenceTransitionTypes {
        UNKNOWN(0),
        ENTERED_ONLY(1),
        EXITED_ONLY(2),
        ENTERED_AND_EXITED(3);
        
        private final int mValue;

        private GeofenceTransitionTypes(int val) {
            this.mValue = val;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public GeofenceData(Parcel source) {
        this.mNotifyResponsiveness = source.readInt();
        this.mLatitude = source.readDouble();
        this.mLongitude = source.readDouble();
        this.mRadius = source.readDouble();
        try {
            this.mTransitionType = GeofenceTransitionTypes.valueOf(source.readString());
        } catch (IllegalArgumentException e) {
            this.mTransitionType = null;
        }
        try {
            this.mConfidence = GeofenceConfidence.valueOf(source.readString());
        } catch (IllegalArgumentException e2) {
            this.mConfidence = null;
        }
        try {
            this.mDwellType = DwellTypes.valueOf(source.readString());
        } catch (IllegalArgumentException e3) {
            this.mDwellType = null;
        }
        this.mDwellTime = source.readInt();
        try {
            this.mAppTextData = source.readString();
        } catch (IllegalArgumentException e4) {
            this.mAppTextData = null;
        }
        this.mAppBundleData = source.readBundle();
        this.mGeofenceId = source.readInt();
    }

    public GeofenceData(int notifyResponsiveness, double latitude, double longitude, double radius, int transitionType, int confidence, int dwellType, int dwellTime, String appTextData, Bundle appBundleData, int geofenceId) {
        this.mNotifyResponsiveness = notifyResponsiveness;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        setTransitionType(transitionType);
        setConfidence(confidence);
        setDwellType(dwellType);
        this.mDwellTime = dwellTime;
        this.mAppTextData = appTextData;
        this.mAppBundleData = appBundleData;
        this.mGeofenceId = geofenceId;
    }

    public int getNotifyResponsiveness() {
        return this.mNotifyResponsiveness;
    }

    public double getLatitude() {
        return this.mLatitude;
    }

    public double getLongitude() {
        return this.mLongitude;
    }

    public double getRadius() {
        return this.mRadius;
    }

    public GeofenceTransitionTypes getTransitionType() {
        return this.mTransitionType;
    }

    public GeofenceConfidence getConfidence() {
        return this.mConfidence;
    }

    public DwellTypes getDwellType() {
        return this.mDwellType;
    }

    public int getDwellTime() {
        return this.mDwellTime;
    }

    public String getAppTextData() {
        return this.mAppTextData;
    }

    public Bundle getAppBundleData() {
        return this.mAppBundleData;
    }

    public int getGeofenceId() {
        return this.mGeofenceId;
    }

    public int describeContents() {
        return 0;
    }

    public void setNotifyResponsiveness(int notifyResponsiveness) {
        this.mNotifyResponsiveness = notifyResponsiveness;
    }

    public void setTransitionType(int transitionType) {
        this.mTransitionType = GeofenceTransitionTypes.UNKNOWN;
        for (GeofenceTransitionTypes type : GeofenceTransitionTypes.values()) {
            if (type.getValue() == transitionType) {
                this.mTransitionType = type;
                return;
            }
        }
    }

    public void setConfidence(int confidence) {
        this.mConfidence = GeofenceConfidence.LOW;
        for (GeofenceConfidence type : GeofenceConfidence.values()) {
            if (type.getValue() == confidence) {
                this.mConfidence = type;
                return;
            }
        }
    }

    public void setDwellType(int dwellType) {
        this.mDwellType = DwellTypes.UNKNOWN;
        for (DwellTypes type : DwellTypes.values()) {
            if (type.getValue() == dwellType) {
                this.mDwellType = type;
                return;
            }
        }
    }

    public void setGeofenceId(int gfId) {
        this.mGeofenceId = gfId;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (VERBOSE) {
            Log.v(TAG, "in GeofenceData: writeToParcel(); responsiveness is " + this.mNotifyResponsiveness + "; latitude is " + this.mLatitude + "; longitude is " + this.mLongitude + "; radius is " + this.mRadius + "; transitionTypes is " + this.mTransitionType.name() + "; confidence is " + this.mConfidence.name() + "; dwellTimeMask is " + this.mDwellType.name() + "; dwellTime is " + this.mDwellTime + "; AppTextData is " + this.mAppTextData + "; Geofence id is " + this.mGeofenceId);
        }
        dest.writeInt(this.mNotifyResponsiveness);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeDouble(this.mRadius);
        dest.writeString(this.mTransitionType == null ? "" : this.mTransitionType.name());
        dest.writeString(this.mConfidence == null ? "" : this.mConfidence.name());
        dest.writeString(this.mDwellType == null ? "" : this.mDwellType.name());
        dest.writeInt(this.mDwellTime);
        dest.writeString(this.mAppTextData);
        dest.writeBundle(this.mAppBundleData);
        dest.writeInt(this.mGeofenceId);
    }
}
