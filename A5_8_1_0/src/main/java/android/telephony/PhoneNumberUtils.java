package android.telephony;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Country;
import android.location.CountryDetector;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Settings.System;
import android.provider.SettingsStringUtil;
import android.telecom.PhoneAccount;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.TtsSpan;
import android.text.style.TtsSpan.TelephoneBuilder;
import android.util.SparseIntArray;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberUtils {
    private static final int CCC_LENGTH = COUNTRY_CALLING_CALL.length;
    private static final String CLIR_OFF = "#31#";
    private static final String CLIR_ON = "*31#";
    private static final boolean[] COUNTRY_CALLING_CALL = new boolean[]{true, true, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, true, true, false, true, true, true, true, true, false, true, false, false, true, true, false, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, false, true, false, false, true, true, true, true, true, true, true, false, false, true, false};
    private static final boolean DBG = "true".equalsIgnoreCase(SystemProperties.get("persist.sys.assert.panic", "false"));
    public static final int FORMAT_JAPAN = 2;
    public static final int FORMAT_NANP = 1;
    public static final int FORMAT_UNKNOWN = 0;
    private static final Pattern GLOBAL_PHONE_NUMBER_PATTERN = Pattern.compile("[\\+]?[0-9.-]+");
    private static final String IP_CALL = "ip_call";
    private static final String IP_CALL_PREFIX = "ip_call_prefix_sub";
    private static final String JAPAN_ISO_COUNTRY_CODE = "JP";
    private static final SparseIntArray KEYPAD_MAP = new SparseIntArray();
    private static final String KOREA_ISO_COUNTRY_CODE = "KR";
    static final String LOG_TAG = "PhoneNumberUtils";
    static final int MIN_MATCH = SystemProperties.getInt("persist.radio.phone.matchnum", 7);
    private static final String[] NANP_COUNTRIES = new String[]{"US", "CA", "AS", "AI", "AG", "BS", "BB", "BM", "VG", "KY", "DM", "DO", "GD", "GU", "JM", "PR", "MS", "MP", "KN", "LC", "VC", "TT", "TC", "VI"};
    private static final String NANP_IDP_STRING = "011";
    private static final int NANP_LENGTH = 10;
    private static final int NANP_STATE_DASH = 4;
    private static final int NANP_STATE_DIGIT = 1;
    private static final int NANP_STATE_ONE = 3;
    private static final int NANP_STATE_PLUS = 2;
    public static final char PAUSE = ',';
    private static final char PLUS_SIGN_CHAR = '+';
    private static final String PLUS_SIGN_STRING = "+";
    public static final int TOA_International = 145;
    public static final int TOA_Unknown = 129;
    public static final char WAIT = ';';
    public static final char WILD = 'N';
    private static String[] sConvertToEmergencyMap = null;
    private static Country sCountryDetector = null;

    private static class CountryCallingCodeAndNewIndex {
        public final int countryCallingCode;
        public final int newIndex;

        public CountryCallingCodeAndNewIndex(int countryCode, int newIndex) {
            this.countryCallingCode = countryCode;
            this.newIndex = newIndex;
        }
    }

    static {
        KEYPAD_MAP.put(97, 50);
        KEYPAD_MAP.put(98, 50);
        KEYPAD_MAP.put(99, 50);
        KEYPAD_MAP.put(65, 50);
        KEYPAD_MAP.put(66, 50);
        KEYPAD_MAP.put(67, 50);
        KEYPAD_MAP.put(100, 51);
        KEYPAD_MAP.put(101, 51);
        KEYPAD_MAP.put(102, 51);
        KEYPAD_MAP.put(68, 51);
        KEYPAD_MAP.put(69, 51);
        KEYPAD_MAP.put(70, 51);
        KEYPAD_MAP.put(103, 52);
        KEYPAD_MAP.put(104, 52);
        KEYPAD_MAP.put(105, 52);
        KEYPAD_MAP.put(71, 52);
        KEYPAD_MAP.put(72, 52);
        KEYPAD_MAP.put(73, 52);
        KEYPAD_MAP.put(106, 53);
        KEYPAD_MAP.put(107, 53);
        KEYPAD_MAP.put(108, 53);
        KEYPAD_MAP.put(74, 53);
        KEYPAD_MAP.put(75, 53);
        KEYPAD_MAP.put(76, 53);
        KEYPAD_MAP.put(109, 54);
        KEYPAD_MAP.put(110, 54);
        KEYPAD_MAP.put(111, 54);
        KEYPAD_MAP.put(77, 54);
        KEYPAD_MAP.put(78, 54);
        KEYPAD_MAP.put(79, 54);
        KEYPAD_MAP.put(112, 55);
        KEYPAD_MAP.put(113, 55);
        KEYPAD_MAP.put(114, 55);
        KEYPAD_MAP.put(115, 55);
        KEYPAD_MAP.put(80, 55);
        KEYPAD_MAP.put(81, 55);
        KEYPAD_MAP.put(82, 55);
        KEYPAD_MAP.put(83, 55);
        KEYPAD_MAP.put(116, 56);
        KEYPAD_MAP.put(117, 56);
        KEYPAD_MAP.put(118, 56);
        KEYPAD_MAP.put(84, 56);
        KEYPAD_MAP.put(85, 56);
        KEYPAD_MAP.put(86, 56);
        KEYPAD_MAP.put(119, 57);
        KEYPAD_MAP.put(120, 57);
        KEYPAD_MAP.put(121, 57);
        KEYPAD_MAP.put(122, 57);
        KEYPAD_MAP.put(87, 57);
        KEYPAD_MAP.put(88, 57);
        KEYPAD_MAP.put(89, 57);
        KEYPAD_MAP.put(90, 57);
    }

    public static boolean isISODigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static final boolean is12Key(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#';
    }

    public static final boolean isDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR || c == WILD;
    }

    public static final boolean isReallyDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR;
    }

    public static final boolean isNonSeparator(char c) {
        if ((c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR || c == WILD || c == ';' || c == ',') {
            return true;
        }
        return false;
    }

    public static final boolean isStartsPostDial(char c) {
        return c == ',' || c == ';';
    }

    private static boolean isPause(char c) {
        return c == 'p' || c == 'P';
    }

    private static boolean isToneWait(char c) {
        return c == 'w' || c == 'W';
    }

    private static boolean isSeparator(char ch) {
        boolean z = true;
        if (isDialable(ch)) {
            return false;
        }
        if (DateFormat.AM_PM <= ch && ch <= DateFormat.TIME_ZONE) {
            return false;
        }
        if (DateFormat.CAPITAL_AM_PM <= ch && ch <= 'Z') {
            z = false;
        }
        return z;
    }

    public static String getNumberFromIntent(Intent intent, Context context) {
        String number = null;
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        int subscription = 0;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            subscription = intent.getIntExtra("subscription", SubscriptionManager.getDefaultVoicePhoneId());
        }
        if (scheme.equals(PhoneAccount.SCHEME_TEL) || scheme.equals("sip")) {
            return checkAndAppendPrefix(intent, subscription, uri.getSchemeSpecificPart(), context);
        } else if (scheme.equals(PhoneAccount.SCHEME_VOICEMAIL)) {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                int[] subId = SubscriptionManager.getSubId(subscription);
                if (subId != null && subId[0] > 0) {
                    return TelephonyManager.getDefault().getCompleteVoiceMailNumber(subId[0]);
                }
            }
            return TelephonyManager.getDefault().getCompleteVoiceMailNumber();
        } else if (context == null) {
            return null;
        } else {
            String type = intent.resolveType(context);
            String phoneColumn = null;
            String authority = uri.getAuthority();
            if (Contacts.AUTHORITY.equals(authority)) {
                phoneColumn = "number";
            } else if (ContactsContract.AUTHORITY.equals(authority)) {
                phoneColumn = "data1";
            }
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{phoneColumn}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    number = cursor.getString(cursor.getColumnIndex(phoneColumn));
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RuntimeException e) {
                Rlog.e(LOG_TAG, "Error getting phone number.", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return checkAndAppendPrefix(intent, subscription, number, context);
        }
    }

    public static String extractNetworkPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (c == PLUS_SIGN_CHAR) {
                String prefix = ret.toString();
                if (prefix.length() == 0 || prefix.equals(CLIR_ON) || prefix.equals(CLIR_OFF)) {
                    ret.append(c);
                }
            } else if (isDialable(c)) {
                ret.append(c);
            } else if (isStartsPostDial(c)) {
                break;
            }
        }
        return ret.toString();
    }

    public static String extractNetworkPortionAlt(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        boolean haveSeenPlus = false;
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (c == PLUS_SIGN_CHAR) {
                if (haveSeenPlus) {
                    continue;
                } else {
                    haveSeenPlus = true;
                }
            }
            if (isDialable(c)) {
                ret.append(c);
            } else if (isStartsPostDial(c)) {
                break;
            }
        }
        return ret.toString();
    }

    public static String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static String convertAndStrip(String phoneNumber) {
        return stripSeparators(convertKeypadLettersToDigits(phoneNumber));
    }

    public static String convertPreDial(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (isPause(c)) {
                c = ',';
            } else if (isToneWait(c)) {
                c = ';';
            }
            ret.append(c);
        }
        return ret.toString();
    }

    private static int minPositive(int a, int b) {
        if (a >= 0 && b >= 0) {
            if (a >= b) {
                a = b;
            }
            return a;
        } else if (a >= 0) {
            return a;
        } else {
            if (b >= 0) {
                return b;
            }
            return -1;
        }
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static int indexOfLastNetworkChar(String a) {
        int origLength = a.length();
        int trimIndex = minPositive(a.indexOf(44), a.indexOf(59));
        if (trimIndex < 0) {
            return origLength - 1;
        }
        return trimIndex - 1;
    }

    public static String extractPostDialPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder();
        int s = phoneNumber.length();
        for (int i = indexOfLastNetworkChar(phoneNumber) + 1; i < s; i++) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static boolean compare(String a, String b) {
        return compare(a, b, false);
    }

    public static boolean compare(Context context, String a, String b) {
        return compare(a, b, context.getResources().getBoolean(R.bool.config_use_strict_phone_number_comparation));
    }

    public static boolean compare(String a, String b, boolean useStrictComparation) {
        return useStrictComparation ? compareStrictly(a, b) : compareLoosely(a, b);
    }

    public static boolean compareLoosely(String a, String b) {
        boolean z = true;
        int numNonDialableCharsInA = 0;
        int numNonDialableCharsInB = 0;
        if (a == null || b == null) {
            if (a != b) {
                z = false;
            }
            return z;
        } else if (a.length() == 0 || b.length() == 0) {
            return false;
        } else {
            int ia = indexOfLastNetworkChar(a);
            int ib = indexOfLastNetworkChar(b);
            int matched = 0;
            while (ia >= 0 && ib >= 0) {
                boolean skipCmp = false;
                char ca = a.charAt(ia);
                if (!isDialable(ca)) {
                    ia--;
                    skipCmp = true;
                    numNonDialableCharsInA++;
                }
                char cb = b.charAt(ib);
                if (!isDialable(cb)) {
                    ib--;
                    skipCmp = true;
                    numNonDialableCharsInB++;
                }
                if (!skipCmp) {
                    if (cb != ca && ca != WILD && cb != WILD) {
                        break;
                    }
                    ia--;
                    ib--;
                    matched++;
                }
            }
            if (matched < MIN_MATCH) {
                int effectiveALen = a.length() - numNonDialableCharsInA;
                return effectiveALen == b.length() - numNonDialableCharsInB && effectiveALen == matched;
            } else if (matched >= MIN_MATCH && (ia < 0 || ib < 0)) {
                return true;
            } else {
                if (matchIntlPrefix(a, ia + 1) && matchIntlPrefix(b, ib + 1)) {
                    return true;
                }
                if (matchTrunkPrefix(a, ia + 1) && matchIntlPrefixAndCC(b, ib + 1)) {
                    return true;
                }
                return matchTrunkPrefix(b, ib + 1) && matchIntlPrefixAndCC(a, ia + 1);
            }
        }
    }

    public static boolean compareStrictly(String a, String b) {
        return compareStrictly(a, b, true);
    }

    public static boolean compareStrictly(String a, String b, boolean acceptInvalidCCCPrefix) {
        if (a == null || b == null) {
            return a == b;
        } else if (a.length() == 0 && b.length() == 0) {
            return false;
        } else {
            char chA;
            char chB;
            int forwardIndexA = 0;
            int forwardIndexB = 0;
            CountryCallingCodeAndNewIndex cccA = tryGetCountryCallingCodeAndNewIndex(a, acceptInvalidCCCPrefix);
            CountryCallingCodeAndNewIndex cccB = tryGetCountryCallingCodeAndNewIndex(b, acceptInvalidCCCPrefix);
            boolean bothHasCountryCallingCode = false;
            boolean okToIgnorePrefix = true;
            boolean trunkPrefixIsOmittedA = false;
            boolean trunkPrefixIsOmittedB = false;
            if (cccA != null && cccB != null) {
                if (cccA.countryCallingCode != cccB.countryCallingCode) {
                    return false;
                }
                okToIgnorePrefix = false;
                bothHasCountryCallingCode = true;
                forwardIndexA = cccA.newIndex;
                forwardIndexB = cccB.newIndex;
            } else if (cccA == null && cccB == null) {
                okToIgnorePrefix = false;
            } else {
                int tmp;
                if (cccA != null) {
                    forwardIndexA = cccA.newIndex;
                } else {
                    tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                    if (tmp >= 0) {
                        forwardIndexA = tmp;
                        trunkPrefixIsOmittedA = true;
                    }
                }
                if (cccB != null) {
                    forwardIndexB = cccB.newIndex;
                } else {
                    tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                    if (tmp >= 0) {
                        forwardIndexB = tmp;
                        trunkPrefixIsOmittedB = true;
                    }
                }
            }
            int backwardIndexA = a.length() - 1;
            int backwardIndexB = b.length() - 1;
            while (backwardIndexA >= forwardIndexA && backwardIndexB >= forwardIndexB) {
                boolean skip_compare = false;
                chA = a.charAt(backwardIndexA);
                chB = b.charAt(backwardIndexB);
                if (isSeparator(chA)) {
                    backwardIndexA--;
                    skip_compare = true;
                }
                if (isSeparator(chB)) {
                    backwardIndexB--;
                    skip_compare = true;
                }
                if (!skip_compare) {
                    if (chA != chB) {
                        return false;
                    }
                    backwardIndexA--;
                    backwardIndexB--;
                }
            }
            if (!okToIgnorePrefix) {
                int i = bothHasCountryCallingCode ^ 1;
                while (backwardIndexA >= forwardIndexA) {
                    chA = a.charAt(backwardIndexA);
                    if (isDialable(chA)) {
                        if (i == 0 || tryGetISODigit(chA) != 1) {
                            return false;
                        }
                        i = 0;
                    }
                    backwardIndexA--;
                }
                while (backwardIndexB >= forwardIndexB) {
                    chB = b.charAt(backwardIndexB);
                    if (isDialable(chB)) {
                        if (i == 0 || tryGetISODigit(chB) != 1) {
                            return false;
                        }
                        i = 0;
                    }
                    backwardIndexB--;
                }
            } else if ((!trunkPrefixIsOmittedA || forwardIndexA > backwardIndexA) && (checkPrefixIsIgnorable(a, forwardIndexA, backwardIndexA) ^ 1) == 0) {
                if ((trunkPrefixIsOmittedB && forwardIndexB <= backwardIndexB) || (checkPrefixIsIgnorable(b, forwardIndexA, backwardIndexB) ^ 1) != 0) {
                    if (acceptInvalidCCCPrefix) {
                        return compare(a, b, false);
                    }
                    return false;
                }
            } else if (acceptInvalidCCCPrefix) {
                return compare(a, b, false);
            } else {
                return false;
            }
            return true;
        }
    }

    public static String toCallerIDMinMatch(String phoneNumber) {
        return internalGetStrippedReversed(extractNetworkPortionAlt(phoneNumber), MIN_MATCH);
    }

    public static String getStrippedReversed(String phoneNumber) {
        String np = extractNetworkPortionAlt(phoneNumber);
        if (np == null) {
            return null;
        }
        return internalGetStrippedReversed(np, np.length());
    }

    private static String internalGetStrippedReversed(String np, int numDigits) {
        if (np == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(numDigits);
        int length = np.length();
        int i = length - 1;
        int s = length;
        while (i >= 0 && length - i <= numDigits) {
            ret.append(np.charAt(i));
            i--;
        }
        return ret.toString();
    }

    public static String stringFromStringAndTOA(String s, int TOA) {
        if (s == null) {
            return null;
        }
        if (TOA != 145 || s.length() <= 0 || s.charAt(0) == PLUS_SIGN_CHAR) {
            return s;
        }
        return PLUS_SIGN_STRING + s;
    }

    public static int toaFromString(String s) {
        if (s == null || s.length() <= 0 || s.charAt(0) != PLUS_SIGN_CHAR) {
            return 129;
        }
        return 145;
    }

    public static String calledPartyBCDToString(byte[] bytes, int offset, int length) {
        boolean prependPlus = false;
        StringBuilder ret = new StringBuilder((length * 2) + 1);
        if (length < 2) {
            return "";
        }
        if ((bytes[offset] & 240) == 144) {
            prependPlus = true;
        }
        internalCalledPartyBCDFragmentToString(ret, bytes, offset + 1, length - 1);
        if (prependPlus && ret.length() == 0) {
            return "";
        }
        if (prependPlus) {
            String retString = ret.toString();
            Matcher m = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(retString);
            if (!m.matches()) {
                m = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(retString);
                if (m.matches()) {
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(2));
                    ret.append(m.group(3));
                    ret.append(PLUS_SIGN_STRING);
                    ret.append(m.group(4));
                } else {
                    ret = new StringBuilder();
                    ret.append(PLUS_SIGN_CHAR);
                    ret.append(retString);
                }
            } else if ("".equals(m.group(2))) {
                ret = new StringBuilder();
                ret.append(m.group(1));
                ret.append(m.group(3));
                ret.append(m.group(4));
                ret.append(m.group(5));
                ret.append(PLUS_SIGN_STRING);
            } else {
                ret = new StringBuilder();
                ret.append(m.group(1));
                ret.append(m.group(2));
                ret.append(m.group(3));
                ret.append(PLUS_SIGN_STRING);
                ret.append(m.group(4));
                ret.append(m.group(5));
            }
        }
        return ret.toString();
    }

    private static void internalCalledPartyBCDFragmentToString(StringBuilder sb, byte[] bytes, int offset, int length) {
        int i = offset;
        while (i < length + offset) {
            char c = bcdToChar((byte) (bytes[i] & 15));
            if (c != 0) {
                sb.append(c);
                byte b = (byte) ((bytes[i] >> 4) & 15);
                if (b == MidiConstants.STATUS_CHANNEL_MASK && i + 1 == length + offset) {
                    break;
                }
                c = bcdToChar(b);
                if (c != 0) {
                    sb.append(c);
                    i++;
                } else {
                    return;
                }
            }
            return;
        }
    }

    public static String calledPartyBCDFragmentToString(byte[] bytes, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);
        internalCalledPartyBCDFragmentToString(ret, bytes, offset, length);
        return ret.toString();
    }

    private static char bcdToChar(byte b) {
        if (b < (byte) 10) {
            return (char) (b + 48);
        }
        switch (b) {
            case (byte) 10:
                return '*';
            case (byte) 11:
                return '#';
            case (byte) 12:
                return ',';
            case (byte) 13:
                return WILD;
            default:
                return 0;
        }
    }

    private static int charToBCD(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c == '*') {
            return 10;
        }
        if (c == '#') {
            return 11;
        }
        if (c == ',') {
            return 12;
        }
        if (c == WILD) {
            return 13;
        }
        if (c == ';') {
            return 14;
        }
        throw new RuntimeException("invalid char for BCD " + c);
    }

    public static boolean isWellFormedSmsAddress(String address) {
        boolean z;
        String networkPortion = extractNetworkPortion(address);
        if (networkPortion.equals(PLUS_SIGN_STRING)) {
            z = true;
        } else {
            z = TextUtils.isEmpty(networkPortion);
        }
        if (z) {
            return false;
        }
        return isDialable(networkPortion);
    }

    public static boolean isGlobalPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        return GLOBAL_PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }

    private static boolean isDialable(String address) {
        int count = address.length();
        for (int i = 0; i < count; i++) {
            if (!isDialable(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNonSeparator(String address) {
        int count = address.length();
        for (int i = 0; i < count; i++) {
            if (!isNonSeparator(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static byte[] networkPortionToCalledPartyBCD(String s) {
        return numberToCalledPartyBCDHelper(extractNetworkPortion(s), false);
    }

    public static byte[] networkPortionToCalledPartyBCDWithLength(String s) {
        return numberToCalledPartyBCDHelper(extractNetworkPortion(s), true);
    }

    public static byte[] numberToCalledPartyBCD(String number) {
        number = OppoTelephonyFunction.stripSeparators(number);
        if (number == null) {
            return null;
        }
        return numberToCalledPartyBCDHelper(number, false);
    }

    private static byte[] numberToCalledPartyBCDHelper(String number, boolean includeLength) {
        int numberLenReal = number.length();
        int numberLenEffective = numberLenReal;
        boolean hasPlus = number.indexOf(43) != -1;
        if (hasPlus) {
            numberLenEffective = numberLenReal - 1;
        }
        if (numberLenEffective == 0) {
            return null;
        }
        int i;
        int resultLen = (numberLenEffective + 1) / 2;
        int extraBytes = 1;
        if (includeLength) {
            extraBytes = 2;
        }
        resultLen += extraBytes;
        byte[] result = new byte[resultLen];
        int digitCount = 0;
        for (int i2 = 0; i2 < numberLenReal; i2++) {
            char c = number.charAt(i2);
            if (c != PLUS_SIGN_CHAR) {
                i = (digitCount >> 1) + extraBytes;
                result[i] = (byte) (result[i] | ((byte) ((charToBCD(c) & 15) << ((digitCount & 1) == 1 ? 4 : 0))));
                digitCount++;
            }
        }
        if ((digitCount & 1) == 1) {
            i = (digitCount >> 1) + extraBytes;
            result[i] = (byte) (result[i] | 240);
        }
        int offset = 0;
        if (includeLength) {
            offset = 1;
            result[0] = (byte) (resultLen - 1);
        }
        result[offset] = (byte) (hasPlus ? 145 : 129);
        return result;
    }

    @Deprecated
    public static String formatNumber(String source) {
        Editable text = new SpannableStringBuilder(source);
        formatNumber(text, getFormatTypeForLocale(Locale.getDefault()));
        return text.toString();
    }

    @Deprecated
    public static String formatNumber(String source, int defaultFormattingType) {
        Editable text = new SpannableStringBuilder(source);
        formatNumber(text, defaultFormattingType);
        return text.toString();
    }

    @Deprecated
    public static int getFormatTypeForLocale(Locale locale) {
        return getFormatTypeFromCountryCode(locale.getCountry());
    }

    @Deprecated
    public static void formatNumber(Editable text, int defaultFormattingType) {
        int formatType = defaultFormattingType;
        if (text.length() > 2 && text.charAt(0) == PLUS_SIGN_CHAR) {
            formatType = text.charAt(1) == '1' ? 1 : (text.length() >= 3 && text.charAt(1) == '8' && text.charAt(2) == '1') ? 2 : 0;
        }
        switch (formatType) {
            case 0:
                removeDashes(text);
                return;
            case 1:
                formatNanpNumber(text);
                return;
            case 2:
                formatJapaneseNumber(text);
                return;
            default:
                return;
        }
    }

    /* JADX WARNING: Missing block: B:15:0x003a, code:
            r2 = r2 + 1;
            r6 = r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    public static void formatNanpNumber(Editable text) {
        int length = text.length();
        if (length <= "+1-nnn-nnn-nnnn".length() && length > 5) {
            int numDashes;
            CharSequence saved = text.subSequence(0, length);
            removeDashes(text);
            length = text.length();
            int[] dashPositions = new int[3];
            int state = 1;
            int numDigits = 0;
            int i = 0;
            int numDashes2 = 0;
            while (i < length) {
                switch (text.charAt(i)) {
                    case '+':
                        if (i != 0) {
                            break;
                        }
                        state = 2;
                        numDashes = numDashes2;
                        continue;
                    case '-':
                        state = 4;
                        numDashes = numDashes2;
                        continue;
                    case '1':
                        if (numDigits == 0 || state == 2) {
                            state = 3;
                            numDashes = numDashes2;
                            continue;
                        }
                    case '0':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        if (state == 2) {
                            text.replace(0, length, saved);
                            return;
                        }
                        if (state == 3) {
                            numDashes = numDashes2 + 1;
                            dashPositions[numDashes2] = i;
                        } else if (state == 4 || !(numDigits == 3 || numDigits == 6)) {
                            numDashes = numDashes2;
                        } else {
                            numDashes = numDashes2 + 1;
                            dashPositions[numDashes2] = i;
                        }
                        state = 1;
                        numDigits++;
                        continue;
                    default:
                        break;
                }
                text.replace(0, length, saved);
                return;
            }
            if (numDigits == 7) {
                numDashes = numDashes2 - 1;
            } else {
                numDashes = numDashes2;
            }
            for (i = 0; i < numDashes; i++) {
                int pos = dashPositions[i];
                text.replace(pos + i, pos + i, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            }
            int len = text.length();
            while (len > 0 && text.charAt(len - 1) == '-') {
                text.delete(len - 1, len);
                len--;
            }
        }
    }

    @Deprecated
    public static void formatJapaneseNumber(Editable text) {
        JapanesePhoneNumberFormatter.format(text);
    }

    private static void removeDashes(Editable text) {
        int p = 0;
        while (p < text.length()) {
            if (text.charAt(p) == '-') {
                text.delete(p, p + 1);
            } else {
                p++;
            }
        }
    }

    public static String formatNumberToE164(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberFormat.E164);
    }

    public static String formatNumberToRFC3966(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberFormat.RFC3966);
    }

    private static String formatNumberInternal(String rawPhoneNumber, String defaultCountryIso, PhoneNumberFormat formatIdentifier) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber phoneNumber = util.parse(rawPhoneNumber, defaultCountryIso);
            if (util.isValidNumber(phoneNumber)) {
                return util.format(phoneNumber, formatIdentifier);
            }
        } catch (NumberParseException e) {
        }
        return null;
    }

    public static boolean isInternationalNumber(String phoneNumber, String defaultCountryIso) {
        boolean z = false;
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.startsWith("#") || phoneNumber.startsWith(PhoneConstants.APN_TYPE_ALL)) {
            return false;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            if (util.parseAndKeepRawInput(phoneNumber, defaultCountryIso).getCountryCode() != util.getCountryCodeForRegion(defaultCountryIso)) {
                z = true;
            }
            return z;
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static String formatNumber(String phoneNumber, String defaultCountryIso) {
        if (phoneNumber.startsWith("#") || phoneNumber.startsWith(PhoneConstants.APN_TYPE_ALL)) {
            return phoneNumber;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        String result = null;
        try {
            PhoneNumber pn = util.parseAndKeepRawInput(phoneNumber, defaultCountryIso);
            if (KOREA_ISO_COUNTRY_CODE.equalsIgnoreCase(defaultCountryIso) && pn.getCountryCode() == util.getCountryCodeForRegion(KOREA_ISO_COUNTRY_CODE) && pn.getCountryCodeSource() == CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN) {
                result = util.format(pn, PhoneNumberFormat.NATIONAL);
                return result;
            }
            if (JAPAN_ISO_COUNTRY_CODE.equalsIgnoreCase(defaultCountryIso) && pn.getCountryCode() == util.getCountryCodeForRegion(JAPAN_ISO_COUNTRY_CODE) && pn.getCountryCodeSource() == CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN) {
                result = util.format(pn, PhoneNumberFormat.NATIONAL);
            } else {
                result = util.formatInOriginalFormat(pn, defaultCountryIso);
            }
            return result;
        } catch (NumberParseException e) {
        }
    }

    public static String formatNumber(String phoneNumber, String phoneNumberE164, String defaultCountryIso) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            if (!isDialable(phoneNumber.charAt(i))) {
                return phoneNumber;
            }
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        if (phoneNumberE164 != null && phoneNumberE164.length() >= 2 && phoneNumberE164.charAt(0) == PLUS_SIGN_CHAR) {
            try {
                String regionCode = util.getRegionCodeForNumber(util.parse(phoneNumberE164, "ZZ"));
                if (!TextUtils.isEmpty(regionCode) && normalizeNumber(phoneNumber).indexOf(phoneNumberE164.substring(1)) <= 0) {
                    defaultCountryIso = regionCode;
                }
            } catch (NumberParseException e) {
            }
        }
        String result = formatNumber(phoneNumber, defaultCountryIso);
        if (result == null) {
            result = phoneNumber;
        }
        return result;
    }

    public static String normalizeNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (sb.length() == 0 && c == PLUS_SIGN_CHAR) {
                sb.append(c);
            } else if ((c >= DateFormat.AM_PM && c <= DateFormat.TIME_ZONE) || (c >= DateFormat.CAPITAL_AM_PM && c <= 'Z')) {
                return normalizeNumber(convertKeypadLettersToDigits(phoneNumber));
            }
        }
        return sb.toString();
    }

    public static String replaceUnicodeDigits(String number) {
        StringBuilder normalizedDigits = new StringBuilder(number.length());
        for (char c : number.toCharArray()) {
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                normalizedDigits.append(digit);
            } else {
                normalizedDigits.append(c);
            }
        }
        return normalizedDigits.toString();
    }

    public static boolean isEmergencyNumber(String number) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    public static boolean isEmergencyNumber(int subId, String number) {
        return isEmergencyNumberInternal(subId, number, true);
    }

    public static boolean isPotentialEmergencyNumber(String number) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    public static boolean isPotentialEmergencyNumber(int subId, String number) {
        return isEmergencyNumberInternal(subId, number, false);
    }

    private static boolean isEmergencyNumberInternal(String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, useExactMatch);
    }

    private static boolean isEmergencyNumberInternal(int subId, String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(subId, number, null, useExactMatch);
    }

    public static boolean isEmergencyNumber(String number, String defaultCountryIso) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    public static boolean isEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number, defaultCountryIso, true);
    }

    public static boolean isPotentialEmergencyNumber(String number, String defaultCountryIso) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    public static boolean isPotentialEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number, defaultCountryIso, false);
    }

    private static boolean isEmergencyNumberInternal(String number, String defaultCountryIso, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, defaultCountryIso, useExactMatch);
    }

    private static boolean isEmergencyNumberInternal(int subId, String number, String defaultCountryIso, boolean useExactMatch) {
        if (number == null) {
            return false;
        }
        if (isUriNumber(number)) {
            return false;
        }
        number = extractNetworkPortionAlt(number);
        String emergencyNumbers = "";
        int slotId = SubscriptionManager.getSlotIndex(subId);
        emergencyNumbers = SystemProperties.get(slotId <= 0 ? "ril.ecclist" : "ril.ecclist" + slotId, "");
        Rlog.d(LOG_TAG, "slotId:" + slotId + " subId:" + subId + " country:" + defaultCountryIso + " emergencyNumbers: " + emergencyNumbers);
        if (TextUtils.isEmpty(emergencyNumbers)) {
            emergencyNumbers = SystemProperties.get("ro.ril.ecclist");
        }
        if (!TextUtils.isEmpty(emergencyNumbers)) {
            for (String emergencyNum : emergencyNumbers.split(",")) {
                if (useExactMatch || "BR".equalsIgnoreCase(defaultCountryIso)) {
                    if (number.equals(emergencyNum)) {
                        return true;
                    }
                } else if (number.equals(emergencyNum)) {
                    return true;
                }
            }
        }
        Rlog.d(LOG_TAG, "System property doesn't provide any emergency numbers. Use embedded logic for determining ones.");
        String version = SystemProperties.get("ro.oppo.version", "CN");
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        boolean hasIccCard = (slotId < 0 || slotId >= 2) ? false : TelephonyManager.getDefault().hasIccCard(slotId);
        int hideEmergency = hasIccCard ? "AU".equals(region) : 0;
        if (version.equalsIgnoreCase("US") && (hideEmergency ^ 1) != 0) {
            emergencyNumbers = SystemProperties.get("ril.ecclist", "");
            if (!TextUtils.isEmpty(emergencyNumbers)) {
                for (String emergencyNum2 : emergencyNumbers.split(",")) {
                    if (number.equals(emergencyNum2)) {
                        Rlog.d(LOG_TAG, "It seems that number:" + number + " is Slot0's sim ecc number, is in emergencyNumbers:" + emergencyNumbers);
                        return true;
                    }
                }
            }
            emergencyNumbers = SystemProperties.get("ril.ecclist1", "");
            if (!TextUtils.isEmpty(emergencyNumbers)) {
                for (String emergencyNum22 : emergencyNumbers.split(",")) {
                    if (number.equals(emergencyNum22)) {
                        Rlog.d(LOG_TAG, "It seems that number:" + number + " is Slot1's sim ecc number, is in emergencyNumbers:" + emergencyNumbers);
                        return true;
                    }
                }
            }
        }
        if (!(version.equalsIgnoreCase("US") && hideEmergency == 0)) {
            for (String emergencyNum222 : (slotId < 0 ? "112,911,000,08,110,118,119,999" : "112,911").split(",")) {
                if (useExactMatch) {
                    if (number.equals(emergencyNum222)) {
                        return true;
                    }
                } else if (number.startsWith(emergencyNum222)) {
                    return true;
                }
            }
        }
        if (!version.equalsIgnoreCase("US")) {
            return false;
        }
        if (hideEmergency != 0) {
            return false;
        }
        if (hasIccCard && "RU".equals(region) && "119".equals(number)) {
            return false;
        }
        return oppoIsEmergencyNumberInternal(number, useExactMatch);
    }

    public static boolean isLocalEmergencyNumber(Context context, String number) {
        return isLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    public static boolean isLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number, context, true);
    }

    public static boolean isPotentialLocalEmergencyNumber(Context context, String number) {
        return isPotentialLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    public static boolean isPotentialLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number, context, false);
    }

    private static boolean isLocalEmergencyNumberInternal(String number, Context context, boolean useExactMatch) {
        return isLocalEmergencyNumberInternal(getDefaultVoiceSubId(), number, context, useExactMatch);
    }

    private static boolean isLocalEmergencyNumberInternal(int subId, String number, Context context, boolean useExactMatch) {
        String countryIso = getCountryIso(context);
        Rlog.w(LOG_TAG, "isLocalEmergencyNumberInternal" + countryIso);
        if (countryIso == null) {
            countryIso = context.getResources().getConfiguration().locale.getCountry();
            Rlog.w(LOG_TAG, "No CountryDetector; falling back to countryIso based on locale: " + countryIso);
        }
        return isEmergencyNumberInternal(subId, number, countryIso, useExactMatch);
    }

    private static String getCountryIso(Context context) {
        Rlog.w(LOG_TAG, "getCountryIso " + sCountryDetector);
        if (sCountryDetector == null) {
            CountryDetector detector = (CountryDetector) context.getSystemService("country_detector");
            if (detector != null) {
                sCountryDetector = detector.detectCountry();
            }
        }
        if (sCountryDetector == null) {
            return null;
        }
        return sCountryDetector.getCountryIso();
    }

    public static void resetCountryDetectorInfo() {
        sCountryDetector = null;
    }

    public static boolean isVoiceMailNumber(String number) {
        return isVoiceMailNumber(SubscriptionManager.getDefaultSubscriptionId(), number);
    }

    public static boolean isVoiceMailNumber(int subId, String number) {
        return isVoiceMailNumber(null, subId, number);
    }

    public static boolean isVoiceMailNumber(Context context, int subId, String number) {
        TelephonyManager tm;
        if (context == null) {
            try {
                tm = TelephonyManager.getDefault();
                if (DBG) {
                    log("isVoiceMailNumber: default tm");
                }
            } catch (SecurityException e) {
                if (DBG) {
                    log("isVoiceMailNumber: SecurityExcpetion caught");
                }
                return false;
            }
        }
        tm = TelephonyManager.from(context);
        if (DBG) {
            log("isVoiceMailNumber: tm from context");
        }
        String vmNumber = tm.getVoiceMailNumber(subId);
        String mdn = tm.getLine1Number(subId);
        if (DBG) {
            log("isVoiceMailNumber: mdn=" + mdn + ", vmNumber=" + vmNumber + ", number=" + number);
        }
        number = extractNetworkPortionAlt(number);
        if (TextUtils.isEmpty(number)) {
            if (DBG) {
                log("isVoiceMailNumber: number is empty after stripping");
            }
            return false;
        }
        boolean compareWithMdn = false;
        if (context != null) {
            CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
            if (configManager != null) {
                PersistableBundle b = configManager.getConfigForSubId(subId);
                if (b != null) {
                    compareWithMdn = b.getBoolean(CarrierConfigManager.KEY_MDN_IS_ADDITIONAL_VOICEMAIL_NUMBER_BOOL);
                    if (DBG) {
                        log("isVoiceMailNumber: compareWithMdn=" + compareWithMdn);
                    }
                }
            }
        }
        if (compareWithMdn) {
            if (DBG) {
                log("isVoiceMailNumber: treating mdn as additional vm number");
            }
            return !compare(number, vmNumber) ? compare(number, mdn) : true;
        }
        if (DBG) {
            log("isVoiceMailNumber: returning regular compare");
        }
        return compare(number, vmNumber);
    }

    public static String convertKeypadLettersToDigits(String input) {
        if (input == null) {
            return input;
        }
        int len = input.length();
        if (len == 0) {
            return input;
        }
        char[] out = input.toCharArray();
        for (int i = 0; i < len; i++) {
            char c = out[i];
            out[i] = (char) KEYPAD_MAP.get(c, c);
        }
        return new String(out);
    }

    public static String cdmaCheckAndProcessPlusCode(String dialStr) {
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (!(TextUtils.isEmpty(currIso) || (TextUtils.isEmpty(defaultIso) ^ 1) == 0)) {
                return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, getFormatTypeFromCountryCode(currIso), getFormatTypeFromCountryCode(defaultIso));
            }
        }
        return dialStr;
    }

    public static String cdmaCheckAndProcessPlusCodeForSms(String dialStr) {
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (!TextUtils.isEmpty(defaultIso)) {
                int format = getFormatTypeFromCountryCode(defaultIso);
                return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, format, format);
            }
        }
        return dialStr;
    }

    public static String cdmaCheckAndProcessPlusCodeByNumberFormat(String dialStr, int currFormat, int defaultFormat) {
        String retStr = dialStr;
        boolean useNanp = currFormat == defaultFormat && currFormat == 1;
        if (dialStr != null && dialStr.lastIndexOf(PLUS_SIGN_STRING) != -1) {
            String tempDialStr = dialStr;
            retStr = null;
            if (DBG) {
                log("checkAndProcessPlusCode,dialStr=" + dialStr);
            }
            do {
                String networkDialStr;
                if (useNanp) {
                    networkDialStr = extractNetworkPortion(tempDialStr);
                } else {
                    networkDialStr = extractNetworkPortionAlt(tempDialStr);
                }
                networkDialStr = processPlusCode(networkDialStr, useNanp);
                if (!TextUtils.isEmpty(networkDialStr)) {
                    if (retStr == null) {
                        retStr = networkDialStr;
                    } else {
                        retStr = retStr.concat(networkDialStr);
                    }
                    String postDialStr = extractPostDialPortion(tempDialStr);
                    if (!TextUtils.isEmpty(postDialStr)) {
                        int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);
                        if (dialableIndex >= 1) {
                            retStr = appendPwCharBackToOrigDialStr(dialableIndex, retStr, postDialStr);
                            tempDialStr = postDialStr.substring(dialableIndex);
                        } else {
                            if (dialableIndex < 0) {
                                postDialStr = "";
                            }
                            Rlog.e("wrong postDialStr=", postDialStr);
                        }
                    }
                    if (DBG) {
                        log("checkAndProcessPlusCode,postDialStr=" + postDialStr);
                    }
                    if (TextUtils.isEmpty(postDialStr)) {
                        break;
                    }
                } else {
                    Rlog.e("checkAndProcessPlusCode: null newDialStr", networkDialStr);
                    return dialStr;
                }
            } while ((TextUtils.isEmpty(tempDialStr) ^ 1) != 0);
        }
        return retStr;
    }

    public static CharSequence createTtsSpannable(CharSequence phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        Spannable spannable = Factory.getInstance().newSpannable(phoneNumber);
        addTtsSpan(spannable, 0, spannable.length());
        return spannable;
    }

    public static void addTtsSpan(Spannable s, int start, int endExclusive) {
        s.setSpan(createTtsSpan(s.subSequence(start, endExclusive).toString()), start, endExclusive, 33);
    }

    @Deprecated
    public static CharSequence ttsSpanAsPhoneNumber(CharSequence phoneNumber) {
        return createTtsSpannable(phoneNumber);
    }

    @Deprecated
    public static void ttsSpanAsPhoneNumber(Spannable s, int start, int end) {
        addTtsSpan(s, start, end);
    }

    public static TtsSpan createTtsSpan(String phoneNumberString) {
        if (phoneNumberString == null) {
            return null;
        }
        PhoneNumber phoneNumber = null;
        try {
            phoneNumber = PhoneNumberUtil.getInstance().parse(phoneNumberString, null);
        } catch (NumberParseException e) {
        }
        TelephoneBuilder builder = new TelephoneBuilder();
        if (phoneNumber == null) {
            builder.setNumberParts(splitAtNonNumerics(phoneNumberString));
        } else {
            if (phoneNumber.hasCountryCode()) {
                builder.setCountryCode(Integer.toString(phoneNumber.getCountryCode()));
            }
            builder.setNumberParts(Long.toString(phoneNumber.getNationalNumber()));
        }
        return builder.build();
    }

    private static String splitAtNonNumerics(CharSequence number) {
        StringBuilder sb = new StringBuilder(number.length());
        for (int i = 0; i < number.length(); i++) {
            Object valueOf;
            if (is12Key(number.charAt(i))) {
                valueOf = Character.valueOf(number.charAt(i));
            } else {
                valueOf = " ";
            }
            sb.append(valueOf);
        }
        return sb.toString().replaceAll(" +", " ").trim();
    }

    private static String getCurrentIdp(boolean useNanp) {
        int i = 0;
        String mSimConfig = SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG, "");
        if (mSimConfig.equals("dsds") || mSimConfig.equals("dsda")) {
            int phoneid = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId());
            String ret = TelephonyManager.getTelephonyProperty(phoneid, TelephonyProperties.PROPERTY_OPERATOR_IDP_STRING, "");
            if (TextUtils.isEmpty(ret)) {
                if (phoneid == 0) {
                    i = 1;
                }
                ret = TelephonyManager.getTelephonyProperty(i, TelephonyProperties.PROPERTY_OPERATOR_IDP_STRING, "");
            }
            if (TextUtils.isEmpty(ret)) {
                ret = useNanp ? NANP_IDP_STRING : PLUS_SIGN_STRING;
            }
            return ret;
        }
        String ps;
        if (useNanp) {
            ps = NANP_IDP_STRING;
        } else {
            ps = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_IDP_STRING, PLUS_SIGN_STRING);
        }
        return ps;
    }

    private static boolean isTwoToNine(char c) {
        if (c < '2' || c > '9') {
            return false;
        }
        return true;
    }

    private static int getFormatTypeFromCountryCode(String country) {
        for (String compareToIgnoreCase : NANP_COUNTRIES) {
            if (compareToIgnoreCase.compareToIgnoreCase(country) == 0) {
                return 1;
            }
        }
        if ("jp".compareToIgnoreCase(country) == 0) {
            return 2;
        }
        return 0;
    }

    public static boolean isNanp(String dialStr) {
        if (dialStr == null) {
            Rlog.e("isNanp: null dialStr passed in", dialStr);
            return false;
        } else if (dialStr.length() != 10 || !isTwoToNine(dialStr.charAt(0)) || !isTwoToNine(dialStr.charAt(3))) {
            return false;
        } else {
            for (int i = 1; i < 10; i++) {
                if (!isISODigit(dialStr.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isOneNanp(String dialStr) {
        if (dialStr != null) {
            String newDialStr = dialStr.substring(1);
            if (dialStr.charAt(0) == '1' && isNanp(newDialStr)) {
                return true;
            }
            return false;
        }
        Rlog.e("isOneNanp: null dialStr passed in", dialStr);
        return false;
    }

    public static boolean isUriNumber(String number) {
        if (number != null) {
            return !number.contains("@") ? number.contains("%40") : true;
        } else {
            return false;
        }
    }

    public static String getUsernameFromUriNumber(String number) {
        int delimiterIndex = number.indexOf(64);
        if (delimiterIndex < 0) {
            delimiterIndex = number.indexOf("%40");
        }
        if (delimiterIndex < 0) {
            Rlog.w(LOG_TAG, "getUsernameFromUriNumber: no delimiter found in SIP addr '" + number + "'");
            delimiterIndex = number.length();
        }
        return number.substring(0, delimiterIndex);
    }

    public static Uri convertSipUriToTelUri(Uri source) {
        if (!"sip".equals(source.getScheme())) {
            return source;
        }
        String[] numberParts = source.getSchemeSpecificPart().split("[@;:]");
        if (numberParts.length == 0) {
            return source;
        }
        return Uri.fromParts(PhoneAccount.SCHEME_TEL, numberParts[0], null);
    }

    private static String processPlusCode(String networkDialStr, boolean useNanp) {
        String retStr = networkDialStr;
        if (DBG) {
            log("processPlusCode, networkDialStr = " + networkDialStr + "for NANP = " + useNanp);
        }
        if (networkDialStr != null && networkDialStr.charAt(0) == PLUS_SIGN_CHAR && networkDialStr.length() > 1) {
            String newStr = networkDialStr.substring(1);
            retStr = (useNanp && isOneNanp(newStr)) ? newStr : networkDialStr.replaceFirst("[+]", getCurrentIdp(useNanp));
        }
        if (DBG) {
            log("processPlusCode, retStr=" + retStr);
        }
        return retStr;
    }

    private static int findDialableIndexFromPostDialStr(String postDialStr) {
        for (int index = 0; index < postDialStr.length(); index++) {
            if (isReallyDialable(postDialStr.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static String appendPwCharBackToOrigDialStr(int dialableIndex, String origStr, String dialStr) {
        if (dialableIndex == 1) {
            return dialStr.charAt(0);
        }
        return origStr.concat(dialStr.substring(0, dialableIndex));
    }

    private static boolean matchIntlPrefix(String a, int len) {
        boolean z = true;
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            switch (state) {
                case 0:
                    if (c != PLUS_SIGN_CHAR) {
                        if (c != '0') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return false;
                        }
                        state = 2;
                        break;
                    }
                    state = 1;
                    break;
                case 2:
                    if (c != '0') {
                        if (c != '1') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return false;
                        }
                        state = 4;
                        break;
                    }
                    state = 3;
                    break;
                case 4:
                    if (c != '1') {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return false;
                    }
                    state = 5;
                    break;
                default:
                    if (!isNonSeparator(c)) {
                        break;
                    }
                    return false;
            }
        }
        if (!(state == 1 || state == 3 || state == 5)) {
            z = false;
        }
        return z;
    }

    private static boolean matchIntlPrefixAndCC(String a, int len) {
        boolean z = true;
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            switch (state) {
                case 0:
                    if (c != PLUS_SIGN_CHAR) {
                        if (c != '0') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return false;
                        }
                        state = 2;
                        break;
                    }
                    state = 1;
                    break;
                case 1:
                case 3:
                case 5:
                    if (!isISODigit(c)) {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return false;
                    }
                    state = 6;
                    break;
                case 2:
                    if (c != '0') {
                        if (c != '1') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return false;
                        }
                        state = 4;
                        break;
                    }
                    state = 3;
                    break;
                case 4:
                    if (c != '1') {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return false;
                    }
                    state = 5;
                    break;
                case 6:
                case 7:
                    if (!isISODigit(c)) {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return false;
                    }
                    state++;
                    break;
                default:
                    if (!isNonSeparator(c)) {
                        break;
                    }
                    return false;
            }
        }
        if (!(state == 6 || state == 7 || state == 8)) {
            z = false;
        }
        return z;
    }

    private static boolean matchTrunkPrefix(String a, int len) {
        boolean found = false;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            if (c == '0' && (found ^ 1) != 0) {
                found = true;
            } else if (isNonSeparator(c)) {
                return false;
            }
        }
        return found;
    }

    private static boolean isCountryCallingCode(int countryCallingCodeCandidate) {
        if (countryCallingCodeCandidate <= 0 || countryCallingCodeCandidate >= CCC_LENGTH) {
            return false;
        }
        return COUNTRY_CALLING_CALL[countryCallingCodeCandidate];
    }

    private static int tryGetISODigit(char ch) {
        if ('0' > ch || ch > '9') {
            return -1;
        }
        return ch - 48;
    }

    private static CountryCallingCodeAndNewIndex tryGetCountryCallingCodeAndNewIndex(String str, boolean acceptThailandCase) {
        int state = 0;
        int ccc = 0;
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            switch (state) {
                case 0:
                    if (ch != PLUS_SIGN_CHAR) {
                        if (ch != '0') {
                            if (ch != '1') {
                                if (!isDialable(ch)) {
                                    break;
                                }
                                return null;
                            } else if (acceptThailandCase) {
                                state = 8;
                                break;
                            } else {
                                return null;
                            }
                        }
                        state = 2;
                        break;
                    }
                    state = 1;
                    break;
                case 1:
                case 3:
                case 5:
                case 6:
                case 7:
                    int ret = tryGetISODigit(ch);
                    if (ret <= 0) {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    ccc = (ccc * 10) + ret;
                    if (ccc < 100 && !isCountryCallingCode(ccc)) {
                        if (state != 1 && state != 3 && state != 5) {
                            state++;
                            break;
                        }
                        state = 6;
                        break;
                    }
                    return new CountryCallingCodeAndNewIndex(ccc, i + 1);
                    break;
                case 2:
                    if (ch != '0') {
                        if (ch != '1') {
                            if (!isDialable(ch)) {
                                break;
                            }
                            return null;
                        }
                        state = 4;
                        break;
                    }
                    state = 3;
                    break;
                case 4:
                    if (ch != '1') {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    state = 5;
                    break;
                case 8:
                    if (ch != '6') {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    state = 9;
                    break;
                case 9:
                    if (ch == '6') {
                        return new CountryCallingCodeAndNewIndex(66, i + 1);
                    }
                    return null;
                default:
                    return null;
            }
        }
        return null;
    }

    private static int tryGetTrunkPrefixOmittedIndex(String str, int currentIndex) {
        int length = str.length();
        for (int i = currentIndex; i < length; i++) {
            char ch = str.charAt(i);
            if (tryGetISODigit(ch) >= 0) {
                return i + 1;
            }
            if (isDialable(ch)) {
                return -1;
            }
        }
        return -1;
    }

    private static boolean checkPrefixIsIgnorable(String str, int forwardIndex, int backwardIndex) {
        boolean trunk_prefix_was_read = false;
        while (backwardIndex >= forwardIndex) {
            if (tryGetISODigit(str.charAt(backwardIndex)) >= 0) {
                if (trunk_prefix_was_read) {
                    return false;
                }
                trunk_prefix_was_read = true;
            } else if (isDialable(str.charAt(backwardIndex))) {
                return false;
            }
            backwardIndex--;
        }
        return true;
    }

    private static int getDefaultVoiceSubId() {
        return SubscriptionManager.getDefaultVoiceSubscriptionId();
    }

    public static String convertToEmergencyNumber(Context context, String number) {
        if (context == null || TextUtils.isEmpty(number)) {
            return number;
        }
        String normalizedNumber = normalizeNumber(number);
        if (isEmergencyNumber(normalizedNumber)) {
            return number;
        }
        if (sConvertToEmergencyMap == null) {
            sConvertToEmergencyMap = context.getResources().getStringArray(R.array.config_convert_to_emergency_number_map);
        }
        if (sConvertToEmergencyMap == null || sConvertToEmergencyMap.length == 0) {
            return number;
        }
        for (String convertMap : sConvertToEmergencyMap) {
            if (DBG) {
                log("convertToEmergencyNumber: " + convertMap);
            }
            String[] entry = null;
            String[] filterNumbers = null;
            String convertedNumber = null;
            if (!TextUtils.isEmpty(convertMap)) {
                entry = convertMap.split(SettingsStringUtil.DELIMITER);
            }
            if (entry != null && entry.length == 2) {
                convertedNumber = entry[1];
                if (!TextUtils.isEmpty(entry[0])) {
                    filterNumbers = entry[0].split(",");
                }
            }
            if (!(TextUtils.isEmpty(convertedNumber) || filterNumbers == null || filterNumbers.length == 0)) {
                int length = filterNumbers.length;
                int i = 0;
                while (i < length) {
                    String filterNumber = filterNumbers[i];
                    if (DBG) {
                        log("convertToEmergencyNumber: filterNumber = " + filterNumber + ", convertedNumber = " + convertedNumber);
                    }
                    if (TextUtils.isEmpty(filterNumber) || !filterNumber.equals(normalizedNumber)) {
                        i++;
                    } else {
                        if (DBG) {
                            log("convertToEmergencyNumber: Matched. Successfully converted to: " + convertedNumber);
                        }
                        return convertedNumber;
                    }
                }
                continue;
            }
        }
        return number;
    }

    private static String[] getEmergencyNumList() {
        String[] emergencyNumList = new String[]{"112", "911", "000", "08", "110", "118", "119", "999", "122", "120"};
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        if (region.equalsIgnoreCase("TH")) {
            return new String[]{"112", "911", "000", "08", "110", "118", "119", "999", "122", "120", "191", "1195", "1199", "199", "1669"};
        } else if (region.equalsIgnoreCase("ID")) {
            return new String[]{"112", "911", "000", "08", "110", "118", "119", "999", "122", "120", "113", "1131", "115", "129", "123"};
        } else if (region.equalsIgnoreCase("VN")) {
            return new String[]{"112", "911", "000", "08", "110", "118", "119", "999", "122", "120", "113", "114", "115"};
        } else if (region.equalsIgnoreCase("RU")) {
            return new String[]{"112", "911", "119"};
        } else if (region.equalsIgnoreCase("MX")) {
            return new String[]{"112", "911", "000", "08", "118", "119", "999", "060", "065", "066", "068"};
        } else if (region.equalsIgnoreCase("IN")) {
            return new String[]{"112", "911", "100", "101", "102", "108"};
        } else if (region.equalsIgnoreCase("MY")) {
            return new String[]{"112", "911", "08", "118", "119", "999", "994", "991"};
        } else if (region.equalsIgnoreCase("SG")) {
            return new String[]{"112", "911", "000", "08", "118", "119", "999", "1777"};
        } else if (region.equalsIgnoreCase("TW")) {
            return new String[]{"112", "911", "000", "08", "110", "118", "119", "113", "165"};
        } else if (region.equalsIgnoreCase("DK")) {
            return new String[]{"112", "911", "000", "08", "114", "118", "119"};
        } else if (region.equalsIgnoreCase("PH")) {
            return new String[]{"112", "911", "000", "08", "118", "117"};
        } else if (region.equalsIgnoreCase("MM")) {
            return new String[]{"112", "911", "000", "08", "118", "199", "191", "192"};
        } else if (region.equalsIgnoreCase("PK")) {
            return new String[]{"112", "911", "000", "08", "15", "16", "115", "1122"};
        } else if (region.equalsIgnoreCase("NG")) {
            return new String[]{"112", "911", "000", "08", "119"};
        } else if (region.equalsIgnoreCase("DE")) {
            return new String[]{"112", "911", "000", "08", "110"};
        } else if (region.equalsIgnoreCase("NL")) {
            return new String[]{"112", "911"};
        } else if (region.equalsIgnoreCase("SE")) {
            return new String[]{"112", "911", "000", "08"};
        } else if (region.equalsIgnoreCase("TR")) {
            return new String[]{"112", "911", "000", "08", "110", "155"};
        } else if (region.equalsIgnoreCase("IR")) {
            return new String[]{"112", "911", "000", "08", "115", "125", "110"};
        } else if (region.equalsIgnoreCase("AE")) {
            return new String[]{"112", "911", "000", "08", "998", "999", "997", "996", "993", "991", "992"};
        } else if (region.equalsIgnoreCase("EG")) {
            return new String[]{"112", "911", "000", "08"};
        } else if (region.equalsIgnoreCase("AU")) {
            return new String[]{"112", "911", "000", "08", "110", "999", "118", "119"};
        } else if (region.equalsIgnoreCase("DZ")) {
            return new String[]{"112", "911", "000", "08", "14", "17", "1055", "1548"};
        } else if (region.equalsIgnoreCase("BH")) {
            return new String[]{"112", "911", "999", "998", "997", "199", "990", "992", "994"};
        } else if (region.equalsIgnoreCase("OM")) {
            return new String[]{"112", "911", "9999", "15", "154"};
        } else if (region.equalsIgnoreCase("QA")) {
            return new String[]{"112", "911", "999", "991", "992", "996", "998"};
        } else if (region.equalsIgnoreCase("KW")) {
            return new String[]{"112", "911", "777"};
        } else if (region.equalsIgnoreCase("MA")) {
            return new String[]{"112", "911", "000", "08", "15", "16", "19", "177"};
        } else if (region.equalsIgnoreCase("BD")) {
            return new String[]{"112", "911", "999"};
        } else if (region.equalsIgnoreCase("KE")) {
            return new String[]{"112", "911", "999"};
        } else if (region.equalsIgnoreCase("LA")) {
            return new String[]{"112", "911", "1191"};
        } else if (region.equalsIgnoreCase("LK")) {
            return new String[]{"112", "911", "110", "118", "119"};
        } else if (region.equalsIgnoreCase("KZ")) {
            return new String[]{"112", "911", "101", "102", "103", "104"};
        } else if (region.equalsIgnoreCase("NZ")) {
            return new String[]{"112", "911", "111", "000", "119", "999"};
        } else if (region.equalsIgnoreCase("NP")) {
            return new String[]{"197", "100", "101", "102", "911"};
        } else if (region.equalsIgnoreCase(JAPAN_ISO_COUNTRY_CODE)) {
            return new String[]{"110", "118", "119", "112", "911"};
        } else if (region.equalsIgnoreCase("SA")) {
            return new String[]{"112", "911", "993", "994", "996", "966", "999", "998", "997"};
        } else if (region.equalsIgnoreCase("PL")) {
            return new String[]{"112", "999", "998", "997"};
        } else if (region.equalsIgnoreCase("FR")) {
            return new String[]{"112", "911", "999"};
        } else if (region.equalsIgnoreCase("IT")) {
            return new String[]{"112", "118", "911"};
        } else if (region.equalsIgnoreCase("ES")) {
            return new String[]{"112", "911"};
        } else if (region.equalsIgnoreCase("KH")) {
            return new String[]{"117", "118", "119"};
        } else if (region.equalsIgnoreCase("GB")) {
            return new String[]{"112", "999"};
        } else if (!region.equalsIgnoreCase("TN")) {
            return emergencyNumList;
        } else {
            return new String[]{"198", "190", "197", "193"};
        }
    }

    public static boolean oppoIsEmergencyNumberInternal(String number, boolean useExactMatch) {
        boolean z = true;
        if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
            for (String emergencyNum : getEmergencyNumList()) {
                if (number.equals(emergencyNum)) {
                    return true;
                }
            }
            return false;
        } else if (useExactMatch) {
            if (!(number.equals("112") || number.equals("911") || number.equals("999") || number.equals("110") || number.equals("122") || number.equals("119") || number.equals("120") || number.equals("000") || number.equals("08"))) {
                z = number.equals("118");
            }
            return z;
        } else if (number.length() > 3) {
            return false;
        } else {
            if (!(number.startsWith("112") || number.startsWith("911") || number.startsWith("999") || number.startsWith("110") || number.startsWith("122") || number.startsWith("119") || number.startsWith("120") || number.startsWith("000") || number.startsWith("08"))) {
                z = number.startsWith("118");
            }
            return z;
        }
    }

    private static String checkAndAppendPrefix(Intent intent, int subscription, String number, Context context) {
        if (intent.getBooleanExtra(IP_CALL, false) && number != null && subscription < TelephonyManager.getDefault().getPhoneCount()) {
            String IPPrefix = System.getString(context.getContentResolver(), IP_CALL_PREFIX + (subscription + 1));
            if (!TextUtils.isEmpty(IPPrefix)) {
                return IPPrefix + number;
            }
        }
        return number;
    }

    public static boolean oppoCompare(String a, String b) {
        return oppoCompare(a, b, false);
    }

    public static boolean oppoCompare(String a, String b, boolean useStrictComparation) {
        return useStrictComparation ? compareStrictly(a, b) : oppoCompareLoosely(a, b);
    }

    public static boolean oppoCompareLoosely(String a, String b) {
        int numNonDialableCharsInA = 0;
        int numNonDialableCharsInB = 0;
        if (a == null || b == null) {
            return a == b;
        } else if (a.length() == 0 || b.length() == 0) {
            return false;
        } else {
            int ia = indexOfLastNetworkChar(a);
            int ib = indexOfLastNetworkChar(b);
            int matched = 0;
            while (ia >= 0 && ib >= 0) {
                boolean skipCmp = false;
                char ca = a.charAt(ia);
                if (!isDialable(ca)) {
                    ia--;
                    skipCmp = true;
                    numNonDialableCharsInA++;
                }
                char cb = b.charAt(ib);
                if (!isDialable(cb)) {
                    ib--;
                    skipCmp = true;
                    numNonDialableCharsInB++;
                }
                if (!skipCmp) {
                    if (cb != ca && ca != WILD && cb != WILD) {
                        break;
                    }
                    ia--;
                    ib--;
                    matched++;
                }
            }
            if (matched < MIN_MATCH) {
                int effectiveALen = a.length() - numNonDialableCharsInA;
                if (effectiveALen == b.length() - numNonDialableCharsInB && effectiveALen == matched) {
                    return true;
                }
                return false;
            } else if (matched >= MIN_MATCH && (ia < 0 || ib < 0)) {
                return true;
            } else {
                if (matchIntlPrefix(a, ia + 1) && matchIntlPrefix(b, ib + 1)) {
                    return true;
                }
                if (matchTrunkPrefix(a, ia + 1) && matchIntlPrefixAndCC(b, ib + 1)) {
                    return true;
                }
                if (matchTrunkPrefix(b, ib + 1) && matchIntlPrefixAndCC(a, ia + 1)) {
                    return true;
                }
                boolean aPlusFirst = a.charAt(0) == PLUS_SIGN_CHAR;
                boolean bPlusFirst = b.charAt(0) == PLUS_SIGN_CHAR;
                if (ia < 4 && ib < 4 && (aPlusFirst || bPlusFirst)) {
                    if (!aPlusFirst) {
                        bPlusFirst = false;
                    }
                    if ((bPlusFirst ^ 1) != 0) {
                        Rlog.d(LOG_TAG, "add debug unmatched number if firsit is +");
                        return true;
                    }
                }
                return false;
            }
        }
    }
}
