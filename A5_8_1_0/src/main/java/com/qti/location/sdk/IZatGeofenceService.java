package com.qti.location.sdk;

import android.app.PendingIntent;
import android.location.Location;
import java.util.Map;

public interface IZatGeofenceService {
    public static final int GEOFENCE_EVENT_DWELL_INSIDE = 8;
    public static final int GEOFENCE_EVENT_DWELL_OUTSIDE = 16;
    public static final int GEOFENCE_EVENT_ENTERED = 1;
    public static final int GEOFENCE_EVENT_EXITED = 2;
    public static final int GEOFENCE_EVENT_UNCERTAIN = 4;
    public static final int GEOFENCE_GEN_ALERT_GNSS_AVAILABLE = 2;
    public static final int GEOFENCE_GEN_ALERT_GNSS_UNAVAILABLE = 1;
    public static final int GEOFENCE_GEN_ALERT_OOS = 3;
    public static final int GEOFENCE_GEN_ALERT_TIME_INVALID = 4;
    public static final int GEOFENCE_REQUEST_TYPE_ADD = 1;
    public static final int GEOFENCE_REQUEST_TYPE_PAUSE = 3;
    public static final int GEOFENCE_REQUEST_TYPE_REMOVE = 2;
    public static final int GEOFENCE_REQUEST_TYPE_RESUME = 4;
    public static final int GEOFENCE_REQUEST_TYPE_UPDATE = 5;
    public static final int GEOFENCE_RESULT_ERROR_GENERIC = -149;
    public static final int GEOFENCE_RESULT_ERROR_ID_UNKNOWN = -102;
    public static final int GEOFENCE_RESULT_ERROR_INVALID_TRANSITION = -103;
    public static final int GEOFENCE_RESULT_ERROR_TOO_MANY_GEOFENCES = -100;
    public static final int GEOFENCE_RESULT_SUCCESS = 0;

    public interface IZatGeofenceCallback {
        void onEngineReportStatus(int i, Location location);

        void onRequestFailed(IZatGeofenceHandle iZatGeofenceHandle, int i, int i2);

        void onTransitionEvent(IZatGeofenceHandle iZatGeofenceHandle, int i, Location location);
    }

    public interface IZatGeofenceHandle {
        Object getContext();

        void pause() throws IZatIllegalArgumentException;

        void resume() throws IZatIllegalArgumentException;

        void update(IzatGeofenceTransitionTypes izatGeofenceTransitionTypes, int i) throws IZatIllegalArgumentException;
    }

    public static class IzatDwellNotify {
        public static final int DWELL_TYPE_INSIDE_MASK = 1;
        public static final int DWELL_TYPE_OUTSIDE_MASK = 2;
        int mDwellTime;
        int mDwellType;

        public IzatDwellNotify(int dwellTime, int dwellType) {
            this.mDwellTime = dwellTime;
            this.mDwellType = dwellType;
        }

        public int getDwellTime() {
            return this.mDwellTime;
        }

        public int getDwellType() {
            return this.mDwellType;
        }
    }

    public static class IzatGeofence {
        private IzatGeofenceConfidence mConfidence = IzatGeofenceConfidence.LOW;
        private IzatDwellNotify mDwellNotify;
        private double mLatitude;
        private double mLongitude;
        private int mNotifyResponsiveness = 0;
        private double mRadius;
        private IzatGeofenceTransitionTypes mTransitionTypes = IzatGeofenceTransitionTypes.UNKNOWN;

        public IzatGeofence(double latitude, double longitude, double radius) {
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mRadius = radius;
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

        public void setTransitionTypes(IzatGeofenceTransitionTypes transitionTypes) {
            this.mTransitionTypes = transitionTypes;
        }

        public IzatGeofenceTransitionTypes getTransitionTypes() {
            return this.mTransitionTypes;
        }

        public void setNotifyResponsiveness(int notifyResponsiveness) {
            this.mNotifyResponsiveness = notifyResponsiveness;
        }

        public int getNotifyResponsiveness() {
            return this.mNotifyResponsiveness;
        }

        public void setConfidence(IzatGeofenceConfidence confidence) {
            this.mConfidence = confidence;
        }

        public IzatGeofenceConfidence getConfidence() {
            return this.mConfidence;
        }

        public void setDwellNotify(IzatDwellNotify dwellNotify) {
            this.mDwellNotify = dwellNotify;
        }

        public IzatDwellNotify getDwellNotify() {
            return this.mDwellNotify;
        }
    }

    public enum IzatGeofenceConfidence {
        LOW(1),
        MEDIUM(2),
        HIGH(3);
        
        private final int value;

        private IzatGeofenceConfidence(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum IzatGeofenceTransitionTypes {
        UNKNOWN(0),
        ENTERED_ONLY(1),
        EXITED_ONLY(2),
        ENTERED_AND_EXITED(3);
        
        private final int value;

        private IzatGeofenceTransitionTypes(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    IZatGeofenceHandle addGeofence(Object obj, IzatGeofence izatGeofence) throws IZatIllegalArgumentException;

    void deregisterForGeofenceCallbacks(IZatGeofenceCallback iZatGeofenceCallback) throws IZatIllegalArgumentException;

    Map<IZatGeofenceHandle, IzatGeofence> recoverGeofences() throws IZatIllegalArgumentException;

    void registerForGeofenceCallbacks(IZatGeofenceCallback iZatGeofenceCallback) throws IZatIllegalArgumentException;

    void registerPendingIntent(PendingIntent pendingIntent) throws IZatIllegalArgumentException;

    void removeGeofence(IZatGeofenceHandle iZatGeofenceHandle) throws IZatIllegalArgumentException;

    void unregisterPendingIntent(PendingIntent pendingIntent) throws IZatIllegalArgumentException;
}
