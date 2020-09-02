package com.oppo.internal.telephony.utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OppoPhoneUtil {
    private static final String CT_IIN = "898603,898611,898612,8985231,8985302,8985307";
    private static final String[] ETISALAT_MCCMNC_LIST = {"42401", "42402"};
    private static final String[] IDEA_MCCMNC_LIST = {"40422", "40444", "40424", "40407", "40478", "40404", "40412", "40456", "40419", "40489", "40487", "40482", "405799", "40570", "405845", "405846", "405848", "405849", "405850", "405852", "405853", "40414"};
    private static final String[] JIO_MCCMNC_LIST = {"405840", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874"};
    private static final String OEM_VOOC_STATE = "sys.oppo.disable_vooc";
    public static final String OP_DEFAULT = "OP_DEFAULT";
    private static final String[] ORANGE_MCCMNC_LIST = {"20801", "20802"};
    private static final String TAG = "OppoPhoneUtil";
    private static final String[] TPG_MCCMNC_LIST = {"52510"};
    private static final String[] USSD_ENABLED_IN_VOLTE_CALL = new String[0];
    private static boolean mPowerBackOffEnable = true;
    private static boolean mPowerCenterEnable = "true".equalsIgnoreCase(SystemProperties.get("persist.sys.oppopcm.enable", "true"));
    private static final Map<String, String> opName = new HashMap<String, String>() {
        /* class com.oppo.internal.telephony.utils.OppoPhoneUtil.AnonymousClass1 */

        {
            put("1", "CMCC");
            put(RegionLockConstant.TEST_OP_CUANDCMCC, "CU");
            put("9", "CT");
            put("110", "FET");
            put("124", "APTG");
            put("109", "CHT");
            put("108", "TWM");
            put("176", "TST");
            put("125", "DTAC");
            put("131", "TRUE");
            put("122", "AIS");
            put("18", "JIO");
            put("147", "AIRTEL");
            put("103", "SINGTEL");
            put("104", "STARHUB");
            put("151", "M1");
            put("19", "TELSTRA");
            put("152", "OPTUS");
            put("153", "VDF");
            put("127", "MGFN");
            put("185", "TwoDGREE");
            put("129", "KDDI");
            put("50", "SOFTBANK");
            put("17", "DOCOMO");
        }
    };

    public static boolean isCtCard(String iccId) {
        String[] split = CT_IIN.split(",");
        int length = split.length;
        int i = 0;
        while (i < length) {
            String iin = split[i];
            if (iccId == null || !iccId.startsWith(iin)) {
                i++;
            } else {
                logd("Iccid " + iccId + "is CT card");
                return true;
            }
        }
        return false;
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
            return isCtCard(phone.getFullIccSerialNumber());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static void logd(String s) {
        Rlog.d(TAG, s);
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

    public static ImsPhone getPricseImsPhone(Phone phone) {
        Phone mImsPhone = phone.getImsPhone();
        if (mImsPhone == null || !(mImsPhone instanceof ImsPhone)) {
            return null;
        }
        return (ImsPhone) mImsPhone;
    }

    public static boolean isConferenceHostConnection(boolean isConf, String connAddr, Phone phone) {
        logd("isConf = " + isConf);
        if (!isConf) {
            return false;
        }
        try {
            ImsPhone imsPhone = getPricseImsPhone(phone);
            if (imsPhone != null) {
                Uri[] hostHandles = imsPhone.getCurrentSubscriberUris();
                if (!(hostHandles == null || hostHandles.length == 0)) {
                    if (connAddr != null) {
                        for (Uri hostHandle : hostHandles) {
                            if (hostHandle != null) {
                                String hostNumber = hostHandle.getSchemeSpecificPart();
                                boolean isHost = PhoneNumberUtils.compare(hostNumber, connAddr);
                                logd("isParticipantHost : " + isHost + ", host: " + hostNumber + "number:" + connAddr);
                                if (isHost) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                }
                logd("isParticipantHost(N) : host or participant uri null");
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isParticipantHost(Uri[] hostHandles, Uri handle) {
        if (hostHandles == null || hostHandles.length == 0 || handle == null) {
            logd("isParticipantHost(N) : host or participant uri null");
            return false;
        }
        String[] numberParts = handle.getSchemeSpecificPart().split("[@;:]");
        if (numberParts.length == 0) {
            logd("isParticipantHost(N) : no number in participant handle");
            return false;
        }
        String number = numberParts[0];
        for (Uri hostHandle : hostHandles) {
            if (hostHandle != null) {
                String hostNumber = hostHandle.getSchemeSpecificPart();
                if (PhoneNumberUtils.compare(hostNumber, number)) {
                    return true;
                }
                if (!TextUtils.isEmpty(hostNumber)) {
                    String[] hostNumberParts = hostNumber.split("@");
                    if (hostNumberParts.length == 0) {
                        logd("isParticipantHost(N) : no hostNumberParts in hostNumber handle");
                        return false;
                    } else if (PhoneNumberUtils.compare(hostNumberParts[0], number)) {
                        return true;
                    }
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    public static boolean isJioCard(Phone phone) {
        if (phone == null) {
            Rlog.d("oem", "isJioCard, phone == null");
            return false;
        }
        int phoneId = phone.getPhoneId();
        List<String> jioMccmncList = new ArrayList<>(Arrays.asList(JIO_MCCMNC_LIST));
        String mccmnc = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
        Rlog.d("oem", "isJioCard, mccmnc: " + mccmnc);
        if (TextUtils.isEmpty(mccmnc) || !jioMccmncList.contains(mccmnc)) {
            return false;
        }
        return true;
    }

    public static boolean isOrangeCard(Phone phone) {
        if (phone == null) {
            Rlog.d("oem", "isOrangeCard, phone == null");
            return false;
        }
        int phoneId = phone.getPhoneId();
        List<String> orangeMccmncList = new ArrayList<>(Arrays.asList(ORANGE_MCCMNC_LIST));
        String mccmnc = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
        Rlog.d("oem", "isOrangeCard, mccmnc: " + mccmnc);
        if (TextUtils.isEmpty(mccmnc) || !orangeMccmncList.contains(mccmnc)) {
            return false;
        }
        return true;
    }

    public static boolean isEtisalatCard(Phone phone) {
        if (phone == null) {
            Rlog.d("oem", "isEtisalatCard, phone == null");
            return false;
        }
        int phoneId = phone.getPhoneId();
        List<String> etisalatMccmncList = new ArrayList<>(Arrays.asList(ETISALAT_MCCMNC_LIST));
        String mccmnc = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
        Rlog.d("oem", "isEtisalatCard, mccmnc: " + mccmnc);
        if (TextUtils.isEmpty(mccmnc) || !etisalatMccmncList.contains(mccmnc)) {
            return false;
        }
        return true;
    }

    public static boolean isTpgCard(Phone phone) {
        if (phone == null) {
            Rlog.d("oem", "isTpgCard, phone == null");
            return false;
        }
        int phoneId = phone.getPhoneId();
        List<String> tpgMccmncList = new ArrayList<>(Arrays.asList(TPG_MCCMNC_LIST));
        String mccmnc = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
        Rlog.d("oem", "isTpgCard, mccmnc: " + mccmnc);
        if (TextUtils.isEmpty(mccmnc) || !tpgMccmncList.contains(mccmnc)) {
            return false;
        }
        return true;
    }

    public static boolean isIdeaCard(Phone phone) {
        if (phone == null) {
            Rlog.d("oem", "idea, phone == null");
            return false;
        }
        int phoneId = phone.getPhoneId();
        List<String> ideaMccmncList = new ArrayList<>(Arrays.asList(IDEA_MCCMNC_LIST));
        String mccmnc = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
        Rlog.d("oem", "isIdeaCard, mccmnc: " + mccmnc);
        if (TextUtils.isEmpty(mccmnc) || !ideaMccmncList.contains(mccmnc)) {
            return false;
        }
        return true;
    }

    public static boolean getWlanAssistantEnable(Context context) {
        boolean romUpdateWlanAssistant = Settings.Global.getInt(context.getContentResolver(), "rom.update.wifi.assistant", 1) == 1;
        boolean wlanAssistantFeature = context.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant");
        if (OemConstant.SWITCH_LOG) {
            Rlog.w("oem", "wlanAssistantFeature = " + wlanAssistantFeature + ", romUpdateWlanAssistant= " + romUpdateWlanAssistant);
        }
        if (!wlanAssistantFeature || !romUpdateWlanAssistant) {
            return false;
        }
        return true;
    }

    public static void setOemVoocState(PhoneConstants.State oldState, PhoneConstants.State newState) {
        String value;
        if (oldState != newState) {
            if (oldState == PhoneConstants.State.IDLE) {
                value = "true";
            } else if (newState == PhoneConstants.State.IDLE) {
                value = "false";
            } else {
                return;
            }
            if (!value.equalsIgnoreCase(SystemProperties.get(OEM_VOOC_STATE, "null"))) {
                Rlog.d("oem", "setOemVoocState :" + value);
                SystemProperties.set(OEM_VOOC_STATE, value);
                checkVoocState(value);
            }
        }
    }

    public static void checkVoocState(String value) {
        if (!value.equalsIgnoreCase(SystemProperties.get(OEM_VOOC_STATE, "false"))) {
            Rlog.d("oem", "checkVoocState :" + value);
            SystemProperties.set(OEM_VOOC_STATE, value);
        }
    }

    public static boolean isVsimIgnoreUserDataSetting(Context context) {
        return context.getPackageManager().hasSystemFeature("oppo.softsim.ignore_data_setting");
    }

    public static boolean getPowerCenterEnable(Context context) {
        return mPowerCenterEnable;
    }

    public static boolean getPowerCenterEnableFromProp(Context context) {
        mPowerCenterEnable = "true".equalsIgnoreCase(SystemProperties.get("persist.sys.oppopcm.enable", "true"));
        if (OemConstant.SWITCH_LOG) {
            Rlog.w("oem", "getPowerCenterEnableFromProp = " + mPowerCenterEnable);
        }
        return mPowerCenterEnable;
    }

    public static boolean getPowerBackOffEnable() {
        return mPowerBackOffEnable;
    }

    public static void setPowerBackOffEnable(boolean enable) {
        mPowerBackOffEnable = enable;
    }

    public static String oppoGeOperatorByPlmn(Context context, String operatorNumic) {
        if (operatorNumic != null) {
            try {
                Resources resources = context.getResources();
                return context.getString(resources.getIdentifier("mccmnc" + operatorNumic, "string", "com.android.phone"));
            } catch (Exception e) {
                logd("oppoGeOperatorByPlmn error for operatorNumic=" + operatorNumic);
            }
        }
        return operatorNumic;
    }

    public static String getNameByPhoneId(int phoneId) {
        String opId = "0";
        if (phoneId == 0) {
            opId = SystemProperties.get("persist.radio.sim.opid");
        } else if (phoneId == 1) {
            opId = SystemProperties.get("persist.radio.sim.opid_1");
        }
        return opName.getOrDefault(opId, OP_DEFAULT);
    }

    public static boolean isUssdEnabledInVolteCall(String mccMnc) {
        if (mccMnc != null) {
            for (String tempMccMnc : USSD_ENABLED_IN_VOLTE_CALL) {
                if (tempMccMnc.equals(mccMnc)) {
                    Rlog.d("oem", "UssdEnabledInVolte");
                    return true;
                }
            }
        }
        return false;
    }
}
