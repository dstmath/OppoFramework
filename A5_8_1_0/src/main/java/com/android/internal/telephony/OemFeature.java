package com.android.internal.telephony;

import android.os.SystemProperties;
import com.android.internal.telephony.uicc.SpnOverride;

public class OemFeature {
    public static final String FEATURE_BASE = SystemProperties.get("persist.sys.oem_feature", SpnOverride.MVNO_TYPE_NONE);
    public static final boolean FEATURE_CALL_STABILITY;
    public static final boolean FEATURE_COMM_STACK = (FEATURE_BASE.indexOf("cm_st") >= 0);
    public static final boolean FEATURE_OPPO_COMM_ORIENTATION;
    public static final boolean FEATURE_OPPO_COMM_PROXIMITY;
    public static final boolean FEATURE_SMS_BUG_FIX;
    public static final boolean FEATURE_SMS_CALCULATOR;
    public static final boolean FEATURE_SMS_COLOR_OS_BLOCK = (FEATURE_BASE.indexOf("sms_blo") >= 0);
    public static final boolean FEATURE_SMS_CROSSMAP_TWO_CT_NOT_ALLOWED;
    public static final boolean FEATURE_SMS_DUPLICATE;
    public static final boolean FEATURE_SMS_GSM_GARBLED;
    public static final boolean FEATURE_SMS_IMS_RETRY;
    public static final boolean FEATURE_SMS_POLICY;
    public static final boolean FEATURE_SMS_PUSH;
    public static final boolean FEATURE_SMS_REGISTER;
    public static final boolean FEATURE_SMS_ROM_REQUIREMENT_GROUP_SEND_MORE_ALLOWD;
    public static final boolean FEATURE_SMS_ROM_REQUIREMENT_NOT_CHECK_CHARGE_DESTINATION;
    public static final boolean FEATURE_SMS_ROM_REQUIREMENT_RECEIVE_AND_SET;
    public static final boolean FEATURE_SMS_ROM_REQUIREMENT_THIRD_APP_SEND;
    public static final boolean FEATURE_SMS_SEDN_NOT_SHOW_IN_UI;
    public static final boolean FEATURE_SMS_WRITE_DB_PERMISSION;
    public static final int ORIENTATION_MAX_TIMER = 0;
    public static final int ORIENTATION_MAX_TIMES = 0;

    static {
        boolean z;
        boolean z2 = true;
        if (FEATURE_BASE.indexOf("c_sta") < 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_CALL_STABILITY = z;
        if (FEATURE_BASE.indexOf("sms_dup") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_DUPLICATE = z;
        if (FEATURE_BASE.indexOf("sms_reg") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_REGISTER = z;
        if (FEATURE_BASE.indexOf("sms_pus") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_PUSH = z;
        if (FEATURE_BASE.indexOf("sms_gga") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_GSM_GARBLED = z;
        if (FEATURE_BASE.indexOf("sms_sns") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_SEDN_NOT_SHOW_IN_UI = z;
        if (FEATURE_BASE.indexOf("sms_wdp") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_WRITE_DB_PERMISSION = z;
        if (FEATURE_BASE.indexOf("sms_cal") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_CALCULATOR = z;
        if (FEATURE_BASE.indexOf("sms_ire") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_IMS_RETRY = z;
        if (FEATURE_BASE.indexOf("sms_ctc") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_CROSSMAP_TWO_CT_NOT_ALLOWED = z;
        if (FEATURE_BASE.indexOf("sms_pol") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_POLICY = z;
        if (FEATURE_BASE.indexOf("sms_rr_tas") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_ROM_REQUIREMENT_THIRD_APP_SEND = z;
        if (FEATURE_BASE.indexOf("sms_rr_ras") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_ROM_REQUIREMENT_RECEIVE_AND_SET = z;
        if (FEATURE_BASE.indexOf("sms_rr_ncc") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_ROM_REQUIREMENT_NOT_CHECK_CHARGE_DESTINATION = z;
        if (FEATURE_BASE.indexOf("sms_rr_gsm") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_ROM_REQUIREMENT_GROUP_SEND_MORE_ALLOWD = z;
        if (FEATURE_BASE.indexOf("sms_bfi") >= 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_SMS_BUG_FIX = z;
        if (FEATURE_BASE.indexOf("cm_pss") < 0) {
            z = true;
        } else {
            z = false;
        }
        FEATURE_OPPO_COMM_PROXIMITY = z;
        if (FEATURE_BASE.indexOf("cm_ori") >= 0) {
            z2 = false;
        }
        FEATURE_OPPO_COMM_ORIENTATION = z2;
    }
}
