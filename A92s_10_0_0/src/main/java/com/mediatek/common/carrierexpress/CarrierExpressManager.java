package com.mediatek.common.carrierexpress;

public class CarrierExpressManager {
    public static final String ACTION_CXP_NOTIFY_FEATURE = "com.mediatek.common.carrierexpress.cxp_notify_feature";
    public static final String ACTION_CXP_RESET_MODEM = "com.mediatek.common.carrierexpress.cxp_reset_modem";
    public static final String ACTION_CXP_SET_VENDOR_PROP = "com.mediatek.common.carrierexpress.cxp_set_vendor_prop";
    public static final String ACTION_OPERATOR_CONFIG_CHANGED = "com.mediatek.common.carrierexpress.operator_config_changed";
    public static final String ACTION_USER_NOTIFICATION_INTENT = "com.mediatek.common.carrierexpress.action.user_notification_intent";
    public static final String CARRIEREXPRESS_SERVICE = "carrierexpress";
    private static final boolean DBG = true;
    public static final int OPERATOR_OP03_SUBID_1 = 1;
    public static final int OPERATOR_OP03_SUBID_2 = 2;
    public static final int OPERATOR_OP03_SUBID_3 = 3;
    public static final int OPERATOR_OP03_SUBID_4 = 4;
    public static final int OPERATOR_OP03_SUBID_5 = 5;
    public static final int OPERATOR_OP03_SUBID_DEFAULT = 0;
    public static final int SRV_CONFIG_STATE_IDLE = 2;
    public static final int SRV_CONFIG_STATE_INIT = 0;
    public static final int SRV_CONFIG_STATE_WAIT = 1;
    private static final String TAG = "CarrierExpressManager";
    private static CarrierExpressManager sInstance = null;
}
