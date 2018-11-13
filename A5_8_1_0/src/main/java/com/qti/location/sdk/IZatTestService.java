package com.qti.location.sdk;

public interface IZatTestService {
    public static final long GNSS_AIDING_DATA_ALL = -1;
    public static final long GNSS_AIDING_DATA_BDS_ALMANAC = 8388608;
    public static final long GNSS_AIDING_DATA_BDS_ALMANAC_CORR = 134217728;
    public static final long GNSS_AIDING_DATA_BDS_EPHEMERIS = 4194304;
    public static final long GNSS_AIDING_DATA_BDS_SVDIR = 16777216;
    public static final long GNSS_AIDING_DATA_BDS_SVSTEER = 33554432;
    public static final long GNSS_AIDING_DATA_BDS_TIME = 67108864;
    public static final long GNSS_AIDING_DATA_CELLDB_INFO = 512;
    public static final long GNSS_AIDING_DATA_FREQ_BIAS_EST = 256;
    public static final long GNSS_AIDING_DATA_GLO_ALMANAC = 131072;
    public static final long GNSS_AIDING_DATA_GLO_ALMANAC_CORR = 2097152;
    public static final long GNSS_AIDING_DATA_GLO_EPHEMERIS = 65536;
    public static final long GNSS_AIDING_DATA_GLO_SVDIR = 262144;
    public static final long GNSS_AIDING_DATA_GLO_SVSTEER = 524288;
    public static final long GNSS_AIDING_DATA_GLO_TIME = 1048576;
    public static final long GNSS_AIDING_DATA_GPS_ALMANAC = 2048;
    public static final long GNSS_AIDING_DATA_GPS_ALMANAC_CORR = 32768;
    public static final long GNSS_AIDING_DATA_GPS_EPHEMERIS = 1024;
    public static final long GNSS_AIDING_DATA_GPS_SVDIR = 4096;
    public static final long GNSS_AIDING_DATA_GPS_SVSTEER = 8192;
    public static final long GNSS_AIDING_DATA_GPS_TIME = 16384;
    public static final long GNSS_AIDING_DATA_HEALTH = 32;
    public static final long GNSS_AIDING_DATA_IONO = 8;
    public static final long GNSS_AIDING_DATA_POSITION = 2;
    public static final long GNSS_AIDING_DATA_RTI = 128;
    public static final long GNSS_AIDING_DATA_SADATA = 64;
    public static final long GNSS_AIDING_DATA_TIME = 4;
    public static final long GNSS_AIDING_DATA_UTC = 16;

    public interface IFlpMaxPowerAllocatedCallback {
        void onMaxPowerAllocatedChanged(double d);
    }

    void deleteAidingData(long j) throws IZatIllegalArgumentException;

    void deregisterForMaxPowerAllocatedChange(IFlpMaxPowerAllocatedCallback iFlpMaxPowerAllocatedCallback) throws IZatIllegalArgumentException;

    void registerForMaxPowerAllocatedChange(IFlpMaxPowerAllocatedCallback iFlpMaxPowerAllocatedCallback) throws IZatIllegalArgumentException;
}
