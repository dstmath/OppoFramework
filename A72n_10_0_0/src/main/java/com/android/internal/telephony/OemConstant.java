package com.android.internal.telephony;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.util.OemTelephonyUtils;

public class OemConstant {
    public static final int CARD_NOT_PRESENT = -2;
    public static final int CARD_TYPE_CM = 2;
    public static final int CARD_TYPE_CT = 1;
    public static final int CARD_TYPE_CU = 3;
    public static final int CARD_TYPE_OTHER = 4;
    public static final int CARD_TYPE_TEST = 9;
    public static final int CARD_TYPE_UNKNOWN = -1;
    protected static final int CMD_OPPO_SET_SAR_RF_STATE = 5;
    public static final String CT_AUTOREG_IMS_PROP = "persist.sys.ct_auto_ims";
    public static final int EFPLMNsel = 28464;
    public static final int EF_OPLMNwAcT = 28513;
    public static final int EF_PLMN = 28464;
    public static final int EF_UPLMNwAcT = 28512;
    public static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    public static final boolean FEATURE_MTK_CTA_SUPPORT = SystemProperties.get("ro.mtk_cta_support").equals("1");
    public static final boolean FEATURE_SCREEN_SOLUTION = SystemProperties.get("ro.product.oppo.nw_scr_solution", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals("1");
    public static final boolean FEATURE_TEST_SIM = SystemProperties.get("persist.sys.nw_lab_simtest", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals("1");
    public static final int INVALID_STATE = -1;
    public static final int IS_NEW_SIM_CARD = 1;
    public static final int IS_OLD_SIM_CARD = 0;
    public static final int MODULE_IMS = 0;
    public static final int MODULE_IWLAN = 2;
    public static final int MODULE_WO = 1;
    public static final int NOT_PROVISIONED = 0;
    public static final int NT_CDMA = 5;
    public static final int NT_EVDO = 6;
    public static final int NT_GSM = 1;
    public static final int NT_LTE = 3;
    public static final int NT_TDS = 2;
    public static final int NT_UNKNOWN = 0;
    public static final int NT_WCDMA = 4;
    public static final int NW_INFO_TYPE_SEARCH_COUNT = 0;
    public static final String[] OEM_CHECK_CALL_STATE = {"sys.oppo.call_mode"};
    public static final int OOS_DELAY_NONE = 0;
    public static final int OOS_DELAY_TIMEOUT = 2;
    public static final int OOS_DELAY_TIMING = 1;
    public static final String PERSIST_SYS_LTEWIFI_COEXIST = "persist.sys.ltewificoexist";
    public static final boolean PRINT_TEST = SystemProperties.get("persist.sys.oem_print", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals("1");
    public static final String PROJECT_NAME = SystemProperties.get("ro.product.hw", "oppo");
    public static final int PROVISIONED = 1;
    public static final boolean RM_VERSION = SystemProperties.get("ro.product.brand.sub", PhoneConfigurationManager.SSSS).equalsIgnoreCase("Realme");
    protected static final int SAR_RF_STATE_DEFAULT = 0;
    protected static final int SAR_RF_STATE_PROXIMITY_ACQUIRED = 1;
    protected static final int SAR_RF_STATE_PROXIMITY_ACQUIRED_CHARGE_CALL = 7;
    protected static final int SAR_RF_STATE_PROXIMITY_ACQUIRED_HEADSET = 5;
    protected static final int SAR_RF_STATE_PROXIMITY_ACQUIRED_USB = 3;
    protected static final int SAR_RF_STATE_PROXIMITY_RELEASE = 2;
    protected static final int SAR_RF_STATE_PROXIMITY_RELEASE_CHARGE_CALL = 8;
    protected static final int SAR_RF_STATE_PROXIMITY_RELEASE_HEADSET = 6;
    protected static final int SAR_RF_STATE_PROXIMITY_RELEASE_USB = 4;
    protected static final int SAR_WIFI_STATE_CELLULAR_ONLY = 0;
    protected static final int SAR_WIFI_STATE_CELLULAR_WIFI_2p4G = 1;
    protected static final int SAR_WIFI_STATE_CELLULAR_WIFI_2p4G_5G = 3;
    protected static final int SAR_WIFI_STATE_CELLULAR_WIFI_5G = 2;
    public static final boolean SWITCH_LOG = "true".equalsIgnoreCase(SystemProperties.get("persist.sys.assert.panic", "false"));
    public static final boolean SWITCH_STACK = OemFeature.FEATURE_COMM_STACK;
    public static final int SWITCH_VOLTE_CS = Integer.parseInt(SystemProperties.get("persist.sys.oem_volte", "-110"));
    public static final int TYPE_TIMEZONE_AUTOTIMEZONE = 13;
    public static final int TYPE_TIMEZONE_FIXTIMEZONE = 11;
    public static final int TYPE_TIMEZONE_NITZTIMEZONE = 12;
    public static final int TYPE_TIMEZONE_OEMTIMEZONE = 14;
    public static final int TYPE_TIMEZONE_POLLSTATEDONE = 10;
    private static boolean lockDevice = false;
    private static boolean mPowerCenterEnable = "true".equalsIgnoreCase(SystemProperties.get("sys.oppopcm.enable", "true"));
    private static int operatorType = -1;
    private static boolean restrictCallIn = false;
    private static boolean restrictCallOut = false;
    private static boolean restrictPs = false;
    private static boolean restrictSmsReceive = false;
    private static boolean restrictSmsSend = false;
    public static final Uri sRawUriStaticFinal = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw");

    public interface RILConstants {
        public static final int RIL_REQUEST_FACTORY_MODE_MODEM_GPIO = 2;
        public static final int RIL_REQUEST_FACTORY_MODE_NV_PROCESS = 1;
        public static final int RIL_REQUEST_GET_BAND_MODE = 3;
        public static final int RIL_REQUEST_GET_MDM_BASEBAND = 9;
        public static final int RIL_REQUEST_GET_RFFE_DEV_INFO = 5;
        public static final int RIL_REQUEST_GO_TO_ERROR_FATAL = 8;
        public static final int RIL_REQUEST_OEM_BASE = 0;
        public static final int RIL_REQUEST_OEM_COMMON_REQ = 25;
        public static final int RIL_REQUEST_OEM_SET_ECC_LIST = 31;
        public static final int RIL_REQUEST_OPPO_CONTROL_MODEM_FEATURE = 23;
        public static final int RIL_REQUEST_OPPO_EXP_IND_REGION_CHANGED_FOR_RIL_ECCLIST = 16;
        public static final int RIL_REQUEST_OPPO_GET_ASDIV_STATE = 24;
        public static final int RIL_REQUEST_OPPO_GET_LTE_BW = 26;
        public static final int RIL_REQUEST_OPPO_GET_NW_SEARCH_COUNT = 30;
        public static final int RIL_REQUEST_OPPO_GET_RADIO_INFO = 12;
        public static final int RIL_REQUEST_OPPO_GET_TX_RX_INFO = 15;
        public static final int RIL_REQUEST_OPPO_LOCK_GSM_ARFCN = 20;
        public static final int RIL_REQUEST_OPPO_LOCK_LTE_CELL = 22;
        public static final int RIL_REQUEST_OPPO_RFFE_CMD = 21;
        public static final int RIL_REQUEST_OPPO_SET_FAKEBS_WEIGHT = 17;
        public static final int RIL_REQUEST_OPPO_SET_FILTER_ARFCN = 13;
        public static final int RIL_REQUEST_OPPO_SET_PPLMN_LIST = 14;
        public static final int RIL_REQUEST_OPPO_SET_VOLTE_FR1 = 19;
        public static final int RIL_REQUEST_OPPO_SET_VOLTE_FR2 = 18;
        public static final int RIL_REQUEST_OPPO_SIMLOCK_REQ = 31;
        public static final int RIL_REQUEST_REPORT_BOOTUPNVRESTOR_STATE = 4;
        public static final int RIL_REQUEST_SET_TDD_LTE = 10;
        public static final int RIL_REQUEST_SIM_TRANSMIT_BASIC = 6;
        public static final int RIL_REQUEST_SIM_TRANSMIT_CHANNEL = 7;
    }

    public static boolean getWlanAssistantEnable(Context context) {
        boolean romUpdateWlanAssistant = Settings.Global.getInt(context.getContentResolver(), "rom.update.wifi.assistant", 1) == 1;
        boolean wlanAssistantFeature = context.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant");
        if (SWITCH_LOG) {
            Rlog.w("oem", "wlanAssistantFeature = " + wlanAssistantFeature + ", romUpdateWlanAssistant= " + romUpdateWlanAssistant);
        }
        if (!wlanAssistantFeature || !romUpdateWlanAssistant) {
            return false;
        }
        return true;
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

    public static boolean isHostswapSIMReboot(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.commcenter.reboot.dialog");
    }

    public static boolean isTestCard(Context context, byte b) {
        return b == 128 || b == 129 || ((b == 2 || b == 4) && OemTelephonyUtils.isNwLabTest());
    }

    public static boolean isTestCard(String mImsi) {
        return (mImsi != null && (mImsi.startsWith("00101") || mImsi.startsWith("001001") || mImsi.equals("111223333333333"))) || FEATURE_TEST_SIM;
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
            return SystemProperties.get("persist.sys.dump", "1").equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isUiccSlotForbid(int slotid) {
        return "1".equals(TelephonyManager.getTelephonyProperty(slotid, "persist.sys.oem_forbid_slots", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK));
    }

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
        return !getCTEnable(key, "-1").equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK);
    }

    public static boolean getNONCTEnable(String key) {
        return !getNONCTEnable(key, "-1").equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK);
    }

    public static boolean getFeatureEnable(String key) {
        return !getFeatureEnable(key, "-1").equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK);
    }

    public static boolean isPrimaryCard(Phone phone) {
        if (phone == null || phone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            return true;
        }
        return false;
    }

    public static boolean canSwitchByUser(Phone phone) {
        if (getFeatureEnable("db", "-1").equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK) || getFeatureEnable("db", "-1").equals("1") || getFeatureEnable("p_db", "-1").equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK)) {
            return false;
        }
        if (!isCtCard(phone) || !getCTEnable("db", "-1").equals("2")) {
            return true;
        }
        return false;
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
        if (isCtCard(phone)) {
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
        if (restrictSmsSend || isSellMode(phone)) {
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
        if (!getFeatureEnable("ss")) {
            return false;
        }
        boolean isct = isCtCard(phone);
        if ((!isct || getCTEnable("ss")) && (isct || getNONCTEnable("sb"))) {
            return true;
        }
        return false;
    }

    public static boolean isSmsReceiveEnable(Phone phone) {
        if (restrictSmsReceive || isSellMode(phone)) {
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
        boolean isct = isCtCard(phone);
        if ((!isct || getCTEnable("sr")) && (isct || getNONCTEnable("sb"))) {
            return true;
        }
        return false;
    }

    public static boolean isCallInEnable(Phone phone) {
        if (restrictCallIn || isSellMode(phone)) {
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
        boolean isct = isCtCard(phone);
        if (!getFeatureEnable("vi") || ((isct && !getCTEnable("vi")) || (!isct && !getNONCTEnable("vb")))) {
            return false;
        }
        return true;
    }

    public static boolean isCallOutEnable(Phone phone) {
        if (isSellMode(phone)) {
            Rlog.d("oem", "isSeleMode");
            Toast.makeText(phone.getContext(), OemTelephonyUtils.getOemRes(phone.getContext(), "oppo_salemode_call_barring", "Call is forbidden in demo phone"), 1).show();
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
            boolean isct = isCtCard(phone);
            return getFeatureEnable("vo") && (!isct || getCTEnable("vo")) && (isct || getNONCTEnable("vb"));
        }
    }

    public static boolean isCtCard(Phone phone) {
        String rilMccMncProp;
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
            String[] imslist = ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone)).getLteCdmaImsi(phoneId);
            if (imslist != null) {
                String ims = imslist[0];
                if (ims.length() >= 5) {
                    ims = ims.substring(0, 5);
                }
                if ("46003".equals(ims) || "46011".equals(ims) || "45502".equals(ims)) {
                    return true;
                }
            }
            if (phone.getPhoneType() == 2) {
                rilMccMncProp = "vendor.cdma.ril.uicc.mccmnc";
            } else {
                rilMccMncProp = "vendor.gsm.ril.uicc.mccmnc";
            }
            if (phone.getPhoneId() != 0) {
                rilMccMncProp = rilMccMncProp + "." + phone.getPhoneId();
            }
            String rilMccMnc = SystemProperties.get(rilMccMncProp, PhoneConfigurationManager.SSSS);
            if ("46003".equals(rilMccMnc) || "46011".equals(rilMccMnc)) {
                return true;
            }
            return ((AbstractSubscriptionController) OemTelephonyUtils.typeCasting(AbstractSubscriptionController.class, SubscriptionController.getInstance())).isCTCCard(phoneId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setOemCallState(PhoneConstants.State oldState, PhoneConstants.State newState) {
        setOemCallState(OEM_CHECK_CALL_STATE, oldState, newState);
    }

    public static void setOemCallState(String[] names, PhoneConstants.State oldState, PhoneConstants.State newState) {
        String value;
        if (oldState != newState) {
            if (oldState == PhoneConstants.State.IDLE) {
                value = "true";
            } else if (newState == PhoneConstants.State.IDLE) {
                value = "false";
            } else {
                return;
            }
            boolean isChanged = false;
            for (String s : names) {
                if (!value.equalsIgnoreCase(SystemProperties.get(s, "null"))) {
                    isChanged = true;
                    if (SWITCH_LOG) {
                        Rlog.d("oem", "setOemCallState," + s + ":" + value);
                    }
                    SystemProperties.set(s, value);
                }
            }
            if (isChanged) {
                checkCallState(names, value);
            }
        }
    }

    public static void checkCallState(String value) {
        checkCallState(OEM_CHECK_CALL_STATE, value);
    }

    public static void checkCallState(String[] names, String value) {
        for (String s : names) {
            if (!value.equalsIgnoreCase(SystemProperties.get(s, "null"))) {
                if (SWITCH_LOG) {
                    Rlog.d("oem", "checkCallState," + s + ":" + value);
                }
                SystemProperties.set(s, value);
            }
        }
    }

    public static boolean isVsimIgnoreUserDataSetting(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.softsim.ignore_data_setting");
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

    public static boolean usePSensorForSarDetect(Context context) {
        return !context.getPackageManager().hasSystemFeature("oppo.network.sar.earpiece.detect.support");
    }

    public static boolean isSupportWifiSingleSar() {
        if (Build.HARDWARE.equals("mt6873")) {
            return true;
        }
        return false;
    }

    public static boolean needSetSarForWifi() {
        if (SystemProperties.get("ro.separate.soft", "oppo").equals("19420")) {
            return true;
        }
        return false;
    }

    public static String getCountryForWifi() {
        return SystemProperties.get("ro.oppo.regionmark", "EX");
    }

    public static String getProjectForWifi() {
        return SystemProperties.get("ro.separate.soft", "oppo");
    }

    public static boolean runExecCmd(int sar, String country, String project) {
        if (SWITCH_LOG) {
            Log.d("WifiOemProximitySensor", "runExecCmd:" + sar + "," + country + "," + project);
        }
        Process process = null;
        try {
            process = new ProcessBuilder("/system/bin/iwpriv", "wlan0", "driver", "set_pwr_ctrl OppoSar " + sar).start();
            process.waitFor();
            try {
                process.destroy();
            } catch (Exception e) {
            }
            return true;
        } catch (Exception ex) {
            Log.e("oem", "execCmd error :" + ex.getMessage());
            try {
                process.destroy();
            } catch (Exception e2) {
            }
            return false;
        } catch (Throwable th) {
            try {
                process.destroy();
            } catch (Exception e3) {
            }
            throw th;
        }
    }

    public static boolean setWifiSar(int sar) {
        return execCmd("/system/bin/iwpriv wlan0 driver O-SAR-ENABLE-" + sar);
    }

    public static boolean execCmd(String cmd) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            try {
                process.destroy();
                return true;
            } catch (Exception e) {
                return true;
            }
        } catch (Exception ex) {
            Rlog.e("oem", "execCmd error :" + ex.getMessage());
            try {
                process.destroy();
            } catch (Exception e2) {
            }
            return false;
        } catch (Throwable th) {
            try {
                process.destroy();
            } catch (Exception e3) {
            }
            throw th;
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
            } else if (mccmnc.equals("46001") || mccmnc.equals("46009") || mccmnc.equals("45407") || mccmnc.equals("46006")) {
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
            } else if (operator.equals("898601") || operator.equals("898609")) {
                result = 3;
            } else {
                result = 4;
            }
        }
        Rlog.d("oem", "getCardType by iccid result = " + result);
        return result;
    }

    public static boolean isCMCC(String plmn) {
        if (plmn == null) {
            return false;
        }
        if ("46000".equals(plmn) || "46002".equals(plmn) || "46004".equals(plmn) || "46007".equals(plmn) || "46008".equals(plmn)) {
            return true;
        }
        return false;
    }

    public static boolean isDatEnable() {
        return "DB091".equals(PROJECT_NAME) || "DB093".equals(PROJECT_NAME) || "DB096".equals(PROJECT_NAME);
    }

    public static boolean isVersionMOVISTAR() {
        try {
            String operator = SystemProperties.get("ro.oppo.operator", PhoneConfigurationManager.SSSS);
            String region = SystemProperties.get("ro.oppo.regionmark", PhoneConfigurationManager.SSSS);
            String country = SystemProperties.get("ro.oppo.euex.country", PhoneConfigurationManager.SSSS);
            if (TextUtils.isEmpty(operator) || TextUtils.isEmpty(region) || TextUtils.isEmpty(country) || !"EUEX".equals(region) || !"ES".equals(country)) {
                return false;
            }
            if ("MOVISTAR".equals(operator) || "MOVISTAR_LITE".equals(operator)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean isDomesticRoamingSpecialSim(String imsi) {
        return isPlaySim(imsi);
    }

    public static boolean isPlaySim(String imsi) {
        if (!TextUtils.isEmpty(imsi) && imsi.length() > 5) {
            String mccmnc = imsi.substring(0, 5);
            if (mccmnc.equals("26006") || mccmnc.equals("26007") || mccmnc.equals("26098")) {
                return true;
            }
        }
        return false;
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

    public static int getOperatorType() {
        return operatorType;
    }

    public static void setOperatorType(int value) {
        operatorType = value;
    }

    public static boolean isOppoSimLockVersion() {
        return operatorType > 0;
    }

    public static boolean isOppoSimLockCallOutEnable() {
        return !restrictCallOut;
    }
}
