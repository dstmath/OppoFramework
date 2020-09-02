package com.mediatek.internal.telephony;

public interface MtkTelephonyProperties {
    public static final String CFU_QUERY_ICCID_PROP = "persist.vendor.radio.cfu.iccid.";
    public static final String CFU_QUERY_OVER_IMS = "persist.vendor.radio.cfu_over_ims";
    public static final String CFU_QUERY_SIM_CHANGED_PROP = "persist.vendor.radio.cfu.change.";
    public static final String CFU_QUERY_TYPE_DEF_VALUE = "0";
    public static final String CFU_QUERY_TYPE_PROP = "persist.vendor.radio.cfu.querytype";
    public static final String CFU_STATUS_SYNC_FOR_OTA = "persist.vendor.radio.cfu.sync_for_ota";
    public static final String PROPERTY_ACTIVE_MD = "vendor.ril.active.md";
    public static final String PROPERTY_ERROR_MESSAGE_FROM_XCAP = "vendor.gsm.radio.ss.errormsg";
    public static final String PROPERTY_EXTERNAL_DISABLE_SIM_DIALOG = "vendor.gsm.disable.sim.dialog";
    public static final String PROPERTY_EXTERNAL_SIM_ENABLED = "vendor.gsm.external.sim.enabled";
    public static final String PROPERTY_EXTERNAL_SIM_INSERTED = "vendor.gsm.external.sim.inserted";
    public static final String PROPERTY_EXTERNAL_SIM_TIMEOUT = "vendor.gsm.external.sim.timeout";
    public static final String PROPERTY_ICC_OPERATOR_DEFAULT_NAME = "vendor.gsm.sim.operator.default-name";
    public static final String PROPERTY_INECM_MODE_BY_SLOT = "vendor.ril.cdma.inecmmode_by_slot";
    public static final String PROPERTY_NITZ_OPER_CODE = "persist.vendor.radio.nitz_oper_code";
    public static final String PROPERTY_NITZ_OPER_LNAME = "persist.vendor.radio.nitz_oper_lname";
    public static final String PROPERTY_NITZ_OPER_SNAME = "persist.vendor.radio.nitz_oper_sname";
    public static final String PROPERTY_PERSIST_EXTERNAL_SIM = "persist.vendor.radio.external.sim";
    public static final String PROPERTY_PERSIST_EXTERNAL_SIM_TIMEOUT = "persist.vendor.radio.vsim.timeout";
    public static final String PROPERTY_PREFERED_AKA_SIM = "vendor.gsm.prefered.aka.sim.slot";
    public static final String PROPERTY_PREFERED_REMOTE_SIM = "vendor.gsm.prefered.rsim.slot";
    public static final String PROPERTY_TBCW_MODE = "persist.vendor.radio.terminal-based.cw";
    public static final String PROPERTY_UT_CFU_NOTIFICATION_MODE = "persist.vendor.radio.cfu.mode";
    public static final String SS_SERVICE_CLASS_PROP = "vendor.gsm.radio.ss.sc";
    public static final String TBCW_DISABLED = "disabled_tbcw";
    public static final String TBCW_OFF = "enabled_tbcw_off";
    public static final String TBCW_OFF_VOLTE_ONLY = "tbcw_off_volte_only";
    public static final String TBCW_ON = "enabled_tbcw_on";
    public static final String TBCW_ON_VOLTE_ONLY = "tbcw_on_volte_only";
    public static final String UT_CFU_NOTIFICATION_MODE_DISABLED = "disabled_cfu_mode";
    public static final String UT_CFU_NOTIFICATION_MODE_OFF = "enabled_cfu_mode_off";
    public static final String UT_CFU_NOTIFICATION_MODE_ON = "enabled_cfu_mode_on";
}
