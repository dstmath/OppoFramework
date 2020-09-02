package com.mediatek.internal.telephony.cdma.pluscode;

import android.os.Build;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import java.util.List;

public class PlusCodeToIddNddUtils {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    public static final String INTERNATIONAL_PREFIX_SYMBOL = "+";
    static final String LOG_TAG = "PlusCodeToIddNddUtils";
    private static final SparseIntArray MOBILE_NUMBER_SPEC_MAP = TelephonyPlusCode.MOBILE_NUMBER_SPEC_MAP;
    private static PlusCodeHpcdTable sHpcd = PlusCodeHpcdTable.getInstance();
    private static MccIddNddSid sMccIddNddSid = null;

    private static int getCdmaSubId() {
        int[] subId;
        TelephonyManager tm = TelephonyManager.getDefault();
        int simCount = tm.getSimCount();
        for (int i = 0; i < simCount; i++) {
            if (tm.getCurrentPhoneTypeForSlot(i) == 2 && (subId = SubscriptionManager.getSubId(i)) != null) {
                return subId[0];
            }
        }
        return -1;
    }

    public static boolean canFormatPlusToIddNdd() {
        Rlog.d(LOG_TAG, "-------------canFormatPlusToIddNdd-------------");
        int cdmaSubId = getCdmaSubId();
        TelephonyManager tm = TelephonyManager.getDefault();
        String value = tm.getNetworkOperator(cdmaSubId);
        Rlog.d(LOG_TAG, "cdmaSubId:" + cdmaSubId + ", network operator numeric:" + value);
        String mccStr = "";
        int length = value.length();
        boolean z = DBG;
        if (length == 7) {
            mccStr = value.substring(0, 4);
        } else if (value.length() >= 3) {
            mccStr = value.substring(0, 3);
        }
        String sidStr = "";
        try {
            ServiceState ss = tm.getServiceStateForSubscriber(cdmaSubId);
            if (ss != null) {
                sidStr = Integer.toString(ss.getCdmaSystemId());
            }
        } catch (NullPointerException ex) {
            Rlog.d(LOG_TAG, "canFormatPlusToIddNdd, NullPointerException:" + ex);
        }
        String ltmoffStr = TelephonyManager.getTelephonyProperty(SubscriptionManager.getPhoneId(cdmaSubId), TelephonyPlusCode.PROPERTY_TIME_LTMOFFSET, "");
        Rlog.d(LOG_TAG, "mcc = " + mccStr + ", sid = " + Rlog.pii(LOG_TAG, sidStr) + ", ltm_off = " + ltmoffStr);
        boolean find = DBG;
        sMccIddNddSid = null;
        if (sHpcd != null) {
            boolean isValid = !mccStr.startsWith("2134");
            Rlog.d(LOG_TAG, "[canFormatPlusToIddNdd] Mcc = " + mccStr + ", !Mcc.startsWith(2134) = " + isValid);
            if (TextUtils.isEmpty(mccStr) || !Character.isDigit(mccStr.charAt(0)) || mccStr.startsWith("000") || !isValid) {
                List<String> mccArray = PlusCodeHpcdTable.getMccFromConflictTableBySid(sidStr);
                if (mccArray == null || mccArray.size() == 0) {
                    Rlog.d(LOG_TAG, "[canFormatPlusToIddNdd] Do not find cc by SID from confilcts table, so from lookup table");
                    sMccIddNddSid = PlusCodeHpcdTable.getCcFromMINSTableBySid(sidStr);
                    Rlog.d(LOG_TAG, "[canFormatPlusToIddNdd] getCcFromMINSTableBySid mccIddNddSid = " + Rlog.pii(LOG_TAG, sMccIddNddSid));
                } else if (mccArray.size() >= 2) {
                    String findMcc = sHpcd.getCcFromMINSTableByLTM(mccArray, ltmoffStr);
                    if (!(findMcc == null || findMcc.length() == 0)) {
                        sMccIddNddSid = PlusCodeHpcdTable.getCcFromTableByMcc(findMcc);
                    }
                    Rlog.d(LOG_TAG, "[canFormatPlusToIddNdd] conflicts, getCcFromTableByMcc mccIddNddSid = " + Rlog.pii(LOG_TAG, sMccIddNddSid));
                } else if (mccArray.size() == 1) {
                    sMccIddNddSid = PlusCodeHpcdTable.getCcFromTableByMcc(mccArray.get(0));
                    Rlog.d(LOG_TAG, "[canFormatPlusToIddNdd] do not conflicts, getCcFromTableByMcc mccIddNddSid = " + Rlog.pii(LOG_TAG, sMccIddNddSid));
                }
                if (sMccIddNddSid != null) {
                    z = true;
                }
                find = z;
            } else {
                sMccIddNddSid = PlusCodeHpcdTable.getCcFromTableByMcc(mccStr);
                Rlog.d(LOG_TAG, "[canFormatPlusToIddNdd] getCcFromTableByMcc mccIddNddSid = " + Rlog.pii(LOG_TAG, sMccIddNddSid));
                if (sMccIddNddSid != null) {
                    z = true;
                }
                find = z;
            }
        }
        Rlog.d(LOG_TAG, "[canFormatPlusToIddNdd] find = " + find + ", mccIddNddSid = " + Rlog.pii(LOG_TAG, sMccIddNddSid));
        return find;
    }

    private static String formatPlusCode(String number) {
        String formatNumber = null;
        MccIddNddSid mccIddNddSid = sMccIddNddSid;
        if (mccIddNddSid != null) {
            String sCC = mccIddNddSid.mCc;
            Rlog.d(LOG_TAG, "number auto format correctly, mccIddNddSid = " + Rlog.pii(LOG_TAG, sMccIddNddSid.toString()));
            if (!number.startsWith(sCC)) {
                formatNumber = sMccIddNddSid.mIdd + number;
                if (DBG) {
                    Rlog.d(LOG_TAG, "CC dismatch, remove +(already erased before), add IDD formatNumber = " + Rlog.pii(LOG_TAG, formatNumber));
                }
            } else {
                String nddStr = sMccIddNddSid.mNdd;
                if (sMccIddNddSid.mCc.equals("86") || sMccIddNddSid.mCc.equals("853")) {
                    Rlog.d(LOG_TAG, "CC matched, cc is chinese");
                    nddStr = "00";
                } else {
                    number = number.substring(sCC.length(), number.length());
                    if (DBG) {
                        Rlog.d(LOG_TAG, "[isMobileNumber] number = " + Rlog.pii(LOG_TAG, number));
                    }
                    if (isMobileNumber(sCC, number)) {
                        Rlog.d(LOG_TAG, "CC matched, isMobile = true Ndd = ");
                        nddStr = "";
                    }
                }
                formatNumber = nddStr + number;
                if (DBG) {
                    Rlog.d(LOG_TAG, "CC matched, remove +(already erased before) and CC, add NDD formatNumber = " + Rlog.pii(LOG_TAG, formatNumber));
                }
            }
        }
        return formatNumber;
    }

    public static String replacePlusCodeWithIddNdd(String number) {
        if (DBG) {
            Rlog.d(LOG_TAG, "replacePlusCodeWithIddNdd number = " + Rlog.pii(LOG_TAG, number));
        }
        if (number == null || number.length() == 0 || !number.startsWith(INTERNATIONAL_PREFIX_SYMBOL)) {
            if (DBG) {
                Rlog.d(LOG_TAG, "number can't format correctly, number = " + Rlog.pii(LOG_TAG, number));
            }
            return null;
        }
        boolean bFind = canFormatPlusToIddNdd();
        if (!bFind) {
            return null;
        }
        if (number.startsWith(INTERNATIONAL_PREFIX_SYMBOL)) {
            if (DBG) {
                Rlog.d(LOG_TAG, "number before remove plus char , number = " + Rlog.pii(LOG_TAG, number));
            }
            number = number.substring(1, number.length());
            if (DBG) {
                Rlog.d(LOG_TAG, "number after   remove plus char , number = " + Rlog.pii(LOG_TAG, number));
            }
        }
        if (bFind) {
            return formatPlusCode(number);
        }
        return null;
    }

    private static boolean isMobileNumber(String sCC, String number) {
        if (DBG) {
            Rlog.d(LOG_TAG, "[isMobileNumber] number = " + Rlog.pii(LOG_TAG, number) + ", sCC = " + sCC);
        }
        if (number == null || number.length() == 0) {
            Rlog.d(LOG_TAG, "[isMobileNumber] please check the param ");
            return DBG;
        }
        SparseIntArray sparseIntArray = MOBILE_NUMBER_SPEC_MAP;
        if (sparseIntArray == null) {
            Rlog.d(LOG_TAG, "[isMobileNumber] MOBILE_NUMBER_SPEC_MAP == null ");
            return DBG;
        }
        int size = sparseIntArray.size();
        try {
            int iCC = Integer.parseInt(sCC);
            Rlog.d(LOG_TAG, "[isMobileNumber] iCC = " + iCC);
            for (int i = 0; i < size; i++) {
                Rlog.d(LOG_TAG, "[isMobileNumber] value = " + MOBILE_NUMBER_SPEC_MAP.valueAt(i) + ", key =  " + MOBILE_NUMBER_SPEC_MAP.keyAt(i));
                if (MOBILE_NUMBER_SPEC_MAP.valueAt(i) == iCC) {
                    Rlog.d(LOG_TAG, "[isMobileNumber]  value = icc");
                    String prfix = Integer.toString(MOBILE_NUMBER_SPEC_MAP.keyAt(i));
                    Rlog.d(LOG_TAG, "[isMobileNumber]  prfix = " + prfix);
                    if (number.startsWith(prfix)) {
                        Rlog.d(LOG_TAG, "[isMobileNumber]  number.startsWith(prfix) = true");
                        return true;
                    }
                }
            }
            return DBG;
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e));
            return DBG;
        }
    }

    public static String removeIddNddAddPlusCode(String number) {
        if (DBG) {
            Rlog.d(LOG_TAG, "[removeIddNddAddPlusCode] befor format number = " + Rlog.pii(LOG_TAG, number));
        }
        if (number == null || number.length() == 0) {
            Rlog.d(LOG_TAG, "[removeIddNddAddPlusCode] please check the param ");
            return number;
        }
        String formatNumber = number;
        if (!number.startsWith(INTERNATIONAL_PREFIX_SYMBOL)) {
            if (!canFormatPlusToIddNdd()) {
                Rlog.d(LOG_TAG, "[removeIddNddAddPlusCode] find no operator that match the MCC ");
                return number;
            }
            MccIddNddSid mccIddNddSid = sMccIddNddSid;
            if (mccIddNddSid != null) {
                String strIdd = mccIddNddSid.mIdd;
                Rlog.d(LOG_TAG, "[removeIddNddAddPlusCode] find match the cc, Idd = " + strIdd);
                if (number.startsWith(strIdd) && number.length() > strIdd.length()) {
                    formatNumber = INTERNATIONAL_PREFIX_SYMBOL + number.substring(strIdd.length(), number.length());
                }
            }
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "[removeIddNddAddPlusCode] number after format = " + Rlog.pii(LOG_TAG, formatNumber));
        }
        return formatNumber;
    }

    public static boolean canFormatPlusCodeForSms() {
        String mcc = SystemProperties.get(TelephonyPlusCode.PROPERTY_ICC_CDMA_OPERATOR_MCC, "");
        if (DBG) {
            Rlog.d(LOG_TAG, "[canFormatPlusCodeForSms] Mcc = " + mcc);
        }
        sMccIddNddSid = null;
        if (sHpcd == null) {
            return DBG;
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "[canFormatPlusCodeForSms] Mcc = " + mcc);
        }
        if (mcc == null || mcc.length() == 0) {
            return DBG;
        }
        sMccIddNddSid = PlusCodeHpcdTable.getCcFromTableByMcc(mcc);
        if (DBG) {
            Rlog.d(LOG_TAG, "[canFormatPlusCodeForSms] getCcFromTableByMcc mccIddNddSid = " + Rlog.pii(LOG_TAG, sMccIddNddSid));
        }
        return sMccIddNddSid != null ? true : DBG;
    }

    public static String replacePlusCodeForSms(String number) {
        if (DBG) {
            Rlog.d(LOG_TAG, "replacePlusCodeForSms number = " + Rlog.pii(LOG_TAG, number));
        }
        if (number == null || number.length() == 0 || !number.startsWith(INTERNATIONAL_PREFIX_SYMBOL)) {
            if (DBG) {
                Rlog.d(LOG_TAG, "number can't format correctly, number = " + Rlog.pii(LOG_TAG, number));
            }
            return null;
        }
        boolean camFormat = canFormatPlusCodeForSms();
        if (!camFormat) {
            return null;
        }
        if (number.startsWith(INTERNATIONAL_PREFIX_SYMBOL)) {
            if (DBG) {
                Rlog.d(LOG_TAG, "number before remove plus char , number = " + Rlog.pii(LOG_TAG, number));
            }
            number = number.substring(1, number.length());
            if (DBG) {
                Rlog.d(LOG_TAG, "number after   remove plus char , number = " + Rlog.pii(LOG_TAG, number));
            }
        }
        if (camFormat) {
            return formatPlusCode(number);
        }
        return null;
    }

    public static String removeIddNddAddPlusCodeForSms(String number) {
        if (DBG) {
            Rlog.d(LOG_TAG, "[removeIddNddAddPlusCodeForSms] befor format number = " + Rlog.pii(LOG_TAG, number));
        }
        if (number == null || number.length() == 0) {
            Rlog.d(LOG_TAG, "[removeIddNddAddPlusCodeForSms] please check the param ");
            return number;
        }
        String formatNumber = number;
        if (!number.startsWith(INTERNATIONAL_PREFIX_SYMBOL)) {
            if (!canFormatPlusCodeForSms()) {
                Rlog.d(LOG_TAG, "[removeIddNddAddPlusCodeForSms] find no operator that match the MCC ");
                return formatNumber;
            }
            MccIddNddSid mccIddNddSid = sMccIddNddSid;
            if (mccIddNddSid != null) {
                String strIdd = mccIddNddSid.mIdd;
                if (DBG) {
                    Rlog.d(LOG_TAG, "[removeIddNddAddPlusCodeForSms] find match the cc, Idd = " + strIdd);
                }
                if (number.startsWith(strIdd) && number.length() > strIdd.length()) {
                    String number2 = number.substring(strIdd.length(), number.length());
                    if (DBG) {
                        Rlog.d(LOG_TAG, "[removeIddNddAddPlusCodeForSms] sub num = " + Rlog.pii(LOG_TAG, number2));
                    }
                    formatNumber = INTERNATIONAL_PREFIX_SYMBOL + number2;
                }
            }
        }
        if (DBG) {
            Rlog.d(LOG_TAG, "[removeIddNddAddPlusCodeForSms] number after format = " + Rlog.pii(LOG_TAG, formatNumber));
        }
        return formatNumber;
    }

    public static String checkMccBySidLtmOff(String mccMnc) {
        String tempMcc;
        Rlog.d(LOG_TAG, "[checkMccBySidLtmOff] mccMnc = " + mccMnc);
        int cdmaSubId = getCdmaSubId();
        String strSid = "";
        try {
            ServiceState ss = TelephonyManager.getDefault().getServiceStateForSubscriber(cdmaSubId);
            if (ss != null) {
                strSid = Integer.toString(ss.getCdmaSystemId());
            }
        } catch (NullPointerException ex) {
            Rlog.d(LOG_TAG, "checkMccBySidLtmOff, NullPointerException:" + ex);
        }
        String strLtmOff = TelephonyManager.getTelephonyProperty(SubscriptionManager.getPhoneId(cdmaSubId), TelephonyPlusCode.PROPERTY_TIME_LTMOFFSET, "");
        Rlog.d(LOG_TAG, "[checkMccBySidLtmOff] Sid = " + Rlog.pii(LOG_TAG, strSid) + ", Ltm_off = " + strLtmOff);
        String strMcc = PlusCodeHpcdTable.getMccFromConflictTableBySidLtmOff(strSid, strLtmOff);
        StringBuilder sb = new StringBuilder();
        sb.append("[checkMccBySidLtmOff] MccFromConflictTable = ");
        sb.append(strMcc);
        Rlog.d(LOG_TAG, sb.toString());
        if (strMcc != null) {
            tempMcc = strMcc;
        } else {
            String strMcc2 = PlusCodeHpcdTable.getMccFromMINSTableBySid(strSid);
            Rlog.d(LOG_TAG, "[checkMccBySidLtmOff] MccFromMINSTable = " + strMcc2);
            if (strMcc2 != null) {
                tempMcc = strMcc2;
            } else {
                tempMcc = mccMnc;
            }
        }
        Rlog.d(LOG_TAG, "[checkMccBySidLtmOff] tempMcc = " + tempMcc);
        if (!tempMcc.startsWith("310") && !tempMcc.startsWith("311") && !tempMcc.startsWith("312")) {
            return tempMcc;
        }
        String strMccMnc = PlusCodeHpcdTable.getMccMncFromSidMccMncListBySid(strSid);
        Rlog.d(LOG_TAG, "[checkMccBySidLtmOff] MccMnc = " + strMccMnc);
        return strMccMnc != null ? strMccMnc : tempMcc;
    }
}
