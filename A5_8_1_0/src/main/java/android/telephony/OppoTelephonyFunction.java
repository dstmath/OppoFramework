package android.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OppoTelephonyFunction {
    public static final int CONCATENATED_8_BIT_REFERENCE_LENGTH = 5;
    private static final boolean DBG = false;
    private static String OPPO_SINGLE_SIM_CARD_FEATURE = "ro.oppo.nw.singlecard";
    private static final List<String> OPTUS = Arrays.asList(new String[]{"50502", "50590", "00101"});
    public static final int OP_MTK_SIMLOCKED = 4;
    private static final String OP_SIMLOCK = "ro.oppo.nw.simlock";
    private static final String OP_SIMLOCK_FOREVER = "ro.oppo.nw.simlock.forever";
    private static final String OP_SIMLOCK_LOCKINIT = "persist.oppo.nw.simlock.init";
    private static final int OP_SIMLOCK_OPTUS = 0;
    private static final int OP_SIMLOCK_SMART = 3;
    private static final String OP_SIMLOCK_SUPPORTED = "ro.oppo.nw.simlock.supported";
    private static final int OP_SIMLOCK_TELCEL = 4;
    private static final int OP_SIMLOCK_TELSTRA = 2;
    private static final int OP_SIMLOCK_VODAFONE = 1;
    public static final int PORT_ADDRESS_16_REFERENCE_LENGTH = 6;
    private static final String PROJECT_OPERATOR = SystemProperties.get("ro.oppo.operator", "oppo");
    private static final List<String> SMART = Arrays.asList(new String[]{"51503", "51505", "00101"});
    private static final String TAG = "OppoTelephonyFunction";
    private static final List<String> TELCEL = Arrays.asList(new String[]{"334020", "334030", "00101"});
    private static final List<String> TELSTRA = Arrays.asList(new String[]{"50501", "50571", "50572"});
    private static final List<String> VODAFONE = Arrays.asList(new String[]{"50503", "50506", "00101"});
    private static final List<String> WHITE_OPERATOR = Arrays.asList(new String[]{"460016146523189", "460016106522210", "460016146522277", "460016146523200", "460016145222280", "460016126523145", "460016126523058", "460016146523197", "460016106523319", "460016146523196", "460016106523578", "460016146522279", "460001431568515"});

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
        int dataLen = data.length;
        int bytePreSeg = 133;
        if (dataLen > 133) {
            bytePreSeg = 128;
        }
        int remainLen = dataLen;
        int count = 0;
        byte[][] dataSegList = new byte[(((dataLen + bytePreSeg) - 1) / bytePreSeg)][];
        while (remainLen > 0) {
            int copyLen;
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

    public static int countGsmSeptets(CharSequence s, boolean throwsException, int rfu) throws EncodeException {
        int count = 0;
        for (int charIndex = 0; charIndex < s.length(); charIndex++) {
            count += GsmAlphabet.countGsmSeptets(s.charAt(charIndex), throwsException);
        }
        return count;
    }

    public static byte[] stringToGsm8BitOrUCSPackedForADN(String s) {
        if (s == null) {
            return null;
        }
        byte[] ret;
        try {
            ret = new byte[countGsmSeptets(s, true, 1)];
            GsmAlphabet.stringToGsm8BitUnpackedField(s, ret, 0, ret.length);
        } catch (EncodeException e) {
            try {
                byte[] temp = s.getBytes("utf-16be");
                ret = new byte[(temp.length + 1)];
                ret[0] = MidiConstants.STATUS_NOTE_OFF;
                System.arraycopy(temp, 0, ret, 1, temp.length);
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "unsurport encoding.", ex);
                return null;
            }
        }
        return ret;
    }

    public static int dmAutoRegisterSmsOrigPort(String address) {
        int origPort = 0;
        if (TextUtils.isEmpty(address)) {
            return origPort;
        }
        int index = address.indexOf(SettingsStringUtil.DELIMITER);
        if (-1 == index) {
            return origPort;
        }
        try {
            return Integer.parseInt(address.substring(index + 1).toString());
        } catch (NumberFormatException e) {
            return origPort;
        }
    }

    public static String dmAutoRegisterSmsAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return address;
        }
        int index = address.indexOf(SettingsStringUtil.DELIMITER);
        if (-1 == index) {
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
                return context.getString(context.getResources().getIdentifier("mmcmnc" + operatorNumic, "string", "oppo"));
            } catch (Exception e) {
            }
        }
        if (ss == null) {
            return null;
        }
        String OperatorName = ss.getOperatorAlphaShort();
        if (TextUtils.isEmpty(OperatorName) || OperatorName.equals(operatorNumic)) {
            OperatorName = ss.getOperatorAlphaLong();
        }
        return OperatorName;
    }

    public static boolean oppoGetSingleSimCard() {
        boolean singleSimCard = isOpenMarketSingleSimCard();
        if (singleSimCard) {
            return singleSimCard;
        }
        return isOperatorSingleSimCard();
    }

    private static boolean isOpenMarketSingleSimCard() {
        return SystemProperties.get(OPPO_SINGLE_SIM_CARD_FEATURE, "false").equals("true");
    }

    private static boolean isOperatorSingleSimCard() {
        if ("OPTUS".equals(PROJECT_OPERATOR) || "VODAFONE".equals(PROJECT_OPERATOR) || "VIRGIN".equals(PROJECT_OPERATOR) || "TELSTRA".equals(PROJECT_OPERATOR)) {
            return true;
        }
        return false;
    }

    public static boolean colorIsSimLockedEnabledTH() {
        return false;
    }

    public static boolean colorIsSimLockedEnabled() {
        return SystemProperties.getBoolean(OP_SIMLOCK_SUPPORTED, false);
    }

    public static boolean colorGetSimLockedStatus(boolean qcomPlatform, String simPlmn, String imsi, int simstate) {
        boolean z = true;
        if (!colorIsSimLockedEnabled()) {
            return false;
        }
        if (isSimLockedForever()) {
            return true;
        }
        if (qcomPlatform) {
            return getSimLockStatus(getSimLockOpeartorIndex(), simPlmn, imsi);
        }
        if (4 != simstate) {
            z = false;
        }
        return z;
    }

    private static int getSimLockOpeartorIndex() {
        int index;
        String str = PROJECT_OPERATOR;
        if (str.equals("OPTUS")) {
            index = 0;
        } else if (str.equals("VODAFONE")) {
            index = 1;
        } else if (str.equals("TELSTRA")) {
            index = 2;
        } else if (str.equals("SMART")) {
            index = 3;
        } else if (str.equals("TELCEL")) {
            index = 4;
        } else {
            index = -1;
        }
        Log.i(TAG, "simlock operator index is " + index);
        return index;
    }

    private static boolean isSimLockedForever() {
        return SystemProperties.getBoolean(OP_SIMLOCK_FOREVER, false);
    }

    private static boolean getSimLockStatus(int index, String simOperator, String imsi) {
        if (!TextUtils.isEmpty(simOperator)) {
            List list = null;
            boolean isWhiteOperaor = false;
            if (getSimLockOpeartorIndex() != -1) {
                list = WHITE_OPERATOR;
                if (!TextUtils.isEmpty(imsi) && list.contains(imsi)) {
                    isWhiteOperaor = true;
                }
            }
            switch (index) {
                case 0:
                    list = OPTUS;
                    break;
                case 1:
                    list = OPTUS;
                    break;
                case 2:
                    list = TELSTRA;
                    break;
                case 3:
                    list = SMART;
                    break;
                case 4:
                    list = TELCEL;
                    break;
            }
            if (list == null || (list.contains(simOperator) ^ 1) == 0 || isWhiteOperaor) {
                return false;
            }
            return true;
        }
        return false;
    }
}
