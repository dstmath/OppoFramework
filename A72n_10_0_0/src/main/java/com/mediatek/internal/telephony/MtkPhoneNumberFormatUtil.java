package com.mediatek.internal.telephony;

import android.telephony.MtkRadioAccessFamily;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import com.mediatek.provider.MtkContactsContract;
import java.util.Arrays;
import java.util.Locale;
import mediatek.telephony.MtkServiceState;

public class MtkPhoneNumberFormatUtil {
    public static final String[] AUSTRALIA_INTERNATIONAL_PREFIXS = {"0011", "0014", "0015", "0016", "0018", "0019"};
    public static final String[] BRAZIL_INTERNATIONAL_PREFIXS = {"0012", "0014", "0015", "0021", "0023", "0025", "0031", "0041"};
    public static final boolean DEBUG = false;
    public static final int FORMAT_AUSTRALIA = 21;
    public static final int FORMAT_BRAZIL = 23;
    public static final int FORMAT_CHINA_HONGKONG = 4;
    public static final int FORMAT_CHINA_MACAU = 5;
    public static final int FORMAT_CHINA_MAINLAND = 3;
    public static String[] FORMAT_COUNTRY_CODES = {"1", "81", "86", "852", "853", "886", "44", "33", "39", "49", "7", "91", "34", "60", "65", "62", "66", "84", "351", "48", "61", "64", "55", "90"};
    public static final String[] FORMAT_COUNTRY_NAMES = {"US", "JP", "CN", "HK", "MO", "TW", "GB", "FR", "IT", "DE", "RU", "IN", "ES", "MY", "SG", "ID", "TH", "VN", "PT", "PL", "AU", "NZ", "BR", "TR"};
    public static final int FORMAT_ENGLAND = 7;
    public static final int FORMAT_FRANCE = 8;
    public static final int FORMAT_GERMANY = 10;
    public static final int FORMAT_INDIA = 12;
    public static final int FORMAT_INDONESIA = 16;
    public static final int FORMAT_ITALY = 9;
    public static final int FORMAT_JAPAN = 2;
    public static final int FORMAT_MALAYSIA = 14;
    public static final int FORMAT_NANP = 1;
    public static final int FORMAT_NEW_ZEALAND = 22;
    public static final int FORMAT_POLAND = 20;
    public static final int FORMAT_PORTUGAL = 19;
    public static final int FORMAT_RUSSIAN = 11;
    public static final int FORMAT_SINGAPORE = 15;
    public static final int FORMAT_SPAIN = 13;
    public static final int FORMAT_TAIWAN = 6;
    public static final int FORMAT_THAILAND = 17;
    public static final int FORMAT_TURKEY = 24;
    public static final int FORMAT_UNKNOWN = 0;
    public static final int FORMAT_VIETNAM = 18;
    public static final String[] FRANCE_INTERNATIONAL_PREFIXS = {"00", "40", "50", "70", "90"};
    private static final int[] Germany_FOUR_PART_REGION_CODES = {3301, 3302, 3303, 3304, 3306, 3307, 3321, 3322, 3327, 3328, 3329, 3331, 3332, 3334, 3335, 3337, 3338, 3341, 3342, 3344, 3346, 3361, 3362, 3364, 3366, 3371, 3372, 3375, 3377, 3378, 3379, 3381, 3382, 3385, 3386, 3391, 3394, 3395, 3421, 3423, 3425, 3431, 3433, 3435, 3437, 3441, 3443, 3445, 3447, 3448, 3461, 3462, 3464, 3466, 3471, 3473, 3475, 3476, 3491, 3493, 3494, 3496, 3501, 3504, 3521, 3522, 3523, 3525, 3528, 3529, 3531, 3533, 3537, 3541, 3542, 3544, 3546, 3561, 3562, 3563, 3564, 3571, 3573, 3574, 3576, 3578, 3581, 3583, 3585, 3586, 3588, 3591, 3592, 3594, 3596, 3601, 3603, 3605, 3606, 3621, 3622, 3623, 3624, 3626, 3627, 3628, 3629, 3631, 3632, 3634, 3635, 3636, 3641, 3643, 3644, 3647, 3661, 3663, 3671, 3672, 3675, 3677, 3679, 3680, 3681, 3682, 3683, 3685, 3686, 3691, 3693, 3695, 3721, 3722, 3723, 3724, 3725, 3726, 3727, 3731, 3733, 3735, 3737, 3741, 3744, 3745, 3761, 3762, 3763, 3764, 3765, 3771, 3772, 3773, 3774, 3821, 3831, 3834, 3838, 3841, 3843, 3844, 3847, 3871, 3874, 3876, 3877, 3881, 3883, 3886, 3901, 3921, 3923, 3925, 3928, 3931, 3933, 3935, 3937, 3941, 3942, 3943, 3944, 3946, 3947, 3949, 3961, 3962, 3963, 3964, 3965, 3966, 3967, 3968, 3969, 3971, 3973, 3976, 3981, 3984, 3991, 3994, 3996, 3997};
    private static final int[] Germany_THREE_PART_REGION_CODES = {202, 203, 208, 209, 212, 214, 221, 228, 234, 249, 310, 335, 340, 345, 365, 375, 385, 395, 457, 458, 459, 700, 709, 710, 728, 729, 749, 759, 769, 778, 779, 786, 787, 788, 789, 792, 798, 799, 800, 872, 875, 879, 900, 902, 903, 906};
    public static final String[] HONGKONG_INTERNATIONAL_PREFIXS = {"001", "0080", "0082", "009"};
    private static final int[] INDIA_THREE_DIGIG_AREA_CODES = {120, 121, 122, 124, 129, MtkServiceState.RIL_RADIO_TECHNOLOGY_HSDPAP_UPA, MtkServiceState.RIL_RADIO_TECHNOLOGY_HSUPAP, MtkServiceState.RIL_RADIO_TECHNOLOGY_HSUPAP_DPA, MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_HSDPAP, 141, 144, MtkPhoneNumberUtils.TOA_International, 151, 154, 160, 161, 164, 171, 172, 175, 177, 180, 181, 183, 184, 186, 191, 194, 212, 215, 217, 230, 231, 233, 240, 241, 250, 251, 253, 257, 260, 261, 265, 268, 278, 281, 285, 286, 288, 291, 294, 326, 341, 342, 343, 353, 354, 360, 361, 364, 368, 369, 370, 372, 373, 374, 376, 381, 385, 389, 413, 416, 421, 422, 423, 424, 427, 431, 435, 451, 452, 461, 462, 468, 469, 470, 471, 474, 475, 476, 477, 478, 479, 480, 481, 483, 484, 485, 487, 490, 491, 494, 495, 496, 497, MtkRadioAccessFamily.RAF_HSPA, 515, 522, 532, 535, 542, 548, 551, 562, 565, 571, 581, 591, 595, 612, 621, 631, 641, 651, 657, 661, 663, 671, 674, 680, 712, 721, 724, 731, 733, 734, 744, 747, 751, 755, 761, 771, 788, 816, 820, 821, 824, 831, 832, 836, 861, 863, 866, 870, 877, 878, 883, 884, 891};
    public static final String[] INDONESIA_INTERNATIONAL_PREFIXS = {"001", "007", "008", "009"};
    private static final int[] ITALY_MOBILE_PREFIXS = {328, 329, 330, 333, 334, 335, 336, 337, 338, 339, 347, 348, 349, 360, 368, 380, 388, 389};
    public static final String[] JAPAN_INTERNATIONAL_PREFIXS = {"010", "001", "0041", "0061"};
    private static final String[] NANP_COUNTRIES = {"US", "CA", "AS", "AI", "AG", "BS", "BB", "BM", "VG", "KY", "DM", "DO", "GD", "GU", "JM", "PR", "MS", "MP", "KN", "LC", "VC", "TT", "TC", "VI"};
    public static final String[] NANP_INTERNATIONAL_PREFIXS = {"011"};
    public static final String[] SINGAPORE_INTERNATIONAL_PREFIXS = {"001", "002", "008", "012", "013", "018", "019"};
    public static final String TAG = "MtkPhoneNumberFormatUtil";
    public static final String[] TAIWAN_INTERNATIONAL_PREFIXS = {"002", "005", "006", "007", "009", "019"};
    public static final String[] THAILAND_INTERNATIONAL_PREFIXS = {"001", "004", "005", "006", "007", "008", "009"};

    public static int getFormatTypeForLocale(Locale locale) {
        String simIso = getDefaultSimCountryIso();
        log("getFormatTypeForLocale Get sim sio:" + simIso);
        return getFormatTypeFromCountryCode(simIso);
    }

    static String getDefaultSimCountryIso() {
        int simId = 0;
        if (TelephonyManager.getDefault().hasIccCard(0)) {
            simId = 0;
        } else if (TelephonyManager.getDefault().hasIccCard(1)) {
            simId = 1;
        } else if (TelephonyManager.getDefault().hasIccCard(2)) {
            simId = 2;
        } else if (TelephonyManager.getDefault().hasIccCard(3)) {
            simId = 3;
        }
        int[] subId = SubscriptionManager.getSubId(simId);
        if (subId == null || subId.length <= 0) {
            return null;
        }
        return TelephonyManager.getDefault().getSimCountryIso(subId[0]);
    }

    private static int getFormatTypeFromCountryCodeInternal(String country) {
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

    public static int getFormatTypeFromCountryCode(String country) {
        int type = 0;
        if (!(country == null || country.length() == 0 || (type = getFormatTypeFromCountryCodeInternal(country)) != 0)) {
            int index = 0;
            String[] strArr = FORMAT_COUNTRY_NAMES;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                index++;
                if (strArr[i].compareToIgnoreCase(country) == 0) {
                    type = index;
                    break;
                }
                i++;
            }
            if (type == 0 && "UK".compareToIgnoreCase(country) == 0) {
                type = 7;
            }
        }
        log("Get Format Type:" + type);
        return type;
    }

    public static String formatNumber(String source) {
        return formatNumber(source, getFormatTypeForLocale(Locale.getDefault()));
    }

    public static void formatNumber(Editable text, int defaultFormattingType) {
        String result = formatNumber(text.toString(), defaultFormattingType);
        if (!(result == null || result.equals(text.toString()))) {
            int oldIndex = Selection.getSelectionStart(text);
            int digitCount = oldIndex;
            for (int i = 0; i < oldIndex; i++) {
                char c = text.charAt(i);
                if (c == ' ' || c == '-') {
                    digitCount--;
                }
            }
            text.replace(0, text.length(), result);
            int count = 0;
            int i2 = 0;
            while (i2 < text.length() && count < digitCount) {
                char c2 = text.charAt(i2);
                if (!(c2 == ' ' || c2 == '-')) {
                    count++;
                }
                i2++;
            }
            Selection.setSelection(text, i2);
        }
    }

    static boolean checkInputNormalNumber(CharSequence text) {
        for (int index = 0; index < text.length(); index++) {
            char c = text.charAt(index);
            if (!((c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == ' ' || c == '-')) {
                return false;
            }
        }
        return true;
    }

    public static String formatNumber(String text, int defaultFormattingType) {
        log("MTK Format Number:" + text + " " + defaultFormattingType);
        if (!checkInputNormalNumber(text)) {
            log("Abnormal Number:" + text + ", do nothing.");
            return text;
        }
        String text2 = removeAllDash(new StringBuilder(text));
        int formatType = defaultFormattingType == 0 ? 1 : defaultFormattingType;
        if (text2.length() > 2 && text2.charAt(0) == '+') {
            if (text2.charAt(1) == '1') {
                formatType = 1;
            } else if (text2.length() >= 3 && text2.charAt(1) == '8' && text2.charAt(2) == '1') {
                formatType = 2;
            } else if (formatType == 1 || formatType == 2) {
                return mtkFormatNumber(text2, formatType);
            }
        }
        log("formatNumber:" + formatType);
        if (formatType == 1 || formatType == 2) {
            return PhoneNumberUtils.formatNumber(text2, formatType);
        }
        return mtkFormatNumber(text2, formatType);
    }

    static String mtkFormatNumber(String text, int defaultFormatType) {
        log("MTK Format Number:" + text + " " + defaultFormatType);
        int length = text.length();
        if (length < 6) {
            return text;
        }
        if (text.contains("*") || text.contains("#") || text.contains("@")) {
            return removeAllDash(new StringBuilder(text));
        }
        int formatType = defaultFormatType;
        int[] match = getFormatTypeFromNumber(text, defaultFormatType);
        int startIndex = 0;
        if (!(match == null || match[1] == 0)) {
            formatType = match[1];
            startIndex = match[0];
        }
        if (length < startIndex + 4 || length > startIndex + 15) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        int blankPosition = removeAllDashAndFormatBlank(sb, startIndex);
        if (sb.length() < startIndex + 4 || (sb.length() == startIndex + 4 && sb.charAt(blankPosition + 1) == '0')) {
            return sb.toString();
        }
        switch (formatType) {
            case 1:
                if (blankPosition >= 0) {
                    SpannableStringBuilder ssb = new SpannableStringBuilder(sb.substring(startIndex + 1));
                    PhoneNumberUtils.formatNanpNumber(ssb);
                    return sb.substring(0, startIndex + 1).concat(ssb.toString());
                }
                SpannableStringBuilder ssb2 = new SpannableStringBuilder(sb);
                PhoneNumberUtils.formatNanpNumber(ssb2);
                return ssb2.toString();
            case 2:
                if (blankPosition >= 0) {
                    SpannableStringBuilder ssb22 = new SpannableStringBuilder(sb.substring(startIndex + 1));
                    PhoneNumberUtils.formatJapaneseNumber(ssb22);
                    return sb.substring(0, startIndex + 1).concat(ssb22.toString());
                }
                SpannableStringBuilder ssb23 = new SpannableStringBuilder(sb);
                PhoneNumberUtils.formatJapaneseNumber(ssb23);
                return ssb23.toString();
            case 3:
                return formatChinaNumber(sb, blankPosition);
            case 4:
            case 15:
                return formatHeightLengthWithoutRegionCodeNumber(sb, blankPosition);
            case 5:
                return formatMacauNumber(sb, blankPosition);
            case 6:
                return formatTaiwanNumber(sb, blankPosition);
            case 7:
                return formatEnglandNumber(sb, blankPosition);
            case 8:
                return formatFranceNumber(sb, blankPosition);
            case 9:
                return formatItalyNumber(sb, blankPosition);
            case 10:
                return formatGermanyNumber(sb, blankPosition);
            case 11:
                return formatRussianNumber(sb, blankPosition);
            case 12:
                return formatIndiaNumber(sb, blankPosition);
            case 13:
                return formatSpainNumber(sb, blankPosition);
            case 14:
                return formatMalaysiaNumber(sb, blankPosition);
            case 16:
                return formatIndonesiaNumber(sb, blankPosition);
            case FORMAT_THAILAND /* 17 */:
                return formatThailandNumber(sb, blankPosition);
            case FORMAT_VIETNAM /* 18 */:
                return formatVietnamNubmer(sb, blankPosition);
            case FORMAT_PORTUGAL /* 19 */:
                return formatPortugalNumber(sb, blankPosition);
            case FORMAT_POLAND /* 20 */:
                return formatPolandNumber(sb, blankPosition);
            case FORMAT_AUSTRALIA /* 21 */:
                return formatAustraliaNumber(sb, blankPosition);
            case FORMAT_NEW_ZEALAND /* 22 */:
                return formatNewZealandNumber(sb, blankPosition);
            case FORMAT_BRAZIL /* 23 */:
                return formatBrazilNumber(sb, blankPosition);
            case 24:
                return formatTurkeyNumber(sb, blankPosition);
            default:
                return removeAllDash(sb);
        }
    }

    private static int[] getFormatTypeByCommonPrefix(String text) {
        int result = 0;
        int startIndex = 0;
        int[] match = new int[2];
        if (text.length() > 0 && text.charAt(0) == '+') {
            startIndex = 1;
        } else if (text.length() > 1 && text.charAt(0) == '0' && text.charAt(1) == '0') {
            startIndex = 2;
        }
        if (startIndex != 0) {
            String[] strArr = FORMAT_COUNTRY_CODES;
            int length = strArr.length;
            int index = 0;
            int index2 = 0;
            while (true) {
                if (index2 >= length) {
                    break;
                }
                String pattern = strArr[index2];
                index++;
                if (text.startsWith(pattern, startIndex)) {
                    result = index;
                    startIndex += pattern.length();
                    break;
                }
                index2++;
            }
        }
        if (result == 0) {
            startIndex = 0;
        }
        match[0] = startIndex;
        match[1] = result;
        return match;
    }

    private static int[] getFormatNumberBySpecialPrefix(String text, String[] prefixs) {
        int result = 0;
        int startIndex = 0;
        int[] match = new int[2];
        if (text.charAt(0) != '+') {
            int length = prefixs.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String prefix = prefixs[i];
                if (text.startsWith(prefix)) {
                    startIndex = prefix.length();
                    break;
                }
                i++;
            }
        } else {
            startIndex = 1;
        }
        if (startIndex > 0) {
            String[] strArr = FORMAT_COUNTRY_CODES;
            int length2 = strArr.length;
            int index = 0;
            int index2 = 0;
            while (true) {
                if (index2 >= length2) {
                    break;
                }
                String pattern = strArr[index2];
                index++;
                if (text.startsWith(pattern, startIndex)) {
                    result = index;
                    startIndex += pattern.length();
                    break;
                }
                index2++;
            }
        }
        if (result == 0) {
            startIndex = 0;
        }
        match[0] = startIndex;
        match[1] = result;
        return match;
    }

    private static int[] getFormatTypeFromNumber(String text, int defaultFormatType) {
        switch (defaultFormatType) {
            case 1:
                return getFormatNumberBySpecialPrefix(text, NANP_INTERNATIONAL_PREFIXS);
            case 2:
                return getFormatNumberBySpecialPrefix(text, JAPAN_INTERNATIONAL_PREFIXS);
            case 3:
            case 5:
            case 7:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case FORMAT_VIETNAM /* 18 */:
            case FORMAT_PORTUGAL /* 19 */:
            case FORMAT_POLAND /* 20 */:
            case FORMAT_NEW_ZEALAND /* 22 */:
            case 24:
                return getFormatTypeByCommonPrefix(text);
            case 4:
                return getFormatNumberBySpecialPrefix(text, HONGKONG_INTERNATIONAL_PREFIXS);
            case 6:
                return getFormatNumberBySpecialPrefix(text, TAIWAN_INTERNATIONAL_PREFIXS);
            case 8:
                return getFormatNumberBySpecialPrefix(text, FRANCE_INTERNATIONAL_PREFIXS);
            case 15:
                return getFormatNumberBySpecialPrefix(text, SINGAPORE_INTERNATIONAL_PREFIXS);
            case 16:
                return getFormatNumberBySpecialPrefix(text, INDONESIA_INTERNATIONAL_PREFIXS);
            case FORMAT_THAILAND /* 17 */:
                return getFormatNumberBySpecialPrefix(text, THAILAND_INTERNATIONAL_PREFIXS);
            case FORMAT_AUSTRALIA /* 21 */:
                return getFormatNumberBySpecialPrefix(text, AUSTRALIA_INTERNATIONAL_PREFIXS);
            case FORMAT_BRAZIL /* 23 */:
                return getFormatNumberBySpecialPrefix(text, BRAZIL_INTERNATIONAL_PREFIXS);
            default:
                return null;
        }
    }

    private static String removeAllDash(StringBuilder sb) {
        int p = 0;
        while (p < sb.length()) {
            if (sb.charAt(p) == '-' || sb.charAt(p) == ' ') {
                sb.deleteCharAt(p);
            } else {
                p++;
            }
        }
        return sb.toString();
    }

    private static int removeAllDashAndFormatBlank(StringBuilder sb, int startIndex) {
        int p = 0;
        while (p < sb.length()) {
            if (sb.charAt(p) == '-' || sb.charAt(p) == ' ') {
                sb.deleteCharAt(p);
            } else {
                p++;
            }
        }
        if (startIndex <= 0) {
            return -1;
        }
        sb.replace(startIndex, startIndex, " ");
        return startIndex;
    }

    private static String removeTrailingDashes(StringBuilder sb) {
        int len = sb.length();
        while (len > 0 && sb.charAt(len - 1) == '-') {
            sb.delete(len - 1, len);
            len--;
        }
        return sb.toString();
    }

    private static String formatChinaNumber(StringBuilder sb, int blankPosition) {
        int numDashes;
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes2 = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            char c1 = sb.charAt(index);
            char c2 = sb.charAt(index + 1);
            if ((c1 == '1' && c2 == '0') || c1 == '2') {
                dashPositions[0] = index + 2;
                numDashes2 = 0 + 1;
            } else if (c1 == '1') {
                if (length > index + 4) {
                    dashPositions[0] = index + 3;
                    numDashes2 = 0 + 1;
                }
                if (length > index + 8) {
                    dashPositions[numDashes2] = index + 7;
                    numDashes2++;
                }
            } else {
                dashPositions[0] = index + 3;
                numDashes2 = 0 + 1;
            }
            numDashes = numDashes2;
        } else {
            char c12 = sb.charAt(phoneNumPosition);
            char c22 = sb.charAt(phoneNumPosition + 1);
            if (c12 != '1' || c22 == '0') {
                if (c12 == '1' && c22 == '0') {
                    if (length > phoneNumPosition + 3) {
                        numDashes = 0 + 1;
                        dashPositions[0] = phoneNumPosition + 2;
                    }
                } else if (length > phoneNumPosition + 8) {
                    if (c12 == '2') {
                        numDashes = 0 + 1;
                        dashPositions[0] = phoneNumPosition + 2;
                    } else {
                        numDashes = 0 + 1;
                        dashPositions[0] = phoneNumPosition + 3;
                    }
                }
                numDashes = 0;
            } else {
                if (length > phoneNumPosition + 4) {
                    dashPositions[0] = phoneNumPosition + 3;
                    numDashes2 = 0 + 1;
                }
                if (length > phoneNumPosition + 8) {
                    numDashes = numDashes2 + 1;
                    dashPositions[numDashes2] = phoneNumPosition + 7;
                } else {
                    numDashes = numDashes2;
                }
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatTaiwanNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            char c1 = sb.charAt(index);
            char c2 = sb.charAt(index + 1);
            char c3 = sb.charAt(index + 2);
            if (c1 == '9') {
                if (length > index + 4) {
                    dashPositions[0] = index + 3;
                    numDashes = 0 + 1;
                }
                if (length > index + 7) {
                    dashPositions[numDashes] = index + 6;
                    numDashes++;
                }
            } else if ((c1 == '8' && c2 == '2' && c3 == '6') || (c1 == '8' && c2 == '3' && c3 == '6')) {
                if (length > index + 4) {
                    dashPositions[0] = index + 3;
                    numDashes = 0 + 1;
                }
                if (length > index + 7) {
                    dashPositions[numDashes] = index + 6;
                    numDashes++;
                }
            } else if ((c1 == '3' && c2 == '7') || ((c1 == '4' && c2 == '9') || ((c1 == '8' && c2 == '9') || (c1 == '8' && c2 == '2')))) {
                int numDashes2 = 0 + 1;
                dashPositions[0] = index + 2;
                if (length > index + 6 && length < index + 10) {
                    numDashes = numDashes2 + 1;
                    dashPositions[numDashes2] = index + 5;
                } else if (length >= index + 10) {
                    numDashes = numDashes2 + 1;
                    dashPositions[numDashes2] = index + 6;
                } else {
                    numDashes = numDashes2;
                }
            } else {
                int numDashes3 = 0 + 1;
                dashPositions[0] = index + 1;
                if (length > index + 6 && length < index + 9) {
                    numDashes = numDashes3 + 1;
                    dashPositions[numDashes3] = index + 4;
                } else if (length >= index + 9) {
                    numDashes = numDashes3 + 1;
                    dashPositions[numDashes3] = index + 5;
                } else {
                    numDashes = numDashes3;
                }
            }
        } else if (length > phoneNumPosition + 4 && length < phoneNumPosition + 8) {
            dashPositions[0] = phoneNumPosition + 3;
            numDashes = 0 + 1;
        } else if (length >= phoneNumPosition + 8) {
            dashPositions[0] = phoneNumPosition + 4;
            numDashes = 0 + 1;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatMacauNumber(StringBuilder sb, int blankPosition) {
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (sb.charAt(phoneNumPosition) != '0' || sb.charAt(phoneNumPosition + 1) != '1') {
            return formatHeightLengthWithoutRegionCodeNumber(sb, blankPosition);
        }
        sb.replace(phoneNumPosition + 2, phoneNumPosition + 2, " ");
        return formatHeightLengthWithoutRegionCodeNumber(sb, blankPosition + 3);
    }

    private static String formatHeightLengthWithoutRegionCodeNumber(StringBuilder sb, int blankPosition) {
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (sb.length() >= phoneNumPosition + 6) {
            dashPositions[0] = phoneNumPosition + 4;
            numDashes = 0 + 1;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return removeTrailingDashes(sb);
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x00a9 A[LOOP:0: B:52:0x00a7->B:53:0x00a9, LOOP_END] */
    private static String formatVietnamNubmer(StringBuilder sb, int blankPosition) {
        int numDashes;
        int i;
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes2 = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            char c1 = sb.charAt(index);
            char c2 = sb.charAt(index + 1);
            if (c1 == '4' || c1 == '8') {
                numDashes = 0 + 1;
                dashPositions[0] = index + 1;
                for (i = 0; i < numDashes; i++) {
                    int pos = dashPositions[i];
                    sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
                }
                return sb.toString();
            } else if ((c1 != '2' || (c2 != '1' && c2 != '3' && c2 != '4' && c2 != '8')) && ((c1 != '3' || (c2 != '2' && c2 != '5')) && ((c1 != '6' || c2 != '5') && (c1 != '7' || (c2 != '1' && c2 != '8'))))) {
                if (c1 == '9') {
                    numDashes = 0 + 1;
                    dashPositions[0] = index + 2;
                    if (length > index + 6) {
                        dashPositions[numDashes] = index + 5;
                        numDashes++;
                    }
                } else if (c1 == '1') {
                    if (length > index + 4) {
                        dashPositions[0] = index + 3;
                        numDashes2 = 0 + 1;
                    }
                    if (length > index + 7) {
                        numDashes = numDashes2 + 1;
                        dashPositions[numDashes2] = index + 6;
                    } else {
                        numDashes = numDashes2;
                    }
                } else {
                    numDashes = 0 + 1;
                    dashPositions[0] = index + 2;
                }
                while (i < numDashes) {
                }
                return sb.toString();
            } else if (length > index + 4) {
                numDashes = 0 + 1;
                dashPositions[0] = index + 3;
                while (i < numDashes) {
                }
                return sb.toString();
            }
        }
        numDashes = 0;
        while (i < numDashes) {
        }
        return sb.toString();
    }

    private static String formatPortugalNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (length > phoneNumPosition + 4) {
            dashPositions[0] = phoneNumPosition + 2;
            numDashes = 0 + 1;
        }
        if (length > phoneNumPosition + 8) {
            dashPositions[numDashes] = phoneNumPosition + 5;
            numDashes++;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatBrazilNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[5];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                dashPositions[0] = phoneNumPosition + 1;
                index++;
                numDashes = 0 + 1;
            }
            if (length > index + 3) {
                dashPositions[numDashes] = index + 2;
                numDashes++;
            }
            if (length > index + 7 && length <= index + 10) {
                dashPositions[numDashes] = index + 6;
                numDashes++;
            } else if (length > index + 10) {
                int numDashes2 = numDashes + 1;
                dashPositions[numDashes] = index + 4;
                numDashes = numDashes2 + 1;
                dashPositions[numDashes2] = index + 8;
            }
        } else if (length > phoneNumPosition + 5) {
            dashPositions[0] = phoneNumPosition + 4;
            numDashes = 0 + 1;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatPolandNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[3];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (sb.charAt(phoneNumPosition) < '5' || sb.charAt(phoneNumPosition) > '8') {
            if (length > phoneNumPosition + 5) {
                dashPositions[0] = phoneNumPosition + 3;
                numDashes = 0 + 1;
            }
            if (length > phoneNumPosition + 8) {
                dashPositions[numDashes] = phoneNumPosition + 6;
                numDashes++;
            }
        } else {
            if (length > phoneNumPosition + 4) {
                dashPositions[0] = phoneNumPosition + 2;
                numDashes = 0 + 1;
            }
            if (length > phoneNumPosition + 6) {
                dashPositions[numDashes] = phoneNumPosition + 5;
                numDashes++;
            }
            if (length > phoneNumPosition + 8) {
                dashPositions[numDashes] = phoneNumPosition + 7;
                numDashes++;
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatAustraliaNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            if (sb.charAt(index) == '4') {
                if (length > index + 5) {
                    dashPositions[0] = index + 3;
                    numDashes = 0 + 1;
                }
                if (length > index + 8) {
                    dashPositions[numDashes] = index + 6;
                    numDashes++;
                }
            } else {
                if (length > index + 4) {
                    dashPositions[0] = index + 1;
                    numDashes = 0 + 1;
                }
                if (length > index + 6) {
                    dashPositions[numDashes] = index + 5;
                    numDashes++;
                }
            }
        } else {
            System.out.println(length);
            if (length == phoneNumPosition + 8) {
                dashPositions[0] = phoneNumPosition + 4;
                numDashes = 0 + 1;
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatNewZealandNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            if (sb.charAt(index) != '2' || sb.charAt(index + 1) == '4') {
                if (length > index + 3) {
                    dashPositions[0] = index + 1;
                    numDashes = 0 + 1;
                }
                if (length > index + 6) {
                    dashPositions[numDashes] = index + 4;
                    numDashes++;
                }
            } else {
                if (length > index + 4) {
                    dashPositions[0] = index + 2;
                    numDashes = 0 + 1;
                }
                if (length > index + 6) {
                    dashPositions[numDashes] = index + 5;
                    numDashes++;
                }
            }
        } else {
            System.out.println(length);
            if (length == phoneNumPosition + 7) {
                dashPositions[0] = phoneNumPosition + 3;
                numDashes = 0 + 1;
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatThailandNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            if (sb.charAt(index) == '8') {
                if (length > index + 4) {
                    dashPositions[0] = index + 2;
                    numDashes = 0 + 1;
                }
                if (length > index + 6) {
                    dashPositions[numDashes] = index + 5;
                    numDashes++;
                }
            } else if (sb.charAt(index) == 50) {
                if (length > index + 3) {
                    dashPositions[0] = index + 1;
                    numDashes = 0 + 1;
                }
                if (length > index + 6) {
                    dashPositions[numDashes] = index + 4;
                    numDashes++;
                }
            } else {
                if (length > index + 4) {
                    dashPositions[0] = index + 2;
                    numDashes = 0 + 1;
                }
                if (length > index + 6) {
                    dashPositions[numDashes] = index + 5;
                    numDashes++;
                }
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatIndonesiaNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            char c1 = sb.charAt(index);
            char c2 = sb.charAt(index + 1);
            char c3 = sb.charAt(index + 2);
            if (c1 == '8') {
                if (length > index + 5) {
                    dashPositions[0] = index + 3;
                    numDashes = 0 + 1;
                }
                if (length >= index + 8 && length <= index + 10) {
                    dashPositions[numDashes] = index + 6;
                    numDashes++;
                }
                if (length > index + 10) {
                    dashPositions[numDashes] = index + 7;
                    numDashes++;
                }
            } else if ((c1 == '2' && (c2 == '1' || c2 == '2' || c2 == '4')) || ((c1 == '3' && c2 == '1') || (c1 == '6' && c2 == '1' && c3 != '9'))) {
                if (length > index + 3) {
                    dashPositions[0] = index + 2;
                    numDashes = 0 + 1;
                }
                if (length > index + 7) {
                    dashPositions[numDashes] = index + 6;
                    numDashes++;
                }
            } else {
                if (length > index + 4) {
                    dashPositions[0] = index + 3;
                    numDashes = 0 + 1;
                }
                if (length > index + 7) {
                    dashPositions[numDashes] = index + 6;
                    numDashes++;
                }
            }
        } else if (length == phoneNumPosition + 7) {
            dashPositions[0] = phoneNumPosition + 3;
            numDashes = 0 + 1;
        } else if (length == phoneNumPosition + 8) {
            dashPositions[0] = phoneNumPosition + 4;
            numDashes = 0 + 1;
        } else if (sb.charAt(phoneNumPosition) == '8') {
            if (length > phoneNumPosition + 8 && length <= phoneNumPosition + 10) {
                int numDashes2 = 0 + 1;
                dashPositions[0] = phoneNumPosition + 3;
                numDashes = numDashes2 + 1;
                dashPositions[numDashes2] = phoneNumPosition + 6;
            } else if (length > phoneNumPosition + 10) {
                int numDashes3 = 0 + 1;
                dashPositions[0] = phoneNumPosition + 3;
                numDashes = numDashes3 + 1;
                dashPositions[numDashes3] = phoneNumPosition + 7;
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatMalaysiaNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            char c1 = sb.charAt(index);
            if ((c1 < '3' || c1 > '7') && c1 != '9') {
                if (c1 == '8') {
                    if (length > index + 4) {
                        dashPositions[0] = index + 2;
                        numDashes = 0 + 1;
                    }
                } else if (c1 == '1') {
                    if (length > index + 4) {
                        dashPositions[0] = index + 2;
                        numDashes = 0 + 1;
                    }
                    if (length > index + 6) {
                        dashPositions[numDashes] = index + 5;
                        numDashes++;
                    }
                } else if (c1 == '2') {
                    if (length > index + 4) {
                        dashPositions[0] = index + 1;
                        numDashes = 0 + 1;
                    }
                    if (length > index + 7) {
                        dashPositions[numDashes] = index + 5;
                        numDashes++;
                    }
                }
            } else if (length > index + 4) {
                dashPositions[0] = index + 1;
                numDashes = 0 + 1;
            }
        } else if (sb.charAt(phoneNumPosition) == '2' && length > phoneNumPosition + 8) {
            int numDashes2 = 0 + 1;
            dashPositions[0] = phoneNumPosition + 1;
            numDashes = numDashes2 + 1;
            dashPositions[numDashes2] = phoneNumPosition + 5;
        } else if (sb.charAt(phoneNumPosition) == '1' && length > phoneNumPosition + 8) {
            int numDashes3 = 0 + 1;
            dashPositions[0] = phoneNumPosition + 2;
            numDashes = numDashes3 + 1;
            dashPositions[numDashes3] = phoneNumPosition + 5;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatSpainNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (length > phoneNumPosition + 5) {
            dashPositions[0] = phoneNumPosition + 3;
            numDashes = 0 + 1;
        }
        if (length > phoneNumPosition + 7) {
            dashPositions[numDashes] = phoneNumPosition + 6;
            numDashes++;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static int checkIndiaNumber(char c1, char c2, char c3, char c4) {
        int result = -1;
        int temp = ((c3 - '0') * 10) + (c4 - '0');
        if (c1 == '9') {
            result = 0;
        } else if (c1 == '8') {
            if ((c2 == '0' && (temp < 20 || ((temp >= 50 && temp <= 60) || temp >= 80))) || ((c2 == '1' && (temp < 10 || ((temp >= 20 && temp <= 29) || (temp >= 40 && temp <= 49)))) || ((c2 == '7' && (temp >= 90 || temp == 69)) || ((c2 == '8' && (temp < 10 || temp == 17 || ((temp >= 25 && temp <= 28) || temp == 44 || temp == 53 || temp >= 90))) || (c3 == '9' && (temp < 10 || temp == 23 || temp == 39 || ((temp >= 50 && temp <= 62) || temp == 67 || temp == 68 || temp >= 70))))))) {
                result = 0;
            }
        } else if (c1 == '7' && (c2 == '0' || ((c2 == '2' && (temp == 0 || ((temp >= 4 && temp <= 9) || temp == 50 || temp == 59 || ((temp >= 75 && temp <= 78) || temp == 93 || temp == 9)))) || ((c2 == '3' && (temp == 73 || temp == 76 || temp == 77 || temp == 96 || temp == 98 || temp == 99)) || ((c2 == '4' && (temp < 10 || temp == 11 || ((temp >= 15 && temp <= 19) || temp == 28 || temp == 29 || temp == 39 || temp == 83 || temp == 88 || temp == 89 || temp == 98 || temp == 99))) || ((c2 == '5' && (temp <= 4 || temp == 49 || temp == 50 || ((temp >= 66 && temp <= 69) || temp == 79 || ((temp >= 87 && temp <= 89) || temp >= 97)))) || ((c2 == '6' && (temp == 0 || temp == 2 || temp == 7 || temp == 20 || temp == 31 || temp == 39 || temp == 54 || temp == 55 || ((temp >= 65 && temp <= 69) || ((temp >= 76 && temp <= 79) || temp >= 96)))) || ((c2 == '7' && (temp == 2 || temp == 8 || temp == 9 || ((temp >= 35 && temp <= 39) || temp == 42 || temp == 60 || temp == 77 || temp >= 95))) || ((c2 == '8' && temp <= 39 && (temp == 0 || ((temp >= 7 && temp <= 9) || temp == 14 || ((temp >= 27 && temp <= 30) || (temp >= 37 && temp <= 39))))) || (c2 == '8' && temp > 39 && (temp == 42 || temp == 45 || temp == 60 || ((temp >= 69 && temp <= 79) || temp >= 90)))))))))))) {
            result = 0;
        }
        if (result == 0) {
            return result;
        }
        if ((c1 == '1' && c2 == '1') || ((c1 == '2' && (c2 == '0' || c2 == '2')) || ((c1 == '3' && c2 == '3') || ((c1 == '4' && (c2 == '0' || c2 == '4')) || ((c1 == '7' && c2 == '9') || (c1 == '8' && c2 == '0')))))) {
            return 2;
        }
        if (Arrays.binarySearch(INDIA_THREE_DIGIG_AREA_CODES, ((c1 - '0') * 100) + ((c2 - '0') * 10) + (c3 - '0')) >= 0) {
            return 3;
        }
        return 4;
    }

    private static String formatIndiaNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        char c = sb.charAt(phoneNumPosition);
        if ((phoneNumPosition > 0 && c != '0') || (c == '0' && length > phoneNumPosition + 4)) {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            int type = checkIndiaNumber(sb.charAt(index), sb.charAt(index + 1), sb.charAt(index + 2), sb.charAt(index + 3));
            if (type == 0) {
                int numDashes2 = 0 + 1;
                dashPositions[0] = index + 2;
                if (length > index + 7) {
                    numDashes = numDashes2 + 1;
                    dashPositions[numDashes2] = index + 4;
                } else {
                    numDashes = numDashes2;
                }
            } else if (type == 2) {
                dashPositions[0] = index + 2;
                numDashes = 0 + 1;
            } else if (type == 3) {
                dashPositions[0] = index + 3;
                numDashes = 0 + 1;
            } else if (length > index + 5) {
                dashPositions[0] = index + 4;
                numDashes = 0 + 1;
            }
        } else if (length > phoneNumPosition + 8) {
            int numDashes3 = 0 + 1;
            dashPositions[0] = phoneNumPosition + 2;
            numDashes = numDashes3 + 1;
            dashPositions[numDashes3] = phoneNumPosition + 4;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatRussianNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[3];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0) {
            if (length > phoneNumPosition + 5) {
                dashPositions[0] = phoneNumPosition + 3;
                numDashes = 0 + 1;
            }
            if (length > phoneNumPosition + 7) {
                dashPositions[numDashes] = phoneNumPosition + 6;
                numDashes++;
            }
            if (length > phoneNumPosition + 9) {
                dashPositions[numDashes] = phoneNumPosition + 8;
                numDashes++;
            }
        } else if (length == phoneNumPosition + 6) {
            int numDashes2 = 0 + 1;
            dashPositions[0] = phoneNumPosition + 2;
            numDashes = numDashes2 + 1;
            dashPositions[numDashes2] = phoneNumPosition + 4;
        } else if (length == phoneNumPosition + 7) {
            int numDashes3 = 0 + 1;
            dashPositions[0] = phoneNumPosition + 3;
            numDashes = numDashes3 + 1;
            dashPositions[numDashes3] = phoneNumPosition + 5;
        } else if (length >= phoneNumPosition + 8) {
            int numDashes4 = 0 + 1;
            dashPositions[0] = phoneNumPosition + 3;
            numDashes = numDashes4 + 1;
            dashPositions[numDashes4] = phoneNumPosition + 6;
            if (length > phoneNumPosition + 9) {
                dashPositions[numDashes] = phoneNumPosition + 8;
                numDashes++;
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatGermanyNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            char c1 = sb.charAt(index);
            char c2 = sb.charAt(index + 1);
            if (c1 == '1') {
                if (length > index + 4) {
                    dashPositions[0] = index + 3;
                    numDashes = 0 + 1;
                }
                if ((c2 == '5' || c2 == '6' || c2 == '7') && length > index + 10) {
                    dashPositions[numDashes] = index + 9;
                    numDashes++;
                }
            } else if ((c1 == '3' && c2 == '0') || ((c1 == '4' && c2 == '0') || ((c1 == '6' && c2 == '9') || (c1 == '8' && c2 == '9')))) {
                if (length > index + 4) {
                    dashPositions[0] = index + 2;
                    numDashes = 0 + 1;
                }
                if (length > index + 6) {
                    dashPositions[numDashes] = index + 5;
                    numDashes++;
                }
            } else if (length > index + 3) {
                char c3 = sb.charAt(index + 2);
                char c4 = sb.charAt(index + 3);
                int key3 = ((c1 - '0') * 100) + ((c2 - '0') * 10) + (c3 - '0');
                int key4 = (key3 * 10) + (c4 - '0');
                if (c3 == '1' || (Arrays.binarySearch(Germany_THREE_PART_REGION_CODES, key3) >= 0 && (key3 != 212 || (key3 == 212 && c4 != '9')))) {
                    if (length > index + 4) {
                        dashPositions[0] = index + 3;
                        numDashes = 0 + 1;
                    }
                    if (length > index + 7) {
                        dashPositions[numDashes] = index + 6;
                        numDashes++;
                    }
                } else if (c1 != '3' || (c1 == '3' && Arrays.binarySearch(Germany_FOUR_PART_REGION_CODES, key4) >= 0)) {
                    if (length > index + 5) {
                        dashPositions[0] = index + 4;
                        numDashes = 0 + 1;
                    }
                    if (length > index + 8) {
                        dashPositions[numDashes] = index + 7;
                        numDashes++;
                    }
                } else {
                    if (length > index + 6) {
                        dashPositions[0] = index + 5;
                        numDashes = 0 + 1;
                    }
                    if (length > index + 9) {
                        dashPositions[numDashes] = index + 8;
                        numDashes++;
                    }
                }
            }
        } else if (length >= phoneNumPosition + 6 && length <= phoneNumPosition + 8) {
            dashPositions[0] = phoneNumPosition + 3;
            numDashes = 0 + 1;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    /* JADX INFO: Multiple debug info for r5v1 int: [D('c2' char), D('index' int)] */
    private static String formatItalyNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            char c1 = sb.charAt(index);
            char c2 = sb.charAt(index + 1);
            if (Arrays.binarySearch(ITALY_MOBILE_PREFIXS, ((c1 - '0') * 100) + ((c2 - '0') * 10) + (sb.charAt(index + 2) - '0')) >= 0) {
                if (length > index + 5) {
                    dashPositions[0] = index + 3;
                    numDashes = 0 + 1;
                }
                if (length > index + 8) {
                    dashPositions[numDashes] = index + 6;
                    numDashes++;
                }
            } else if (c1 == '2' || c1 == '6') {
                dashPositions[0] = index + 1;
                numDashes = 0 + 1;
            } else if (c2 == '0' || c2 == '1' || c2 == '5' || c2 == '9') {
                if (length > index + 4) {
                    dashPositions[0] = index + 2;
                    numDashes = 0 + 1;
                }
            } else if (length > index + 5) {
                dashPositions[0] = index + 3;
                numDashes = 0 + 1;
            }
        } else if (Arrays.binarySearch(ITALY_MOBILE_PREFIXS, ((sb.charAt(phoneNumPosition) - '0') * 100) + ((sb.charAt(phoneNumPosition + 1) - '0') * 10) + (sb.charAt(phoneNumPosition + 2) - '0')) >= 0) {
            if (length > phoneNumPosition + 5) {
                dashPositions[0] = phoneNumPosition + 3;
                numDashes = 0 + 1;
            }
            if (length > phoneNumPosition + 7) {
                dashPositions[numDashes] = phoneNumPosition + 6;
                numDashes++;
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatFranceNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[4];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        int c = sb.charAt(phoneNumPosition);
        if (phoneNumPosition > 0 || c == 48 || c == 52 || c == 53 || c == 55 || c == 57) {
            int index = phoneNumPosition;
            if ((phoneNumPosition == 0 && (c == 48 || c == 52 || c == 53 || c == 55 || c == 57)) || (phoneNumPosition > 0 && c == 48)) {
                index++;
            }
            int numDashes2 = 0 + 1;
            dashPositions[0] = index + 1;
            if (length > index + 4) {
                numDashes = numDashes2 + 1;
                dashPositions[numDashes2] = index + 3;
            } else {
                numDashes = numDashes2;
            }
            if (length > index + 6) {
                dashPositions[numDashes] = index + 5;
                numDashes++;
            }
            if (length > index + 8) {
                dashPositions[numDashes] = index + 7;
                numDashes++;
            }
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatEnglandNumber(StringBuilder sb, int blankPosition) {
        int numDashes;
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes2 = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            char c1 = sb.charAt(index);
            char c2 = sb.charAt(index + 1);
            char c3 = sb.charAt(index + 2);
            if (c1 == '7') {
                if (length > index + 5) {
                    dashPositions[0] = index + 4;
                    numDashes2 = 0 + 1;
                }
            } else if (c1 == '2') {
                int numDashes3 = 0 + 1;
                dashPositions[0] = index + 2;
                if (length > index + 7) {
                    numDashes2 = numDashes3 + 1;
                    dashPositions[numDashes3] = index + 6;
                } else {
                    numDashes2 = numDashes3;
                }
            } else if (c1 == '1') {
                int key = ((c1 - '0') * 1000) + ((c2 - '0') * 100) + ((c3 - '0') * 10) + sb.charAt(index + 2);
                if (c2 == '1' || c3 == '1') {
                    if (length > index + 4) {
                        dashPositions[0] = index + 3;
                        numDashes2 = 0 + 1;
                    }
                    if (length > index + 7) {
                        numDashes = numDashes2 + 1;
                        dashPositions[numDashes2] = index + 6;
                    } else {
                        numDashes = numDashes2;
                    }
                } else {
                    if (key == 1387 || key == 1539 || key == 1697 || key == 1768 || key == 1946) {
                        if (length > index + 6) {
                            numDashes = 0 + 1;
                            dashPositions[0] = index + 5;
                        }
                    } else if (length > index + 5) {
                        numDashes = 0 + 1;
                        dashPositions[0] = index + 4;
                    }
                    numDashes = 0;
                }
                numDashes2 = numDashes;
            } else if (c1 == '3' || c1 == '8' || c1 == '9') {
                if (length > index + 4) {
                    dashPositions[0] = index + 3;
                    numDashes2 = 0 + 1;
                }
                if (length > index + 7) {
                    dashPositions[numDashes2] = index + 6;
                    numDashes2++;
                }
            } else {
                int numDashes4 = 0 + 1;
                dashPositions[0] = index + 2;
                if (length > index + 7) {
                    numDashes2 = numDashes4 + 1;
                    dashPositions[numDashes4] = index + 6;
                } else {
                    numDashes2 = numDashes4;
                }
            }
        } else if (length > phoneNumPosition + 4 && length < phoneNumPosition + 8) {
            dashPositions[0] = phoneNumPosition + 3;
            numDashes2 = 0 + 1;
        } else if (length >= phoneNumPosition + 8) {
            dashPositions[0] = phoneNumPosition + 4;
            numDashes2 = 0 + 1;
        }
        for (int i = 0; i < numDashes2; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    private static String formatTurkeyNumber(StringBuilder sb, int blankPosition) {
        int length = sb.length();
        int[] dashPositions = new int[2];
        int numDashes = 0;
        int phoneNumPosition = blankPosition == -1 ? 0 : blankPosition + 1;
        if (phoneNumPosition > 0 || sb.charAt(phoneNumPosition) == '0') {
            int index = phoneNumPosition;
            if (sb.charAt(phoneNumPosition) == '0') {
                index++;
            }
            if (length > index + 4) {
                dashPositions[0] = index + 3;
                numDashes = 0 + 1;
            }
            if (length > index + 7) {
                dashPositions[numDashes] = index + 6;
                numDashes++;
            }
        } else if (length > phoneNumPosition + 4) {
            dashPositions[0] = phoneNumPosition + 3;
            numDashes = 0 + 1;
        }
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            sb.replace(pos + i, pos + i, MtkContactsContract.Aas.ENCODE_SYMBOL);
        }
        return sb.toString();
    }

    public static void log(String info) {
    }
}
