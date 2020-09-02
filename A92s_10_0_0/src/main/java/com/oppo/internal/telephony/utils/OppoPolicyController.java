package com.oppo.internal.telephony.utils;

import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.Phone;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;

public class OppoPolicyController {
    private static boolean lockDevice = false;
    private static String mSimlockOperator = "-1";
    private static boolean restrictCallIn = false;
    private static boolean restrictCallOut = false;
    private static boolean restrictPs = false;
    private static boolean restrictSlot1CallIn = false;
    private static boolean restrictSlot1CallOut = false;
    private static boolean restrictSlot1SmsReceive = false;
    private static boolean restrictSlot1SmsSend = false;
    private static boolean restrictSlot2CallIn = false;
    private static boolean restrictSlot2CallOut = false;
    private static boolean restrictSlot2SmsReceive = false;
    private static boolean restrictSlot2SmsSend = false;
    private static boolean restrictSmsReceive = false;
    private static boolean restrictSmsSend = false;

    public static boolean isSellMode(Phone phone) {
        return false;
    }

    public static boolean isPoliceVersion(Phone phone) {
        if (phone == null) {
            return false;
        }
        try {
            return phone.getContext().getPackageManager().hasSystemFeature("oppo.customize.function.mdpoe");
        } catch (Exception e) {
            return false;
        }
    }

    private static String propGetEnable(String prop, String defval) {
        try {
            return SystemProperties.get(prop, defval);
        } catch (Exception ex) {
            Rlog.e("oem", "getProp error :" + ex.getMessage());
            return defval;
        }
    }

    public static String getCTEnable(String key, String defval) {
        return propGetEnable("persist.sys.oem_ct_" + key, defval);
    }

    public static String getNONCTEnable(String key, String defval) {
        return propGetEnable("persist.sys.oem_nct_" + key, defval);
    }

    public static String getFeatureEnable(String key, String defval) {
        return propGetEnable("persist.sys.oem_" + key, defval);
    }

    public static boolean getCTEnable(String key) {
        return !getCTEnable(key, "-1").equals("0");
    }

    public static boolean getNONCTEnable(String key) {
        return !getNONCTEnable(key, "-1").equals("0");
    }

    public static boolean getFeatureEnable(String key) {
        return !getFeatureEnable(key, "-1").equals("0");
    }

    public static boolean isPrimaryCard(Phone phone) {
        if (phone == null || phone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            return true;
        }
        return false;
    }

    public static boolean canSwitchByUser(Phone phone) {
        if (getFeatureEnable("db", "-1").equals("0") || getFeatureEnable("db", "-1").equals("1") || getFeatureEnable("p_db", "-1").equals("0")) {
            return false;
        }
        if (!OppoPhoneUtil.isCtCard(phone) || !getCTEnable("db", "-1").equals(RegionLockConstant.TEST_OP_CUANDCMCC)) {
            return true;
        }
        return false;
    }

    public static boolean isCallInEnable(Phone phone) {
        if (restrictCallIn || ((phone.getPhoneId() == 0 && restrictSlot1CallIn) || ((phone.getPhoneId() == 1 && restrictSlot2CallIn) || isSellMode(phone)))) {
            return false;
        }
        if (!isPoliceVersion(phone)) {
            return true;
        }
        if (isPrimaryCard(phone)) {
            if (!getFeatureEnable("p_vi")) {
                return false;
            }
        } else if (!getFeatureEnable("s_vb")) {
            return false;
        }
        boolean isct = OppoPhoneUtil.isCtCard(phone);
        return getFeatureEnable("vi") && (!isct || getCTEnable("vi")) && (isct || getNONCTEnable("vb"));
    }

    public static boolean isCallOutEnable(Phone phone) {
        if (restrictCallOut || ((phone.getPhoneId() == 0 && restrictSlot1CallOut) || (phone.getPhoneId() == 1 && restrictSlot2CallOut))) {
            return false;
        }
        if (isSellMode(phone)) {
            Rlog.d("oem", "isSeleMode");
            return false;
        } else if (!isPoliceVersion(phone)) {
            return true;
        } else {
            if (isPrimaryCard(phone)) {
                if (!getFeatureEnable("p_vo")) {
                    return false;
                }
            } else if (!getFeatureEnable("s_vb")) {
                return false;
            }
            boolean isct = OppoPhoneUtil.isCtCard(phone);
            return getFeatureEnable("vo") && (!isct || getCTEnable("vo")) && (isct || getNONCTEnable("vb"));
        }
    }

    public static boolean isDataAllow(Phone phone) {
        if (isPsRestrictedEnable() || isSellMode(phone)) {
            return false;
        }
        if (!isPoliceVersion(phone)) {
            return true;
        }
        if (isPrimaryCard(phone)) {
            if (!getFeatureEnable("p_db")) {
                return false;
            }
        } else if (!getFeatureEnable("s_db")) {
            return false;
        }
        if (!getFeatureEnable("db")) {
            return false;
        }
        if (OppoPhoneUtil.isCtCard(phone)) {
            if (!getCTEnable("db") || (getCTEnable("db", "-1").equals("1") && !getCTEnable("dc"))) {
                return false;
            }
        } else if (!getNONCTEnable("db")) {
            return false;
        }
        return true;
    }

    public static boolean isMmsAllow() {
        if (!getFeatureEnable("mb")) {
            return false;
        }
        return true;
    }

    public static boolean isSmsSendEnable(Phone phone) {
        if (restrictSmsSend || ((phone.getPhoneId() == 0 && restrictSlot1SmsSend) || ((phone.getPhoneId() == 1 && restrictSlot2SmsSend) || isSellMode(phone)))) {
            return false;
        }
        if (!isPoliceVersion(phone)) {
            return true;
        }
        if (isPrimaryCard(phone)) {
            if (!getFeatureEnable("p_ss")) {
                return false;
            }
        } else if (!getFeatureEnable("s_sb")) {
            return false;
        }
        if (!getFeatureEnable(NetworkDiagnoseUtils.INFO_SERVICESTATE)) {
            return false;
        }
        boolean isct = OppoPhoneUtil.isCtCard(phone);
        return (!isct || getCTEnable(NetworkDiagnoseUtils.INFO_SERVICESTATE)) && (isct || getNONCTEnable("sb"));
    }

    public static boolean isSmsReceiveEnable(Phone phone) {
        if (restrictSmsReceive || ((phone.getPhoneId() == 0 && restrictSlot1SmsReceive) || ((phone.getPhoneId() == 1 && restrictSlot2SmsReceive) || isSellMode(phone)))) {
            return false;
        }
        if (!isPoliceVersion(phone)) {
            return true;
        }
        if (isPrimaryCard(phone)) {
            if (!getFeatureEnable("p_sr")) {
                return false;
            }
        } else if (!getFeatureEnable("s_sb")) {
            return false;
        }
        if (!getFeatureEnable("sr")) {
            return false;
        }
        boolean isct = OppoPhoneUtil.isCtCard(phone);
        return (!isct || getCTEnable("sr")) && (isct || getNONCTEnable("sb"));
    }

    public static boolean isSimlockLockdeviceEnable() {
        return lockDevice;
    }

    public static boolean isPsRestrictedEnable() {
        return restrictPs;
    }

    public static void setSimlockLockDevice(boolean value) {
        lockDevice = value;
    }

    public static void setPsRestricted(boolean value) {
        restrictPs = value;
    }

    public static void setCallInRestricted(boolean value) {
        restrictCallIn = value;
    }

    public static void setCallOutRestricted(boolean value) {
        restrictCallOut = value;
    }

    public static void setSmsReceiveRestricted(boolean value) {
        restrictSmsReceive = value;
    }

    public static void setSmsSendRestricted(boolean value) {
        restrictSmsSend = value;
    }

    public static boolean getSimlockLockDevice() {
        return lockDevice;
    }

    public static boolean getCallInRestricted() {
        return restrictCallIn;
    }

    public static boolean getCallOutRestricted() {
        return restrictCallOut;
    }

    public static boolean getSmsReceiveRestricted() {
        return restrictSmsReceive;
    }

    public static boolean getSmsSendRestricted() {
        return restrictSmsSend;
    }

    public static boolean getCallInRestricted(int slot) {
        if (slot == 0) {
            return restrictSlot1CallIn;
        }
        if (slot == 1) {
            return restrictSlot2CallIn;
        }
        return false;
    }

    public static boolean getCallOutRestricted(int slot) {
        if (slot == 0) {
            return restrictSlot1CallOut;
        }
        if (slot == 1) {
            return restrictSlot2CallOut;
        }
        return false;
    }

    public static boolean getSmsReceiveRestricted(int slot) {
        if (slot == 0) {
            return restrictSlot1SmsReceive;
        }
        if (slot == 1) {
            return restrictSlot2SmsReceive;
        }
        return false;
    }

    public static boolean getSmsSendRestricted(int slot) {
        if (slot == 0) {
            return restrictSlot1SmsSend;
        }
        if (slot == 1) {
            return restrictSlot2SmsSend;
        }
        return false;
    }

    public static void setCallInRestricted(boolean value, int slot) {
        if (slot == 0) {
            restrictSlot1CallIn = value;
        }
        if (slot == 1) {
            restrictSlot2CallIn = value;
        }
    }

    public static void setCallOutRestricted(boolean value, int slot) {
        if (slot == 0) {
            restrictSlot1CallOut = value;
        }
        if (slot == 1) {
            restrictSlot2CallOut = value;
        }
    }

    public static void setSmsReceiveRestricted(boolean value, int slot) {
        if (slot == 0) {
            restrictSlot1SmsReceive = value;
        }
        if (slot == 1) {
            restrictSlot2SmsReceive = value;
        }
    }

    public static void setSmsSendRestricted(boolean value, int slot) {
        if (slot == 0) {
            restrictSlot1SmsSend = value;
        }
        if (slot == 1) {
            restrictSlot2SmsSend = value;
        }
    }

    public static void setSimlockOperator(String simlockOperator) {
        mSimlockOperator = simlockOperator;
    }

    public static String getSimlockOperator() {
        return mSimlockOperator;
    }

    public static boolean isOppoSimLockVersion() {
        return !"-1".equals(mSimlockOperator);
    }
}
