package com.oppo.internal.telephony.utils;

import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.Phone;
import com.oppo.internal.telephony.OppoNewNitzStateMachine;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;

public class OppoPolicyController {
    private static long MILLISECONDS_OF_DAY = OppoNewNitzStateMachine.NITZ_NTP_INTERVAL_OEM;
    private static final int SMS_LIMIT_BY_DAY = 0;
    private static final int SMS_LIMIT_BY_MONTH = 2;
    private static final int SMS_LIMIT_BY_WEEK = 1;
    private static boolean lockDevice = false;
    private static String mSimlockOperator = "-1";
    private static boolean restrictCallIn = false;
    private static boolean restrictCallOut = false;
    private static boolean restrictPs = false;
    private static boolean restrictSlot1CallIn = false;
    private static boolean restrictSlot1CallOut = false;
    private static boolean restrictSlot1Mms = false;
    private static boolean restrictSlot1SmsReceive = false;
    private static boolean restrictSlot1SmsSend = false;
    private static boolean restrictSlot2CallIn = false;
    private static boolean restrictSlot2CallOut = false;
    private static boolean restrictSlot2Mms = false;
    private static boolean restrictSlot2SmsReceive = false;
    private static boolean restrictSlot2SmsSend = false;
    private static boolean restrictSmsReceive = false;
    private static boolean restrictSmsSend = false;

    public static boolean isSellMode(Phone phone) {
        return false;
    }

    public static boolean isPoliceVersion(Phone phone) {
        if (phone != null) {
            try {
                if (phone.getContext().getPackageManager().hasSystemFeature("oppo.customize.function.mdpoe") || phone.getContext().getPackageManager().hasSystemFeature("oppo.business.custom")) {
                    return true;
                }
                return false;
            } catch (Exception e) {
            }
        }
        return false;
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
        if (OppoPhoneUtil.isCtCard(phone) && getCTEnable("db", "-1").equals(RegionLockConstant.TEST_OP_CUANDCMCC)) {
            return false;
        }
        if (phone != null) {
            if (phone.getPhoneId() == 0) {
                if (getFeatureEnable("slot1_db", "-1").equals("0") || getFeatureEnable("slot1_db", "-1").equals("1")) {
                    return false;
                }
            } else if (phone.getPhoneId() != 1 || (!getFeatureEnable("slot2_db", "-1").equals("0") && !getFeatureEnable("slot2_db", "-1").equals("1"))) {
                return true;
            } else {
                return false;
            }
        }
        return true;
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
        if (phone.getPhoneId() == 0 && !getFeatureEnable("s1_vi")) {
            return false;
        }
        if (phone.getPhoneId() == 1 && !getFeatureEnable("s2_vi")) {
            return false;
        }
        if (OppoPhoneUtil.isRoamingState(phone) && !getFeatureEnable("r_vb")) {
            return false;
        }
        boolean isct = OppoPhoneUtil.isCtCard(phone);
        return getFeatureEnable("vi") && (!isct || getCTEnable("vi")) && (isct || getNONCTEnable("vb"));
    }

    public static boolean isCallOutEnable(Phone phone, boolean isEmergencyNumber) {
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
            if (phone.getPhoneId() == 0 && !getFeatureEnable("s1_vo")) {
                return false;
            }
            if (phone.getPhoneId() == 1 && !getFeatureEnable("s2_vo")) {
                return false;
            }
            if (OppoPhoneUtil.isRoamingState(phone) && !isEmergencyNumber && !getFeatureEnable("r_vb")) {
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
        if (phone != null) {
            if (phone.getPhoneId() == 0) {
                if (getFeatureEnable("slot1_db", "-1").equals("0")) {
                    return false;
                }
            } else if (phone.getPhoneId() == 1 && getFeatureEnable("slot2_db", "-1").equals("0")) {
                return false;
            }
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
        if (phone.getPhoneId() == 0 && (!getFeatureEnable("s1_ss") || !isPrimarySmsSendTimesEnable())) {
            return false;
        }
        if ((phone.getPhoneId() == 1 && (!getFeatureEnable("s2_ss") || !isSecondSmsSendTimesEnable())) || !getFeatureEnable(NetworkDiagnoseUtils.INFO_SERVICESTATE)) {
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
        if (phone.getPhoneId() == 0 && (!getFeatureEnable("s1_sr") || !isPrimarySmsReceiveTimesEnable())) {
            return false;
        }
        if ((phone.getPhoneId() == 1 && (!getFeatureEnable("s2_sr") || !isSecondSmsReceiveTimesEnable())) || !getFeatureEnable("sr")) {
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

    public static boolean getMmsRestricted(int slot) {
        if (slot == 0) {
            return restrictSlot1Mms;
        }
        if (slot == 1) {
            return restrictSlot2Mms;
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

    public static void setMmsRestricted(boolean value, int slot) {
        if (slot == 0) {
            restrictSlot1Mms = value;
        }
        if (slot == 1) {
            restrictSlot2Mms = value;
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

    private static boolean isPrimarySmsSendTimesEnable() {
        try {
            int mControlTime = Integer.parseInt(SystemProperties.get("persist.sys.oem_p_sst", "-1"));
            int mSendTime = Integer.parseInt(SystemProperties.get("persist.sys.oem_p_st", "0"));
            int mDateType = Integer.parseInt(SystemProperties.get("persist.sys.oem_p_sdt", "-1"));
            long mLimitTime = Long.parseLong(SystemProperties.get("persist.sys.oem_p_slt", "0"));
            if (mDateType > -1) {
                mLimitTime = transDateTypeToTime(mDateType);
                SystemProperties.set("persist.sys.oem_p_slt", Long.toString(mLimitTime));
                SystemProperties.set("persist.sys.oem_p_sdt", "-1");
            }
            if (mControlTime == -1) {
                return true;
            }
            if (mLimitTime != 0 && System.currentTimeMillis() > mLimitTime) {
                return true;
            }
            if (mSendTime >= mControlTime || (mLimitTime != 0 && System.currentTimeMillis() > mLimitTime)) {
                return false;
            }
            SystemProperties.set("persist.sys.oem_p_st", Integer.toString(mSendTime + 1));
            return true;
        } catch (Exception ex) {
            Rlog.e("oem", "getsmssendtimes error :" + ex.getMessage());
            return false;
        }
    }

    private static boolean isSecondSmsSendTimesEnable() {
        try {
            int mControlTime = Integer.parseInt(SystemProperties.get("persist.sys.oem_s_sst", "-1"));
            int mSendTime = Integer.parseInt(SystemProperties.get("persist.sys.oem_s_st", "0"));
            int mDateType = Integer.parseInt(SystemProperties.get("persist.sys.oem_s_sdt", "-1"));
            long mLimitTime = Long.parseLong(SystemProperties.get("persist.sys.oem_s_slt", "0"));
            if (mDateType > -1) {
                mLimitTime = transDateTypeToTime(mDateType);
                SystemProperties.set("persist.sys.oem_s_slt", Long.toString(mLimitTime));
                SystemProperties.set("persist.sys.oem_s_sdt", "-1");
            }
            if (mControlTime == -1) {
                return true;
            }
            if (mLimitTime != 0 && System.currentTimeMillis() > mLimitTime) {
                return true;
            }
            if (mSendTime >= mControlTime || (mLimitTime != 0 && System.currentTimeMillis() > mLimitTime)) {
                return false;
            }
            SystemProperties.set("persist.sys.oem_s_st", Integer.toString(mSendTime + 1));
            return true;
        } catch (Exception ex) {
            Rlog.e("oem", "getSecondsmssendtimes error :" + ex.getMessage());
            return false;
        }
    }

    private static boolean isPrimarySmsReceiveTimesEnable() {
        try {
            int mControlTime = Integer.parseInt(SystemProperties.get("persist.sys.oem_p_rrt", "-1"));
            int mReceiveTime = Integer.parseInt(SystemProperties.get("persist.sys.oem_p_rt", "0"));
            int mDateType = Integer.parseInt(SystemProperties.get("persist.sys.oem_p_rdt", "-1"));
            long mLimitTime = Long.parseLong(SystemProperties.get("persist.sys.oem_p_rlt", "0"));
            if (mDateType > -1) {
                mLimitTime = transDateTypeToTime(mDateType);
                SystemProperties.set("persist.sys.oem_p_rlt", Long.toString(mLimitTime));
                SystemProperties.set("persist.sys.oem_p_rdt", "-1");
            }
            if (mControlTime == -1) {
                return true;
            }
            if (mLimitTime != 0 && System.currentTimeMillis() > mLimitTime) {
                return true;
            }
            if (mReceiveTime >= mControlTime || (mLimitTime != 0 && System.currentTimeMillis() > mLimitTime)) {
                return false;
            }
            SystemProperties.set("persist.sys.oem_p_rt", Integer.toString(mReceiveTime + 1));
            return true;
        } catch (Exception ex) {
            Rlog.e("oem", "getsmssendtimes error :" + ex.getMessage());
            return false;
        }
    }

    private static boolean isSecondSmsReceiveTimesEnable() {
        try {
            int mControlTime = Integer.parseInt(SystemProperties.get("persist.sys.oem_s_rrt", "-1"));
            int mReceiveTime = Integer.parseInt(SystemProperties.get("persist.sys.oem_s_rt", "0"));
            int mDateType = Integer.parseInt(SystemProperties.get("persist.sys.oem_s_rdt", "-1"));
            long mLimitTime = Long.parseLong(SystemProperties.get("persist.sys.oem_s_rlt", "0"));
            if (mDateType > -1) {
                mLimitTime = transDateTypeToTime(mDateType);
                SystemProperties.set("persist.sys.oem_s_rlt", Long.toString(mLimitTime));
                SystemProperties.set("persist.sys.oem_s_rdt", "-1");
            }
            if (mControlTime == -1) {
                return true;
            }
            if (mLimitTime != 0 && System.currentTimeMillis() > mLimitTime) {
                return true;
            }
            if (mReceiveTime >= mControlTime || (mLimitTime != 0 && System.currentTimeMillis() > mLimitTime)) {
                return false;
            }
            SystemProperties.set("persist.sys.oem_s_rt", Integer.toString(mReceiveTime + 1));
            return true;
        } catch (Exception ex) {
            Rlog.e("oem", "getSecondsmssendtimes error :" + ex.getMessage());
            return false;
        }
    }

    private static long transDateTypeToTime(int mDateType) {
        long mLimitTime = System.currentTimeMillis();
        if (mDateType == 0) {
            return mLimitTime + MILLISECONDS_OF_DAY;
        }
        if (mDateType == 1) {
            return mLimitTime + (MILLISECONDS_OF_DAY * 7);
        }
        if (mDateType != 2) {
            return mLimitTime;
        }
        return mLimitTime + (MILLISECONDS_OF_DAY * 30);
    }
}
