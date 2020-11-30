package android.telephony;

import android.content.Context;
import android.content.res.OppoThemeResources;
import android.content.res.Resources;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyProperties;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OppoTelephonyFunction {
    public static final int CONCATENATED_8_BIT_REFERENCE_LENGTH = 5;
    private static final boolean DBG = false;
    private static final String DEVICE_LOCK_TH = SystemProperties.get("ro.oppo.operator.device.lock", OppoThemeResources.OPPO_PACKAGE);
    private static final List<String> LONG_NAME_OPERATOR = Arrays.asList("28602", "52505", "45406");
    public static final int MAX_LONG_NAME_LENGTH = 18;
    private static String OPPO_SINGLE_SIM_CARD_FEATURE = "ro.oppo.nw.singlecard";
    private static String OPPO_SINGLE_SIM_COUNTRY = SystemProperties.get("ro.oppo.euex.country", "US");
    private static String OPPO_SINGLE_SIM_PROJECT_FEATURE = "ro.oppo.nw.singlecard.project";
    private static final List<String> OPTUS = Arrays.asList("50502", "50590", "00101");
    public static final int OP_MTK_SIMLOCKED = 4;
    private static final String OP_SIMLOCK = "ro.oppo.nw.simlock";
    private static final String OP_SIMLOCK_FOREVER = "ro.oppo.nw.simlock.forever";
    private static final String OP_SIMLOCK_LOCKINIT = "persist.oppo.nw.simlock.init";
    private static final String OP_SIMLOCK_OPERATOR = "persist.oppo.network.operator";
    private static final int OP_SIMLOCK_OPTUS = 0;
    private static final int OP_SIMLOCK_SMART = 3;
    private static final String OP_SIMLOCK_SUPPORTED = "ro.oppo.nw.simlock.supported";
    private static final int OP_SIMLOCK_TELCEL = 4;
    private static final int OP_SIMLOCK_TELSTRA = 2;
    private static final int OP_SIMLOCK_VODAFONE = 1;
    public static final int PORT_ADDRESS_16_REFERENCE_LENGTH = 6;
    private static String PROJECT_MULTISIM_CONFIG = SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG, OppoThemeResources.OPPO_PACKAGE);
    private static final String PROJECT_OPERATOR = SystemProperties.get("ro.oppo.operator", OppoThemeResources.OPPO_PACKAGE);
    private static final List<String> SHOW_LONGNAME_OPERATORS = Arrays.asList("20404");
    private static final List<String> SMART = Arrays.asList("51503", "51505", "00101");
    private static final String TAG = "OppoTelephonyFunction";
    private static final List<String> TELCEL = Arrays.asList("334020", "334030", "00101");
    private static final List<String> TELSTRA = Arrays.asList("50501", "50571", "50572");
    private static final List<String> VODAFONE = Arrays.asList("50503", "50506", "00101");
    private static final List<String> WHITE_OPERATOR = Arrays.asList("460016146523189", "460016106522210", "460016146522277", "460016146523200", "460016145222280", "460016126523145", "460016126523058", "460016146523197", "460016106523319", "460016146523196", "460016106523578", "460016146522279", "460001431568515");

    public static String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static byte[][] divideDataMessage(byte[] data) {
        int copyLen;
        int dataLen = data.length;
        int bytePreSeg = 133;
        if (dataLen > 133) {
            bytePreSeg = 133 - 5;
        }
        int remainLen = dataLen;
        int count = 0;
        byte[][] dataSegList = new byte[(((dataLen + bytePreSeg) - 1) / bytePreSeg)][];
        while (remainLen > 0) {
            if (remainLen > bytePreSeg) {
                copyLen = bytePreSeg;
            } else {
                copyLen = remainLen;
            }
            remainLen -= copyLen;
            dataSegList[count] = new byte[copyLen];
            System.arraycopy(data, count * bytePreSeg, dataSegList[count], 0, copyLen);
            count++;
        }
        return dataSegList;
    }

    public static ByteBuffer createBufferWithNativeByteOrder(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(ByteOrder.nativeOrder());
        return buf;
    }

    public static int getMinMatch() {
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN") ? 11 : 7;
    }

    private static int countGsmSeptets(CharSequence s, boolean throwsException, int rfu) throws EncodeException {
        int sz = s.length();
        int count = 0;
        for (int charIndex = 0; charIndex < sz; charIndex++) {
            count += GsmAlphabet.countGsmSeptets(s.charAt(charIndex), throwsException);
        }
        return count;
    }

    public static byte[] stringToGsm8BitOrUCSPackedForADN(String s) {
        if (s == null) {
            return null;
        }
        try {
            byte[] ret = new byte[countGsmSeptets(s, true, 1)];
            GsmAlphabet.stringToGsm8BitUnpackedField(s, ret, 0, ret.length);
            return ret;
        } catch (EncodeException e) {
            try {
                byte[] temp = s.getBytes("utf-16be");
                byte[] ret2 = new byte[(temp.length + 1)];
                ret2[0] = Byte.MIN_VALUE;
                System.arraycopy(temp, 0, ret2, 1, temp.length);
                return ret2;
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "unsurport encoding.", ex);
                return null;
            }
        }
    }

    public static int dmAutoRegisterSmsOrigPort(String address) {
        int index;
        if (TextUtils.isEmpty(address) || -1 == (index = address.indexOf(SettingsStringUtil.DELIMITER))) {
            return 0;
        }
        try {
            return Integer.parseInt(address.substring(index + 1).toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String dmAutoRegisterSmsAddress(String address) {
        int index;
        if (TextUtils.isEmpty(address) || -1 == (index = address.indexOf(SettingsStringUtil.DELIMITER))) {
            return address;
        }
        if (index == 0) {
            return null;
        }
        try {
            return address.substring(0, index).toString();
        } catch (IndexOutOfBoundsException e) {
            return address;
        }
    }

    public static String oppoGetPlmnOverride(Context context, String operatorNumic, ServiceState ss) {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage().toLowerCase();
        String country = locale.getCountry().toLowerCase();
        if (language.equals("zh")) {
            if (country.equals("cn")) {
                language = "zh_cn";
            } else {
                language = "zh_tw";
            }
        }
        if (!TextUtils.isEmpty(operatorNumic)) {
            try {
                if (OppoSpnOverride.getInstance(language).containsCarrier(operatorNumic)) {
                    return OppoSpnOverride.getInstance(language).getSpn(operatorNumic);
                }
                if (OppoSpnOverride.getInstance("en").containsCarrier(operatorNumic)) {
                    return OppoSpnOverride.getInstance("en").getSpn(operatorNumic);
                }
                Resources resources = context.getResources();
                return context.getString(resources.getIdentifier(Telephony.CarrierId.All.MCCMNC + operatorNumic, "string", TelephonyManager.PHONE_PROCESS_NAME));
            } catch (Exception e) {
            }
        }
        if (ss == null) {
            return null;
        }
        String OperatorName = ss.getOperatorAlphaShort();
        String longName = ss.getOperatorAlphaLong();
        if (!TextUtils.isEmpty(longName) && (longName.length() <= 18 || (!TextUtils.isEmpty(operatorNumic) && SHOW_LONGNAME_OPERATORS.contains(operatorNumic)))) {
            OperatorName = longName;
        }
        return (TextUtils.isEmpty(OperatorName) || OperatorName.equals(operatorNumic)) ? ss.getOperatorAlphaLong() : OperatorName;
    }

    public static boolean oppoGetSingleSimCard() {
        boolean singleSimCard = isOpenMarketSingleSimCard();
        if (!singleSimCard) {
            return isOperatorSingleSimCard();
        }
        return singleSimCard;
    }

    private static boolean isOpenMarketSingleSimCard() {
        return SystemProperties.get(OPPO_SINGLE_SIM_CARD_FEATURE, "false").equals("true");
    }

    private static boolean isOperatorSingleSimCard() {
        if ("ssss".equals(PROJECT_MULTISIM_CONFIG) || IccCardConstants.INTENT_KEY_ICC_STATE.equals(PROJECT_MULTISIM_CONFIG)) {
            return true;
        }
        return false;
    }

    public static boolean colorIsSimLockedEnabledTH() {
        if (isOperatorSingleSimCard()) {
            return true;
        }
        return false;
    }

    public static boolean colorIsSimLockedEnabled() {
        if (CallerInfo.UNKNOWN_NUMBER.equals(SystemProperties.get(OP_SIMLOCK_OPERATOR, CallerInfo.UNKNOWN_NUMBER)) || WifiEnterpriseConfig.ENGINE_DISABLE.equals(SystemProperties.get(OP_SIMLOCK_OPERATOR, CallerInfo.UNKNOWN_NUMBER))) {
            return SystemProperties.getBoolean(OP_SIMLOCK_SUPPORTED, false);
        }
        return true;
    }

    public static boolean colorGetSimLockedStatus(boolean qcomPlatform, String simPlmn, String imsi, int simstate) {
        if (!colorIsSimLockedEnabled()) {
            return false;
        }
        if (isSimLockedForever()) {
            return true;
        }
        if (qcomPlatform) {
            return getSimLockStatus(getSimLockOpeartorIndex(), simPlmn, imsi);
        }
        if (4 == simstate) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int getSimLockOpeartorIndex() {
        char c;
        int index;
        String str = PROJECT_OPERATOR;
        switch (str.hashCode()) {
            case -1824064945:
                if (str.equals("TELCEL")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1343151615:
                if (str.equals("OPTUS_PREPAID")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1334844874:
                if (str.equals("VODAFONE")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -710947125:
                if (str.equals("TELSTRA")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 75424881:
                if (str.equals("OPTUS")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 79011241:
                if (str.equals("SMART")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 244781659:
                if (str.equals("TELSTRA_PREPAID")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 641632966:
                if (str.equals("VODAFONE_PREPAID")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                index = 0;
                break;
            case 2:
            case 3:
                index = 1;
                break;
            case 4:
            case 5:
                index = 2;
                break;
            case 6:
                index = 3;
                break;
            case 7:
                index = 4;
                break;
            default:
                index = -1;
                break;
        }
        Log.i(TAG, "simlock operator index is " + index);
        return index;
    }

    private static boolean isSimLockedForever() {
        return SystemProperties.getBoolean(OP_SIMLOCK_FOREVER, false);
    }

    private static boolean getSimLockStatus(int index, String simOperator, String imsi) {
        if (!TextUtils.isEmpty(simOperator)) {
            List<String> list = null;
            boolean isWhiteOperaor = false;
            if (getSimLockOpeartorIndex() != -1) {
                list = WHITE_OPERATOR;
                if (!TextUtils.isEmpty(imsi) && list.contains(imsi)) {
                    isWhiteOperaor = true;
                }
            }
            if (index == 0) {
                list = OPTUS;
            } else if (index == 1) {
                list = OPTUS;
            } else if (index == 2) {
                list = TELSTRA;
            } else if (index == 3) {
                list = SMART;
            } else if (index == 4) {
                list = TELCEL;
            }
            if (list == null || list.contains(simOperator) || isWhiteOperaor) {
                return false;
            }
            return true;
        }
        return false;
    }
}
