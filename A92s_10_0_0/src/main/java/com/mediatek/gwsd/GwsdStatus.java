package com.mediatek.gwsd;

public class GwsdStatus {
    public static final String ERROR_INVALID_DEFAULT_DATA_SUBSCRIPTION_ID = "invalid id for default data phone";
    public static final String ERROR_INVALID_DSDS_OPERATION = "can not configure mode on non default data sim when device is under dsds";
    public static final String ERROR_INVALID_PHONE_INSTANCE = "invalid phone instance (null)";
    public static final String ERROR_NONE = "SUCCESS";
    public static final String ERROR_OUT_OF_SERVICE = "out of service on the device";
    public static final String ERROR_REPEAT_REGISTRE_LISTENER = "multi listener be registered";
    public static final String ERROR_SERVICE_BUSY = "service busy";
    public static final String ERROR_SIM_ABSENT = "sim is absent";
    public static final String ERROR_UNKNOW = "unknow error";
    public static final int STATUS_FAIL = -1;
    public static final int STATUS_SUCCESS = 0;
    public static final int SYSTEM_STATE_DEFAULT_DATA_SWITCHED = 7340034;
    public static final int SYSTEM_STATE_DEFAULT_DSDA_CHANGED = 7340035;
    public static final int SYSTEM_STATE_MODEM_RESET = 7340033;
    public static final int SYSTEM_STATE_OUT_OF_SERVICE = 7340036;
}
