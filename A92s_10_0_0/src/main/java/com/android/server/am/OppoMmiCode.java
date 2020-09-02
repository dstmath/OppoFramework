package com.android.server.am;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OppoMmiCode {
    static final String ACTION_ACTIVATE = "*";
    static final String ACTION_DEACTIVATE = "#";
    static final String ACTION_ERASURE = "##";
    static final String ACTION_INTERROGATE = "*#";
    static final String ACTION_REGISTER = "**";
    public static final String CFB_ACTIVATE_NUMBER = "*90";
    public static final String CFB_DEACTIVATE_NUMBER = "*900";
    public static final String CFCA_DEACTIVATE_NUMBER = "*730";
    public static final String CFNRC_ACTIVATE_NUMBER = "*68";
    public static final String CFNRC_DEACTIVATE_NUMBER = "*680";
    public static final String CFNRY_ACTIVATE_NUMBER = "*92";
    public static final String CFNRY_DEACTIVATE_NUMBER = "*920";
    public static final String CFU_ACTIVATE_NUMBER = "*72";
    public static final String CFU_DEACTIVATE_NUMBER = "*720";
    static final char END_OF_USSD_COMMAND = '#';
    public static final String LOG_TAG = "OppoMmiCode";
    static final int MATCH_GROUP_ACTION = 2;
    static final int MATCH_GROUP_DIALING_NUMBER = 12;
    static final int MATCH_GROUP_POUND_STRING = 1;
    static final int MATCH_GROUP_PWD_CONFIRM = 11;
    static final int MATCH_GROUP_SERVICE_CODE = 3;
    static final int MATCH_GROUP_SIA = 5;
    static final int MATCH_GROUP_SIB = 7;
    static final int MATCH_GROUP_SIC = 9;
    static final int MAX_LENGTH_SHORT_CODE = 2;
    static final String SC_BAIC = "35";
    static final String SC_BAICR = "351";
    static final String SC_BAOC = "33";
    static final String SC_BAOIC = "331";
    static final String SC_BAOICXH = "332";
    static final String SC_BA_ALL = "330";
    static final String SC_BA_MO = "333";
    static final String SC_BA_MT = "353";
    static final String SC_CFB = "67";
    static final String SC_CFNR = "62";
    static final String SC_CFNRY = "61";
    static final String SC_CFU = "21";
    static final String SC_CF_ALL = "002";
    static final String SC_CF_ALL_CONDITIONAL = "004";
    static final String SC_CLIP = "30";
    static final String SC_CLIR = "31";
    static final String SC_PIN = "04";
    static final String SC_PIN2 = "042";
    static final String SC_PUK = "05";
    static final String SC_PUK2 = "052";
    static final String SC_PWD = "03";
    static final String SC_WAIT = "43";
    static Pattern sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*))?)?)?)?#)(.*)");
    String mAction;
    String mDialingNumber;
    String mPoundString;
    String mPwd;
    String mSc;
    String mSia;
    String mSib;
    String mSic;

    public static OppoMmiCode newFromDialString(Context context, String dialString) {
        Matcher m = sPatternSuppService.matcher(dialString);
        if (m.matches()) {
            OppoMmiCode ret = new OppoMmiCode();
            ret.mPoundString = makeEmptyNull(m.group(1));
            ret.mAction = makeEmptyNull(m.group(2));
            ret.mSc = makeEmptyNull(m.group(3));
            ret.mSia = makeEmptyNull(m.group(5));
            ret.mSib = makeEmptyNull(m.group(7));
            ret.mSic = makeEmptyNull(m.group(9));
            ret.mPwd = makeEmptyNull(m.group(11));
            ret.mDialingNumber = makeEmptyNull(m.group(MATCH_GROUP_DIALING_NUMBER));
            String str = ret.mDialingNumber;
            if (str == null || !str.endsWith(ACTION_DEACTIVATE) || !dialString.endsWith(ACTION_DEACTIVATE)) {
                return ret;
            }
            OppoMmiCode ret2 = new OppoMmiCode();
            ret2.mPoundString = dialString;
            return ret2;
        } else if (dialString.endsWith(ACTION_DEACTIVATE)) {
            OppoMmiCode ret3 = new OppoMmiCode();
            ret3.mPoundString = dialString;
            return ret3;
        } else if (!isShortCode(dialString, context)) {
            return null;
        } else {
            OppoMmiCode ret4 = new OppoMmiCode();
            ret4.mDialingNumber = dialString;
            return ret4;
        }
    }

    public static boolean isServiceCodeCallForwarding(Context context, String dialString) {
        if (context == null || TextUtils.isEmpty(dialString)) {
            return false;
        }
        if (!dialString.endsWith(ACTION_DEACTIVATE) && (dialString.startsWith(CFU_ACTIVATE_NUMBER) || dialString.startsWith(CFB_ACTIVATE_NUMBER) || dialString.startsWith(CFNRY_ACTIVATE_NUMBER) || dialString.startsWith(CFNRC_ACTIVATE_NUMBER) || dialString.startsWith(CFCA_DEACTIVATE_NUMBER))) {
            return true;
        }
        OppoMmiCode mmiCode = newFromDialString(context, dialString);
        if (mmiCode == null) {
            return false;
        }
        return isServiceCodeCallForwarding(mmiCode.mSc);
    }

    private static String makeEmptyNull(String s) {
        if (s == null || s.length() != 0) {
            return s;
        }
        return null;
    }

    private static boolean isEmptyOrNull(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean isServiceCodeCallForwarding(String sc) {
        return sc != null && (sc.equals(SC_CFU) || sc.equals(SC_CFB) || sc.equals(SC_CFNRY) || sc.equals(SC_CFNR) || sc.equals(SC_CF_ALL) || sc.equals(SC_CF_ALL_CONDITIONAL));
    }

    static boolean isServiceCodeCallBarring(String sc) {
        if (sc != null) {
            for (String match : new String[]{SC_BAOC, SC_BAOIC, SC_BAOICXH, SC_BAIC, SC_BAICR, SC_BA_ALL, SC_BA_MO, SC_BA_MT}) {
                if (sc.equals(match)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isMMI() {
        return this.mPoundString != null;
    }

    /* access modifiers changed from: package-private */
    public boolean isShortCode() {
        String str;
        return this.mPoundString == null && (str = this.mDialingNumber) != null && str.length() <= 2;
    }

    private static boolean isShortCode(String dialString, Context context) {
        if (dialString == null || dialString.length() == 0 || PhoneNumberUtils.isLocalEmergencyNumber(context, dialString)) {
            return false;
        }
        return isShortCodeUSSD(dialString, context);
    }

    private static boolean isShortCodeUSSD(String dialString, Context context) {
        if (dialString == null || dialString.length() > 2 || (dialString.length() == 2 && dialString.charAt(0) == '1')) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isPinPukCommand() {
        String str = this.mSc;
        return str != null && (str.equals(SC_PIN) || this.mSc.equals(SC_PIN2) || this.mSc.equals(SC_PUK) || this.mSc.equals(SC_PUK2));
    }

    /* access modifiers changed from: package-private */
    public boolean isTemporaryModeCLIR() {
        String str = this.mSc;
        return str != null && str.equals(SC_CLIR) && this.mDialingNumber != null && (isActivate() || isDeactivate());
    }

    /* access modifiers changed from: package-private */
    public boolean isActivate() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_ACTIVATE);
    }

    /* access modifiers changed from: package-private */
    public boolean isDeactivate() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_DEACTIVATE);
    }

    /* access modifiers changed from: package-private */
    public boolean isInterrogate() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_INTERROGATE);
    }

    /* access modifiers changed from: package-private */
    public boolean isRegister() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_REGISTER);
    }

    /* access modifiers changed from: package-private */
    public boolean isErasure() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_ERASURE);
    }

    public static boolean isUssdNumber(Context context, String dialString) {
        OppoMmiCode mmi = newFromDialString(context, PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.stripSeparators(dialString)));
        if (mmi == null || mmi.isTemporaryModeCLIR() || "08".equals(mmi.mDialingNumber)) {
            return false;
        }
        if (mmi.isShortCode() || mmi.mDialingNumber != null) {
            return true;
        }
        String str = mmi.mSc;
        if ((str == null || (!str.equals(SC_CLIP) && !mmi.mSc.equals(SC_CLIR) && !isServiceCodeCallForwarding(mmi.mSc) && !isServiceCodeCallBarring(mmi.mSc) && !mmi.mSc.equals(SC_PWD) && !mmi.mSc.equals(SC_WAIT) && !mmi.isPinPukCommand())) && mmi.mPoundString != null) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("OppoMmiCode {");
        if (this.mAction != null) {
            sb.append(" action=" + this.mAction);
        }
        if (this.mSc != null) {
            sb.append(" sc=" + this.mSc);
        }
        if (this.mSia != null) {
            sb.append(" sia=" + this.mSia);
        }
        if (this.mSib != null) {
            sb.append(" sib=" + this.mSib);
        }
        if (this.mSic != null) {
            sb.append(" sic=" + this.mSic);
        }
        if (this.mPoundString != null) {
            sb.append(" poundString=" + this.mPoundString);
        }
        if (this.mDialingNumber != null) {
            sb.append(" dialingNumber=" + this.mDialingNumber);
        }
        if (this.mPwd != null) {
            sb.append(" pwd=" + this.mPwd);
        }
        sb.append("}");
        return sb.toString();
    }
}
