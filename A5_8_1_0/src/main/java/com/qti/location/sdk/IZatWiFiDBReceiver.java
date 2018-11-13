package com.qti.location.sdk;

import java.util.List;

public abstract class IZatWiFiDBReceiver {
    protected final IZatWiFiDBReceiverResponseListener mResponseListener;

    public static class IZatAPInfo {
        IZatAPInfoExtra mExtra;
        String mMacAddress;

        public IZatAPInfo(String mac, IZatAPInfoExtra extra) {
            this.mMacAddress = mac;
            this.mExtra = extra;
        }

        public String getMacAddress() {
            return this.mMacAddress;
        }

        public IZatAPInfoExtra getExtra() {
            return this.mExtra;
        }

        public boolean isExtraAvailable() {
            return this.mExtra == null ? false : this.mExtra.isAvailable();
        }
    }

    public static class IZatAPInfoExtra {
        IZatCellInfo mCellInfo;
        IZatAPSSIDInfo mSSID;

        public IZatAPInfoExtra(IZatCellInfo cellInfo, IZatAPSSIDInfo ssid) {
            this.mCellInfo = cellInfo;
            this.mSSID = ssid;
        }

        public IZatCellInfo getCellInfo() {
            return this.mCellInfo;
        }

        public IZatAPSSIDInfo getSSID() {
            return this.mSSID;
        }

        public boolean isAvailable() {
            return (this.mSSID == null && this.mCellInfo == null) ? false : true;
        }
    }

    public static class IZatAPLocationData {
        public static final int IZAT_AP_LOC_HORIZONTAL_ERR_VALID = 2;
        public static final int IZAT_AP_LOC_MAR_VALID = 1;
        public static final int IZAT_AP_LOC_RELIABILITY_VALID = 4;
        public static final int IZAT_AP_LOC_WITH_LAT_LON = 0;
        float mHorizontalError;
        float mLatitude;
        float mLongitude;
        String mMacAddress;
        float mMaxAntenaRange;
        IZatReliablityTypes mReliability;
        int mValidBits = 0;

        public enum IZatReliablityTypes {
            VERY_LOW,
            LOW,
            MEDIUM,
            HIGH,
            VERY_HIGH
        }

        public IZatAPLocationData(String mac, float latitude, float longitude) {
            this.mMacAddress = mac;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
        }

        public void setLatitude(float latitude) {
            this.mLatitude = latitude;
        }

        public void setLongitude(float longitude) {
            this.mLongitude = longitude;
        }

        public void setMaxAntenaRange(float mar) {
            this.mMaxAntenaRange = mar;
            this.mValidBits |= 1;
        }

        public void setHorizontalError(float he) {
            this.mHorizontalError = he;
            this.mValidBits |= 2;
        }

        public void setReliability(IZatReliablityTypes reliability) {
            this.mReliability = reliability;
            this.mValidBits |= 4;
        }

        public String getMacAddress() {
            return this.mMacAddress;
        }

        public float getLatitude() {
            return this.mLatitude;
        }

        public float getLongitude() {
            return this.mLongitude;
        }

        public float getMaxAntenaRange() throws IZatStaleDataException {
            if ((this.mValidBits & 1) != 0) {
                return this.mMaxAntenaRange;
            }
            throw new IZatStaleDataException("Maximum Antena Range information is not valid");
        }

        public float getHorizontalError() throws IZatStaleDataException {
            if ((this.mValidBits & 2) != 0) {
                return this.mHorizontalError;
            }
            throw new IZatStaleDataException("Horizontal error information is not valid");
        }

        public IZatReliablityTypes getReliability() throws IZatStaleDataException {
            if ((this.mValidBits & 4) != 0) {
                return this.mReliability;
            }
            throw new IZatStaleDataException("Reliability information is not valid");
        }
    }

    public static class IZatAPSSIDInfo {
        public final byte[] mSSID;

        public IZatAPSSIDInfo(byte[] ssid, short validBytesCount) {
            this.mSSID = new byte[validBytesCount];
        }
    }

    public static class IZatAPSpecialInfo {
        public final IZatAPSpecialInfoTypes mInfo;
        public final String mMacAddress;

        public enum IZatAPSpecialInfoTypes {
            NO_INFO_AVAILABLE,
            MOVING_AP
        }

        public IZatAPSpecialInfo(String mac, IZatAPSpecialInfoTypes info) {
            this.mMacAddress = mac;
            this.mInfo = info;
        }
    }

    public static class IZatCellInfo {
        public final int mRegionID1;
        public final int mRegionID2;
        public final int mRegionID3;
        public final int mRegionID4;
        public final IZatCellTypes mType;

        public enum IZatCellTypes {
            GSM,
            CDMA,
            WCDMA,
            LTE
        }

        public IZatCellInfo(int regionID1, int regionID2, int regionID3, int regionID4, IZatCellTypes type) {
            this.mRegionID1 = regionID1;
            this.mRegionID2 = regionID2;
            this.mRegionID3 = regionID3;
            this.mRegionID4 = regionID4;
            this.mType = type;
        }
    }

    public interface IZatWiFiDBReceiverResponseListener {
        void onAPListAvailable(List<IZatAPInfo> list);

        void onServiceRequest();

        void onStatusUpdate(boolean z, String str);
    }

    public abstract void pushWiFiDB(List<IZatAPLocationData> list, List<IZatAPSpecialInfo> list2, int i);

    public abstract void requestAPList(int i);

    protected IZatWiFiDBReceiver(IZatWiFiDBReceiverResponseListener listener) throws IZatIllegalArgumentException {
        if (listener == null) {
            throw new IZatIllegalArgumentException("Unable to obtain IZatWiFiDBReceiver instance");
        }
        this.mResponseListener = listener;
    }
}
