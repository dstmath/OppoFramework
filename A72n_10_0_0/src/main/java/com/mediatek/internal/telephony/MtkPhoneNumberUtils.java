package com.mediatek.internal.telephony;

import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils;
import com.mediatek.internal.telephony.cdma.pluscode.PlusCodeProcessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mediatek.telephony.MtkSignalStrength;

public class MtkPhoneNumberUtils {
    private static final String[] CF_HEADERS = {"*72", "*720", "*90", "*900", "*92", "*920", "*68", "*680", "*730"};
    private static final String CLIR_OFF = "#31#";
    private static final String CLIR_ON = "*31#";
    public static final int FORMAT_JAPAN = 2;
    public static final int FORMAT_NANP = 1;
    public static final int FORMAT_UNKNOWN = 0;
    static final String LOG_TAG = "MtkPhoneNumberUtils";
    private static final int MIN_MATCH = 7;
    private static final int MIN_MATCH_CTA = 11;
    private static final String[] NANP_COUNTRIES = {"US", "CA", "AS", "AI", "AG", "BS", "BB", "BM", "VG", "KY", "DM", "DO", "GD", "GU", "JM", "PR", "MS", "MP", "KN", "LC", "VC", "TT", "TC", "VI"};
    public static final char PAUSE = ',';
    private static final char PLUS_SIGN_CHAR = '+';
    private static final String PLUS_SIGN_STRING = "+";
    private static final String SC_CFB = "67";
    private static final String SC_CFNR = "62";
    private static final String SC_CFNRy = "61";
    private static final String SC_CFU = "21";
    private static final String SC_CF_All = "002";
    private static final String SC_CF_All_Conditional = "004";
    public static final int TOA_International = 145;
    public static final int TOA_Unknown = 129;
    private static final boolean VDBG = false;
    public static final char WAIT = ';';
    public static final char WILD = 'N';
    private static IPlusCodeUtils sPlusCodeUtils = null;

    static {
        initialize();
    }

    public static byte[] numberToCalledPartyBCD(String number) {
        return PhoneNumberUtils.numberToCalledPartyBCD(number);
    }

    public static String calledPartyBCDFragmentToString(byte[] bytes, int offset, int length) {
        return PhoneNumberUtils.calledPartyBCDFragmentToString(bytes, offset, length);
    }

    public static String calledPartyBCDToString(byte[] bytes, int offset, int length) {
        return PhoneNumberUtils.calledPartyBCDToString(bytes, offset, length);
    }

    public static String stripSeparators(String phoneNumber) {
        return PhoneNumberUtils.stripSeparators(phoneNumber);
    }

    public static String extractNetworkPortion(String phoneNumber) {
        return PhoneNumberUtils.extractNetworkPortion(phoneNumber);
    }

    public static String stringFromStringAndTOA(String s, int TOA) {
        return PhoneNumberUtils.stringFromStringAndTOA(s, TOA);
    }

    public static String convertPreDial(String phoneNumber) {
        return PhoneNumberUtils.convertPreDial(phoneNumber);
    }

    public static boolean isNonSeparator(String address) {
        int count = address.length();
        for (int i = 0; i < count; i++) {
            if (!PhoneNumberUtils.isNonSeparator(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int getFormatTypeFromCountryCode(String country) {
        int length = NANP_COUNTRIES.length;
        for (int i = 0; i < length; i++) {
            if (NANP_COUNTRIES[i].compareToIgnoreCase(country) == 0) {
                return 1;
            }
        }
        if ("jp".compareToIgnoreCase(country) == 0) {
            return 2;
        }
        return 0;
    }

    private static int findDialableIndexFromPostDialStr(String postDialStr) {
        for (int index = 0; index < postDialStr.length(); index++) {
            if (PhoneNumberUtils.isReallyDialable(postDialStr.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    /* JADX INFO: Multiple debug info for r0v2 java.lang.String: [D('nonDigitStr' java.lang.String), D('retStr' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v5 java.lang.String: [D('ret' java.lang.StringBuilder), D('retStr' java.lang.String)] */
    private static String appendPwCharBackToOrigDialStr(int dialableIndex, String origStr, String dialStr) {
        if (dialableIndex == 1) {
            return origStr + dialStr.charAt(0);
        }
        return origStr.concat(dialStr.substring(0, dialableIndex));
    }

    public static boolean isEmergencyNumber(String number) {
        return PhoneNumberUtils.isEmergencyNumber(number);
    }

    public static boolean isEmergencyNumber(int subId, String number) {
        return PhoneNumberUtils.isEmergencyNumber(subId, number);
    }

    private static void initialize() {
        sPlusCodeUtils = PlusCodeProcessor.getPlusCodeUtils();
    }

    public static String cdmaCheckAndProcessPlusCode(String dialStr) {
        String result = preProcessPlusCode(dialStr);
        if (result != null && !result.equals(dialStr)) {
            return result;
        }
        if (!TextUtils.isEmpty(dialStr) && PhoneNumberUtils.isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (!TextUtils.isEmpty(currIso) && !TextUtils.isEmpty(defaultIso)) {
                return PhoneNumberUtils.cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, getFormatTypeFromCountryCode(currIso), getFormatTypeFromCountryCode(defaultIso));
            }
        }
        return dialStr;
    }

    public static String cdmaCheckAndProcessPlusCodeForSms(String dialStr) {
        String result = preProcessPlusCodeForSms(dialStr);
        if (result != null && !result.equals(dialStr)) {
            return result;
        }
        if (!TextUtils.isEmpty(dialStr) && PhoneNumberUtils.isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (!TextUtils.isEmpty(defaultIso)) {
                int format = getFormatTypeFromCountryCode(defaultIso);
                return PhoneNumberUtils.cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, format, format);
            }
        }
        return dialStr;
    }

    public static String extractCLIRPortion(String phoneNumber) {
        String strDialNumber;
        if (phoneNumber == null) {
            return null;
        }
        Matcher m = Pattern.compile("^([*][#]|[*]{1,2}|[#]{1,2})([0-9]{2,3})([*])([+]?[0-9]+)(.*)(#)$").matcher(phoneNumber);
        if (m.matches()) {
            return m.group(4);
        }
        if (phoneNumber.startsWith(CLIR_ON) || phoneNumber.startsWith(CLIR_OFF)) {
            vlog(phoneNumber + " Start with *31# or #31#, return " + phoneNumber.substring(4));
            return phoneNumber.substring(4);
        }
        if (phoneNumber.indexOf(PLUS_SIGN_STRING) != -1 && phoneNumber.indexOf(PLUS_SIGN_STRING) == phoneNumber.lastIndexOf(PLUS_SIGN_STRING)) {
            Matcher m2 = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(phoneNumber);
            if (!m2.matches()) {
                Matcher m3 = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(phoneNumber);
                if (m3.matches() && (strDialNumber = m3.group(4)) != null && strDialNumber.length() > 1 && strDialNumber.charAt(0) == '+') {
                    vlog(phoneNumber + " matcher pattern2, return " + strDialNumber);
                    return strDialNumber;
                }
            } else if ("".equals(m2.group(2))) {
                vlog(phoneNumber + " matcher pattern1, return empty string.");
                return "";
            } else {
                String strDialNumber2 = m2.group(4);
                if (strDialNumber2 != null && strDialNumber2.length() > 1 && strDialNumber2.charAt(0) == '+') {
                    vlog(phoneNumber + " matcher pattern1, return " + strDialNumber2);
                    return strDialNumber2;
                }
            }
        }
        return phoneNumber;
    }

    public static String prependPlusToNumber(String number) {
        StringBuilder ret;
        String retString = number.toString();
        Matcher m = Pattern.compile("^([*][#]|[*]{1,2}|[#]{1,2})([0-9]{2,3})([*])([0-9]+)(.*)(#)$").matcher(retString);
        if (m.matches()) {
            ret = new StringBuilder();
            ret.append(m.group(1));
            ret.append(m.group(2));
            ret.append(m.group(3));
            ret.append(PLUS_SIGN_STRING);
            ret.append(m.group(4));
            ret.append(m.group(5));
            ret.append(m.group(6));
        } else {
            Matcher m2 = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(retString);
            if (!m2.matches()) {
                Matcher m3 = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(retString);
                if (m3.matches()) {
                    ret = new StringBuilder();
                    ret.append(m3.group(1));
                    ret.append(m3.group(2));
                    ret.append(m3.group(3));
                    ret.append(PLUS_SIGN_STRING);
                    ret.append(m3.group(4));
                } else {
                    ret = new StringBuilder();
                    ret.append(PLUS_SIGN_CHAR);
                    ret.append(retString);
                }
            } else if ("".equals(m2.group(2))) {
                ret = new StringBuilder();
                ret.append(m2.group(1));
                ret.append(m2.group(3));
                ret.append(m2.group(4));
                ret.append(m2.group(5));
                ret.append(PLUS_SIGN_STRING);
            } else {
                ret = new StringBuilder();
                ret.append(m2.group(1));
                ret.append(m2.group(2));
                ret.append(m2.group(3));
                ret.append(PLUS_SIGN_STRING);
                ret.append(m2.group(4));
                ret.append(m2.group(5));
            }
        }
        return ret.toString();
    }

    private static String preProcessPlusCode(String dialStr) {
        if (TextUtils.isEmpty(dialStr) || !PhoneNumberUtils.isReallyDialable(dialStr.charAt(0)) || !isNonSeparator(dialStr)) {
            return dialStr;
        }
        String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
        String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
        boolean needToFormat = true;
        if (!TextUtils.isEmpty(currIso) && !TextUtils.isEmpty(defaultIso)) {
            int currFormat = getFormatTypeFromCountryCode(currIso);
            needToFormat = (currFormat == getFormatTypeFromCountryCode(defaultIso) && currFormat == 1) ? false : true;
        }
        if (needToFormat) {
            vlog("preProcessPlusCode, before format number:" + dialStr);
            String retStr = dialStr;
            if (dialStr.lastIndexOf(PLUS_SIGN_STRING) != -1) {
                String tempDialStr = dialStr;
                retStr = null;
                do {
                    String networkDialStr = PhoneNumberUtils.extractNetworkPortionAlt(tempDialStr);
                    if (networkDialStr != null && networkDialStr.charAt(0) == '+' && networkDialStr.length() > 1) {
                        if (sPlusCodeUtils.canFormatPlusToIddNdd()) {
                            networkDialStr = sPlusCodeUtils.replacePlusCodeWithIddNdd(networkDialStr);
                        } else {
                            dlog("preProcessPlusCode, can't format plus code.");
                            return dialStr;
                        }
                    }
                    vlog("preProcessPlusCode, networkDialStr:" + networkDialStr);
                    if (!TextUtils.isEmpty(networkDialStr)) {
                        if (retStr == null) {
                            retStr = networkDialStr;
                        } else {
                            retStr = retStr.concat(networkDialStr);
                        }
                        String postDialStr = PhoneNumberUtils.extractPostDialPortion(tempDialStr);
                        if (!TextUtils.isEmpty(postDialStr)) {
                            int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);
                            if (dialableIndex >= 1) {
                                retStr = appendPwCharBackToOrigDialStr(dialableIndex, retStr, postDialStr);
                                tempDialStr = postDialStr.substring(dialableIndex);
                            } else {
                                if (dialableIndex < 0) {
                                    postDialStr = "";
                                }
                                Rlog.e(LOG_TAG, "preProcessPlusCode, wrong postDialStr:" + postDialStr);
                            }
                        }
                        vlog("preProcessPlusCode, postDialStr:" + postDialStr + ", tempDialStr:" + tempDialStr);
                        if (TextUtils.isEmpty(postDialStr)) {
                            break;
                        }
                    } else {
                        Rlog.e(LOG_TAG, "preProcessPlusCode, null newDialStr:" + networkDialStr);
                        return dialStr;
                    }
                } while (!TextUtils.isEmpty(tempDialStr));
            }
            vlog("preProcessPlusCode, after format number:" + retStr);
            return retStr;
        }
        dlog("preProcessPlusCode, no need format, currIso:" + currIso + ", defaultIso:" + defaultIso);
        return dialStr;
    }

    private static String preProcessPlusCodeForSms(String dialStr) {
        dlog("preProcessPlusCodeForSms ENTER.");
        if (TextUtils.isEmpty(dialStr) || !dialStr.startsWith(PLUS_SIGN_STRING) || !PhoneNumberUtils.isReallyDialable(dialStr.charAt(0)) || !isNonSeparator(dialStr) || getFormatTypeFromCountryCode(TelephonyManager.getDefault().getSimCountryIso()) == 1 || !sPlusCodeUtils.canFormatPlusCodeForSms()) {
            return dialStr;
        }
        String retAddr = sPlusCodeUtils.replacePlusCodeForSms(dialStr);
        if (TextUtils.isEmpty(retAddr)) {
            dlog("preProcessPlusCodeForSms, can't handle the plus code by PlusCodeUtils");
            return dialStr;
        }
        vlog("preProcessPlusCodeForSms, new dialStr = " + retAddr);
        return retAddr;
    }

    public static boolean isEmergencyNumberExt(int subId, String number, String defaultCountryIso, boolean useExactMatch) {
        log("[isEmergencyNumberExt] Warning: Deprecated!");
        return PhoneNumberUtils.isEmergencyNumber(subId, number);
    }

    public static int getMinMatch() {
        boolean isCtaSupport = "1".equals(SystemProperties.get("ro.vendor.mtk_cta_support"));
        if (("OP09".equals(SystemProperties.get(MtkSignalStrength.PROPERTY_OPERATOR_OPTR)) && ("SEGDEFAULT".equals(SystemProperties.get("persist.vendor.operator.seg")) || "SEGC".equals(SystemProperties.get("persist.vendor.operator.seg")))) || isCtaSupport) {
            vlog("[DBG] getMinMatch return 11 for CTA/OP09");
            return 11;
        }
        vlog("[DBG] getMinMatch return 7");
        return 7;
    }

    public static boolean isCallForwardCode(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        if (!PhoneNumberUtils.isUriNumber(number)) {
            PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.stripSeparators(number));
        }
        if (isGsmCallForwardCode(number) || isCdmaCallForwardCode(number)) {
            return true;
        }
        return false;
    }

    private static boolean isGsmCallForwardCode(String number) {
        boolean isCf = false;
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        Matcher m = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*))?)?)?)?#)(.*)").matcher(number);
        if (!m.matches()) {
            return false;
        }
        String sc = m.group(3);
        if (sc != null && sc.length() == 0) {
            sc = null;
        }
        if (sc != null && (sc.equals(SC_CFU) || sc.equals(SC_CFB) || sc.equals(SC_CFNRy) || sc.equals(SC_CFNR) || sc.equals(SC_CF_All) || sc.equals(SC_CF_All_Conditional))) {
            isCf = true;
        }
        log("[isGsmCallForwardCode] sc = " + sc + "isCf = " + isCf);
        return isCf;
    }

    private static boolean isCdmaCallForwardCode(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        for (String header : CF_HEADERS) {
            if (number.indexOf(header) == 0) {
                log("[isCdmaCallForwardCode] This is cdma call forward code");
                return true;
            }
        }
        return false;
    }

    private static void log(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    private static void dlog(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static void vlog(String msg) {
    }
}
