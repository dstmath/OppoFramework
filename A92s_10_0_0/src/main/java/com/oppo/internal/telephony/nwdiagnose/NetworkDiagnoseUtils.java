package com.oppo.internal.telephony.nwdiagnose;

import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

public class NetworkDiagnoseUtils {
    public static final int CALLSTATE_IDLE = 0;
    public static final int CALLSTATE_MO = 2;
    public static final int CALLSTATE_MT = 1;
    public static final int CALL_SMS_SIM1 = 1;
    public static final int CALL_SMS_SIM2 = 2;
    public static final int CARD_TYPE_CM = 2;
    public static final int CARD_TYPE_CT = 1;
    public static final int CARD_TYPE_CU = 3;
    public static final int CARD_TYPE_OTHER = 4;
    public static final int CARD_TYPE_TEST = 9;
    public static final int CARD_TYPE_UNKNOWN = -1;
    public static final String CA_PCELL = "p";
    public static final String CA_SCELL = "s";
    public static final int DATATYPE_CDMA = 2;
    public static final int DATATYPE_EVDO = 5;
    public static final int DATATYPE_GSM = 1;
    public static final int DATATYPE_LTE = 6;
    public static final int DATATYPE_NOSRV = 0;
    public static final int DATATYPE_TDS = 4;
    public static final int DATATYPE_UNKNOWN = -1;
    public static final int DATATYPE_WCDMA = 3;
    public static final String INFO_APCONFIG = "ap";
    public static final String INFO_APCONFIG_DATA = "data";
    public static final String INFO_CELL = "cell";
    public static final String INFO_OTHER = "add";
    public static final String INFO_OTHER_CALL = "call";
    public static final String INFO_OTHER_SMS = "sms";
    public static final String INFO_SERVICESTATE = "ss";
    public static final String INFO_SIGNAL = "sig";
    public static final String INFO_SIM1 = "s1";
    public static final String INFO_SIM2 = "s2";
    public static final int INT_TEN = 10;
    public static final int MIN_CAINFO_LEN = 2;
    public static final long MIN_RECORD_TIMEMILLIS = 10;
    public static final int NWTYPE_MOBILE_DATA = 2;
    public static final int NWTYPE_UNKNOWN = 0;
    public static final int NWTYPE_WIFI = 1;
    private static final double QCRIL_CM_GW_SIGNAL_STRENGTH_UNKNOWN = 99.0d;
    private static final double QCRIL_CM_RSSI_OFFSET = 182.26d;
    private static final double QCRIL_CM_RSSI_SLOPE = -1.6129032258064515d;
    private static final double QCRIL_CM_RSSI_TOOHI_CODE = 31.0d;
    public static final int RADIO_TECH_1xRTT = 7;
    public static final int RADIO_TECH_CDMA = 4;
    public static final int RADIO_TECH_EDGE = 2;
    public static final int RADIO_TECH_EHRPD = 14;
    public static final int RADIO_TECH_EVDO_0 = 5;
    public static final int RADIO_TECH_EVDO_A = 6;
    public static final int RADIO_TECH_EVDO_B = 12;
    public static final int RADIO_TECH_GPRS = 1;
    public static final int RADIO_TECH_GSM = 16;
    public static final int RADIO_TECH_HSDPA = 8;
    public static final int RADIO_TECH_HSPA = 10;
    public static final int RADIO_TECH_HSPAP = 15;
    public static final int RADIO_TECH_HSUPA = 9;
    public static final int RADIO_TECH_IDEN = 11;
    public static final int RADIO_TECH_IWLAN = 18;
    public static final int RADIO_TECH_LTE = 13;
    public static final int RADIO_TECH_LTE_CA = 19;
    public static final int RADIO_TECH_TD_SCDMA = 17;
    public static final int RADIO_TECH_UMTS = 3;
    public static final int RADIO_TECH_UNKNOWN = 0;
    public static final int RF_BAND1 = 1;
    public static final int RF_BAND11 = 11;
    public static final int RF_BAND12 = 12;
    public static final int RF_BAND17 = 17;
    public static final int RF_BAND18 = 18;
    public static final int RF_BAND1800 = 1800;
    public static final int RF_BAND19 = 19;
    public static final int RF_BAND1900 = 1900;
    public static final int RF_BAND2 = 2;
    public static final int RF_BAND20 = 20;
    public static final int RF_BAND21 = 21;
    public static final int RF_BAND25 = 25;
    public static final int RF_BAND26 = 26;
    public static final int RF_BAND28 = 28;
    public static final int RF_BAND3 = 3;
    public static final int RF_BAND34 = 34;
    public static final int RF_BAND38 = 38;
    public static final int RF_BAND39 = 39;
    public static final int RF_BAND4 = 4;
    public static final int RF_BAND40 = 40;
    public static final int RF_BAND41 = 41;
    public static final int RF_BAND5 = 5;
    public static final int RF_BAND7 = 7;
    public static final int RF_BAND8 = 8;
    public static final int RF_BAND850 = 850;
    public static final int RF_BAND900 = 900;
    public static final int RF_BAND_NONE = 0;
    public static final int RF_EGSM_900_ARFCN_BOTTOM = 975;
    public static final int RF_EGSM_900_ARFCN_UPPER = 1023;
    public static final int RF_GSM_1800_ARFCN_BOTTOM = 512;
    public static final int RF_GSM_1800_ARFCN_UPPER = 885;
    public static final int RF_GSM_850_ARFCN_BOTTOM = 128;
    public static final int RF_GSM_850_ARFCN_UPPER = 251;
    public static final int RF_LTE_BAND11_ARFCN_BOTTOM = 4750;
    public static final int RF_LTE_BAND11_ARFCN_UPPER = 4949;
    public static final int RF_LTE_BAND12_ARFCN_BOTTOM = 5010;
    public static final int RF_LTE_BAND12_ARFCN_UPPER = 5179;
    public static final int RF_LTE_BAND17_ARFCN_BOTTOM = 5280;
    public static final int RF_LTE_BAND17_ARFCN_UPPER = 5379;
    public static final int RF_LTE_BAND18_ARFCN_BOTTOM = 5850;
    public static final int RF_LTE_BAND18_ARFCN_UPPER = 5999;
    public static final int RF_LTE_BAND19_ARFCN_BOTTOM = 6000;
    public static final int RF_LTE_BAND19_ARFCN_UPPER = 6149;
    public static final int RF_LTE_BAND1_ARFCN_BOTTOM = 0;
    public static final int RF_LTE_BAND1_ARFCN_UPPER = 599;
    public static final int RF_LTE_BAND20_ARFCN_BOTTOM = 6150;
    public static final int RF_LTE_BAND20_ARFCN_UPPER = 6449;
    public static final int RF_LTE_BAND21_ARFCN_BOTTOM = 6450;
    public static final int RF_LTE_BAND21_ARFCN_UPPER = 6599;
    public static final int RF_LTE_BAND25_ARFCN_BOTTOM = 8040;
    public static final int RF_LTE_BAND25_ARFCN_UPPER = 8689;
    public static final int RF_LTE_BAND26_ARFCN_BOTTOM = 8690;
    public static final int RF_LTE_BAND26_ARFCN_UPPER = 9039;
    public static final int RF_LTE_BAND28_ARFCN_BOTTOM = 9210;
    public static final int RF_LTE_BAND28_ARFCN_UPPER = 9659;
    public static final int RF_LTE_BAND2_ARFCN_BOTTOM = 600;
    public static final int RF_LTE_BAND2_ARFCN_UPPER = 1199;
    public static final int RF_LTE_BAND34_ARFCN_BOTTOM = 36200;
    public static final int RF_LTE_BAND34_ARFCN_UPPER = 36349;
    public static final int RF_LTE_BAND38_ARFCN_BOTTOM = 37750;
    public static final int RF_LTE_BAND38_ARFCN_UPPER = 38249;
    public static final int RF_LTE_BAND39_ARFCN_BOTTOM = 38250;
    public static final int RF_LTE_BAND39_ARFCN_UPPER = 38649;
    public static final int RF_LTE_BAND3_ARFCN_BOTTOM = 1200;
    public static final int RF_LTE_BAND3_ARFCN_UPPER = 1949;
    public static final int RF_LTE_BAND40_ARFCN_BOTTOM = 38650;
    public static final int RF_LTE_BAND40_ARFCN_UPPER = 39649;
    public static final int RF_LTE_BAND41_ARFCN_BOTTOM = 39650;
    public static final int RF_LTE_BAND41_ARFCN_UPPER = 41589;
    public static final int RF_LTE_BAND4_ARFCN_BOTTOM = 1950;
    public static final int RF_LTE_BAND4_ARFCN_UPPER = 2399;
    public static final int RF_LTE_BAND5_ARFCN_BOTTOM = 2400;
    public static final int RF_LTE_BAND5_ARFCN_UPPER = 2649;
    public static final int RF_LTE_BAND7_ARFCN_BOTTOM = 2750;
    public static final int RF_LTE_BAND7_ARFCN_UPPER = 3449;
    public static final int RF_LTE_BAND8_ARFCN_BOTTOM = 3450;
    public static final int RF_LTE_BAND8_ARFCN_UPPER = 3799;
    public static final int RF_PGSM_900_ARFCN_BOTTOM = 1;
    public static final int RF_PGSM_900_ARFCN_UPPER = 124;
    public static final int RF_WCDMA_BAND1_ARFCN_BOTTOM = 10562;
    public static final int RF_WCDMA_BAND1_ARFCN_UPPER = 10838;
    public static final int RF_WCDMA_BAND2_ARFCN_BOTTOM = 9662;
    public static final int RF_WCDMA_BAND2_ARFCN_UPPER = 9938;
    public static final int RF_WCDMA_BAND4_ARFCN_BOTTOM = 1537;
    public static final int RF_WCDMA_BAND4_ARFCN_UPPER = 1738;
    public static final int RF_WCDMA_BAND5_ARFCN_BOTTOM = 4357;
    public static final int RF_WCDMA_BAND5_ARFCN_UPPER = 4458;
    public static final int RF_WCDMA_BAND8_ARFCN_BOTTOM = 2937;
    public static final int RF_WCDMA_BAND8_ARFCN_UPPER = 3088;
    private static final String TAG = "NetworkStatusMonitorService";

    public static int getSimType(Context context, int subId) {
        SubscriptionInfo sir;
        if (context == null || subId < 0 || (sir = SubscriptionManager.from(context).getActiveSubscriptionInfo(subId)) == null) {
            return -1;
        }
        return sir.getIconTint();
    }

    public static String convertLtesignalStrengthToRssi(int lteSignalStrength) {
        double rssi = QCRIL_CM_GW_SIGNAL_STRENGTH_UNKNOWN;
        if (((double) lteSignalStrength) != QCRIL_CM_GW_SIGNAL_STRENGTH_UNKNOWN) {
            rssi = (((((((double) lteSignalStrength) - 0.5d) * 100.0d) / QCRIL_CM_RSSI_TOOHI_CODE) - QCRIL_CM_RSSI_OFFSET) / QCRIL_CM_RSSI_SLOPE) * -1.0d;
        }
        return (((double) Math.round(100.0d * rssi)) * 0.01d) + "";
    }

    public static int convertLTEarfcnToBand(int arfcn) {
        if (arfcn >= 0 && arfcn <= 599) {
            return 1;
        }
        if (arfcn >= 600 && arfcn <= 1199) {
            return 2;
        }
        if (arfcn >= 1200 && arfcn <= 1949) {
            return 3;
        }
        if (arfcn >= 1950 && arfcn <= 2399) {
            return 4;
        }
        if (arfcn >= 2400 && arfcn <= 2649) {
            return 5;
        }
        if (arfcn >= 2750 && arfcn <= 3449) {
            return 7;
        }
        if (arfcn >= 3450 && arfcn <= 3799) {
            return 8;
        }
        if (arfcn >= 4750 && arfcn <= 4949) {
            return 11;
        }
        if (arfcn >= 5010 && arfcn <= 5179) {
            return 12;
        }
        if (arfcn >= 5280 && arfcn <= 5379) {
            return 17;
        }
        if (arfcn >= 5850 && arfcn <= 5999) {
            return 18;
        }
        if (arfcn >= 6000 && arfcn <= 6149) {
            return 19;
        }
        if (arfcn >= 6150 && arfcn <= 6449) {
            return 20;
        }
        if (arfcn >= 6450 && arfcn <= 6599) {
            return 21;
        }
        if (arfcn >= 8040 && arfcn <= 8689) {
            return 25;
        }
        if (arfcn >= 8690 && arfcn <= 9039) {
            return 26;
        }
        if (arfcn >= 9210 && arfcn <= 9659) {
            return 28;
        }
        if (arfcn >= 36200 && arfcn <= 36349) {
            return 34;
        }
        if (arfcn >= 37750 && arfcn <= 38249) {
            return 38;
        }
        if (arfcn >= 38250 && arfcn <= 38649) {
            return 39;
        }
        if (arfcn >= 38650 && arfcn <= 39649) {
            return 40;
        }
        if (arfcn < 39650 || arfcn > 41589) {
            return 0;
        }
        return 41;
    }

    public static int convertWCDMAarfcnToBand(int arfcn) {
        if (arfcn >= 10562 && arfcn <= 10838) {
            return 1;
        }
        if (arfcn >= 9662 && arfcn <= 9938) {
            return 2;
        }
        if (arfcn >= 1537 && arfcn <= 1738) {
            return 4;
        }
        if (arfcn >= 4357 && arfcn <= 4458) {
            return 5;
        }
        if (arfcn < 2937 || arfcn > 3088) {
            return 0;
        }
        return 8;
    }

    public static int convertGSMarfcnToBand(int arfcn) {
        if (arfcn >= 128 && arfcn <= 251) {
            return RF_BAND850;
        }
        if (arfcn >= 1 && arfcn <= 124) {
            return RF_BAND900;
        }
        if (arfcn >= 975 && arfcn <= 1023) {
            return RF_BAND900;
        }
        if (arfcn < 512 || arfcn > 885) {
            return 0;
        }
        return RF_BAND1800;
    }

    public static int getDateTypeIndex(int tech, boolean isTds) {
        switch (tech) {
            case 1:
            case 2:
            case 16:
                return 1;
            case 3:
                return 3;
            case 4:
            case 7:
                return 2;
            case 5:
            case 6:
            case 12:
            case 14:
                return 5;
            case 8:
            case 9:
            case 10:
            case 15:
                if (isTds) {
                    return 4;
                }
                return 3;
            case 11:
            case 18:
            default:
                return -1;
            case 13:
            case 19:
                return 6;
            case 17:
                return 4;
        }
    }
}
