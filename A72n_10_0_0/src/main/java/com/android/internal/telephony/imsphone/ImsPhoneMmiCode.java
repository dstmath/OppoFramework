package com.android.internal.telephony.imsphone;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsSsData;
import android.telephony.ims.ImsSsInfo;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import com.android.ims.ImsException;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImsPhoneMmiCode extends Handler implements MmiCode {
    protected static final String ACTION_ACTIVATE = "*";
    protected static final String ACTION_DEACTIVATE = "#";
    protected static final String ACTION_ERASURE = "##";
    protected static final String ACTION_INTERROGATE = "*#";
    protected static final String ACTION_REGISTER = "**";
    protected static final int CLIR_DEFAULT = 0;
    protected static final int CLIR_INVOCATION = 1;
    protected static final int CLIR_NOT_PROVISIONED = 0;
    protected static final int CLIR_PRESENTATION_ALLOWED_TEMPORARY = 4;
    protected static final int CLIR_PRESENTATION_RESTRICTED_TEMPORARY = 3;
    protected static final int CLIR_PROVISIONED_PERMANENT = 1;
    protected static final int CLIR_SUPPRESSION = 2;
    protected static final char END_OF_USSD_COMMAND = '#';
    protected static final int EVENT_GET_CLIR_COMPLETE = 6;
    protected static final int EVENT_QUERY_CF_COMPLETE = 1;
    protected static final int EVENT_QUERY_COMPLETE = 3;
    protected static final int EVENT_QUERY_ICB_COMPLETE = 10;
    protected static final int EVENT_SET_CFF_COMPLETE = 4;
    protected static final int EVENT_SET_COMPLETE = 0;
    protected static final int EVENT_SUPP_SVC_QUERY_COMPLETE = 7;
    protected static final int EVENT_USSD_CANCEL_COMPLETE = 5;
    protected static final int EVENT_USSD_COMPLETE = 2;
    protected static final String IcbAnonymousMmi = "Anonymous Incoming Call Barring";
    protected static final String IcbDnMmi = "Specific Incoming Call Barring";
    static final String LOG_TAG = "ImsPhoneMmiCode";
    protected static final int MATCH_GROUP_ACTION = 2;
    protected static final int MATCH_GROUP_DIALING_NUMBER = 12;
    protected static final int MATCH_GROUP_POUND_STRING = 1;
    protected static final int MATCH_GROUP_PWD_CONFIRM = 11;
    protected static final int MATCH_GROUP_SERVICE_CODE = 3;
    protected static final int MATCH_GROUP_SIA = 5;
    protected static final int MATCH_GROUP_SIB = 7;
    protected static final int MATCH_GROUP_SIC = 9;
    protected static final int MAX_LENGTH_SHORT_CODE = 2;
    protected static final int NUM_PRESENTATION_ALLOWED = 0;
    protected static final int NUM_PRESENTATION_RESTRICTED = 1;
    protected static final String SC_BAIC = "35";
    protected static final String SC_BAICa = "157";
    protected static final String SC_BAICr = "351";
    protected static final String SC_BAOC = "33";
    protected static final String SC_BAOIC = "331";
    protected static final String SC_BAOICxH = "332";
    protected static final String SC_BA_ALL = "330";
    protected static final String SC_BA_MO = "333";
    protected static final String SC_BA_MT = "353";
    protected static final String SC_BS_MT = "156";
    protected static final String SC_CFB = "67";
    protected static final String SC_CFNR = "62";
    protected static final String SC_CFNRy = "61";
    protected static final String SC_CFU = "21";
    protected static final String SC_CFUT = "22";
    protected static final String SC_CF_All = "002";
    protected static final String SC_CF_All_Conditional = "004";
    protected static final String SC_CLIP = "30";
    protected static final String SC_CLIR = "31";
    protected static final String SC_CNAP = "300";
    protected static final String SC_COLP = "76";
    protected static final String SC_COLR = "77";
    protected static final String SC_PIN = "04";
    protected static final String SC_PIN2 = "042";
    protected static final String SC_PUK = "05";
    protected static final String SC_PUK2 = "052";
    protected static final String SC_PWD = "03";
    protected static final String SC_WAIT = "43";
    public static final String UT_BUNDLE_KEY_CLIR = "queryClir";
    public static final String UT_BUNDLE_KEY_SSINFO = "imsSsInfo";
    protected static Pattern sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*))?)?)?)?#)(.*)");
    protected static String[] sTwoDigitNumberPattern;
    protected String mAction;
    protected ResultReceiver mCallbackReceiver;
    @UnsupportedAppUsage
    protected Context mContext;
    protected String mDialingNumber;
    protected IccRecords mIccRecords;
    protected boolean mIsCallFwdReg;
    protected boolean mIsPendingUSSD;
    protected boolean mIsSsInfo = false;
    protected boolean mIsUssdRequest;
    protected CharSequence mMessage;
    @UnsupportedAppUsage
    protected ImsPhone mPhone;
    protected String mPoundString;
    protected String mPwd;
    protected String mSc;
    protected String mSia;
    protected String mSib;
    protected String mSic;
    protected MmiCode.State mState = MmiCode.State.PENDING;

    @UnsupportedAppUsage
    public static ImsPhoneMmiCode newFromDialString(String dialString, ImsPhone phone) {
        return newFromDialString(dialString, phone, null);
    }

    public static ImsPhoneMmiCode newFromDialString(String dialString, ImsPhone phone, ResultReceiver wrappedCallback) {
        if (phone.getDefaultPhone().getServiceState().getVoiceRoaming() && phone.getDefaultPhone().supportsConversionOfCdmaCallerIdMmiCodesWhileRoaming()) {
            dialString = convertCdmaMmiCodesTo3gppMmiCodes(dialString);
        }
        Matcher m = sPatternSuppService.matcher(dialString);
        if (m.matches()) {
            ImsPhoneMmiCode ret = new ImsPhoneMmiCode(phone);
            ret.mPoundString = makeEmptyNull(m.group(1));
            ret.mAction = makeEmptyNull(m.group(2));
            ret.mSc = makeEmptyNull(m.group(3));
            ret.mSia = makeEmptyNull(m.group(5));
            ret.mSib = makeEmptyNull(m.group(7));
            ret.mSic = makeEmptyNull(m.group(9));
            ret.mPwd = makeEmptyNull(m.group(11));
            ret.mDialingNumber = makeEmptyNull(m.group(12));
            ret.mCallbackReceiver = wrappedCallback;
            String str = ret.mDialingNumber;
            if (str == null || !str.endsWith(ACTION_DEACTIVATE) || !dialString.endsWith(ACTION_DEACTIVATE)) {
                return ret;
            }
            ImsPhoneMmiCode ret2 = new ImsPhoneMmiCode(phone);
            ret2.mPoundString = dialString;
            return ret2;
        } else if (dialString.endsWith(ACTION_DEACTIVATE)) {
            ImsPhoneMmiCode ret3 = new ImsPhoneMmiCode(phone);
            ret3.mPoundString = dialString;
            return ret3;
        } else if (isTwoDigitShortCode(phone.getContext(), dialString) || !isShortCode(dialString, phone)) {
            return null;
        } else {
            ImsPhoneMmiCode ret4 = new ImsPhoneMmiCode(phone);
            ret4.mDialingNumber = dialString;
            return ret4;
        }
    }

    protected static String convertCdmaMmiCodesTo3gppMmiCodes(String dialString) {
        Matcher m = sPatternCdmaMmiCodeWhileRoaming.matcher(dialString);
        if (!m.matches()) {
            return dialString;
        }
        String serviceCode = makeEmptyNull(m.group(1));
        String prefix = m.group(2);
        String number = makeEmptyNull(m.group(3));
        if (serviceCode.equals(SC_CFB) && number != null) {
            return "#31#" + prefix + number;
        } else if (!serviceCode.equals("82") || number == null) {
            return dialString;
        } else {
            return "*31#" + prefix + number;
        }
    }

    public static ImsPhoneMmiCode newNetworkInitiatedUssd(String ussdMessage, boolean isUssdRequest, ImsPhone phone) {
        ImsPhoneMmiCode ret = new ImsPhoneMmiCode(phone);
        ret.mMessage = ussdMessage;
        ret.mIsUssdRequest = isUssdRequest;
        if (isUssdRequest) {
            ret.mIsPendingUSSD = true;
            ret.mState = MmiCode.State.PENDING;
        } else {
            ret.mState = MmiCode.State.COMPLETE;
        }
        return ret;
    }

    public static ImsPhoneMmiCode newFromUssdUserInput(String ussdMessge, ImsPhone phone) {
        ImsPhoneMmiCode ret = new ImsPhoneMmiCode(phone);
        ret.mMessage = ussdMessge;
        ret.mState = MmiCode.State.PENDING;
        ret.mIsPendingUSSD = true;
        return ret;
    }

    protected static String makeEmptyNull(String s) {
        if (s == null || s.length() != 0) {
            return s;
        }
        return null;
    }

    protected static boolean isScMatchesSuppServType(String dialString) {
        Matcher m = sPatternSuppService.matcher(dialString);
        if (!m.matches()) {
            return false;
        }
        String sc = makeEmptyNull(m.group(3));
        if (!sc.equals(SC_CFUT) && !sc.equals(SC_BS_MT)) {
            return false;
        }
        return true;
    }

    @UnsupportedAppUsage
    protected static boolean isEmptyOrNull(CharSequence s) {
        return s == null || s.length() == 0;
    }

    protected static int scToCallForwardReason(String sc) {
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
        int serviceCode = 0;
        try {
            serviceCode = Integer.parseInt(si, 10);
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
        }
        if (serviceCode == 16) {
            return 8;
        }
        if (serviceCode == 99) {
            return 64;
        }
        switch (serviceCode) {
            case 10:
                return 13;
            case 11:
                return 1;
            case 12:
                return 12;
            case 13:
                return 4;
            default:
                switch (serviceCode) {
                    case 19:
                        return 5;
                    case 20:
                        return 48;
                    case 21:
                        return 160;
                    case 22:
                        return 80;
                    default:
                        switch (serviceCode) {
                            case 24:
                                return 16;
                            case 25:
                                return 32;
                            case 26:
                                return 17;
                            default:
                                throw new RuntimeException("unsupported MMI service code " + si);
                        }
                }
        }
    }

    protected static int siToTime(String si) {
        if (si == null || si.length() == 0) {
            return 20;
        }
        try {
            return Integer.parseInt(si, 10);
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
            return 0;
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
            return 0;
        }
    }

    protected static boolean isServiceCodeCallForwarding(String sc) {
        return sc != null && (sc.equals(SC_CFU) || sc.equals(SC_CFB) || sc.equals(SC_CFNRy) || sc.equals(SC_CFNR) || sc.equals(SC_CF_All) || sc.equals(SC_CF_All_Conditional));
    }

    protected static boolean isServiceCodeCallBarring(String sc) {
        String[] barringMMI;
        Resources resource = Resources.getSystem();
        if (!(sc == null || (barringMMI = resource.getStringArray(17235999)) == null)) {
            for (String match : barringMMI) {
                if (sc.equals(match)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static String scToBarringFacility(String sc) {
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

    @Override // com.android.internal.telephony.MmiCode
    public MmiCode.State getState() {
        return this.mState;
    }

    @Override // com.android.internal.telephony.MmiCode
    public CharSequence getMessage() {
        return this.mMessage;
    }

    @Override // com.android.internal.telephony.MmiCode
    public Phone getPhone() {
        return this.mPhone;
    }

    @Override // com.android.internal.telephony.MmiCode
    public void cancel() {
        if (this.mState != MmiCode.State.COMPLETE && this.mState != MmiCode.State.FAILED) {
            this.mState = MmiCode.State.CANCELLED;
            if (this.mIsPendingUSSD) {
                this.mPhone.cancelUSSD(obtainMessage(5, this));
            } else {
                this.mPhone.onMMIDone(this);
            }
        }
    }

    @Override // com.android.internal.telephony.MmiCode
    public boolean isCancelable() {
        return this.mIsPendingUSSD;
    }

    @UnsupportedAppUsage
    public String getDialingNumber() {
        return this.mDialingNumber;
    }

    /* access modifiers changed from: protected */
    public boolean isMMI() {
        return this.mPoundString != null;
    }

    /* access modifiers changed from: protected */
    public boolean isShortCode() {
        String str;
        return this.mPoundString == null && (str = this.mDialingNumber) != null && str.length() <= 2;
    }

    @Override // com.android.internal.telephony.MmiCode
    public String getDialString() {
        return this.mPoundString;
    }

    protected static boolean isTwoDigitShortCode(Context context, String dialString) {
        Rlog.d(LOG_TAG, "isTwoDigitShortCode");
        if (dialString == null || dialString.length() > 2) {
            return false;
        }
        if (sTwoDigitNumberPattern == null) {
            sTwoDigitNumberPattern = context.getResources().getStringArray(17236090);
        }
        String[] strArr = sTwoDigitNumberPattern;
        for (String dialnumber : strArr) {
            Rlog.d(LOG_TAG, "Two Digit Number Pattern " + dialnumber);
            if (dialString.equals(dialnumber)) {
                Rlog.d(LOG_TAG, "Two Digit Number Pattern -true");
                return true;
            }
        }
        Rlog.d(LOG_TAG, "Two Digit Number Pattern -false");
        return false;
    }

    protected static boolean isShortCode(String dialString, ImsPhone phone) {
        if (dialString == null || dialString.length() == 0 || PhoneNumberUtils.isLocalEmergencyNumber(phone.getContext(), dialString)) {
            return false;
        }
        return isShortCodeUSSD(dialString, phone);
    }

    protected static boolean isShortCodeUSSD(String dialString, ImsPhone phone) {
        if (dialString == null || dialString.length() > 2 || (!phone.isInCall() && dialString.length() == 2 && dialString.charAt(0) == '1')) {
            return false;
        }
        return true;
    }

    @Override // com.android.internal.telephony.MmiCode
    public boolean isPinPukCommand() {
        String str = this.mSc;
        return str != null && (str.equals(SC_PIN) || this.mSc.equals(SC_PIN2) || this.mSc.equals(SC_PUK) || this.mSc.equals(SC_PUK2));
    }

    @UnsupportedAppUsage
    public boolean isTemporaryModeCLIR() {
        String str = this.mSc;
        return str != null && str.equals(SC_CLIR) && this.mDialingNumber != null && (isActivate() || isDeactivate());
    }

    @UnsupportedAppUsage
    public int getCLIRMode() {
        String str = this.mSc;
        if (str == null || !str.equals(SC_CLIR)) {
            return 0;
        }
        if (isActivate()) {
            return 2;
        }
        if (isDeactivate()) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isActivate() {
        String str = this.mAction;
        return str != null && str.equals("*");
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isDeactivate() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_DEACTIVATE);
    }

    /* access modifiers changed from: protected */
    public boolean isInterrogate() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_INTERROGATE);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isRegister() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_REGISTER);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isErasure() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_ERASURE);
    }

    public boolean isPendingUSSD() {
        return this.mIsPendingUSSD;
    }

    @Override // com.android.internal.telephony.MmiCode
    public boolean isUssdRequest() {
        return this.mIsUssdRequest;
    }

    @UnsupportedAppUsage
    public boolean isSupportedOverImsPhone() {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        if (isShortCode()) {
            return true;
        }
        if (isServiceCodeCallForwarding(this.mSc) || isServiceCodeCallBarring(this.mSc) || (((str = this.mSc) != null && str.equals(SC_WAIT)) || (((str2 = this.mSc) != null && str2.equals(SC_CLIR)) || (((str3 = this.mSc) != null && str3.equals(SC_CLIP)) || (((str4 = this.mSc) != null && str4.equals(SC_COLR)) || (((str5 = this.mSc) != null && str5.equals(SC_COLP)) || (((str6 = this.mSc) != null && str6.equals(SC_BS_MT)) || ((str7 = this.mSc) != null && str7.equals(SC_BAICa))))))))) {
            try {
                int serviceClass = siToServiceClass(this.mSib);
                if (serviceClass == 0 || serviceClass == 1 || serviceClass == 80) {
                    return true;
                }
                return false;
            } catch (RuntimeException exc) {
                Rlog.d(LOG_TAG, "Invalid service class " + exc);
            }
        } else if (isPinPukCommand() || (((str8 = this.mSc) != null && (str8.equals(SC_PWD) || this.mSc.equals(SC_CLIP) || this.mSc.equals(SC_CLIR))) || this.mPoundString == null)) {
            return false;
        } else {
            return true;
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

    @Override // com.android.internal.telephony.MmiCode
    @UnsupportedAppUsage
    public void processCode() throws CallStateException {
        int cfAction;
        try {
            if (!isShortCode()) {
                int isEnableDesired = 1;
                if (isServiceCodeCallForwarding(this.mSc)) {
                    Rlog.d(LOG_TAG, "processCode: is CF");
                    if (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.phone.forbidden.callforwarding") || 1 == Settings.Secure.getInt(this.mContext.getContentResolver(), "forward_call_setting_disable", 0)) {
                        Rlog.d(LOG_TAG, "processCode: CF is restricted");
                        throw new RuntimeException("CF is restricted");
                    }
                    String dialingNumber = this.mSia;
                    int reason = scToCallForwardReason(this.mSc);
                    int serviceClass = siToServiceClass(this.mSib);
                    int time = siToTime(this.mSic);
                    if (isInterrogate()) {
                        this.mPhone.getCallForwardingOption(reason, obtainMessage(1, this));
                        return;
                    }
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
                    if (!(cfAction == 1 || cfAction == 3)) {
                        isEnableDesired = 0;
                    }
                    Rlog.d(LOG_TAG, "processCode: is CF setCallForward");
                    this.mPhone.setCallForwardingOption(cfAction, reason, dialingNumber, serviceClass, time, obtainMessage(4, isSettingUnconditional, isEnableDesired, this));
                } else if (isServiceCodeCallBarring(this.mSc)) {
                    String password = this.mSia;
                    String facility = scToBarringFacility(this.mSc);
                    int serviceClass2 = siToServiceClass(this.mSib);
                    if (isInterrogate()) {
                        this.mPhone.getCallBarring(facility, obtainMessage(7, this), serviceClass2);
                    } else if (isActivate() || isDeactivate()) {
                        this.mPhone.setCallBarring(facility, isActivate(), password, obtainMessage(0, this), serviceClass2);
                    } else {
                        throw new RuntimeException("Invalid or Unsupported MMI Code");
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
                                        this.mPhone.mCT.getUtInterface().updateCallBarring(6, callAction, obtainMessage(0, this), (String[]) null);
                                    } catch (ImsException e2) {
                                        Rlog.d(LOG_TAG, "processCode: Could not get UT handle for ICBa.");
                                    }
                                } else if (this.mSc != null && this.mSc.equals(SC_WAIT)) {
                                    int serviceClass3 = siToServiceClass(this.mSib);
                                    if (isActivate() || isDeactivate()) {
                                        this.mPhone.setCallWaiting(isActivate(), serviceClass3, obtainMessage(0, this));
                                    } else if (isInterrogate()) {
                                        this.mPhone.getCallWaiting(obtainMessage(3, this));
                                    } else {
                                        throw new RuntimeException("Invalid or Unsupported MMI Code");
                                    }
                                } else if (this.mPoundString == null) {
                                    Rlog.d(LOG_TAG, "processCode: invalid or unsupported MMI");
                                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                                } else if (this.mPhone.getDefaultPhone().getServiceStateTracker().mSS.getState() != 0) {
                                    Rlog.i(LOG_TAG, "processCode: CS is out of service, sending ussd string '" + Rlog.pii(LOG_TAG, this.mPoundString) + "' over IMS pipe.");
                                    sendUssd(this.mPoundString);
                                } else {
                                    Rlog.i(LOG_TAG, "processCode: Sending ussd string '" + Rlog.pii(LOG_TAG, this.mPoundString) + "' over CS pipe.");
                                    throw new CallStateException(Phone.CS_FALLBACK);
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
            } else {
                Rlog.d(LOG_TAG, "processCode: isShortCode");
                Rlog.d(LOG_TAG, "processCode: Sending short code '" + this.mDialingNumber + "' over CS pipe.");
                throw new CallStateException(Phone.CS_FALLBACK);
            }
        } catch (RuntimeException exc) {
            this.mState = MmiCode.State.FAILED;
            this.mMessage = this.mContext.getText(17040445);
            Rlog.d(LOG_TAG, "processCode: RuntimeException = " + exc);
            this.mPhone.onMMIDone(this);
        }
    }

    public void onUssdFinished(String ussdMessage, boolean isUssdRequest) {
        if (this.mState == MmiCode.State.PENDING) {
            if (TextUtils.isEmpty(ussdMessage)) {
                this.mMessage = this.mContext.getText(17040444);
                Rlog.v(LOG_TAG, "onUssdFinished: no message; using: " + ((Object) this.mMessage));
            } else {
                Rlog.v(LOG_TAG, "onUssdFinished: message: " + ussdMessage);
                this.mMessage = ussdMessage;
            }
            this.mIsUssdRequest = isUssdRequest;
            if (!isUssdRequest) {
                this.mState = MmiCode.State.COMPLETE;
            }
            this.mPhone.onMMIDone(this);
        }
    }

    public void onUssdFinishedError() {
        if (this.mState == MmiCode.State.PENDING) {
            this.mState = MmiCode.State.FAILED;
            this.mMessage = this.mContext.getText(17040445);
            Rlog.d(LOG_TAG, "onUssdFinishedError: mmi=" + this);
            this.mPhone.onMMIDone(this);
        }
    }

    public void sendUssd(String ussdMessage) {
        this.mIsPendingUSSD = true;
        this.mPhone.sendUSSD(ussdMessage, obtainMessage(2, this));
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i != 10) {
            switch (i) {
                case 0:
                    onSetComplete(msg, (AsyncResult) msg.obj);
                    return;
                case 1:
                    onQueryCfComplete((AsyncResult) msg.obj);
                    return;
                case 2:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        this.mState = MmiCode.State.FAILED;
                        this.mMessage = getErrorMessage(ar);
                        this.mPhone.onMMIDone(this);
                        return;
                    }
                    return;
                case 3:
                    onQueryComplete((AsyncResult) msg.obj);
                    return;
                case 4:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception == null && msg.arg1 == 1) {
                        boolean cffEnabled = msg.arg2 == 1;
                        if (this.mIccRecords != null) {
                            this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, this.mDialingNumber);
                        }
                    }
                    onSetComplete(msg, ar2);
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
                default:
                    return;
            }
        } else {
            onIcbQueryComplete((AsyncResult) msg.obj);
        }
    }

    /* access modifiers changed from: protected */
    public void processIcbMmiCodeForUpdate() {
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

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public CharSequence getErrorMessage(AsyncResult ar) {
        CharSequence errorMessage = getMmiErrorMessage(ar);
        if (errorMessage != null) {
            return errorMessage;
        }
        return this.mContext.getText(17040445);
    }

    /* access modifiers changed from: protected */
    public CharSequence getMmiErrorMessage(AsyncResult ar) {
        if (ar.exception instanceof ImsException) {
            int code = ar.exception.getCode();
            if (code == 241) {
                return this.mContext.getText(17040447);
            }
            switch (code) {
                case 822:
                    return this.mContext.getText(17041096);
                case 823:
                    return this.mContext.getText(17041099);
                case 824:
                    return this.mContext.getText(17041098);
                case 825:
                    return this.mContext.getText(17041097);
                default:
                    return null;
            }
        } else {
            if (ar.exception instanceof CommandException) {
                CommandException err = (CommandException) ar.exception;
                if (err.getCommandError() == CommandException.Error.FDN_CHECK_FAILURE) {
                    return this.mContext.getText(17040447);
                }
                if (err.getCommandError() == CommandException.Error.SS_MODIFIED_TO_DIAL) {
                    return this.mContext.getText(17041096);
                }
                if (err.getCommandError() == CommandException.Error.SS_MODIFIED_TO_USSD) {
                    return this.mContext.getText(17041099);
                }
                if (err.getCommandError() == CommandException.Error.SS_MODIFIED_TO_SS) {
                    return this.mContext.getText(17041098);
                }
                if (err.getCommandError() == CommandException.Error.SS_MODIFIED_TO_DIAL_VIDEO) {
                    return this.mContext.getText(17041097);
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public CharSequence getScString() {
        String str = this.mSc;
        if (str == null) {
            return PhoneConfigurationManager.SSSS;
        }
        if (isServiceCodeCallBarring(str)) {
            return this.mContext.getText(17039397);
        }
        if (isServiceCodeCallForwarding(this.mSc)) {
            return this.mContext.getText(17039403);
        }
        if (this.mSc.equals(SC_PWD)) {
            return this.mContext.getText(17039420);
        }
        if (this.mSc.equals(SC_WAIT)) {
            return this.mContext.getText(17039411);
        }
        if (this.mSc.equals(SC_CLIP)) {
            return this.mContext.getText(17039404);
        }
        if (this.mSc.equals(SC_CLIR)) {
            return this.mContext.getText(17039405);
        }
        if (this.mSc.equals(SC_COLP)) {
            return this.mContext.getText(17039409);
        }
        if (this.mSc.equals(SC_COLR)) {
            return this.mContext.getText(17039410);
        }
        if (this.mSc.equals(SC_BS_MT)) {
            return IcbDnMmi;
        }
        if (this.mSc.equals(SC_BAICa)) {
            return IcbAnonymousMmi;
        }
        return PhoneConfigurationManager.SSSS;
    }

    /* access modifiers changed from: protected */
    public void onSetComplete(Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof CommandException) {
                CommandException err = (CommandException) ar.exception;
                if (err.getCommandError() == CommandException.Error.PASSWORD_INCORRECT) {
                    sb.append(this.mContext.getText(17040538));
                } else {
                    CharSequence errorMessage = getMmiErrorMessage(ar);
                    if (errorMessage != null) {
                        sb.append(errorMessage);
                    } else if (err.getMessage() != null) {
                        sb.append(err.getMessage());
                    } else {
                        sb.append(this.mContext.getText(17040445));
                    }
                }
            } else if (ar.exception instanceof ImsException) {
                sb.append(getImsErrorMessage(ar));
            }
        } else if (isActivate()) {
            this.mState = MmiCode.State.COMPLETE;
            if (this.mIsCallFwdReg) {
                sb.append(this.mContext.getText(17041003));
            } else {
                sb.append(this.mContext.getText(17040999));
            }
            if (this.mSc.equals(SC_CLIR)) {
                this.mPhone.saveClirSetting(1);
            }
        } else if (isDeactivate()) {
            this.mState = MmiCode.State.COMPLETE;
            sb.append(this.mContext.getText(17040998));
            if (this.mSc.equals(SC_CLIR)) {
                this.mPhone.saveClirSetting(2);
            }
        } else if (isRegister()) {
            this.mState = MmiCode.State.COMPLETE;
            sb.append(this.mContext.getText(17041003));
        } else if (isErasure()) {
            this.mState = MmiCode.State.COMPLETE;
            sb.append(this.mContext.getText(17041001));
        } else {
            this.mState = MmiCode.State.FAILED;
            sb.append(this.mContext.getText(17040445));
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onSetComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public CharSequence serviceClassToCFString(int serviceClass) {
        if (serviceClass == 1) {
            return this.mContext.getText(17040997);
        }
        if (serviceClass == 2) {
            return this.mContext.getText(17040990);
        }
        if (serviceClass == 4) {
            return this.mContext.getText(17040993);
        }
        if (serviceClass == 8) {
            return this.mContext.getText(17040996);
        }
        if (serviceClass == 16) {
            return this.mContext.getText(17040992);
        }
        if (serviceClass == 32) {
            return this.mContext.getText(17040991);
        }
        if (serviceClass == 64) {
            return this.mContext.getText(17040995);
        }
        if (serviceClass != 128) {
            return null;
        }
        return this.mContext.getText(17040994);
    }

    /* access modifiers changed from: protected */
    public CharSequence makeCFQueryResultMessage(CallForwardInfo info, int serviceClassMask) {
        CharSequence template;
        String[] sources = {"{0}", "{1}", "{2}"};
        CharSequence[] destinations = new CharSequence[3];
        boolean z = false;
        boolean needTimeTemplate = info.reason == 2;
        if (info.status == 1) {
            if (needTimeTemplate) {
                template = this.mContext.getText(17039648);
            } else {
                template = this.mContext.getText(17039647);
            }
        } else if (info.status == 0 && isEmptyOrNull(info.number)) {
            template = this.mContext.getText(17039649);
        } else if (needTimeTemplate) {
            template = this.mContext.getText(17039651);
        } else {
            template = this.mContext.getText(17039650);
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
        if (info.reason == 0 && (info.serviceClass & serviceClassMask) == 512) {
            ImsPhone imsPhone = this.mPhone;
            if (info.status == 1) {
                z = true;
            }
            imsPhone.setVideoCallForwardingFlag(z);
        }
        return TextUtils.replace(template, sources, destinations);
    }

    /* access modifiers changed from: protected */
    public void onQueryCfComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsErrorMessage(ar));
            } else {
                sb.append(getErrorMessage(ar));
            }
        } else {
            CallForwardInfo[] infos = (CallForwardInfo[]) ar.result;
            if (infos == null || infos.length == 0) {
                sb.append(this.mContext.getText(17040998));
                if (this.mIccRecords != null) {
                    this.mPhone.setVoiceCallForwardingFlag(1, false, null);
                }
            } else {
                SpannableStringBuilder tb = new SpannableStringBuilder();
                for (int serviceClassMask = 1; serviceClassMask <= 512; serviceClassMask <<= 1) {
                    int s = infos.length;
                    for (int i = 0; i < s; i++) {
                        if ((infos[i].serviceClass & serviceClassMask) != 0) {
                            tb.append(makeCFQueryResultMessage(infos[i], serviceClassMask));
                            tb.append((CharSequence) "\n");
                        }
                    }
                }
                sb.append((CharSequence) tb);
            }
            this.mState = MmiCode.State.COMPLETE;
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onQueryCfComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    /* access modifiers changed from: protected */
    public void onSuppSvcQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        this.mState = MmiCode.State.FAILED;
        if (ar.exception != null) {
            if (ar.exception instanceof ImsException) {
                sb.append(getImsErrorMessage(ar));
            } else {
                sb.append(getErrorMessage(ar));
            }
        } else if (ar.result instanceof Bundle) {
            Rlog.d(LOG_TAG, "onSuppSvcQueryComplete: Received CLIP/COLP/COLR Response.");
            ImsSsInfo ssInfo = ((Bundle) ar.result).getParcelable(UT_BUNDLE_KEY_SSINFO);
            if (ssInfo != null) {
                Rlog.d(LOG_TAG, "onSuppSvcQueryComplete: ImsSsInfo mStatus = " + ssInfo.getStatus());
                if (ssInfo.getProvisionStatus() == 0) {
                    sb.append(this.mContext.getText(17041002));
                    this.mState = MmiCode.State.COMPLETE;
                } else if (ssInfo.getStatus() == 0) {
                    sb.append(this.mContext.getText(17040998));
                    this.mState = MmiCode.State.COMPLETE;
                } else if (ssInfo.getStatus() == 1) {
                    sb.append(this.mContext.getText(17040999));
                    this.mState = MmiCode.State.COMPLETE;
                } else {
                    sb.append(this.mContext.getText(17040445));
                }
            } else {
                sb.append(this.mContext.getText(17040445));
            }
        } else {
            Rlog.d(LOG_TAG, "onSuppSvcQueryComplete: Received Call Barring Response.");
            if (((int[]) ar.result)[0] == 1) {
                sb.append(this.mContext.getText(17040999));
                this.mState = MmiCode.State.COMPLETE;
            } else {
                sb.append(this.mContext.getText(17040998));
                this.mState = MmiCode.State.COMPLETE;
            }
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onSuppSvcQueryComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    /* access modifiers changed from: protected */
    public void onIcbQueryComplete(AsyncResult ar) {
        List<ImsSsInfo> infos;
        Rlog.d(LOG_TAG, "onIcbQueryComplete mmi=" + this);
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsErrorMessage(ar));
            } else {
                sb.append(getErrorMessage(ar));
            }
        } else {
            try {
                infos = (List) ar.result;
            } catch (ClassCastException e) {
                infos = Arrays.asList((ImsSsInfo[]) ar.result);
            }
            if (infos == null || infos.size() == 0) {
                sb.append(this.mContext.getText(17040998));
            } else {
                int s = infos.size();
                for (int i = 0; i < s; i++) {
                    ImsSsInfo info = infos.get(i);
                    if (info.getIncomingCommunicationBarringNumber() != null) {
                        sb.append("Num: " + info.getIncomingCommunicationBarringNumber() + " status: " + info.getStatus() + "\n");
                    } else if (info.getStatus() == 1) {
                        sb.append(this.mContext.getText(17040999));
                    } else {
                        sb.append(this.mContext.getText(17040998));
                    }
                }
            }
            this.mState = MmiCode.State.COMPLETE;
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    /* access modifiers changed from: protected */
    public void onQueryClirComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        this.mState = MmiCode.State.FAILED;
        if (ar.exception == null) {
            int[] clirInfo = ((Bundle) ar.result).getIntArray(UT_BUNDLE_KEY_CLIR);
            Rlog.d(LOG_TAG, "onQueryClirComplete: CLIR param n=" + clirInfo[0] + " m=" + clirInfo[1]);
            int i = clirInfo[1];
            if (i == 0) {
                sb.append(this.mContext.getText(17041002));
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 1) {
                sb.append(this.mContext.getText(17039402));
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 3) {
                int i2 = clirInfo[0];
                if (i2 == 0) {
                    sb.append(this.mContext.getText(17039401));
                    this.mState = MmiCode.State.COMPLETE;
                } else if (i2 == 1) {
                    sb.append(this.mContext.getText(17039401));
                    this.mState = MmiCode.State.COMPLETE;
                } else if (i2 != 2) {
                    sb.append(this.mContext.getText(17040445));
                    this.mState = MmiCode.State.FAILED;
                } else {
                    sb.append(this.mContext.getText(17039400));
                    this.mState = MmiCode.State.COMPLETE;
                }
            } else if (i != 4) {
                sb.append(this.mContext.getText(17040445));
                this.mState = MmiCode.State.FAILED;
            } else {
                int i3 = clirInfo[0];
                if (i3 == 0) {
                    sb.append(this.mContext.getText(17039398));
                    this.mState = MmiCode.State.COMPLETE;
                } else if (i3 == 1) {
                    sb.append(this.mContext.getText(17039399));
                    this.mState = MmiCode.State.COMPLETE;
                } else if (i3 != 2) {
                    sb.append(this.mContext.getText(17040445));
                    this.mState = MmiCode.State.FAILED;
                } else {
                    sb.append(this.mContext.getText(17039398));
                    this.mState = MmiCode.State.COMPLETE;
                }
            }
        } else if (ar.exception instanceof ImsException) {
            sb.append(getImsErrorMessage(ar));
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onQueryClirComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    /* access modifiers changed from: protected */
    public void onQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsErrorMessage(ar));
            } else {
                sb.append(getErrorMessage(ar));
            }
        } else {
            int[] ints = (int[]) ar.result;
            if (ints.length == 0) {
                sb.append(this.mContext.getText(17040445));
            } else if (ints[0] == 0) {
                sb.append(this.mContext.getText(17040998));
            } else if (this.mSc.equals(SC_WAIT)) {
                sb.append(createQueryCallWaitingResultMessage(ints[1]));
            } else if (ints[0] == 1) {
                sb.append(this.mContext.getText(17040999));
            } else {
                sb.append(this.mContext.getText(17040445));
            }
            this.mState = MmiCode.State.COMPLETE;
        }
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onQueryComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    /* access modifiers changed from: protected */
    public CharSequence createQueryCallWaitingResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17041000));
        for (int classMask = 1; classMask <= 512; classMask <<= 1) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    /* access modifiers changed from: protected */
    public CharSequence getImsErrorMessage(AsyncResult ar) {
        ImsException error = ar.exception;
        CharSequence errorMessage = getMmiErrorMessage(ar);
        if (errorMessage != null) {
            return errorMessage;
        }
        if (error.getMessage() != null) {
            return error.getMessage();
        }
        return getErrorMessage(ar);
    }

    @Override // com.android.internal.telephony.MmiCode
    public ResultReceiver getUssdCallbackReceiver() {
        return this.mCallbackReceiver;
    }

    public void processImsSsData(AsyncResult data) throws ImsException {
        try {
            parseSsData((ImsSsData) data.result);
        } catch (ClassCastException | NullPointerException e) {
            throw new ImsException("Exception in parsing SS Data", 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void parseSsData(ImsSsData ssData) {
        ImsException ex = ssData.getResult() != 0 ? new ImsException((String) null, ssData.getResult()) : null;
        this.mSc = getScStringFromScType(ssData.getServiceType());
        this.mAction = getActionStringFromReqType(ssData.getRequestType());
        Rlog.d(LOG_TAG, "parseSsData msc = " + this.mSc + ", action = " + this.mAction + ", ex = " + ex);
        int requestType = ssData.getRequestType();
        boolean cffEnabled = false;
        if (!(requestType == 0 || requestType == 1)) {
            if (requestType != 2) {
                if (!(requestType == 3 || requestType == 4)) {
                    Rlog.e(LOG_TAG, "Invaid requestType in SSData : " + ssData.getRequestType());
                    return;
                }
            } else if (ssData.isTypeClir()) {
                Rlog.d(LOG_TAG, "CLIR INTERROGATION");
                Bundle clirInfo = new Bundle();
                clirInfo.putIntArray(UT_BUNDLE_KEY_CLIR, ssData.getSuppServiceInfoCompat());
                onQueryClirComplete(new AsyncResult((Object) null, clirInfo, ex));
                return;
            } else if (ssData.isTypeCF()) {
                Rlog.d(LOG_TAG, "CALL FORWARD INTERROGATION");
                List<ImsCallForwardInfo> mCfInfos = ssData.getCallForwardInfo();
                ImsCallForwardInfo[] mCfInfosCompat = null;
                if (mCfInfos != null) {
                    mCfInfosCompat = (ImsCallForwardInfo[]) mCfInfos.toArray(new ImsCallForwardInfo[mCfInfos.size()]);
                }
                onQueryCfComplete(new AsyncResult((Object) null, this.mPhone.handleCfQueryResult(mCfInfosCompat), ex));
                return;
            } else if (ssData.isTypeBarring()) {
                onSuppSvcQueryComplete(new AsyncResult((Object) null, ssData.getSuppServiceInfoCompat(), ex));
                return;
            } else if (ssData.isTypeColr() || ssData.isTypeClip() || ssData.isTypeColp()) {
                ImsSsInfo ssInfo = new ImsSsInfo.Builder(ssData.getSuppServiceInfoCompat()[0]).build();
                Bundle clInfo = new Bundle();
                clInfo.putParcelable(UT_BUNDLE_KEY_SSINFO, ssInfo);
                onSuppSvcQueryComplete(new AsyncResult((Object) null, clInfo, ex));
                return;
            } else if (ssData.isTypeIcb()) {
                onIcbQueryComplete(new AsyncResult((Object) null, ssData.getSuppServiceInfo(), ex));
                return;
            } else {
                onQueryComplete(new AsyncResult((Object) null, ssData.getSuppServiceInfoCompat(), ex));
                return;
            }
        }
        if (ssData.getResult() == 0 && ssData.isTypeUnConditional()) {
            if ((ssData.getRequestType() == 0 || ssData.getRequestType() == 3) && isServiceClassVoiceVideoOrNone(ssData.getServiceClass())) {
                cffEnabled = true;
            }
            Rlog.d(LOG_TAG, "setCallForwardingFlag cffEnabled: " + cffEnabled);
            if (this.mIccRecords != null) {
                Rlog.d(LOG_TAG, "setVoiceCallForwardingFlag done from SS Info.");
                this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, null);
            } else {
                Rlog.e(LOG_TAG, "setCallForwardingFlag aborted. sim records is null.");
            }
        }
        onSetComplete(null, new AsyncResult((Object) null, ssData.getCallForwardInfo(), ex));
    }

    /* access modifiers changed from: protected */
    public String getScStringFromScType(int serviceType) {
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
            case 6:
            default:
                return null;
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
        }
    }

    /* access modifiers changed from: protected */
    public String getActionStringFromReqType(int requestType) {
        if (requestType == 0) {
            return "*";
        }
        if (requestType == 1) {
            return ACTION_DEACTIVATE;
        }
        if (requestType == 2) {
            return ACTION_INTERROGATE;
        }
        if (requestType == 3) {
            return ACTION_REGISTER;
        }
        if (requestType != 4) {
            return null;
        }
        return ACTION_ERASURE;
    }

    /* access modifiers changed from: protected */
    public boolean isServiceClassVoiceVideoOrNone(int serviceClass) {
        return serviceClass == 0 || serviceClass == 1 || serviceClass == 80;
    }

    public boolean isSsInfo() {
        return this.mIsSsInfo;
    }

    public void setIsSsInfo(boolean isSsInfo) {
        this.mIsSsInfo = isSsInfo;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ImsPhoneMmiCode {");
        sb.append("State=" + getState());
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
            sb.append(" poundString=" + Rlog.pii(LOG_TAG, this.mPoundString));
        }
        if (this.mDialingNumber != null) {
            sb.append(" dialingNumber=" + Rlog.pii(LOG_TAG, this.mDialingNumber));
        }
        if (this.mPwd != null) {
            sb.append(" pwd=" + Rlog.pii(LOG_TAG, this.mPwd));
        }
        if (this.mCallbackReceiver != null) {
            sb.append(" hasReceiver");
        }
        sb.append("}");
        return sb.toString();
    }
}
