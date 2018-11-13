package com.android.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codeaurora.ims.utils.QtiImsExtUtils;

public class OemConstant {
    public static final int CARD_TYPE_CM = 2;
    public static final int CARD_TYPE_CT = 1;
    public static final int CARD_TYPE_CU = 3;
    public static final int CARD_TYPE_OTHER = 4;
    public static final int CARD_TYPE_TEST = 9;
    public static final int CARD_TYPE_UNKNOWN = -1;
    public static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    public static final boolean FEATURE_TEST_SIM = SystemProperties.get("persist.sys.nw_lab_simtest", "0").equals("1");
    private static final String[] JIO_MCCMNC_LIST = new String[]{"405840", QtiImsExtUtils.CARRIER_ONE_DEFAULT_MCC_MNC, "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874"};
    private static final String OEM_VOOC_STATE = "sys.oppo.disable_vooc";
    public static final boolean PRINT_TEST = SystemProperties.get("persist.sys.oem_print", "0").equals("1");
    private static final boolean SUPPORT_BATTERY = SystemProperties.get("ro.separate.soft", "oppo").equals("17123");
    private static final boolean SUPPORT_HEADSET = SystemProperties.get("ro.separate.soft", "oppo").equals("16353");
    public static final boolean SWITCH_LOG = "true".equalsIgnoreCase(SystemProperties.get("persist.sys.assert.panic", "false"));
    public static final int SWITCH_VOLTE_CS = Integer.parseInt(SystemProperties.get("persist.sys.oem_volte", "-110"));
    public static final boolean SWITCH__SMOOTH = SystemProperties.get("persist.sys.oem_smooth", "0").equals("1");
    private static boolean mPowerCenterEnable = "true".equalsIgnoreCase(SystemProperties.get("sys.oppopcm.enable", "true"));

    public static boolean getWlanAssistantEnable(Context context) {
        boolean romUpdateWlanAssistant = Global.getInt(context.getContentResolver(), "rom.update.wifi.assistant", 1) == 1;
        boolean wlanAssistantFeature = context.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant");
        if (SWITCH_LOG) {
            Rlog.w("oem", "wlanAssistantFeature = " + wlanAssistantFeature + ", romUpdateWlanAssistant= " + romUpdateWlanAssistant);
        }
        return wlanAssistantFeature ? romUpdateWlanAssistant : false;
    }

    public static boolean getPowerCenterEnable(Context context) {
        return mPowerCenterEnable;
    }

    public static boolean getPowerCenterEnableFromProp(Context context) {
        mPowerCenterEnable = "true".equalsIgnoreCase(SystemProperties.get("sys.oppopcm.enable", "true"));
        if (SWITCH_LOG) {
            Rlog.w("oem", "getPowerCenterEnableFromProp = " + mPowerCenterEnable);
        }
        return mPowerCenterEnable;
    }

    public static boolean isCTS(Context context) {
        return context.getPackageManager().hasSystemFeature("persist.oppo.ctsversion");
    }

    public static boolean isCTA(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.cta.support");
    }

    public static boolean isNwLabTest() {
        return SystemProperties.get("persist.sys.nw_lab_test", "0").equals("1");
    }

    public static boolean isTestCard(Context context, byte b) {
        if (b == Byte.MIN_VALUE || b == (byte) -127) {
            return true;
        }
        return (b == (byte) 2 || b == (byte) 4) ? isNwLabTest() : false;
    }

    public static boolean isTestCard(String mImsi) {
        if (mImsi == null || (!mImsi.startsWith("00101") && !mImsi.equals("111223333333333"))) {
            return FEATURE_TEST_SIM;
        }
        return true;
    }

    public static void printStack(String msg) {
        if (OemFeature.FEATURE_COMM_STACK) {
            Rlog.e("oem", "oem stack", new Throwable(msg));
        }
    }

    public static void printStack(boolean isPrint, String msg) {
        if (isPrint || OemFeature.FEATURE_COMM_STACK) {
            Rlog.e("oem", "oem stack", new Throwable(msg));
        }
    }

    public static boolean isDumpOff() {
        try {
            return SystemProperties.get("persist.sys.dump", "1").equals("0");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isUiccSlotForbid(int slotid) {
        return "1".equals(TelephonyManager.getTelephonyProperty(slotid, "persist.sys.oem_forbid_slots", "0"));
    }

    public static boolean isPoliceVersion(Phone phone) {
        if (phone != null) {
            try {
                return phone.getContext().getPackageManager().hasSystemFeature("oppo.customize.function.mdpoe");
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
        return getCTEnable(key, "-1").equals("0") ^ 1;
    }

    public static boolean getNONCTEnable(String key) {
        return getNONCTEnable(key, "-1").equals("0") ^ 1;
    }

    public static boolean getFeatureEnable(String key) {
        return getFeatureEnable(key, "-1").equals("0") ^ 1;
    }

    public static boolean canSwitchByUser(Phone phone) {
        if (getFeatureEnable("db", "-1").equals("0") || getFeatureEnable("db", "-1").equals("1")) {
            return false;
        }
        if (isCtCard(phone) && getCTEnable("db", "-1").equals(RegionLockConstant.TEST_OP_CUANDCMCC)) {
            return false;
        }
        return true;
    }

    public static boolean isDataAllow(Phone phone) {
        if (isDeviceLockEnable(phone)) {
            return false;
        }
        if (!isPoliceVersion(phone)) {
            return true;
        }
        if (!getFeatureEnable("db")) {
            return false;
        }
        if (isCtCard(phone)) {
            if (!getCTEnable("db") || (getCTEnable("db", "-1").equals("1") && (getCTEnable("dc") ^ 1) != 0)) {
                return false;
            }
        } else if (!getNONCTEnable("db")) {
            return false;
        }
        return true;
    }

    public static boolean isMmsAllow() {
        if (getFeatureEnable("mb")) {
            return true;
        }
        return false;
    }

    public static boolean isSmsSendEnable(Phone phone) {
        if (isDeviceLockEnable(phone)) {
            return false;
        }
        if (!isPoliceVersion(phone)) {
            return true;
        }
        if (!getFeatureEnable("ss")) {
            return false;
        }
        boolean isct = isCtCard(phone);
        return (!isct || (getCTEnable("ss") ^ 1) == 0) && (isct || (getNONCTEnable("sb") ^ 1) == 0);
    }

    public static boolean isSmsReceiveEnable(Phone phone) {
        if (isDeviceLockEnable(phone)) {
            return false;
        }
        if (!isPoliceVersion(phone)) {
            return true;
        }
        if (!getFeatureEnable("sr")) {
            return false;
        }
        boolean isct = isCtCard(phone);
        return (!isct || (getCTEnable("sr") ^ 1) == 0) && (isct || (getNONCTEnable("sb") ^ 1) == 0);
    }

    public static boolean isCallInEnable(Phone phone) {
        if (isDeviceLockEnable(phone)) {
            return false;
        }
        if (!isPoliceVersion(phone)) {
            return true;
        }
        boolean isct = isCtCard(phone);
        return getFeatureEnable("vi") && ((!isct || (getCTEnable("vi") ^ 1) == 0) && (isct || (getNONCTEnable("vb") ^ 1) == 0));
    }

    public static boolean isCallOutEnable(Phone phone) {
        if (!isPoliceVersion(phone)) {
            return true;
        }
        boolean isct = isCtCard(phone);
        if (!getFeatureEnable("vo") || ((isct && (getCTEnable("vo") ^ 1) != 0) || (!isct && (getNONCTEnable("vb") ^ 1) != 0))) {
            return false;
        }
        return true;
    }

    public static boolean isCtCard(Phone phone) {
        if (phone == null) {
            return false;
        }
        try {
            int subId = phone.getSubId();
            int phoneId = phone.getPhoneId();
            String plmn = TelephonyManager.getDefault().getSubscriberId(subId);
            if (TextUtils.isEmpty(plmn)) {
                plmn = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
            }
            if (!TextUtils.isEmpty(plmn) && (plmn.startsWith("46003") || plmn.startsWith("46011"))) {
                return true;
            }
            String ims = phone.getLteCdmaImsi(phoneId)[0];
            if (ims.length() >= 5) {
                ims = ims.substring(0, 5);
            }
            if ("46003".equals(ims) || "46011".equals(ims) || "45502".equals(ims)) {
                return true;
            }
            return SubscriptionManager.isCTCCard(phoneId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isJioCard(Phone phone) {
        if (phone == null) {
            Rlog.d("oem", "isJioCard, phone == null");
            return false;
        }
        int phoneId = phone.getPhoneId();
        List<String> jioMccmncList = new ArrayList(Arrays.asList(JIO_MCCMNC_LIST));
        String mccmnc = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
        Rlog.d("oem", "isJioCard, mccmnc: " + mccmnc);
        if (TextUtils.isEmpty(mccmnc) || !jioMccmncList.contains(mccmnc)) {
            return false;
        }
        return true;
    }

    public static void setOemVoocState(State oldState, State newState) {
        if (oldState != newState) {
            String value = "false";
            if (oldState == State.IDLE) {
                value = "true";
            } else if (newState == State.IDLE) {
                value = "false";
            } else {
                return;
            }
            if (!value.equalsIgnoreCase(SystemProperties.get(OEM_VOOC_STATE, "null"))) {
                if (SWITCH_LOG) {
                    Rlog.d("oem", "setOemVoocState :" + value);
                }
                SystemProperties.set(OEM_VOOC_STATE, value);
                checkVoocState(value);
            }
        }
    }

    public static void checkVoocState(String value) {
        if (!value.equalsIgnoreCase(SystemProperties.get(OEM_VOOC_STATE, "false"))) {
            if (SWITCH_LOG) {
                Rlog.d("oem", "checkVoocState :" + value);
            }
            SystemProperties.set(OEM_VOOC_STATE, value);
        }
    }

    public static boolean isVsimIgnoreUserDataSetting(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.softsim.ignore_data_setting");
    }

    public static boolean needSetSarForHeadSet() {
        if (EXP_VERSION && SUPPORT_HEADSET) {
            return true;
        }
        return false;
    }

    public static boolean needSetSarForUSB() {
        if (EXP_VERSION && SUPPORT_HEADSET) {
            return false;
        }
        return true;
    }

    public static boolean needSetSarForBattery() {
        if (EXP_VERSION && SUPPORT_BATTERY) {
            return true;
        }
        return false;
    }

    public static boolean isSimProtectContact(Context context, String packageName) {
        return OemSimProtect.getInstance().isSimProtectContact(context, packageName);
    }

    public static boolean isSimProtectSms(Context context, String packageName) {
        return OemSimProtect.getInstance().isSimProtectSms(context, packageName);
    }

    public static String getOemRes(Context context, String resName, String defValue) {
        try {
            return context.getString(context.getResources().getIdentifier(resName, "string", "oppo"));
        } catch (Exception e) {
            return defValue;
        }
    }

    public static int getCardType(String imsi, String iccid) {
        int result = -1;
        Rlog.d("oem", "getCardType imsi : " + imsi + " iccid : " + iccid);
        if (imsi != null && imsi.length() > 5) {
            String mccmnc = imsi.substring(0, 5);
            if (mccmnc.equals("00101") || SystemProperties.getInt("persist.sys.oppo.ctlab", 0) == 1) {
                result = 9;
            } else if (mccmnc.equals("46003") || mccmnc.equals("46011") || mccmnc.equals("45502")) {
                result = 1;
            } else if (mccmnc.equals("46001") || mccmnc.equals("46009") || mccmnc.equals("45407")) {
                result = 3;
            } else if (mccmnc.equals("46000") || mccmnc.equals("46002") || mccmnc.equals("46004") || mccmnc.equals("46007") || mccmnc.equals("46008")) {
                result = 2;
            }
            if (result != -1) {
                Rlog.d("oem", "getCardType by imsi result = " + result);
                return result;
            }
        }
        if (iccid != null && iccid.length() > 6 && result == -1) {
            String operator = iccid.substring(0, 6);
            if (operator.equals("898603") || operator.equals("898611")) {
                result = 1;
            } else if (operator.equals("898600") || operator.equals("898602") || operator.equals("898607")) {
                result = 2;
            } else if (iccid.startsWith("898601234")) {
                result = 9;
            } else if (operator.equals("898601") || operator.equals("898609")) {
                result = 3;
            } else {
                result = 4;
            }
        }
        Rlog.d("oem", "getCardType by iccid result = " + result);
        return result;
    }

    public static boolean isCallOutEnableExp(Phone phone) {
        if (isDeviceLockEnable(phone)) {
            return false;
        }
        return true;
    }

    private static boolean isDeviceLockEnable(Phone phone) {
        boolean z = false;
        if (!EXP_VERSION || phone == null) {
            return false;
        }
        OemDeviceLock oemLock = OemDeviceLock.getInstance(phone.getContext());
        if (OemDeviceLock.getSimLoaded(phone.getPhoneId())) {
            z = OemDeviceLock.getDeviceLockedForPhone(phone.getPhoneId());
        }
        return z;
    }

    public static boolean isDeviceLockVersion() {
        return OemDeviceLock.IS_OP_LOCK;
    }
}
