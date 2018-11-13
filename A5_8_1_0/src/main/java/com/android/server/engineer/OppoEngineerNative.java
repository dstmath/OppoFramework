package com.android.server.engineer;

public class OppoEngineerNative {
    public static final int ACTION_GET_BACKCOVER_COLOR_ID = 100005;
    public static final int ACTION_GET_BOOT_IMG_WATERMARK = 1000029;
    public static final int ACTION_GET_CALIBRATION_STATUS_FROM_NVRAM = 1000025;
    public static final int ACTION_GET_CARRIER_VERSION = 1000008;
    public static final int ACTION_GET_CARRIER_VERSION_FROM_NVRAM = 1000023;
    public static final int ACTION_GET_DEVICELOCK_DAYS = 1000037;
    public static final int ACTION_GET_DEVICELOCK_FIRST_BIND_TIME = 1000043;
    public static final int ACTION_GET_DEVICELOCK_ICCID = 1000041;
    public static final int ACTION_GET_DEVICELOCK_IMSI = 1000035;
    public static final int ACTION_GET_DEVICELOCK_LAST_BIND_TIME = 1000039;
    public static final int ACTION_GET_DEVICELOCK_STATUS = 1000033;
    public static final int ACTION_GET_DEVICELOCK_UNLOCK_TIME = 1000045;
    public static final int ACTION_GET_DOWNLOAD_STATUS = 100001;
    public static final int ACTION_GET_EMMC_HEALTH_INFO = 100004;
    public static final int ACTION_GET_ENCRYPT_IMEI_FROM_NVRAM = 1000022;
    public static final int ACTION_GET_ENG_RESULT_FROM_NVAM = 1000020;
    public static final int ACTION_GET_OPPO_PRODUCT_INFO_FROM_NVRAM = 1000026;
    public static final int ACTION_GET_REGION_NETLOCK = 1000010;
    public static final int ACTION_GET_SERIAL_NO_FROM_NVRAM = 1000018;
    public static final int ACTION_GET_SERIAL_PORT_STATE = 100002;
    public static final int ACTION_GET_SIM_OPERATOR_SWITCH = 1000027;
    public static final int ACTION_GET_SINGLEDOUBLE_CARE = 1000016;
    public static final int ACTION_GET_TELCEL_SIMLOCK = 1000012;
    public static final int ACTION_GET_TELCEL_SIMLOCK_UNLOCK_TIMES = 1000014;
    public static final int ACTION_RESET_BACKCOVER_COLOR_ID = 100007;
    public static final int ACTION_SAVE_CARRIER_VERSION_TO_NVRAM = 1000024;
    public static final int ACTION_SAVE_ENG_RESULT_TO_NVRAM = 1000021;
    public static final int ACTION_SAVE_SERIAL_NO_TO_NVRAM = 1000019;
    public static final int ACTION_SET_BACKCOVER_COLOR_ID = 100006;
    public static final int ACTION_SET_CARRIER_VERSION = 1000009;
    public static final int ACTION_SET_DEVICELOCK_DAYS = 1000038;
    public static final int ACTION_SET_DEVICELOCK_FIRST_BIND_TIME = 1000044;
    public static final int ACTION_SET_DEVICELOCK_ICCID = 1000042;
    public static final int ACTION_SET_DEVICELOCK_IMSI = 1000036;
    public static final int ACTION_SET_DEVICELOCK_LAST_BIND_TIME = 1000040;
    public static final int ACTION_SET_DEVICELOCK_STATUS = 1000034;
    public static final int ACTION_SET_DEVICELOCK_UNLOCK_TIME = 1000046;
    public static final int ACTION_SET_REGION_NETLOCK = 1000011;
    public static final int ACTION_SET_SERIAL_PORT_STATE = 100003;
    public static final int ACTION_SET_SIM_OPERATOR_SWITCH = 1000028;
    public static final int ACTION_SET_SINGLEDOUBLE_CARE = 1000017;
    public static final int ACTION_SET_TELCEL_SIMLOCK = 1000013;
    public static final int ACTION_SET_TELCEL_SIMLOCK_UNLOCK_TIMES = 1000015;
    private static final String TAG = "OppoEngineerNative";

    public static native byte[] nativeGetBadBatteryConfig(int i, int i2);

    public static native boolean nativeGetPartionWriteProtectState();

    public static native byte[] nativeGetProductLineTestResult();

    public static native boolean nativeGetRpmbEnableState();

    public static native boolean nativeGetRpmbState();

    public static native byte[] nativeReadEngineerData(int i);

    public static native boolean nativeResetProductLineTestResult();

    public static native boolean nativeSaveEngineerData(int i, byte[] bArr, int i2);

    public static native int nativeSetBatteryBatteryConfig(int i, int i2, byte[] bArr);

    public static native boolean nativeSetPartionWriteProtectState(int i);

    public static native boolean nativeSetProductLineTestResult(int i, int i2);

    private OppoEngineerNative() {
    }

    public static byte[] native_getDownloadStatus() {
        return nativeReadEngineerData(ACTION_GET_DOWNLOAD_STATUS);
    }
}
