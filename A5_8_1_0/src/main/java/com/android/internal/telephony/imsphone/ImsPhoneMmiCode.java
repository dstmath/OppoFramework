package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import com.android.ims.ImsException;
import com.android.ims.ImsSsData;
import com.android.ims.ImsSsInfo;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.MmiCode.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SpnOverride;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ImsPhoneMmiCode extends Handler implements MmiCode {
    private static final String ACTION_ACTIVATE = "*";
    private static final String ACTION_DEACTIVATE = "#";
    private static final String ACTION_ERASURE = "##";
    private static final String ACTION_INTERROGATE = "*#";
    private static final String ACTION_REGISTER = "**";
    private static final int CLIR_DEFAULT = 0;
    private static final int CLIR_INVOCATION = 1;
    private static final int CLIR_NOT_PROVISIONED = 0;
    private static final int CLIR_PRESENTATION_ALLOWED_TEMPORARY = 4;
    private static final int CLIR_PRESENTATION_RESTRICTED_TEMPORARY = 3;
    private static final int CLIR_PROVISIONED_PERMANENT = 1;
    private static final int CLIR_SUPPRESSION = 2;
    private static final char END_OF_USSD_COMMAND = '#';
    private static final int EVENT_GET_CLIR_COMPLETE = 6;
    private static final int EVENT_QUERY_CF_COMPLETE = 1;
    private static final int EVENT_QUERY_COMPLETE = 3;
    private static final int EVENT_QUERY_ICB_COMPLETE = 10;
    private static final int EVENT_SET_CFF_COMPLETE = 4;
    private static final int EVENT_SET_COMPLETE = 0;
    private static final int EVENT_SUPP_SVC_QUERY_COMPLETE = 7;
    private static final int EVENT_USSD_CANCEL_COMPLETE = 5;
    private static final int EVENT_USSD_COMPLETE = 2;
    static final String IcbAnonymousMmi = "Anonymous Incoming Call Barring";
    static final String IcbDnMmi = "Specific Incoming Call Barring";
    static final String LOG_TAG = "ImsPhoneMmiCode";
    private static final int MATCH_GROUP_ACTION = 2;
    private static final int MATCH_GROUP_DIALING_NUMBER = 12;
    private static final int MATCH_GROUP_POUND_STRING = 1;
    private static final int MATCH_GROUP_PWD_CONFIRM = 11;
    private static final int MATCH_GROUP_SERVICE_CODE = 3;
    private static final int MATCH_GROUP_SIA = 5;
    private static final int MATCH_GROUP_SIB = 7;
    private static final int MATCH_GROUP_SIC = 9;
    private static final int MAX_LENGTH_SHORT_CODE = 2;
    private static final int NUM_PRESENTATION_ALLOWED = 0;
    private static final int NUM_PRESENTATION_RESTRICTED = 1;
    private static final String SC_BAIC = "35";
    private static final int SC_BAIC_COMPLETE = 200;
    private static final String SC_BAICa = "157";
    private static final String SC_BAICr = "351";
    private static final int SC_BAICr_COMPLETE = 201;
    private static final String SC_BAOC = "33";
    private static final int SC_BAOC_COMPLETE = 100;
    private static final String SC_BAOIC = "331";
    private static final int SC_BAOIC_COMPLETE = 101;
    private static final String SC_BAOICxH = "332";
    private static final int SC_BAOICxH_COMPLETE = 102;
    private static final String SC_BA_ALL = "330";
    private static final String SC_BA_MO = "333";
    private static final String SC_BA_MT = "353";
    private static final String SC_BS_MT = "156";
    private static final String SC_CFB = "67";
    private static final String SC_CFNR = "62";
    private static final String SC_CFNRy = "61";
    private static final String SC_CFU = "21";
    private static final String SC_CFUT = "22";
    private static final String SC_CF_All = "002";
    private static final String SC_CF_All_Conditional = "004";
    private static final String SC_CLIP = "30";
    private static final String SC_CLIR = "31";
    private static final String SC_CNAP = "300";
    private static final String SC_COLP = "76";
    private static final String SC_COLR = "77";
    private static final String SC_PIN = "04";
    private static final String SC_PIN2 = "042";
    private static final String SC_PUK = "05";
    private static final String SC_PUK2 = "052";
    private static final String SC_PWD = "03";
    private static final String SC_WAIT = "43";
    public static final String UT_BUNDLE_KEY_CLIR = "queryClir";
    public static final String UT_BUNDLE_KEY_SSINFO = "imsSsInfo";
    private static Pattern sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*))?)?)?)?#)(.*)");
    private static String[] sTwoDigitNumberPattern;
    private String mAction;
    private ResultReceiver mCallbackReceiver;
    private Context mContext;
    private String mDialingNumber;
    private IccRecords mIccRecords;
    private boolean mIsCallFwdReg;
    private boolean mIsPendingUSSD;
    private boolean mIsSsInfo = false;
    private boolean mIsUssdRequest;
    private CharSequence mMessage;
    private ImsPhone mPhone;
    private String mPoundString;
    private String mPwd;
    private String mSc;
    private String mSia;
    private String mSib;
    private String mSic;
    private State mState = State.PENDING;

    static ImsPhoneMmiCode newFromDialString(String dialString, ImsPhone phone) {
        return newFromDialString(dialString, phone, null);
    }

    static ImsPhoneMmiCode newFromDialString(String dialString, ImsPhone phone, ResultReceiver wrappedCallback) {
        if (phone.getDefaultPhone().getServiceState().getVoiceRoaming() && phone.getDefaultPhone().supportsConversionOfCdmaCallerIdMmiCodesWhileRoaming()) {
            dialString = convertCdmaMmiCodesTo3gppMmiCodes(dialString);
        }
        Matcher m = sPatternSuppService.matcher(dialString);
        ImsPhoneMmiCode ret;
        if (m.matches()) {
            ret = new ImsPhoneMmiCode(phone);
            ret.mPoundString = makeEmptyNull(m.group(1));
            ret.mAction = makeEmptyNull(m.group(2));
            ret.mSc = makeEmptyNull(m.group(3));
            ret.mSia = makeEmptyNull(m.group(5));
            ret.mSib = makeEmptyNull(m.group(7));
            ret.mSic = makeEmptyNull(m.group(9));
            ret.mPwd = makeEmptyNull(m.group(11));
            ret.mDialingNumber = makeEmptyNull(m.group(12));
            ret.mCallbackReceiver = wrappedCallback;
            if (ret.mDialingNumber == null || !ret.mDialingNumber.endsWith(ACTION_DEACTIVATE) || !dialString.endsWith(ACTION_DEACTIVATE)) {
                return ret;
            }
            ret = new ImsPhoneMmiCode(phone);
            ret.mPoundString = dialString;
            return ret;
        } else if (dialString.endsWith(ACTION_DEACTIVATE)) {
            ret = new ImsPhoneMmiCode(phone);
            ret.mPoundString = dialString;
            return ret;
        } else if (isTwoDigitShortCode(phone.getContext(), dialString)) {
            return null;
        } else {
            if (!isShortCode(dialString, phone)) {
                return null;
            }
            ret = new ImsPhoneMmiCode(phone);
            ret.mDialingNumber = dialString;
            return ret;
        }
    }

    private static String convertCdmaMmiCodesTo3gppMmiCodes(String dialString) {
        Matcher m = sPatternCdmaMmiCodeWhileRoaming.matcher(dialString);
        if (!m.matches()) {
            return dialString;
        }
        String serviceCode = makeEmptyNull(m.group(1));
        String prefix = m.group(2);
        String number = makeEmptyNull(m.group(3));
        if (serviceCode.equals(SC_CFB) && number != null) {
            return "#31#" + prefix + number;
        }
        if (!serviceCode.equals("82") || number == null) {
            return dialString;
        }
        return "*31#" + prefix + number;
    }

    static ImsPhoneMmiCode newNetworkInitiatedUssd(String ussdMessage, boolean isUssdRequest, ImsPhone phone) {
        ImsPhoneMmiCode ret = new ImsPhoneMmiCode(phone);
        ret.mMessage = ussdMessage;
        ret.mIsUssdRequest = isUssdRequest;
        if (isUssdRequest) {
            ret.mIsPendingUSSD = true;
            ret.mState = State.PENDING;
        } else {
            ret.mState = State.COMPLETE;
        }
        return ret;
    }

    static ImsPhoneMmiCode newFromUssdUserInput(String ussdMessge, ImsPhone phone) {
        ImsPhoneMmiCode ret = new ImsPhoneMmiCode(phone);
        ret.mMessage = ussdMessge;
        ret.mState = State.PENDING;
        ret.mIsPendingUSSD = true;
        return ret;
    }

    private static String makeEmptyNull(String s) {
        if (s == null || s.length() != 0) {
            return s;
        }
        return null;
    }

    static boolean isScMatchesSuppServType(String dialString) {
        Matcher m = sPatternSuppService.matcher(dialString);
        if (!m.matches()) {
            return false;
        }
        String sc = makeEmptyNull(m.group(3));
        if (sc.equals(SC_CFUT)) {
            return true;
        }
        if (sc.equals(SC_BS_MT)) {
            return true;
        }
        return false;
    }

    private static boolean isEmptyOrNull(CharSequence s) {
        return s == null || s.length() == 0;
    }

    private static int scToCallForwardReason(String sc) {
        if (sc == null) {
            throw new RuntimeException("invalid call forward sc");
        } else if (sc.equals(SC_CF_All)) {
            return 4;
        } else {
            if (sc.equals(SC_CFU)) {
                return 0;
            }
            if (sc.equals(SC_CFB)) {
                return 1;
            }
            if (sc.equals(SC_CFNR)) {
                return 3;
            }
            if (sc.equals(SC_CFNRy)) {
                return 2;
            }
            if (sc.equals(SC_CF_All_Conditional)) {
                return 5;
            }
            throw new RuntimeException("invalid call forward sc");
        }
    }

    private static int siToServiceClass(String si) {
        if (si == null || si.length() == 0) {
            return 0;
        }
        switch (Integer.parseInt(si, 10)) {
            case 10:
                return 13;
            case 11:
                return 1;
            case 12:
                return 12;
            case 13:
                return 4;
            case 16:
                return 8;
            case 19:
                return 5;
            case 20:
                return 48;
            case 21:
                return 160;
            case 22:
                return 80;
            case 24:
                return 16;
            case 25:
                return 32;
            case 26:
                return 17;
            case 99:
                return 64;
            default:
                throw new RuntimeException("unsupported MMI service code " + si);
        }
    }

    private static int siToTime(String si) {
        if (si == null || si.length() == 0) {
            return 20;
        }
        return Integer.parseInt(si, 10);
    }

    static boolean isServiceCodeCallForwarding(String sc) {
        if (sc == null) {
            return false;
        }
        if (sc.equals(SC_CFU) || sc.equals(SC_CFB) || sc.equals(SC_CFNRy) || sc.equals(SC_CFNR) || sc.equals(SC_CF_All)) {
            return true;
        }
        return sc.equals(SC_CF_All_Conditional);
    }

    static boolean isServiceCodeCallBarring(String sc) {
        Resources resource = Resources.getSystem();
        if (sc != null) {
            String[] barringMMI = resource.getStringArray(17235990);
            if (barringMMI != null) {
                for (String match : barringMMI) {
                    if (sc.equals(match)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static String scToBarringFacility(String sc) {
        if (sc == null) {
            throw new RuntimeException("invalid call barring sc");
        } else if (sc.equals(SC_BAOC)) {
            return CommandsInterface.CB_FACILITY_BAOC;
        } else {
            if (sc.equals(SC_BAOIC)) {
                return CommandsInterface.CB_FACILITY_BAOIC;
            }
            if (sc.equals(SC_BAOICxH)) {
                return CommandsInterface.CB_FACILITY_BAOICxH;
            }
            if (sc.equals(SC_BAIC)) {
                return CommandsInterface.CB_FACILITY_BAIC;
            }
            if (sc.equals(SC_BAICr)) {
                return CommandsInterface.CB_FACILITY_BAICr;
            }
            if (sc.equals(SC_BA_ALL)) {
                return CommandsInterface.CB_FACILITY_BA_ALL;
            }
            if (sc.equals(SC_BA_MO)) {
                return CommandsInterface.CB_FACILITY_BA_MO;
            }
            if (sc.equals(SC_BA_MT)) {
                return CommandsInterface.CB_FACILITY_BA_MT;
            }
            throw new RuntimeException("invalid call barring sc");
        }
    }

    public ImsPhoneMmiCode(ImsPhone phone) {
        super(phone.getHandler().getLooper());
        this.mPhone = phone;
        this.mContext = phone.getContext();
        this.mIccRecords = this.mPhone.mDefaultPhone.getIccRecords();
    }

    public State getState() {
        return this.mState;
    }

    public CharSequence getMessage() {
        return this.mMessage;
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    public void cancel() {
        if (this.mState != State.COMPLETE && this.mState != State.FAILED) {
            this.mState = State.CANCELLED;
            if (this.mIsPendingUSSD) {
                this.mPhone.cancelUSSD();
            } else {
                this.mPhone.onMMIDone(this);
            }
        }
    }

    public boolean isCancelable() {
        return this.mIsPendingUSSD;
    }

    String getDialingNumber() {
        return this.mDialingNumber;
    }

    boolean isMMI() {
        return this.mPoundString != null;
    }

    boolean isShortCode() {
        if (this.mPoundString != null || this.mDialingNumber == null || this.mDialingNumber.length() > 2) {
            return false;
        }
        return true;
    }

    public String getDialString() {
        return this.mPoundString;
    }

    private static boolean isTwoDigitShortCode(Context context, String dialString) {
        Rlog.d(LOG_TAG, "isTwoDigitShortCode");
        if (dialString == null || dialString.length() > 2) {
            return false;
        }
        if (sTwoDigitNumberPattern == null) {
            sTwoDigitNumberPattern = context.getResources().getStringArray(17236049);
        }
        for (String dialnumber : sTwoDigitNumberPattern) {
            Rlog.d(LOG_TAG, "Two Digit Number Pattern " + dialnumber);
            if (dialString.equals(dialnumber)) {
                Rlog.d(LOG_TAG, "Two Digit Number Pattern -true");
                return true;
            }
        }
        Rlog.d(LOG_TAG, "Two Digit Number Pattern -false");
        return false;
    }

    private static boolean isShortCode(String dialString, ImsPhone phone) {
        if (dialString == null || dialString.length() == 0 || PhoneNumberUtils.isLocalEmergencyNumber(phone.getContext(), dialString)) {
            return false;
        }
        return isShortCodeUSSD(dialString, phone);
    }

    private static boolean isShortCodeUSSD(String dialString, ImsPhone phone) {
        return (dialString == null || dialString.length() > 2 || (!phone.isInCall() && dialString.length() == 2 && dialString.charAt(0) == '1')) ? false : true;
    }

    public boolean isPinPukCommand() {
        if (this.mSc == null) {
            return false;
        }
        if (this.mSc.equals(SC_PIN) || this.mSc.equals(SC_PIN2) || this.mSc.equals(SC_PUK)) {
            return true;
        }
        return this.mSc.equals(SC_PUK2);
    }

    boolean isTemporaryModeCLIR() {
        if (this.mSc == null || !this.mSc.equals(SC_CLIR) || this.mDialingNumber == null) {
            return false;
        }
        return !isActivate() ? isDeactivate() : true;
    }

    int getCLIRMode() {
        if (this.mSc != null && this.mSc.equals(SC_CLIR)) {
            if (isActivate()) {
                return 2;
            }
            if (isDeactivate()) {
                return 1;
            }
        }
        return 0;
    }

    boolean isActivate() {
        return this.mAction != null ? this.mAction.equals("*") : false;
    }

    boolean isDeactivate() {
        return this.mAction != null ? this.mAction.equals(ACTION_DEACTIVATE) : false;
    }

    boolean isInterrogate() {
        return this.mAction != null ? this.mAction.equals(ACTION_INTERROGATE) : false;
    }

    boolean isRegister() {
        return this.mAction != null ? this.mAction.equals(ACTION_REGISTER) : false;
    }

    boolean isErasure() {
        return this.mAction != null ? this.mAction.equals(ACTION_ERASURE) : false;
    }

    public boolean isPendingUSSD() {
        return this.mIsPendingUSSD;
    }

    public boolean isUssdRequest() {
        return this.mIsUssdRequest;
    }

    /* JADX WARNING: Missing block: B:56:0x00d9, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isSupportedOverImsPhone() {
        if (isShortCode()) {
            return true;
        }
        if (!isServiceCodeCallForwarding(this.mSc) && !isServiceCodeCallBarring(this.mSc) && ((this.mSc == null || !this.mSc.equals(SC_WAIT)) && ((this.mSc == null || !this.mSc.equals(SC_CLIR)) && ((this.mSc == null || !this.mSc.equals(SC_CLIP)) && ((this.mSc == null || !this.mSc.equals(SC_COLR)) && ((this.mSc == null || !this.mSc.equals(SC_COLP)) && ((this.mSc == null || !this.mSc.equals(SC_BS_MT)) && (this.mSc == null || !this.mSc.equals(SC_BAICa))))))))) {
            return !isPinPukCommand() && ((this.mSc == null || !(this.mSc.equals(SC_PWD) || this.mSc.equals(SC_CLIP) || this.mSc.equals(SC_CLIR))) && this.mPoundString != null);
        } else {
            try {
                int serviceClass = siToServiceClass(this.mSib);
                return serviceClass == 0 || serviceClass == 1 || serviceClass == 80;
            } catch (RuntimeException exc) {
                Rlog.d(LOG_TAG, "Invalid service class " + exc);
            }
        }
    }

    public int callBarAction(String dialingNumber) {
        if (isActivate()) {
            return 1;
        }
        if (isDeactivate()) {
            return 0;
        }
        if (isRegister()) {
            if (!isEmptyOrNull(dialingNumber)) {
                return 3;
            }
            throw new RuntimeException("invalid action");
        } else if (isErasure()) {
            return 4;
        } else {
            throw new RuntimeException("invalid action");
        }
    }

    public void processCode() throws CallStateException {
        try {
            int serviceClass;
            if (isShortCode()) {
                Rlog.d(LOG_TAG, "processCode: isShortCode");
                Rlog.d(LOG_TAG, "processCode: Sending short code '" + this.mDialingNumber + "' over CS pipe.");
                throw new CallStateException(Phone.CS_FALLBACK);
            } else if (isServiceCodeCallForwarding(this.mSc)) {
                Rlog.d(LOG_TAG, "processCode: is CF");
                String dialingNumber = this.mSia;
                int reason = scToCallForwardReason(this.mSc);
                serviceClass = siToServiceClass(this.mSib);
                int time = siToTime(this.mSic);
                if (isInterrogate()) {
                    this.mPhone.getCallForwardingOption(reason, serviceClass, obtainMessage(1, this));
                    return;
                }
                int cfAction;
                if (isActivate()) {
                    if (isEmptyOrNull(dialingNumber)) {
                        cfAction = 1;
                        this.mIsCallFwdReg = false;
                    } else {
                        cfAction = 3;
                        this.mIsCallFwdReg = true;
                    }
                } else if (isDeactivate()) {
                    cfAction = 0;
                } else if (isRegister()) {
                    cfAction = 3;
                } else if (isErasure()) {
                    cfAction = 4;
                } else {
                    throw new RuntimeException("invalid action");
                }
                int isSettingUnconditional = (reason == 0 || reason == 4) ? 1 : 0;
                int isEnableDesired = (cfAction == 1 || cfAction == 3) ? 1 : 0;
                Rlog.d(LOG_TAG, "processCode: is CF setCallForward");
                this.mPhone.setCallForwardingOption(cfAction, reason, dialingNumber, serviceClass, time, obtainMessage(4, isSettingUnconditional, isEnableDesired, this));
            } else if (isServiceCodeCallBarring(this.mSc)) {
                String password = this.mSia;
                String facility = scToBarringFacility(this.mSc);
                serviceClass = siToServiceClass(this.mSib);
                if (isInterrogate()) {
                    this.mPhone.getCallBarring(facility, serviceClass, obtainMessage(7, this));
                } else if (!isActivate() && !isDeactivate()) {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                } else if (facility == null) {
                } else {
                    if (facility.equals(scToBarringFacility(SC_BA_ALL))) {
                        this.mPhone.setCallBarring(scToBarringFacility(SC_BAOC), isActivate(), serviceClass, password, obtainMessage(0, 100, 1, this));
                    } else {
                        this.mPhone.setCallBarring(facility, isActivate(), serviceClass, password, obtainMessage(0, this));
                    }
                }
            } else if (this.mSc == null || !this.mSc.equals(SC_CLIR)) {
                if (this.mSc == null || !this.mSc.equals(SC_CLIP)) {
                    if (this.mSc == null || !this.mSc.equals(SC_COLP)) {
                        if (this.mSc == null || !this.mSc.equals(SC_COLR)) {
                            if (this.mSc != null && this.mSc.equals(SC_BS_MT)) {
                                try {
                                    if (isInterrogate()) {
                                        this.mPhone.mCT.getUtInterface().queryCallBarring(10, obtainMessage(10, this));
                                    } else {
                                        processIcbMmiCodeForUpdate();
                                    }
                                } catch (ImsException e) {
                                    Rlog.d(LOG_TAG, "processCode: Could not get UT handle for ICB.");
                                }
                            } else if (this.mSc != null && this.mSc.equals(SC_BAICa)) {
                                int callAction = 0;
                                try {
                                    if (isInterrogate()) {
                                        this.mPhone.mCT.getUtInterface().queryCallBarring(6, obtainMessage(10, this));
                                        return;
                                    }
                                    if (isActivate()) {
                                        callAction = 1;
                                    } else if (isDeactivate()) {
                                        callAction = 0;
                                    }
                                    this.mPhone.mCT.getUtInterface().updateCallBarring(6, callAction, obtainMessage(0, this), null);
                                } catch (ImsException e2) {
                                    Rlog.d(LOG_TAG, "processCode: Could not get UT handle for ICBa.");
                                }
                            } else if (this.mSc != null && this.mSc.equals(SC_WAIT)) {
                                serviceClass = siToServiceClass(this.mSib);
                                if (isActivate() || isDeactivate()) {
                                    this.mPhone.setCallWaiting(isActivate(), serviceClass, obtainMessage(0, this));
                                } else if (isInterrogate()) {
                                    this.mPhone.getCallWaiting(obtainMessage(3, this));
                                } else {
                                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                                }
                            } else if (this.mPoundString != null) {
                                Rlog.d(LOG_TAG, "processCode: Sending pound string '" + this.mDialingNumber + "' over CS pipe.");
                                throw new CallStateException(Phone.CS_FALLBACK);
                            } else {
                                Rlog.d(LOG_TAG, "processCode: invalid or unsupported MMI");
                                throw new RuntimeException("Invalid or Unsupported MMI Code");
                            }
                        } else if (isActivate()) {
                            try {
                                this.mPhone.mCT.getUtInterface().updateCOLR(1, obtainMessage(0, this));
                            } catch (ImsException e3) {
                                Rlog.d(LOG_TAG, "processCode: Could not get UT handle for updateCOLR.");
                            }
                        } else if (isDeactivate()) {
                            try {
                                this.mPhone.mCT.getUtInterface().updateCOLR(0, obtainMessage(0, this));
                            } catch (ImsException e4) {
                                Rlog.d(LOG_TAG, "processCode: Could not get UT handle for updateCOLR.");
                            }
                        } else if (isInterrogate()) {
                            try {
                                this.mPhone.mCT.getUtInterface().queryCOLR(obtainMessage(7, this));
                            } catch (ImsException e5) {
                                Rlog.d(LOG_TAG, "processCode: Could not get UT handle for queryCOLR.");
                            }
                        } else {
                            throw new RuntimeException("Invalid or Unsupported MMI Code");
                        }
                    } else if (isInterrogate()) {
                        try {
                            this.mPhone.mCT.getUtInterface().queryCOLP(obtainMessage(7, this));
                        } catch (ImsException e6) {
                            Rlog.d(LOG_TAG, "processCode: Could not get UT handle for queryCOLP.");
                        }
                    } else if (isActivate() || isDeactivate()) {
                        try {
                            this.mPhone.mCT.getUtInterface().updateCOLP(isActivate(), obtainMessage(0, this));
                        } catch (ImsException e7) {
                            Rlog.d(LOG_TAG, "processCode: Could not get UT handle for updateCOLP.");
                        }
                    } else {
                        throw new RuntimeException("Invalid or Unsupported MMI Code");
                    }
                } else if (isInterrogate()) {
                    try {
                        this.mPhone.mCT.getUtInterface().queryCLIP(obtainMessage(7, this));
                    } catch (ImsException e8) {
                        Rlog.d(LOG_TAG, "processCode: Could not get UT handle for queryCLIP.");
                    }
                } else if (isActivate() || isDeactivate()) {
                    try {
                        this.mPhone.mCT.getUtInterface().updateCLIP(isActivate(), obtainMessage(0, this));
                    } catch (ImsException e9) {
                        Rlog.d(LOG_TAG, "processCode: Could not get UT handle for updateCLIP.");
                    }
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (isActivate()) {
                try {
                    this.mPhone.mCT.getUtInterface().updateCLIR(1, obtainMessage(0, this));
                } catch (ImsException e10) {
                    Rlog.d(LOG_TAG, "processCode: Could not get UT handle for updateCLIR.");
                }
            } else if (isDeactivate()) {
                try {
                    this.mPhone.mCT.getUtInterface().updateCLIR(2, obtainMessage(0, this));
                } catch (ImsException e11) {
                    Rlog.d(LOG_TAG, "processCode: Could not get UT handle for updateCLIR.");
                }
            } else if (isInterrogate()) {
                try {
                    this.mPhone.mCT.getUtInterface().queryCLIR(obtainMessage(6, this));
                } catch (ImsException e12) {
                    Rlog.d(LOG_TAG, "processCode: Could not get UT handle for queryCLIR.");
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        } catch (RuntimeException exc) {
            this.mState = State.FAILED;
            this.mMessage = this.mContext.getText(17040333);
            Rlog.d(LOG_TAG, "processCode: RuntimeException = " + exc);
            this.mPhone.onMMIDone(this);
        }
    }

    void onUssdFinished(String ussdMessage, boolean isUssdRequest) {
        if (this.mState == State.PENDING) {
            if (TextUtils.isEmpty(ussdMessage)) {
                this.mMessage = this.mContext.getText(17040332);
                Rlog.v(LOG_TAG, "onUssdFinished: no message; using: " + this.mMessage);
            } else {
                Rlog.v(LOG_TAG, "onUssdFinished: message: " + ussdMessage);
                this.mMessage = ussdMessage;
            }
            this.mIsUssdRequest = isUssdRequest;
            if (!isUssdRequest) {
                this.mState = State.COMPLETE;
            }
            this.mPhone.onMMIDone(this);
        }
    }

    void onUssdFinishedError() {
        if (this.mState == State.PENDING) {
            this.mState = State.FAILED;
            this.mMessage = this.mContext.getText(17040333);
            Rlog.d(LOG_TAG, "onUssdFinishedError: mmi=" + this);
            this.mPhone.onMMIDone(this);
        }
    }

    void sendUssd(String ussdMessage) {
        this.mIsPendingUSSD = true;
        this.mPhone.sendUSSD(ussdMessage, obtainMessage(2, this));
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 0:
                onSetComplete(msg, msg.obj);
                return;
            case 1:
                onQueryCfComplete((AsyncResult) msg.obj);
                return;
            case 2:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    this.mState = State.FAILED;
                    this.mMessage = getErrorMessage(ar);
                    this.mPhone.onMMIDone(this);
                    return;
                }
                return;
            case 3:
                onQueryComplete((AsyncResult) msg.obj);
                return;
            case 4:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && msg.arg1 == 1) {
                    boolean cffEnabled = msg.arg2 == 1;
                    if (this.mIccRecords != null) {
                        if (siToServiceClass(this.mSib) == 80) {
                            this.mPhone.setVideoCallForwardingPreference(cffEnabled);
                            this.mPhone.notifyCallForwardingIndicator();
                        } else if (siToServiceClass(this.mSib) == 1) {
                            this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, this.mDialingNumber);
                        } else {
                            this.mPhone.setVideoCallForwardingPreference(cffEnabled);
                            this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, this.mDialingNumber);
                        }
                    }
                }
                onSetComplete(msg, ar);
                return;
            case 5:
                this.mPhone.onMMIDone(this);
                return;
            case 6:
                onQueryClirComplete((AsyncResult) msg.obj);
                return;
            case 7:
                onSuppSvcQueryComplete((AsyncResult) msg.obj);
                return;
            case 10:
                onIcbQueryComplete((AsyncResult) msg.obj);
                return;
            default:
                return;
        }
    }

    private void processIcbMmiCodeForUpdate() {
        String dialingNumber = this.mSia;
        String[] icbNum = null;
        if (dialingNumber != null) {
            icbNum = dialingNumber.split("\\$");
        }
        try {
            this.mPhone.mCT.getUtInterface().updateCallBarring(10, callBarAction(dialingNumber), obtainMessage(0, this), icbNum);
        } catch (ImsException e) {
            Rlog.d(LOG_TAG, "processIcbMmiCodeForUpdate:Could not get UT handle for updating ICB.");
        }
    }

    private CharSequence getErrorMessage(AsyncResult ar) {
        CharSequence errorMessage = getMmiErrorMessage(ar);
        if (errorMessage != null) {
            return errorMessage;
        }
        return this.mContext.getText(17040333);
    }

    private CharSequence getMmiErrorMessage(AsyncResult ar) {
        if (ar.exception instanceof ImsException) {
            switch (((ImsException) ar.exception).getCode()) {
                case 241:
                    return this.mContext.getText(17040335);
                case 822:
                    return this.mContext.getText(17040928);
                case 823:
                    return this.mContext.getText(17040931);
                case 824:
                    return this.mContext.getText(17040930);
                case 825:
                    return this.mContext.getText(17040929);
                default:
                    return null;
            }
        }
        if (ar.exception instanceof CommandException) {
            CommandException err = ar.exception;
            if (err.getCommandError() == Error.FDN_CHECK_FAILURE) {
                return this.mContext.getText(17040335);
            }
            if (err.getCommandError() == Error.SS_MODIFIED_TO_DIAL) {
                return this.mContext.getText(17040928);
            }
            if (err.getCommandError() == Error.SS_MODIFIED_TO_USSD) {
                return this.mContext.getText(17040931);
            }
            if (err.getCommandError() == Error.SS_MODIFIED_TO_SS) {
                return this.mContext.getText(17040930);
            }
            if (err.getCommandError() == Error.SS_MODIFIED_TO_DIAL_VIDEO) {
                return this.mContext.getText(17040929);
            }
        }
        return null;
    }

    private CharSequence getScString() {
        if (this.mSc != null) {
            if (isServiceCodeCallBarring(this.mSc)) {
                return this.mContext.getText(17039387);
            }
            if (isServiceCodeCallForwarding(this.mSc)) {
                return this.mContext.getText(17039393);
            }
            if (this.mSc.equals(SC_PWD)) {
                return this.mContext.getText(17039413);
            }
            if (this.mSc.equals(SC_WAIT)) {
                return this.mContext.getText(17039404);
            }
            if (this.mSc.equals(SC_CLIP)) {
                return this.mContext.getText(17039397);
            }
            if (this.mSc.equals(SC_CLIR)) {
                return this.mContext.getText(17039398);
            }
            if (this.mSc.equals(SC_COLP)) {
                return this.mContext.getText(17039402);
            }
            if (this.mSc.equals(SC_COLR)) {
                return this.mContext.getText(17039403);
            }
            if (this.mSc.equals(SC_BS_MT)) {
                return IcbDnMmi;
            }
            if (this.mSc.equals(SC_BAICa)) {
                return IcbAnonymousMmi;
            }
        }
        return SpnOverride.MVNO_TYPE_NONE;
    }

    private void onSetComplete(Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            CharSequence errorMessage;
            if (ar.exception instanceof CommandException) {
                CommandException err = ar.exception;
                if (err.getCommandError() == Error.PASSWORD_INCORRECT) {
                    sb.append(this.mContext.getText(17040423));
                } else {
                    errorMessage = getMmiErrorMessage(ar);
                    if (errorMessage != null) {
                        sb.append(errorMessage);
                    } else if (err.getMessage() != null) {
                        sb.append(err.getMessage());
                    } else {
                        sb.append(this.mContext.getText(17040333));
                    }
                }
            } else {
                ImsException error = ar.exception;
                errorMessage = getMmiErrorMessage(ar);
                if (errorMessage != null) {
                    sb.append(errorMessage);
                } else if (error.getMessage() != null) {
                    sb.append(error.getMessage());
                } else {
                    sb.append(getErrorMessage(ar));
                }
            }
        } else if (isActivate()) {
            this.mState = State.COMPLETE;
            if (this.mIsCallFwdReg) {
                sb.append(this.mContext.getText(17040847));
            } else {
                sb.append(this.mContext.getText(17040843));
            }
            if (!(!SC_CLIR.equals(this.mSc) || this.mPhone == null || this.mPhone.getDefaultPhone() == null)) {
                this.mPhone.getDefaultPhone().saveClirSetting(1);
            }
        } else if (isDeactivate()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17040842));
            if (!(!SC_CLIR.equals(this.mSc) || this.mPhone == null || this.mPhone.getDefaultPhone() == null)) {
                this.mPhone.getDefaultPhone().saveClirSetting(2);
            }
        } else if (isRegister()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17040847));
        } else if (isErasure()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17040845));
        } else {
            this.mState = State.FAILED;
            sb.append(this.mContext.getText(17040333));
        }
        if (msg != null && isServiceCodeCallBarring(this.mSc)) {
            String password = this.mSia;
            String facility = scToBarringFacility(this.mSc);
            int serviceClass = siToServiceClass(this.mSib);
            if (msg.arg1 == 100) {
                Rlog.d(LOG_TAG, "BAOC set complete ,set BAOIC message");
                this.mPhone.setCallBarring(scToBarringFacility(SC_BAOIC), isActivate(), serviceClass, password, obtainMessage(0, 101, 1, this));
            } else if (msg.arg1 == 101) {
                Rlog.d(LOG_TAG, "BAOIC set complete ,set BAOICxH message");
                this.mPhone.setCallBarring(scToBarringFacility(SC_BAOICxH), isActivate(), serviceClass, password, obtainMessage(0, 102, 1, this));
            } else if (msg.arg1 == 102) {
                Rlog.d(LOG_TAG, "BAOICxH set complete ,set BAIC message");
                this.mPhone.setCallBarring(scToBarringFacility(SC_BAIC), isActivate(), serviceClass, password, obtainMessage(0, 200, 1, this));
            } else if (msg.arg1 == 200) {
                Rlog.d(LOG_TAG, "BAOIC set complete ,set BAICr message");
                this.mPhone.setCallBarring(scToBarringFacility(SC_BAICr), isActivate(), serviceClass, password, obtainMessage(0, SC_BAICr_COMPLETE, 1, this));
            } else if (msg.arg1 == SC_BAICr_COMPLETE) {
                Rlog.d(LOG_TAG, "BAOIC set complete ,SC_BA_ALL finished");
            } else {
                Rlog.d(LOG_TAG, " Set SC_BA_ALL error");
            }
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onSetComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private CharSequence serviceClassToCFString(int serviceClass) {
        switch (serviceClass) {
            case 1:
                return this.mContext.getText(17040841);
            case 2:
                return this.mContext.getText(17040834);
            case 4:
                return this.mContext.getText(17040837);
            case 8:
                return this.mContext.getText(17040840);
            case 16:
                return this.mContext.getText(17040836);
            case 32:
                return this.mContext.getText(17040835);
            case 64:
                return this.mContext.getText(17040839);
            case 128:
                return this.mContext.getText(17040838);
            default:
                return null;
        }
    }

    private CharSequence makeCFQueryResultMessage(CallForwardInfo info, int serviceClassMask) {
        CharSequence template;
        String[] sources = new String[]{"{0}", "{1}", "{2}"};
        CharSequence[] destinations = new CharSequence[3];
        boolean needTimeTemplate = info.reason == 2;
        if (info.status == 1) {
            if (needTimeTemplate) {
                template = this.mContext.getText(17039626);
            } else {
                template = this.mContext.getText(17039625);
            }
        } else if (info.status == 0 && isEmptyOrNull(info.number)) {
            template = this.mContext.getText(17039627);
        } else if (needTimeTemplate) {
            template = this.mContext.getText(17039629);
        } else {
            template = this.mContext.getText(17039628);
        }
        destinations[0] = serviceClassToCFString(info.serviceClass & serviceClassMask);
        destinations[1] = PhoneNumberUtils.stringFromStringAndTOA(info.number, info.toa);
        destinations[2] = Integer.toString(info.timeSeconds);
        if (info.reason == 0 && (info.serviceClass & serviceClassMask) == 1) {
            boolean cffEnabled = info.status == 1;
            if (this.mIccRecords != null) {
                this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, info.number);
            }
        }
        return TextUtils.replace(template, sources, destinations);
    }

    private void onQueryCfComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                ImsException error = ar.exception;
                CharSequence errorMessage = getMmiErrorMessage(ar);
                if (errorMessage != null) {
                    sb.append(errorMessage);
                } else if (error.getMessage() != null) {
                    sb.append(error.getMessage());
                } else {
                    sb.append(getErrorMessage(ar));
                }
            } else {
                sb.append(getErrorMessage(ar));
            }
        } else {
            CallForwardInfo[] infos = ar.result;
            if (infos.length == 0) {
                sb.append(this.mContext.getText(17040842));
                if (this.mIccRecords != null) {
                    this.mPhone.setVoiceCallForwardingFlag(1, false, null);
                }
            } else {
                SpannableStringBuilder tb = new SpannableStringBuilder();
                for (int serviceClassMask = 1; serviceClassMask <= 128; serviceClassMask <<= 1) {
                    int s = infos.length;
                    for (int i = 0; i < s; i++) {
                        if ((infos[i].serviceClass & serviceClassMask) != 0) {
                            tb.append(makeCFQueryResultMessage(infos[i], serviceClassMask));
                            tb.append("\n");
                        }
                    }
                }
                sb.append(tb);
            }
            this.mState = State.COMPLETE;
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onQueryCfComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private void onSuppSvcQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                ImsException error = ar.exception;
                CharSequence errorMessage = getMmiErrorMessage(ar);
                if (errorMessage != null) {
                    sb.append(errorMessage);
                } else if (error.getMessage() != null) {
                    sb.append(error.getMessage());
                } else {
                    sb.append(getErrorMessage(ar));
                }
            } else {
                sb.append(getErrorMessage(ar));
            }
        } else {
            this.mState = State.FAILED;
            if (ar.result instanceof Bundle) {
                Rlog.d(LOG_TAG, "onSuppSvcQueryComplete: Received CLIP/COLP/COLR Response.");
                ImsSsInfo ssInfo = (ImsSsInfo) ar.result.getParcelable(UT_BUNDLE_KEY_SSINFO);
                if (ssInfo != null) {
                    Rlog.d(LOG_TAG, "onSuppSvcQueryComplete: ImsSsInfo mStatus = " + ssInfo.mStatus);
                    if (ssInfo.mStatus == 0) {
                        sb.append(this.mContext.getText(17040842));
                        this.mState = State.COMPLETE;
                    } else if (ssInfo.mStatus == 1) {
                        sb.append(this.mContext.getText(17040843));
                        this.mState = State.COMPLETE;
                    } else {
                        sb.append(this.mContext.getText(17040333));
                    }
                } else {
                    sb.append(this.mContext.getText(17040333));
                }
            } else {
                Rlog.d(LOG_TAG, "onSuppSvcQueryComplete: Received Call Barring Response.");
                if (ar.result[0] == 1) {
                    sb.append(this.mContext.getText(17040843));
                    this.mState = State.COMPLETE;
                } else {
                    sb.append(this.mContext.getText(17040842));
                    this.mState = State.COMPLETE;
                }
            }
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onSuppSvcQueryComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private void onIcbQueryComplete(AsyncResult ar) {
        Rlog.d(LOG_TAG, "onIcbQueryComplete mmi=" + this);
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                ImsException error = ar.exception;
                CharSequence errorMessage = getMmiErrorMessage(ar);
                if (errorMessage != null) {
                    sb.append(errorMessage);
                } else if (error.getMessage() != null) {
                    sb.append(error.getMessage());
                } else {
                    sb.append(getErrorMessage(ar));
                }
            } else {
                sb.append(getErrorMessage(ar));
            }
        } else {
            ImsSsInfo[] infos = ar.result;
            if (infos.length == 0) {
                sb.append(this.mContext.getText(17040842));
            } else {
                int s = infos.length;
                for (int i = 0; i < s; i++) {
                    if (infos[i].mIcbNum != null) {
                        sb.append("Num: ").append(infos[i].mIcbNum).append(" status: ").append(infos[i].mStatus).append("\n");
                    } else if (infos[i].mStatus == 1) {
                        sb.append(this.mContext.getText(17040843));
                    } else {
                        sb.append(this.mContext.getText(17040842));
                    }
                }
            }
            this.mState = State.COMPLETE;
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private void onQueryClirComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        this.mState = State.FAILED;
        if (ar.exception == null) {
            int[] clirInfo = ar.result.getIntArray(UT_BUNDLE_KEY_CLIR);
            Rlog.d(LOG_TAG, "onQueryClirComplete: CLIR param n=" + clirInfo[0] + " m=" + clirInfo[1]);
            switch (clirInfo[1]) {
                case 0:
                    sb.append(this.mContext.getText(17040846));
                    this.mState = State.COMPLETE;
                    break;
                case 1:
                    sb.append(this.mContext.getText(17039392));
                    this.mState = State.COMPLETE;
                    break;
                case 3:
                    switch (clirInfo[0]) {
                        case 0:
                            sb.append(this.mContext.getText(17039391));
                            this.mState = State.COMPLETE;
                            break;
                        case 1:
                            sb.append(this.mContext.getText(17039391));
                            this.mState = State.COMPLETE;
                            break;
                        case 2:
                            sb.append(this.mContext.getText(17039390));
                            this.mState = State.COMPLETE;
                            break;
                        default:
                            sb.append(this.mContext.getText(17040333));
                            this.mState = State.FAILED;
                            break;
                    }
                case 4:
                    switch (clirInfo[0]) {
                        case 0:
                            sb.append(this.mContext.getText(17039388));
                            this.mState = State.COMPLETE;
                            break;
                        case 1:
                            sb.append(this.mContext.getText(17039389));
                            this.mState = State.COMPLETE;
                            break;
                        case 2:
                            sb.append(this.mContext.getText(17039388));
                            this.mState = State.COMPLETE;
                            break;
                        default:
                            sb.append(this.mContext.getText(17040333));
                            this.mState = State.FAILED;
                            break;
                    }
                default:
                    sb.append(this.mContext.getText(17040333));
                    this.mState = State.FAILED;
                    break;
            }
        } else if (ar.exception instanceof ImsException) {
            ImsException error = ar.exception;
            CharSequence errorMessage = getMmiErrorMessage(ar);
            if (errorMessage != null) {
                sb.append(errorMessage);
            } else if (error.getMessage() != null) {
                sb.append(error.getMessage());
            } else {
                sb.append(getErrorMessage(ar));
            }
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onQueryClirComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private void onQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                ImsException error = ar.exception;
                CharSequence errorMessage = getMmiErrorMessage(ar);
                if (errorMessage != null) {
                    sb.append(errorMessage);
                } else if (error.getMessage() != null) {
                    sb.append(error.getMessage());
                } else {
                    sb.append(getErrorMessage(ar));
                }
            } else {
                sb.append(getErrorMessage(ar));
            }
        } else {
            int[] ints = ar.result;
            if (ints.length == 0) {
                sb.append(this.mContext.getText(17040333));
            } else if (ints[0] == 0) {
                sb.append(this.mContext.getText(17040842));
            } else if (this.mSc.equals(SC_WAIT)) {
                sb.append(createQueryCallWaitingResultMessage(ints[1]));
            } else if (ints[0] == 1) {
                sb.append(this.mContext.getText(17040843));
            } else {
                sb.append(this.mContext.getText(17040333));
            }
            this.mState = State.COMPLETE;
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onQueryComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private CharSequence createQueryCallWaitingResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17040844));
        for (int classMask = 1; classMask <= 128; classMask <<= 1) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    public ResultReceiver getUssdCallbackReceiver() {
        return this.mCallbackReceiver;
    }

    public void processImsSsData(AsyncResult data) {
        this.mIsSsInfo = true;
        try {
            parseSsData(data.result);
        } catch (ClassCastException ex) {
            Rlog.e(LOG_TAG, "Class Cast Exception in parsing SS Data : " + ex);
        } catch (NullPointerException ex2) {
            Rlog.e(LOG_TAG, "Null Pointer Exception in parsing SS Data : " + ex2);
        }
    }

    void parseSsData(ImsSsData ssData) {
        Throwable ex = ssData.mResult != 0 ? new ImsException(null, ssData.mResult) : null;
        this.mSc = getScStringFromScType(ssData.mServiceType);
        this.mAction = getActionStringFromReqType(ssData.mRequestType);
        Rlog.d(LOG_TAG, "parseSsData msc = " + this.mSc + ", action = " + this.mAction + ", ex = " + ex);
        switch (ssData.mRequestType) {
            case 0:
            case 1:
            case 3:
            case 4:
                if (ssData.mResult == 0 && ssData.isTypeUnConditional()) {
                    boolean cffEnabled;
                    if (ssData.mRequestType == 0 || ssData.mRequestType == 3) {
                        cffEnabled = isServiceClassVoiceVideoOrNone(ssData.mServiceClass);
                    } else {
                        cffEnabled = false;
                    }
                    Rlog.d(LOG_TAG, "setCallForwardingFlag cffEnabled: " + cffEnabled);
                    if (this.mIccRecords == null) {
                        Rlog.e(LOG_TAG, "setCallForwardingFlag aborted. sim records is null.");
                    } else if (ssData.mServiceClass == 80) {
                        Rlog.d(LOG_TAG, "setVideoCallForwardingFlag done from SS Info.");
                        this.mPhone.setVideoCallForwardingPreference(cffEnabled);
                        this.mPhone.notifyCallForwardingIndicator();
                    } else {
                        Rlog.d(LOG_TAG, "setVoiceCallForwardingFlag done from SS Info.");
                        this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, null);
                    }
                }
                onSetComplete(null, new AsyncResult(null, ssData.mCfInfo, ex));
                return;
            case 2:
                if (ssData.isTypeClir()) {
                    Rlog.d(LOG_TAG, "CLIR INTERROGATION");
                    Bundle clirInfo = new Bundle();
                    clirInfo.putIntArray(UT_BUNDLE_KEY_CLIR, ssData.mSsInfo);
                    onQueryClirComplete(new AsyncResult(null, clirInfo, ex));
                    return;
                } else if (ssData.isTypeCF()) {
                    Rlog.d(LOG_TAG, "CALL FORWARD INTERROGATION");
                    onQueryCfComplete(new AsyncResult(null, this.mPhone.handleCfQueryResult(ssData.mCfInfo), ex));
                    return;
                } else if (ssData.isTypeBarring()) {
                    onSuppSvcQueryComplete(new AsyncResult(null, ssData.mSsInfo, ex));
                    return;
                } else if (ssData.isTypeColr() || ssData.isTypeClip() || ssData.isTypeColp()) {
                    ImsSsInfo ssInfo = new ImsSsInfo();
                    ssInfo.mStatus = ssData.mSsInfo[0];
                    Bundle clInfo = new Bundle();
                    clInfo.putParcelable(UT_BUNDLE_KEY_SSINFO, ssInfo);
                    onSuppSvcQueryComplete(new AsyncResult(null, clInfo, ex));
                    return;
                } else if (ssData.isTypeIcb()) {
                    onIcbQueryComplete(new AsyncResult(null, ssData.mImsSsInfo, ex));
                    return;
                } else {
                    onQueryComplete(new AsyncResult(null, ssData.mSsInfo, ex));
                    return;
                }
            default:
                Rlog.e(LOG_TAG, "Invaid requestType in SSData : " + ssData.mRequestType);
                return;
        }
    }

    private String getScStringFromScType(int serviceType) {
        switch (serviceType) {
            case 0:
                return SC_CFU;
            case 1:
                return SC_CFB;
            case 2:
                return SC_CFNRy;
            case 3:
                return SC_CFNR;
            case 4:
                return SC_CF_All;
            case 5:
                return SC_CF_All_Conditional;
            case 7:
                return SC_CLIP;
            case 8:
                return SC_CLIR;
            case 9:
                return SC_COLP;
            case 10:
                return SC_COLR;
            case 11:
                return SC_CNAP;
            case 12:
                return SC_WAIT;
            case 13:
                return SC_BAOC;
            case 14:
                return SC_BAOIC;
            case 15:
                return SC_BAOICxH;
            case 16:
                return SC_BAIC;
            case 17:
                return SC_BAICr;
            case 18:
                return SC_BA_ALL;
            case 19:
                return SC_BA_MO;
            case 20:
                return SC_BA_MT;
            case 21:
                return SC_BS_MT;
            case 22:
                return SC_BAICa;
            default:
                return null;
        }
    }

    private String getActionStringFromReqType(int requestType) {
        switch (requestType) {
            case 0:
                return "*";
            case 1:
                return ACTION_DEACTIVATE;
            case 2:
                return ACTION_INTERROGATE;
            case 3:
                return ACTION_REGISTER;
            case 4:
                return ACTION_ERASURE;
            default:
                return null;
        }
    }

    private boolean isServiceClassVoiceVideoOrNone(int serviceClass) {
        if (serviceClass == 0 || serviceClass == 1 || serviceClass == 80) {
            return true;
        }
        return false;
    }

    public boolean isSsInfo() {
        return this.mIsSsInfo;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ImsPhoneMmiCode {");
        sb.append("State=").append(getState());
        if (this.mAction != null) {
            sb.append(" action=").append(this.mAction);
        }
        if (this.mSc != null) {
            sb.append(" sc=").append(this.mSc);
        }
        if (this.mSia != null) {
            sb.append(" sia=").append(this.mSia);
        }
        if (this.mSib != null) {
            sb.append(" sib=").append(this.mSib);
        }
        if (this.mSic != null) {
            sb.append(" sic=").append(this.mSic);
        }
        if (this.mPoundString != null) {
            sb.append(" poundString=").append(Rlog.pii(LOG_TAG, this.mPoundString));
        }
        if (this.mDialingNumber != null) {
            sb.append(" dialingNumber=").append(Rlog.pii(LOG_TAG, this.mDialingNumber));
        }
        if (this.mPwd != null) {
            sb.append(" pwd=").append(Rlog.pii(LOG_TAG, this.mPwd));
        }
        if (this.mCallbackReceiver != null) {
            sb.append(" hasReceiver");
        }
        sb.append("}");
        return sb.toString();
    }
}
