package com.mediatek.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.MtkRadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.ims.ImsManager;
import com.android.internal.telephony.IPhoneSubInfo;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.ratconfiguration.RatConfiguration;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class RadioCapabilitySwitchUtil {
    public static final String CN_MCC = "460";
    public static final int ENHANCEMENT_T_PLUS_C = 2;
    public static final int ENHANCEMENT_T_PLUS_T = 0;
    public static final int ENHANCEMENT_T_PLUS_W = 1;
    public static final int ENHANCEMENT_W_PLUS_C = 3;
    public static final int ENHANCEMENT_W_PLUS_NA = 5;
    public static final int ENHANCEMENT_W_PLUS_W = 4;
    public static final int ICCID_ERROR = 3;
    public static final String IMSI_NOT_READY = "0";
    public static final int IMSI_NOT_READY_OR_SIM_LOCKED = 2;
    public static final String IMSI_READY = "1";
    private static final String LOG_TAG = "RadioCapabilitySwitchUtil";
    public static final int NOT_SHOW_DIALOG = 1;
    private static final String NO_SIM_VALUE = "N/A";
    public static final int OP01_6M_PRIORITY_OP01_SIM = 1;
    public static final int OP01_6M_PRIORITY_OP01_USIM = 0;
    public static final int OP01_6M_PRIORITY_OTHER = 2;
    private static final String[] PLMN_TABLE_OP01 = {"46000", "46002", "46007", "46008", "45412", "45413", "00101", "00211", "00321", "00431", "00541", "00651", "00761", "00871", "00902", "01012", "01122", "01232", "46004", "46602", "50270"};
    private static final String[] PLMN_TABLE_OP02 = {"46001", "46006", "46009", "45407"};
    private static final String[] PLMN_TABLE_OP09 = {"46005", "45502", "46003", "46011"};
    private static final String[] PLMN_TABLE_OP09_3G = {"20404"};
    private static final String[] PLMN_TABLE_OP18 = {"405840", "405854", "405855", "405856", "405857", "405858", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874"};
    private static final String PROPERTY_CAPABILITY_SWITCH = "persist.vendor.radio.simswitch";
    private static final String PROPERTY_ICCID = "vendor.ril.iccid.sim";
    private static final String[] PROPERTY_RIL_CT3G = {"vendor.gsm.ril.ct3g", "vendor.gsm.ril.ct3g.2", "vendor.gsm.ril.ct3g.3", "vendor.gsm.ril.ct3g.4"};
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    private static final String[] PROPERTY_SIM_ICCID = {"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"};
    private static final String[] PROPERTY_SIM_IMSI_STATUS = {"vendor.ril.imsi.status.sim1", "vendor.ril.imsi.status.sim2", "vendor.ril.imsi.status.sim3", "vendor.ril.imsi.status.sim4"};
    public static final int SHOW_DIALOG = 0;
    public static final int SIM_OP_INFO_OP01 = 2;
    public static final int SIM_OP_INFO_OP02 = 3;
    public static final int SIM_OP_INFO_OP09 = 4;
    public static final int SIM_OP_INFO_OP18 = 4;
    public static final int SIM_OP_INFO_OVERSEA = 1;
    public static final int SIM_OP_INFO_UNKNOWN = 0;
    public static final int SIM_SWITCHING = 4;
    public static final int SIM_SWITCH_MODE_DUAL_TALK = 3;
    public static final int SIM_SWITCH_MODE_DUAL_TALK_SWAP = 4;
    public static final int SIM_SWITCH_MODE_SINGLE_TALK_MDSYS = 1;
    public static final int SIM_SWITCH_MODE_SINGLE_TALK_MDSYS_LITE = 2;
    public static final int SIM_TYPE_OTHER = 2;
    public static final int SIM_TYPE_SIM = 0;
    public static final int SIM_TYPE_USIM = 1;
    public static final int SUBSIDY_LOCK_SUPPORT = 10;

    public static boolean getSimInfo(int[] simOpInfo, int[] simType, int insertedStatus) {
        String propStr;
        String propStr2;
        int i = insertedStatus;
        String[] strMnc = new String[simOpInfo.length];
        String[] strSimType = new String[simOpInfo.length];
        int i2 = 0;
        while (i2 < simOpInfo.length) {
            if (i2 == 0) {
                propStr = "vendor.gsm.ril.uicctype";
            } else {
                propStr = "vendor.gsm.ril.uicctype." + (i2 + 1);
            }
            strSimType[i2] = SystemProperties.get(propStr, "");
            int i3 = 0;
            if (strSimType[i2].equals("SIM")) {
                simType[i2] = 0;
            } else if (strSimType[i2].equals("USIM")) {
                simType[i2] = 1;
            } else {
                simType[i2] = 2;
            }
            try {
                IPhoneSubInfo subInfo = IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
                if (subInfo == null) {
                    logd("subInfo stub is null");
                    return false;
                }
                int[] subIdList = SubscriptionManager.getSubId(i2);
                if (subIdList == null) {
                    logd("subIdList is null");
                } else {
                    strMnc[i2] = subInfo.getSubscriberIdForSubscriber(subIdList[0], "com.mediatek.internal.telephony");
                    if (strMnc[i2] == null) {
                        logd("strMnc[" + i2 + "] is null, get mnc by ril.uim.subscriberid");
                        StringBuilder sb = new StringBuilder();
                        sb.append("vendor.ril.uim.subscriberid.");
                        sb.append(i2 + 1);
                        strMnc[i2] = SystemProperties.get(sb.toString(), "");
                    }
                    if (strMnc[i2].equals("")) {
                        logd("strMnc[" + i2 + "] is null, get mnc by vendor.gsm.ril.uicc.mccmnc");
                        if (i2 == 0) {
                            propStr2 = "vendor.gsm.ril.uicc.mccmnc";
                        } else {
                            propStr2 = "vendor.gsm.ril.uicc.mccmnc." + i2;
                        }
                        strMnc[i2] = SystemProperties.get(propStr2, "");
                    }
                }
                if (strMnc[i2] == null) {
                    logd("strMnc[" + i2 + "] is null");
                    strMnc[i2] = "";
                }
                if (strMnc[i2].length() >= 6) {
                    strMnc[i2] = strMnc[i2].substring(0, 6);
                } else if (strMnc[i2].length() >= 5) {
                    strMnc[i2] = strMnc[i2].substring(0, 5);
                }
                logd("SimType[" + i2 + "]= " + strSimType[i2] + "insertedStatus:" + i);
                if (i >= 0 && ((1 << i2) & i) > 0 && isSimOn(i2)) {
                    if (strMnc[i2].equals("") || strMnc[i2].equals("error")) {
                        logd("SIM is inserted but no imsi");
                        return false;
                    } else if (strMnc[i2].equals("sim_lock")) {
                        logd("SIM is lock, wait pin unlock");
                        return false;
                    } else if (strMnc[i2].equals("N/A") || strMnc[i2].equals("sim_absent")) {
                        logd("strMnc have invalid value, return false");
                        return false;
                    } else if (!strMnc[i2].matches("[0-9]+")) {
                        logd("strMnc have non-numeric value, return false");
                        return false;
                    }
                }
                String[] strArr = PLMN_TABLE_OP01;
                int length = strArr.length;
                while (true) {
                    if (i3 >= length) {
                        break;
                    }
                    if (strMnc[i2].startsWith(strArr[i3])) {
                        simOpInfo[i2] = 2;
                        break;
                    }
                    i3++;
                }
                if (simOpInfo[i2] == 0) {
                    String[] strArr2 = PLMN_TABLE_OP02;
                    int length2 = strArr2.length;
                    int i4 = 0;
                    while (true) {
                        if (i4 >= length2) {
                            break;
                        }
                        if (strMnc[i2].startsWith(strArr2[i4])) {
                            simOpInfo[i2] = 3;
                            break;
                        }
                        i4++;
                    }
                }
                if (simOpInfo[i2] == 0) {
                    String[] strArr3 = PLMN_TABLE_OP09;
                    int length3 = strArr3.length;
                    int i5 = 0;
                    while (true) {
                        if (i5 >= length3) {
                            break;
                        }
                        if (strMnc[i2].startsWith(strArr3[i5])) {
                            simOpInfo[i2] = 4;
                            break;
                        }
                        i5++;
                    }
                }
                if (simOpInfo[i2] == 0) {
                    String[] strArr4 = PLMN_TABLE_OP09_3G;
                    int length4 = strArr4.length;
                    int i6 = 0;
                    while (true) {
                        if (i6 >= length4) {
                            break;
                        }
                        if (strMnc[i2].startsWith(strArr4[i6]) && "1".equals(SystemProperties.get(PROPERTY_RIL_CT3G[i2]))) {
                            simOpInfo[i2] = 4;
                            break;
                        }
                        i6++;
                    }
                }
                if (SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, "").equals(DataSubConstants.OPERATOR_OP18) && simOpInfo[i2] == 0) {
                    String[] strArr5 = PLMN_TABLE_OP18;
                    int length5 = strArr5.length;
                    int i7 = 0;
                    while (true) {
                        if (i7 >= length5) {
                            break;
                        }
                        if (strMnc[i2].startsWith(strArr5[i7])) {
                            simOpInfo[i2] = 4;
                            break;
                        }
                        i7++;
                    }
                }
                if (simOpInfo[i2] == 0 && !strMnc[i2].equals("") && !strMnc[i2].equals("N/A")) {
                    simOpInfo[i2] = 1;
                }
                logd("strMnc[" + i2 + "]= " + strMnc[i2] + ", simOpInfo[" + i2 + "]=" + simOpInfo[i2]);
                i2++;
                i = insertedStatus;
            } catch (RemoteException e) {
                logd("get subInfo stub fail");
                strMnc[i2] = "error";
            }
        }
        return true;
    }

    public static boolean isVolteEnabled(int phoneId, Context context) {
        ImsManager imsManager = ImsManager.getInstance(context, phoneId);
        boolean imsUseEnabled = imsManager.isVolteEnabledByPlatform() && imsManager.isEnhanced4gLteModeSettingEnabledByUser();
        if (imsUseEnabled) {
            int[] subId = SubscriptionManager.getSubId(phoneId);
            if (subId != null) {
                ContentResolver contentResolver = context.getContentResolver();
                int nwMode = Settings.Global.getInt(contentResolver, "preferred_network_mode" + subId[0], MtkRILConstants.PREFERRED_NETWORK_MODE);
                int rafFromNwMode = MtkRadioAccessFamily.getRafFromNetworkType(nwMode);
                if ((rafFromNwMode & 266240) == 0) {
                    imsUseEnabled = false;
                }
                logd("isVolteEnabled, imsUseEnabled = " + imsUseEnabled + ", nwMode = " + nwMode + ", rafFromNwMode = " + rafFromNwMode + ", rafLteGroup = 266240");
            } else {
                logd("isVolteEnabled, subId[] is null");
            }
        }
        logd("isVolteEnabled = " + imsUseEnabled);
        return imsUseEnabled;
    }

    public static boolean isHVolteEnabled() {
        if (SystemProperties.get("persist.vendor.mtk_ct_volte_support").equals(MtkGsmCdmaPhone.ACT_TYPE_UTRAN) || SystemProperties.get("persist.vendor.mtk_ct_volte_support").equals("3")) {
            return true;
        }
        return false;
    }

    public static boolean isCdmaCard(int phoneId, int opInfo, Context context) {
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            logd("isCdmaCard invalid phoneId:" + phoneId);
            return false;
        }
        String cardType = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[phoneId]);
        boolean isCdmaSim = cardType.indexOf("CSIM") >= 0 || cardType.indexOf("RUIM") >= 0;
        if (!isCdmaSim && "SIM".equals(cardType) && "1".equals(SystemProperties.get(PROPERTY_RIL_CT3G[phoneId]))) {
            isCdmaSim = true;
        }
        if (opInfo == 4) {
            isCdmaSim = true;
        }
        if (!isCdmaSim || !isVolteEnabled(phoneId, context) || isHVolteEnabled()) {
            return isCdmaSim;
        }
        logd("isCdmaCard, volte is enabled, SRLTE is unused for CT card");
        return false;
    }

    public static boolean isSupportSimSwitchEnhancement(int simType) {
        if (simType == 0 || simType == 1) {
            return true;
        }
        if (simType == 2 || simType == 3) {
            return false;
        }
        if (simType == 4 || simType == 5) {
            return true;
        }
        return false;
    }

    public static boolean isSkipCapabilitySwitch(int majorPhoneId, int phoneNum, Context context) {
        boolean z;
        int[] simOpInfo = new int[phoneNum];
        int[] simType = new int[phoneNum];
        int insertedState = 0;
        int insertedSimCount = 0;
        int tSimCount = 0;
        int wSimCount = 0;
        int cSimCount = 0;
        String[] currIccId = new String[phoneNum];
        int op09VolteOffPhoneId = -1;
        if (!isPS2SupportLTE()) {
            return false;
        }
        if (phoneNum <= 2) {
            for (int i = 0; i < phoneNum; i++) {
                currIccId[i] = SystemProperties.get(PROPERTY_ICCID + (i + 1));
                if (currIccId[i] == null || "".equals(currIccId[i])) {
                    logd("iccid not found, do capability switch");
                    return false;
                }
                if (!"N/A".equals(currIccId[i])) {
                    if (!isSimOn(i) || isRadioOffBySimManagement(i)) {
                        logd("isSkipCapabilitySwitch, slot" + i + " is power off.");
                    } else {
                        insertedSimCount++;
                        insertedState |= 1 << i;
                    }
                }
            }
            if (insertedSimCount == 0) {
                logd("no sim card, skip capability switch");
                return true;
            } else if (!getSimInfo(simOpInfo, simType, insertedState)) {
                logd("cannot get sim operator info, do capability switch");
                return false;
            } else {
                for (int i2 = 0; i2 < phoneNum; i2++) {
                    if (((1 << i2) & insertedState) > 0) {
                        if (2 == simOpInfo[i2]) {
                            tSimCount++;
                        } else if (isCdmaCard(i2, simOpInfo[i2], context)) {
                            cSimCount++;
                            op09VolteOffPhoneId = i2;
                        } else if (simOpInfo[i2] != 0) {
                            wSimCount++;
                        }
                        if (simOpInfo[i2] == 4) {
                        }
                    }
                }
                logd("isSkipCapabilitySwitch : Inserted SIM count: " + insertedSimCount + ", insertedStatus: " + insertedState + ", tSimCount: " + tSimCount + ", wSimCount: " + wSimCount + ", cSimCount: " + cSimCount);
                if (isSupportSimSwitchEnhancement(0)) {
                }
                if (isSupportSimSwitchEnhancement(1) && insertedSimCount == 2 && tSimCount == 1 && wSimCount == 1 && isTPlusWSupport()) {
                    int i3 = simOpInfo[majorPhoneId];
                }
                if (isSupportSimSwitchEnhancement(2) && insertedSimCount == 2 && tSimCount == 1 && cSimCount == 1 && !isCdmaCard(majorPhoneId, simOpInfo[majorPhoneId], context)) {
                    return true;
                }
                if (isSupportSimSwitchEnhancement(3) && insertedSimCount == 2 && wSimCount == 1 && cSimCount == 1 && !isCdmaCard(majorPhoneId, simOpInfo[majorPhoneId], context)) {
                    return true;
                }
                if (isSupportSimSwitchEnhancement(4) && insertedSimCount == 2 && wSimCount == 2) {
                    return true;
                }
                if (isSupportSimSwitchEnhancement(5)) {
                    z = true;
                    z = true;
                    if (insertedSimCount == 1 && wSimCount == 1) {
                        return true;
                    }
                } else {
                    z = true;
                }
                if (4 != simOpInfo[0]) {
                    return false;
                }
                char c = z ? 1 : 0;
                char c2 = z ? 1 : 0;
                char c3 = z ? 1 : 0;
                char c4 = z ? 1 : 0;
                if (4 == simOpInfo[c] && cSimCount == z && wSimCount == z && op09VolteOffPhoneId != majorPhoneId) {
                    return z;
                }
                return false;
            }
        } else if (majorPhoneId >= 2 || getMainCapabilityPhoneId() >= 2 || RatConfiguration.isC2kSupported() || RatConfiguration.isTdscdmaSupported()) {
            return false;
        } else {
            return true;
        }
    }

    public static int getHigherPrioritySimForOp01(int curId, boolean[] op01Usim, boolean[] op01Sim, boolean[] overseaUsim, boolean[] overseaSim) {
        int targetSim = -1;
        int phoneNum = op01Usim.length;
        if (op01Usim[curId]) {
            return curId;
        }
        for (int i = 0; i < phoneNum; i++) {
            if (op01Usim[i]) {
                targetSim = i;
            }
        }
        if (targetSim != -1 || op01Sim[curId]) {
            return targetSim;
        }
        for (int i2 = 0; i2 < phoneNum; i2++) {
            if (op01Sim[i2]) {
                targetSim = i2;
            }
        }
        if (targetSim != -1 || overseaUsim[curId]) {
            return targetSim;
        }
        for (int i3 = 0; i3 < phoneNum; i3++) {
            if (overseaUsim[i3]) {
                targetSim = i3;
            }
        }
        if (targetSim != -1 || overseaSim[curId]) {
            return targetSim;
        }
        for (int i4 = 0; i4 < phoneNum; i4++) {
            if (overseaSim[i4]) {
                targetSim = i4;
            }
        }
        return targetSim;
    }

    public static int getHighestPriorityPhone(int capPhoneId, int[] priority) {
        int targetPhone = 0;
        int phoneNum = priority.length;
        int highestPriorityCount = 0;
        int highestPriorityBitMap = 0;
        for (int i = 0; i < phoneNum; i++) {
            if (priority[i] < priority[targetPhone]) {
                targetPhone = i;
                highestPriorityCount = 1;
                highestPriorityBitMap = 1 << i;
            } else if (priority[i] == priority[targetPhone]) {
                highestPriorityCount++;
                highestPriorityBitMap |= 1 << i;
            }
        }
        if (highestPriorityCount == 1) {
            return targetPhone;
        }
        if (capPhoneId == -1 || ((1 << capPhoneId) & highestPriorityBitMap) == 0) {
            return -1;
        }
        return capPhoneId;
    }

    public static int getMainCapabilityPhoneId() {
        return SystemProperties.getInt("persist.vendor.radio.simswitch", 1) - 1;
    }

    private static void logd(String s) {
        Rlog.d(LOG_TAG, "[RadioCapSwitchUtil] " + s);
    }

    public static int isNeedShowSimDialog() {
        if (SystemProperties.getBoolean("ro.vendor.mtk_disable_cap_switch", false)) {
            logd("mtk_disable_cap_switch is true");
            return 0;
        }
        logd("isNeedShowSimDialog start");
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        int[] simOpInfo = new int[phoneCount];
        int[] simType = new int[phoneCount];
        String[] currIccId = new String[phoneCount];
        int insertedSimCount = 0;
        int insertedStatus = 0;
        int op02CardCount = 0;
        ArrayList<Integer> usimIndexList = new ArrayList<>();
        ArrayList<Integer> simIndexList = new ArrayList<>();
        ArrayList<Integer> op02IndexList = new ArrayList<>();
        ArrayList<Integer> otherIndexList = new ArrayList<>();
        for (int i = 0; i < phoneCount; i++) {
            currIccId[i] = SystemProperties.get(PROPERTY_SIM_ICCID[i]);
            logd("currIccid[" + i + "] : " + currIccId[i]);
            if (currIccId[i] == null || "".equals(currIccId[i])) {
                Log.e(LOG_TAG, "iccid not found, wait for next sim state change");
                return 3;
            }
            if (!"N/A".equals(currIccId[i])) {
                if (isSimOn(i)) {
                    insertedSimCount++;
                    insertedStatus = (1 << i) | insertedStatus;
                } else {
                    logd("isNeedShowSimDialog, slot" + i + " is power off.");
                }
            }
        }
        if (insertedSimCount < 2) {
            logd("isNeedShowSimDialog: insert sim count < 2, do not show dialog");
            return 1;
        } else if (isCapabilitySwitching()) {
            logd("SIM switch executing");
            return 4;
        } else if (!getSimInfo(simOpInfo, simType, insertedStatus)) {
            Log.e(LOG_TAG, "isNeedShowSimDialog: Can't get SIM information");
            return 2;
        } else {
            for (int i2 = 0; i2 < phoneCount; i2++) {
                if (1 == simType[i2]) {
                    usimIndexList.add(Integer.valueOf(i2));
                } else if (simType[i2] == 0) {
                    simIndexList.add(Integer.valueOf(i2));
                }
                if (3 == simOpInfo[i2]) {
                    op02IndexList.add(Integer.valueOf(i2));
                } else {
                    otherIndexList.add(Integer.valueOf(i2));
                }
            }
            logd("usimIndexList size = " + usimIndexList.size());
            logd("op02IndexList size = " + op02IndexList.size());
            if (usimIndexList.size() >= 2) {
                for (int i3 = 0; i3 < usimIndexList.size(); i3++) {
                    if (op02IndexList.contains(usimIndexList.get(i3))) {
                        op02CardCount++;
                    }
                }
                if (op02CardCount == 1) {
                    logd("isNeedShowSimDialog: One OP02Usim inserted, not show dialog");
                    return 1;
                }
            } else if (usimIndexList.size() == 1) {
                logd("isNeedShowSimDialog: One Usim inserted, not show dialog");
                return 1;
            } else {
                for (int i4 = 0; i4 < simIndexList.size(); i4++) {
                    if (op02IndexList.contains(simIndexList.get(i4))) {
                        op02CardCount++;
                    }
                }
                if (op02CardCount == 1) {
                    logd("isNeedShowSimDialog: One non-OP02 Usim inserted, not show dialog");
                    return 1;
                }
            }
            logd("isNeedShowSimDialog: Show dialog");
            return 0;
        }
    }

    public static boolean isAnySimLocked(int phoneNum) {
        if (RatConfiguration.isC2kSupported()) {
            logd("isAnySimLocked always returns false in C2K");
            return false;
        }
        String[] mnc = new String[phoneNum];
        String[] iccid = new String[phoneNum];
        for (int i = 0; i < phoneNum; i++) {
            iccid[i] = SystemProperties.get(PROPERTY_SIM_ICCID[i]);
            if (!iccid[i].equals("N/A")) {
                mnc[i] = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.sim.operator.numeric", "");
                if (mnc[i].length() >= 6) {
                    mnc[i] = mnc[i].substring(0, 6);
                } else if (mnc[i].length() >= 5) {
                    mnc[i] = mnc[i].substring(0, 5);
                }
                if (!mnc[i].equals("")) {
                    logd("i = " + i + " from gsm.sim.operator.numeric:" + mnc[i] + " ,iccid = " + iccid[i]);
                }
            }
            if (!iccid[i].equals("N/A") && (mnc[i].equals("") || mnc[i].equals("sim_lock"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPS2SupportLTE() {
        if (SystemProperties.get("persist.vendor.radio.mtk_ps2_rat").indexOf(76) != -1) {
            return true;
        }
        return false;
    }

    public static boolean isTPlusWSupport() {
        if (SystemProperties.get("vendor.ril.simswitch.tpluswsupport").equals("1")) {
            return true;
        }
        return false;
    }

    public static void updateSimImsiStatus(int slot, String value) {
        logd("updateSimImsiStatus slot = " + slot + ", value = " + value);
        SystemProperties.set(PROPERTY_SIM_IMSI_STATUS[slot], value);
    }

    private static String getSimImsiStatus(int slot) {
        return SystemProperties.get(PROPERTY_SIM_IMSI_STATUS[slot], "0");
    }

    public static void clearAllSimImsiStatus() {
        logd("clearAllSimImsiStatus");
        for (int i = 0; i < PROPERTY_SIM_IMSI_STATUS.length; i++) {
            updateSimImsiStatus(i, "0");
        }
    }

    public static boolean isDssNoResetSupport() {
        if (SystemProperties.get("vendor.ril.simswitch.no_reset_support").equals("1")) {
            return true;
        }
        return false;
    }

    public static int getProtocolStackId(int slot) {
        int majorSlot = getMainCapabilityPhoneId();
        if (slot == majorSlot) {
            return 1;
        }
        if (isDssNoResetSupport()) {
            if (slot < majorSlot) {
                return slot + 2;
            }
        } else if (slot == 0) {
            return majorSlot + 1;
        }
        return slot + 1;
    }

    public static String getHashCode(String iccid) {
        try {
            MessageDigest alga = MessageDigest.getInstance("SHA-256");
            alga.update(iccid.getBytes());
            return new String(alga.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RadioCapabilitySwitchUtil SHA-256 must exist");
        }
    }

    public static boolean isSimOn(int slotId) {
        if (MtkTelephonyManagerEx.getDefault().isSimOnOffEnabled() && MtkTelephonyManagerEx.getDefault().getSimOnOffState(slotId) == 10) {
            return false;
        }
        return true;
    }

    public static boolean isRadioOffBySimManagement(int phoneId) {
        int subId = MtkSubscriptionManager.getSubIdUsingPhoneId(phoneId);
        try {
            IMtkTelephonyEx iTelEx = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
            if (iTelEx != null) {
                return iTelEx.isRadioOffBySimManagement(subId);
            }
            logd("iTelEx is null!");
            return false;
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isCapabilitySwitching() {
        try {
            IMtkTelephonyEx iTelEx = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
            if (iTelEx != null) {
                return iTelEx.isCapabilitySwitching();
            }
            logd("iTelEx is null!");
            return false;
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isSubsidyLockFeatureOn() {
        if (10 == MtkTelephonyManagerEx.getDefault().getSimLockPolicy()) {
            return true;
        }
        return false;
    }

    public static boolean isSubsidyLockForOmSupported() {
        boolean isSubsidyLockSupported = isSubsidyLockFeatureOn();
        boolean subsidylockStatus = !SystemProperties.get("persist.vendor.subsidylock", "0").equals(MtkGsmCdmaPhone.ACT_TYPE_UTRAN);
        if (!isSubsidyLockSupported || !subsidylockStatus) {
            return false;
        }
        return true;
    }
}
