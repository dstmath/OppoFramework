package com.oppo.internal.telephony;

import android.os.SystemProperties;
import android.telephony.CellSignalStrengthNr;
import android.telephony.OppoSignalStrength;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import com.android.internal.telephony.util.OemTelephonyUtils;
import java.util.HashMap;
import java.util.List;

public class OppoSignalStrengthStandard {
    private static final boolean DBG = false;
    private static final int GSM_RSSI_MAX = -51;
    private static final int GSM_RSSI_MIN = -113;
    static final String LOG_TAG = "SST";
    public static final int MAX_LTE_RSRP = -44;
    public static final int MAX_WCDMA_RSCP = -24;
    public static final int MIN_CDMA_VALUE = -119;
    public static final int MIN_GSM_VALUE = -107;
    public static final int MIN_LTE_RSRP = -140;
    public static final int MIN_NR_RSRP = -140;
    public static final int MIN_WCDMA_RSCP = -120;
    public static String NET_BUILD_TYPE = SystemProperties.get("persist.sys.net_build_type", "allnet");
    public static int OEM_LEVLE = (SystemProperties.get("persist.sys.oem_smooth", "0").equals("1") ? 1 : 0);
    private static final int[] WindtreLteRsrpThresholds = {-126, -112, -106, -95};
    private static final int[] WindtreWcdmaRscpThresholds = {-115, -105, -95, -85};
    private static HashMap<String, int[]> mLteRsrpThresholdsMap = new HashMap<String, int[]>() {
        /* class com.oppo.internal.telephony.OppoSignalStrengthStandard.AnonymousClass1 */

        {
            put("WIND3", OppoSignalStrengthStandard.WindtreLteRsrpThresholds);
        }
    };
    private static HashMap<String, int[]> mWcdmaRscpThresholdsMap = new HashMap<String, int[]>() {
        /* class com.oppo.internal.telephony.OppoSignalStrengthStandard.AnonymousClass2 */

        {
            put("WIND3", OppoSignalStrengthStandard.WindtreWcdmaRscpThresholds);
        }
    };
    private static String operator = SystemProperties.get("ro.oppo.operator", "ex");
    private static final String[] operators = {"WIND3"};

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0030, code lost:
        if (r7.equals("LTE") != false) goto L_0x0034;
     */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0043  */
    static int[] getThresholds(String rat) {
        String mOperator;
        boolean z = false;
        int[] mThresholds = new int[0];
        if (operator == null || rat == null || (mOperator = getNeedCustomOperator()) == null) {
            return mThresholds;
        }
        int hashCode = rat.hashCode();
        if (hashCode != 75709) {
            if (hashCode == 82410124 && rat.equals("WCDMA")) {
                z = true;
                if (z) {
                    return mLteRsrpThresholdsMap.get(mOperator);
                }
                if (!z) {
                    return mThresholds;
                }
                return mWcdmaRscpThresholdsMap.get(mOperator);
            }
        }
        z = true;
        if (z) {
        }
    }

    static String getNeedCustomOperator() {
        String mOperator = null;
        int i = 0;
        while (true) {
            String[] strArr = operators;
            if (i >= strArr.length) {
                return mOperator;
            }
            if (operator.equals(strArr[i])) {
                mOperator = operator;
            }
            i++;
        }
    }

    static boolean isTelstraVersion() {
        if ("TELSTRA".equals(operator) || "TELSTRA_PREPAID".equals(operator) || "TELSTRA_POSTPAID".equals(operator)) {
            return true;
        }
        return false;
    }

    static int getNrRsrp(SignalStrength signalStength) {
        List<CellSignalStrengthNr> nrList = signalStength.getCellSignalStrengths(CellSignalStrengthNr.class);
        if (nrList == null || nrList.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return nrList.get(0).getSsRsrp();
    }

    static int getNrLevel(int rsrp) {
        return getLteLevel(rsrp);
    }

    static int getLteLevel(int lteRsrp) {
        int[] oppothreshRsrp = {-126, OEM_LEVLE == 0 ? GSM_RSSI_MIN : -118, OEM_LEVLE == 0 ? -105 : -110, -97, -44};
        if ("allnetcutest".equals(NET_BUILD_TYPE)) {
            oppothreshRsrp[0] = -120;
            oppothreshRsrp[1] = -118;
            oppothreshRsrp[2] = -114;
            oppothreshRsrp[3] = -105;
        }
        int[] arr = getThresholds("LTE");
        if (arr != null && arr.length == 4) {
            for (int i = 0; i < 4; i++) {
                oppothreshRsrp[i] = arr[i];
            }
        }
        if (lteRsrp > oppothreshRsrp[4]) {
            return 0;
        }
        if (lteRsrp >= oppothreshRsrp[3]) {
            return 4;
        }
        if (lteRsrp >= oppothreshRsrp[2]) {
            return 3;
        }
        if (lteRsrp >= oppothreshRsrp[1]) {
            return 2;
        }
        if (lteRsrp >= oppothreshRsrp[0]) {
            return 1;
        }
        return 0;
    }

    static int getTdScdmaLevel(int tdScdmaDbm) {
        if (tdScdmaDbm > -25 || tdScdmaDbm == Integer.MAX_VALUE) {
            return 0;
        }
        if (tdScdmaDbm >= -91) {
            return 4;
        }
        if (tdScdmaDbm >= -97) {
            return 3;
        }
        if (tdScdmaDbm >= -107) {
            return 2;
        }
        if (tdScdmaDbm >= -112) {
            return 1;
        }
        return 0;
    }

    static int getWcdmaLevel(int wcdmaRscp) {
        int[] mWcdmaRscpThresholds = {-112, MIN_GSM_VALUE, -97, -91};
        int[] mTelstraWcdmaCriterion = {-109, -100, -95, -85};
        if (isTelstraVersion()) {
            for (int i = 0; i < 4; i++) {
                mWcdmaRscpThresholds[i] = mTelstraWcdmaCriterion[i];
            }
        }
        int[] arr = getThresholds("WCDMA");
        if (arr != null && arr.length == 4) {
            for (int i2 = 0; i2 < 4; i2++) {
                mWcdmaRscpThresholds[i2] = arr[i2];
            }
        }
        if (wcdmaRscp < -120 || wcdmaRscp > -24) {
            if (wcdmaRscp == Integer.MAX_VALUE) {
                return 0;
            }
            Rlog.d(LOG_TAG, "getWcdmaLevel - invalid WCDMA RSCP: mWcdmaRscp=" + wcdmaRscp);
            return 0;
        } else if (wcdmaRscp >= mWcdmaRscpThresholds[3]) {
            return 4;
        } else {
            if (wcdmaRscp >= mWcdmaRscpThresholds[2]) {
                return 3;
            }
            if (wcdmaRscp >= mWcdmaRscpThresholds[1]) {
                return 2;
            }
            if (wcdmaRscp >= mWcdmaRscpThresholds[0]) {
                return 1;
            }
            return 0;
        }
    }

    static int getGsmLevel(int rssi) {
        if (rssi < GSM_RSSI_MIN || rssi > GSM_RSSI_MAX) {
            return 0;
        }
        if (rssi >= -87) {
            return 4;
        }
        if (rssi >= -97) {
            return 3;
        }
        if (rssi >= -103) {
            return 2;
        }
        return 1;
    }

    static int getCdmaLevel(int cdmaDbm) {
        if (cdmaDbm == Integer.MAX_VALUE) {
            return 0;
        }
        if (cdmaDbm >= -89) {
            return 4;
        }
        if (cdmaDbm >= -100) {
            return 3;
        }
        if (cdmaDbm >= -106) {
            return 2;
        }
        if (cdmaDbm >= -109) {
            return 1;
        }
        return 0;
    }

    static int getEvdoLevel(int evdoDbm) {
        if (evdoDbm == Integer.MAX_VALUE) {
            return 0;
        }
        if (evdoDbm >= -89) {
            return 4;
        }
        if (evdoDbm >= -100) {
            return 3;
        }
        if (evdoDbm >= -106) {
            return 2;
        }
        if (evdoDbm >= -110) {
            return 1;
        }
        return 0;
    }

    static int getGsmRelatedSignalStrength(SignalStrength signalStength) {
        int nrLevel = getNrLevel(getNrRsrp(signalStength));
        if (nrLevel != 0) {
            return nrLevel;
        }
        int level = getLteLevel(signalStength.getLteRsrp());
        if (level != 0) {
            return level;
        }
        int level2 = getTdScdmaLevel(signalStength.getTdScdmaDbm());
        if (level2 != 0) {
            return level2;
        }
        int level3 = getWcdmaLevel(signalStength.getWcdmaDbm());
        if (level3 == 0) {
            return getGsmLevel(signalStength.getGsmDbm());
        }
        return level3;
    }

    static int getCdmaRelatedSignalStrength(SignalStrength signalStength) {
        int cdmaLevel = getCdmaLevel(signalStength.getCdmaDbm());
        int evdoLevel = getEvdoLevel(signalStength.getEvdoDbm());
        if (evdoLevel == 0) {
            return cdmaLevel;
        }
        if (cdmaLevel == 0) {
            return evdoLevel;
        }
        return cdmaLevel > evdoLevel ? cdmaLevel : evdoLevel;
    }

    public static void getSignalStrengthLevel(SignalStrength signalStength, boolean showZero) {
        Rlog.d(LOG_TAG, "getSignalStrengthLevel=" + showZero);
        if (signalStength != null) {
            OppoSignalStrength tempSignalStrength = (OppoSignalStrength) OemTelephonyUtils.typeCasting(OppoSignalStrength.class, signalStength);
            if (!signalStength.isGsm()) {
                tempSignalStrength.setOEMLevel(-1, getCdmaRelatedSignalStrength(signalStength));
                if (!showZero && tempSignalStrength.getOEMLevel_1() == 0) {
                    tempSignalStrength.setOEMLevel(-1, OEM_LEVLE);
                }
            } else if (signalStength.getLteRsrp() == Integer.MAX_VALUE && signalStength.getTdScdmaDbm() == -120 && signalStength.getWcdmaDbm() == -120 && signalStength.getGsmDbm() == 0) {
                tempSignalStrength.setOEMLevel(0, 0);
                return;
            } else {
                tempSignalStrength.setOEMLevel(getGsmRelatedSignalStrength(signalStength), -1);
                if (!showZero && tempSignalStrength.getOEMLevel_0() == 0) {
                    tempSignalStrength.setOEMLevel(OEM_LEVLE, -1);
                }
            }
            int level = tempSignalStrength.getOEMLevel_0() < tempSignalStrength.getOEMLevel_1() ? tempSignalStrength.getOEMLevel_1() : tempSignalStrength.getOEMLevel_0();
            tempSignalStrength.setOEMLevel(level, level);
            Rlog.d(LOG_TAG, "mOEMLevel_0=" + tempSignalStrength.getOEMLevel_0() + ",mOEMLevel_1:" + tempSignalStrength.getOEMLevel_1());
        }
    }
}
