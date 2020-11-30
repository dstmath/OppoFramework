package com.mediatek.internal.telephony;

public class MtkSuppServContants {
    public static final String SYS_PROP_BOOL_CONFIG = "persist.vendor.ss.cfg.boolconfig";
    public static final String SYS_PROP_BOOL_VALUE = "persist.vendor.ss.cfg.boolvalue";

    public enum CUSTOMIZATION_ITEM {
        GSM_UT_SUPPORT,
        TBCLIR,
        IMS_NW_CW,
        NOT_SUPPORT_XCAP,
        NOT_SUPPORT_OCB,
        ENABLE_XCAP_HTTP_RESPONSE_409,
        TRANSFER_XCAP_404,
        NOT_SUPPORT_WFC_UT,
        NOT_SUPPORT_CALL_IDENTITY,
        RE_REGISTER_FOR_CF,
        SUPPORT_SAVE_CF_NUMBER,
        QUERY_CFU_AGAIN_AFTER_SET,
        NEED_CHECK_DATA_ENABLE,
        NEED_CHECK_DATA_ROAMING,
        NEED_CHECK_IMS_WHEN_ROAMING
    }

    public static String toString(CUSTOMIZATION_ITEM item) {
        switch (item) {
            case GSM_UT_SUPPORT:
                return "GSM_UT_SUPPORT";
            case NOT_SUPPORT_XCAP:
                return "NOT_SUPPORT_XCAP";
            case TBCLIR:
                return "TBCLIR";
            case IMS_NW_CW:
                return "IMS_NW_CW";
            case ENABLE_XCAP_HTTP_RESPONSE_409:
                return "ENABLE_XCAP_HTTP_RESPONSE_409";
            case TRANSFER_XCAP_404:
                return "TRANSFER_XCAP_404";
            case NOT_SUPPORT_CALL_IDENTITY:
                return "NOT_SUPPORT_CALL_IDENTITY";
            case RE_REGISTER_FOR_CF:
                return "RE_REGISTER_FOR_CF";
            case SUPPORT_SAVE_CF_NUMBER:
                return "SUPPORT_SAVE_CF_NUMBER";
            case QUERY_CFU_AGAIN_AFTER_SET:
                return "QUERY_CFU_AGAIN_AFTER_SET";
            case NOT_SUPPORT_OCB:
                return "NOT_SUPPORT_OCB";
            case NOT_SUPPORT_WFC_UT:
                return "NOT_SUPPORT_WFC_UT";
            case NEED_CHECK_DATA_ENABLE:
                return "NEED_CHECK_DATA_ENABLE";
            case NEED_CHECK_DATA_ROAMING:
                return "NEED_CHECK_DATA_ROAMING";
            case NEED_CHECK_IMS_WHEN_ROAMING:
                return "NEED_CHECK_IMS_WHEN_ROAMING";
            default:
                return "UNKNOWN_ITEM";
        }
    }
}
