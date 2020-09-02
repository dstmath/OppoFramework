package com.mediatek.omadm;

public class PalConstDefs {
    public static final int ADMIN_NET_AVAILABLE = 1;
    public static final int ADMIN_NET_LOST = 3;
    public static final int ADMIN_NET_UNAVAILABLE = 2;
    public static final String APN_CFG_IDEX = "/apn_cfg_idx";
    public static final int CHILD_DEVICE = 2;
    public static final String CONFIGURATION_VER_PATH = "/last_update_configuration_version";
    public static final int CURRENT_DEVICE = 1;
    public static boolean DEBUG = true;
    public static final String EMPTY_STRING = "";
    public static final String FEATURE_PHONE = "Feature Phone";
    public static final String FIRMWARE_VER_PATH = "/last_update_firmware_version";
    public static final String HOST_OPERATION = "Host device operation";
    public static final String IMS_DOMAIN = "vzims.com";
    public static final String MANUFACTURER = "manufacturer";
    public static final String MO_WORK_PATH = "/data/vendor/verizon/dmclient/data";
    public static final String NOT_AVAILABLE = "n/a";
    public static final String NOT_AVAILABLE_UPPERCASE = "N/A";
    public static final String NOT_IDENTIFIED = "is not identified";
    public static final String NOT_READY = "is not ready";
    public static final String NULL_STRING = "NULL";
    public static final String OPERATION_NOT_SUPPORTED = "Operation not supported";
    public static final int PARENT_DEVICE = 0;
    public static final String PERMISSION_DENIED = "Permission denied";
    public static final int RET_ERR = 1;
    public static final int RET_ERR_ARGS = 7;
    public static final int RET_ERR_NOPERM = 12;
    public static final int RET_ERR_NORES = 11;
    public static final int RET_ERR_STATE = 10;
    public static final int RET_ERR_TMOUT = 13;
    public static final int RET_ERR_UNDEF = 8;
    public static final int RET_SUCC = 0;
    public static final String SIM_STATE = "Sim state";
    public static final String SMART_DEVICE = "Smart Device";
    public static final String TAG = PalConstDefs.class.getSimpleName();
    public static final String TELEPHONYMANAGER = "TelephonyManager";
    public static final String VERSION = "1.2";

    public static void throwEcxeption(int err) {
        if (err == 0) {
            return;
        }
        if (err == 7) {
            throw new IllegalArgumentException(Integer.toString(err));
        } else if (err != 10) {
            throw new NullPointerException(Integer.toString(err));
        } else {
            throw new IllegalStateException(Integer.toString(err));
        }
    }
}
