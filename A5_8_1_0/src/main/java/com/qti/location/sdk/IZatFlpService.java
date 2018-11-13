package com.qti.location.sdk;

import android.location.Location;

public interface IZatFlpService {

    public interface IFlpLocationCallback {
        void onLocationAvailable(Location[] locationArr);
    }

    public interface IFlpStatusCallback {
        void onBatchingStatusCb(IzatFlpStatus izatFlpStatus);
    }

    public interface IZatFlpSessionHandle {
        void pullLocations();

        void registerForSessionStatus(IFlpStatusCallback iFlpStatusCallback) throws IZatIllegalArgumentException, IZatFeatureNotSupportedException;

        void setToBackground();

        void setToForeground();

        void setToTripMode() throws IZatFeatureNotSupportedException;

        void unregisterForSessionStatus() throws IZatFeatureNotSupportedException;
    }

    public static abstract class IzatFlpRequest {
        protected boolean mIsRunningInBackground = false;
        int mMaxDistanceInterval = 0;
        long mMaxTimeInterval = 0;
        long mTripDistance = 0;

        public static IzatFlpFgRequest getForegroundFlprequest() {
            return new IzatFlpFgRequest();
        }

        public static IzatFlpBgRequest getBackgroundFlprequest() {
            return new IzatFlpBgRequest();
        }

        public void setTimeInterval(long maxTimeInterval) throws IZatIllegalArgumentException {
            if (maxTimeInterval <= 0) {
                throw new IZatIllegalArgumentException("invalid time interval");
            }
            this.mMaxTimeInterval = maxTimeInterval;
        }

        public long getTimeInterval() {
            return this.mMaxTimeInterval;
        }

        public void setDistanceInterval(int maxDistanceInterval) throws IZatIllegalArgumentException {
            if (maxDistanceInterval <= 0) {
                throw new IZatIllegalArgumentException("invalid distance of displacement");
            }
            this.mMaxDistanceInterval = maxDistanceInterval;
        }

        public int getDistanceInterval() {
            return this.mMaxDistanceInterval;
        }

        public void setTripDistance(long tripDistance) throws IZatIllegalArgumentException {
            if (tripDistance <= 0) {
                throw new IZatIllegalArgumentException("invalid trip distance");
            }
            this.mTripDistance = tripDistance;
        }

        public long getTripDistance() {
            return this.mTripDistance;
        }
    }

    public static class IzatFlpBgRequest extends IzatFlpRequest {
        private IzatFlpBgRequestMode mActiveMode = IzatFlpBgRequestMode.ROUTINE_MODE;

        public enum IzatFlpBgRequestMode {
            ROUTINE_MODE(0),
            TRIP_MODE(1);
            
            private final int value;

            private IzatFlpBgRequestMode(int value) {
                this.value = value;
            }

            public int getValue() {
                return this.value;
            }
        }

        IzatFlpBgRequest() {
        }

        public void setActiveMode(IzatFlpBgRequestMode mode) {
            this.mActiveMode = mode;
        }

        public IzatFlpBgRequestMode getActiveMode() {
            return this.mActiveMode;
        }
    }

    public static class IzatFlpFgRequest extends IzatFlpRequest {
        IzatFlpFgRequest() {
        }
    }

    public enum IzatFlpStatus {
        OUTDOOR_TRIP_COMPLETED(0),
        POSITION_AVAILABLE(1),
        POSITION_NOT_AVAILABLE(2);
        
        private final int value;

        private IzatFlpStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    void deregisterForPassiveLocations(IFlpLocationCallback iFlpLocationCallback) throws IZatIllegalArgumentException;

    void registerForPassiveLocations(IFlpLocationCallback iFlpLocationCallback) throws IZatIllegalArgumentException;

    IZatFlpSessionHandle startFlpSession(IFlpLocationCallback iFlpLocationCallback, IzatFlpRequest izatFlpRequest) throws IZatIllegalArgumentException;

    void stopFlpSession(IZatFlpSessionHandle iZatFlpSessionHandle) throws IZatIllegalArgumentException;
}
