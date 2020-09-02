package android.telephony;

import com.android.internal.telephony.RILConstants;
import com.mediatek.internal.telephony.MtkPhoneNumberFormatUtil;
import com.mediatek.internal.telephony.MtkRILConstants;

public class MtkRadioAccessFamily {
    private static final int CDMA = 72;
    private static final int EVDO = 10288;
    private static final int GSM = 32771;
    private static final int HS = 17280;
    private static final int LTE = 266240;
    private static final int NR = 524288;
    public static final int RAF_1xRTT = 64;
    public static final int RAF_EDGE = 2;
    public static final int RAF_EHRPD = 8192;
    public static final int RAF_EVDO_0 = 16;
    public static final int RAF_EVDO_A = 32;
    public static final int RAF_EVDO_B = 2048;
    public static final int RAF_GPRS = 1;
    public static final int RAF_GSM = 32768;
    public static final int RAF_HSDPA = 128;
    public static final int RAF_HSPA = 512;
    public static final int RAF_HSPAP = 16384;
    public static final int RAF_HSUPA = 256;
    public static final int RAF_IS95A = 8;
    public static final int RAF_IS95B = 8;
    public static final int RAF_LTE = 4096;
    public static final int RAF_LTE_CA = 262144;
    public static final int RAF_NR = 524288;
    public static final int RAF_TD_SCDMA = 65536;
    public static final int RAF_UMTS = 4;
    public static final int RAF_UNKNOWN = 0;
    private static final int WCDMA = 17284;

    public static int getRafFromNetworkType(int type) {
        switch (type) {
            case 0:
                return 50055;
            case 1:
                return GSM;
            case 2:
                return WCDMA;
            case 3:
                return 50055;
            case 4:
                return 10360;
            case 5:
                return CDMA;
            case 6:
                return EVDO;
            case 7:
                return 60415;
            case 8:
                return 276600;
            case 9:
                return 316295;
            case 10:
                return 326655;
            case 11:
                return LTE;
            case 12:
                return 283524;
            case 13:
                return RAF_TD_SCDMA;
            case 14:
                return 82820;
            case 15:
                return 331776;
            case 16:
                return 98307;
            case MtkPhoneNumberFormatUtil.FORMAT_THAILAND /*{ENCODED_INT: 17}*/:
                return 364547;
            case MtkPhoneNumberFormatUtil.FORMAT_VIETNAM /*{ENCODED_INT: 18}*/:
                return 115591;
            case MtkPhoneNumberFormatUtil.FORMAT_PORTUGAL /*{ENCODED_INT: 19}*/:
                return 349060;
            case MtkPhoneNumberFormatUtil.FORMAT_POLAND /*{ENCODED_INT: 20}*/:
                return 381831;
            case MtkPhoneNumberFormatUtil.FORMAT_AUSTRALIA /*{ENCODED_INT: 21}*/:
                return 125951;
            case MtkPhoneNumberFormatUtil.FORMAT_NEW_ZEALAND /*{ENCODED_INT: 22}*/:
                return 392191;
            case MtkPhoneNumberFormatUtil.FORMAT_BRAZIL /*{ENCODED_INT: 23}*/:
                return 524288;
            case 24:
                return 790528;
            case 25:
                return 800888;
            case 26:
                return 840583;
            case 27:
                return 850943;
            case 28:
                return 807812;
            case 29:
                return 856064;
            case 30:
                return 888835;
            case 31:
                return 873348;
            case RAF_EVDO_A /*{ENCODED_INT: 32}*/:
                return 906119;
            case 33:
                return 916479;
            default:
                switch (type) {
                    case 101:
                        return 299011;
                    case 102:
                        return LTE;
                    case MtkRILConstants.NETWORK_MODE_CDMA_GSM /*{ENCODED_INT: 103}*/:
                        return 32843;
                    case MtkRILConstants.NETWORK_MODE_CDMA_EVDO_GSM /*{ENCODED_INT: 104}*/:
                        return 43131;
                    case MtkRILConstants.NETWORK_MODE_LTE_CDMA_EVDO_GSM /*{ENCODED_INT: 105}*/:
                        return 309371;
                    default:
                        return 0;
                }
        }
    }

    private static int getAdjustedRaf(int raf) {
        int raf2 = (raf & GSM) > 0 ? GSM | raf : raf;
        int raf3 = (raf2 & WCDMA) > 0 ? raf2 | WCDMA : raf2;
        int raf4 = (raf3 & CDMA) > 0 ? raf3 | CDMA : raf3;
        int raf5 = (raf4 & EVDO) > 0 ? raf4 | EVDO : raf4;
        int raf6 = (raf5 & LTE) > 0 ? LTE | raf5 : raf5;
        return (raf6 & 524288) > 0 ? 524288 | raf6 : raf6;
    }

    public static int getNetworkTypeFromRaf(int raf) {
        switch (getAdjustedRaf(raf)) {
            case CDMA /*{ENCODED_INT: 72}*/:
                return 5;
            case EVDO /*{ENCODED_INT: 10288}*/:
                return 6;
            case 10360:
                return 4;
            case WCDMA /*{ENCODED_INT: 17284}*/:
                return 2;
            case GSM /*{ENCODED_INT: 32771}*/:
                return 1;
            case 32843:
                return MtkRILConstants.NETWORK_MODE_CDMA_GSM;
            case 43131:
                return MtkRILConstants.NETWORK_MODE_CDMA_EVDO_GSM;
            case 50055:
                return 0;
            case 60415:
                return 7;
            case RAF_TD_SCDMA /*{ENCODED_INT: 65536}*/:
                return 13;
            case 82820:
                return 14;
            case 98307:
                return 16;
            case 115591:
                return 18;
            case 125951:
                return 21;
            case LTE /*{ENCODED_INT: 266240}*/:
                return 11;
            case 276600:
                return 8;
            case 283524:
                return 12;
            case 299011:
                return 101;
            case 309371:
                return MtkRILConstants.NETWORK_MODE_LTE_CDMA_EVDO_GSM;
            case 316295:
                return 9;
            case 326655:
                return 10;
            case 331776:
                return 15;
            case 349060:
                return 19;
            case 364547:
                return 17;
            case 381831:
                return 20;
            case 392191:
                return 22;
            case 524288:
                return 23;
            case 790528:
                return 24;
            case 800888:
                return 25;
            case 807812:
                return 28;
            case 840583:
                return 26;
            case 850943:
                return 27;
            case 856064:
                return 29;
            case 873348:
                return 31;
            case 888835:
                return 30;
            case 906119:
                return 32;
            case 916479:
                return 33;
            default:
                return RILConstants.PREFERRED_NETWORK_MODE;
        }
    }
}
