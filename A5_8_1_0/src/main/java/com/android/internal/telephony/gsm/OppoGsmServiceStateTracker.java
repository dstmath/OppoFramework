package com.android.internal.telephony.gsm;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.os.SystemProperties;
import android.telephony.OppoTelephonyFunction;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;

public class OppoGsmServiceStateTracker {
    protected static final boolean DBG = OemConstant.SWITCH_LOG;
    private static final String LOG_TAG = "GSM";
    private static String[] needIwlanNameSimArray = new String[]{"50501", "50511", "50571", "50572"};
    private static String[] sMvnoPlmnArray = new String[]{"46605", "46697", "50503", "50502", "50501", "42403", "50506", "52501", "502153", "50211"};

    public static int oppoGetDisplayRule(IccRecords iccRecords, ServiceState ss) {
        int rule = iccRecords != null ? iccRecords.getDisplayRule(ss.getOperatorNumeric()) : 0;
        if (!ss.getRoaming() || (rule & 2) == 2) {
            return rule;
        }
        return 2;
    }

    public static void oppoNvCheckAndRestore(Context context) {
        String nvstate = SystemProperties.get("ril.nvrestored", "success");
        log("oppoNvCheckAndRestore:" + nvstate);
        if (nvstate.equals("fail")) {
            log("nv restore state reached.");
            ((NotificationManager) context.getSystemService("notification")).notify(8888, new Builder(context).setTicker("OppoNvRecovery:The process of NV recovery is unsuccessful!").setSmallIcon(17301624).setContentTitle("OppoNvRecovery").setContentText("The process of NV recovery is unsuccessful!").setWhen(System.currentTimeMillis()).build());
        }
    }

    public static String oppoGetPlmnOverride(Context context, String operatorNumic, ServiceState ss) {
        return OppoTelephonyFunction.oppoGetPlmnOverride(context, operatorNumic, ss);
    }

    public static void oppoSetDataNetmgrdMTU(ServiceState ss) {
        if (ss.getOperatorNumeric() != null) {
            if (ss.getOperatorNumeric().equals("46000") || ss.getOperatorNumeric().equals("46002") || ss.getOperatorNumeric().equals("46007")) {
                SystemProperties.set("persist.data_netmgrd_mtu", "1400");
            }
        } else if (SystemProperties.get("persist.data_netmgrd_mtu").equals("1400")) {
            SystemProperties.set("persist.data_netmgrd_mtu", "1500");
        }
    }

    public static String oppoGetExPlmn(ServiceState ss, String plmn, String simNumeric, String pnn) {
        if (ss == null) {
            return plmn;
        }
        if (DBG) {
            log("oppoGetExPlmn plmn = " + plmn + ", pnn " + pnn);
        }
        if (!(!SystemProperties.get("ro.oppo.version", "CN").equals("US") || simNumeric == null || ss.getOperatorNumeric() == null)) {
            if (DBG) {
                log("oppoGetExPlmn ss.getOperatorNumeric() = " + ss.getOperatorNumeric() + "  simNumeric = " + simNumeric);
            }
            if (ss.getOperatorNumeric().startsWith("405") && (TextUtils.isEmpty(plmn) ^ 1) != 0 && (plmn.equals("JIO") || plmn.equals("Jio 4G"))) {
                plmn = "JIO 4G";
            } else if (simNumeric.equals("23211") && ss.getOperatorNumeric().equals("23201")) {
                plmn = "bob";
            } else if (simNumeric.equals("23212") && ss.getOperatorNumeric().equals("23201")) {
                plmn = "yesss!";
            } else if (simNumeric.equals("50218") && ss.getOperatorNumeric().equals("50212")) {
                plmn = "U Mobile";
            } else if (ss.getOperatorNumeric().equals("50212")) {
                plmn = "MY MAXIS";
            } else if (ss.getOperatorNumeric().startsWith("40520") && simNumeric.equals("405799")) {
                plmn = "Idea";
            } else if (ss.getOperatorNumeric().startsWith("502153") && (simNumeric.equals("502153") || "webe".equals(plmn))) {
                plmn = "unifi";
            } else if (ss.getOperatorNumeric().startsWith("51503") && "51505".equals(simNumeric)) {
                plmn = "Sun";
            } else if (ss.getOperatorNumeric().startsWith("60203") && simNumeric.equals("60204")) {
                plmn = "te";
            } else if (ss.getOperatorNumeric().startsWith("52047") && simNumeric.equals("52005")) {
                plmn = "dtac-T";
            } else if (ss.getOperatorNumeric().equals("40553")) {
                plmn = "Airtel";
            } else if (ss.getOperatorNumeric().equals("52501") && "singtel".equals(plmn)) {
                plmn = "Singtel";
            }
        }
        String vRet = plmn;
        return plmn;
    }

    public static boolean[] oppoShowSpnOrPlmn(ServiceState ss, String plmn, String spn, String simNumeric) {
        boolean[] oppoShow = new boolean[2];
        if (!(ss == null || ss.getOperatorNumeric() == null)) {
            if (ss.getRoaming() && ss.getOperatorNumeric().startsWith("460")) {
                oppoShow[0] = true;
                oppoShow[1] = false;
            }
            if (!TextUtils.isEmpty(simNumeric) && ss.getOperatorNumeric().startsWith("520")) {
                if (simNumeric.startsWith("520")) {
                    if (TextUtils.isEmpty(spn)) {
                        oppoShow[1] = false;
                        oppoShow[0] = true;
                    } else {
                        oppoShow[1] = true;
                        oppoShow[0] = false;
                    }
                } else if (TextUtils.isEmpty(plmn)) {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                } else {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                }
            }
            if (ss.getOperatorNumeric().startsWith("46689")) {
                if (TextUtils.isEmpty(plmn)) {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                } else {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                }
            }
            if (!TextUtils.isEmpty(simNumeric) && ss.getOperatorNumeric().startsWith("46697") && simNumeric.equals("46605")) {
                if (TextUtils.isEmpty(spn)) {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                } else {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                }
            }
            if (ss.getOperatorNumeric().startsWith("51010")) {
                if (TextUtils.isEmpty(plmn)) {
                    oppoShow[1] = true;
                    oppoShow[0] = false;
                } else {
                    oppoShow[1] = false;
                    oppoShow[0] = true;
                }
            }
            if (ss.getOperatorNumeric().equals("50219") && (TextUtils.isEmpty(simNumeric) ^ 1) != 0 && simNumeric.equals("502153") && !TextUtils.isEmpty(spn)) {
                oppoShow[1] = true;
                oppoShow[0] = false;
            }
            if (!TextUtils.isEmpty(simNumeric) && simNumeric.equals("53024") && ss.getOperatorNumeric().startsWith("530") && !TextUtils.isEmpty(spn)) {
                oppoShow[1] = true;
                oppoShow[0] = false;
            }
            if (!TextUtils.isEmpty(simNumeric) && ((simNumeric.equals("42005") || simNumeric.equals("42006")) && ((ss.getOperatorNumeric().startsWith("42001") || ss.getOperatorNumeric().startsWith("42003")) && !TextUtils.isEmpty(spn)))) {
                oppoShow[1] = true;
                oppoShow[0] = false;
            }
        }
        return oppoShow;
    }

    public static boolean isVodafoneNationalRoaming(String operatorMccMnc, String simCardMccMnc) {
        if (TextUtils.isEmpty(operatorMccMnc) || !operatorMccMnc.startsWith("505") || TextUtils.isEmpty(simCardMccMnc) || (!simCardMccMnc.equals("50503") && !simCardMccMnc.equals("50506"))) {
            return false;
        }
        return true;
    }

    public static boolean isNZOperatorCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals("53024")) {
            return false;
        }
        return true;
    }

    public static boolean isAUOperatorCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals("50502")) {
            return false;
        }
        return true;
    }

    public static void log(String s) {
        Rlog.d(LOG_TAG, "[GsmSST] " + s);
    }

    public static boolean isMvnoPlmn(String plmn) {
        for (String equals : sMvnoPlmnArray) {
            if (equals.equals(plmn)) {
                log("is MvnoPlmn");
                return true;
            }
        }
        log("is not MvnoPlmn");
        return false;
    }

    public static boolean isVodafoneHomePlmn(String operatorMccMnc, String simCardMccMnc) {
        if (TextUtils.isEmpty(operatorMccMnc) || (TextUtils.isEmpty(simCardMccMnc) ^ 1) == 0 || (!simCardMccMnc.equals("50503") && !simCardMccMnc.equals("50506"))) {
            return false;
        }
        if (operatorMccMnc.equals("50503") || operatorMccMnc.equals("50506")) {
            return true;
        }
        return false;
    }

    public static boolean isHomePlmn(String operatorMccMnc, String simCardMccMnc) {
        if (TextUtils.isEmpty(operatorMccMnc) || (TextUtils.isEmpty(simCardMccMnc) ^ 1) == 0 || !simCardMccMnc.equals(operatorMccMnc)) {
            return false;
        }
        return true;
    }

    public static boolean isOperatorCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc) || (!simCardMccMnc.equals("50503") && !simCardMccMnc.equals("50506"))) {
            return false;
        }
        return true;
    }

    public static boolean isGT4GSimCardCheck(String simCardMccMnc) {
        if (TextUtils.isEmpty(simCardMccMnc) || !simCardMccMnc.equals("46605")) {
            return false;
        }
        return true;
    }

    public static boolean needIwlanNameSimCheck(String simCardMccMnc) {
        if (!TextUtils.isEmpty(simCardMccMnc)) {
            for (String equals : needIwlanNameSimArray) {
                if (equals.equals(simCardMccMnc)) {
                    log("need name for iwlan true, sim " + simCardMccMnc);
                    return true;
                }
            }
        }
        log("need Iwlan name false");
        return false;
    }

    public static String getNameForIwlanOnly(IccRecords iccRecords, ServiceState ss, String simNumeric) {
        String iwlanName = null;
        if (needIwlanNameSimCheck(simNumeric)) {
            SIMRecords tSimRecords = (SIMRecords) iccRecords;
            String tSpn = tSimRecords.getServiceProviderName();
            if (ss.getVoiceRegState() != 0 && 18 == ss.getRilDataRadioTechnology()) {
                if (!TextUtils.isEmpty(tSpn)) {
                    iwlanName = tSpn;
                    if (DBG) {
                        log("updateSpnDisplay:iwlan name get spn=" + tSpn);
                    }
                } else if (tSimRecords.getEFpnnNetworkNames(0) != null) {
                    iwlanName = tSimRecords.getEFpnnNetworkNames(0).sFullName;
                    if (DBG) {
                        log("updateSpnDisplay:telstra_display plmn=" + iwlanName);
                    }
                }
            }
        }
        return iwlanName;
    }
}
