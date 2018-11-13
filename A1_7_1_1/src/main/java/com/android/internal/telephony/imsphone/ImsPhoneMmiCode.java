package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import com.android.ims.ImsException;
import com.android.ims.ImsSsInfo;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.MmiCode.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
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
    private static final int EVENT_SET_CLIR_COMPLETE = 8;
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
    private static final String SC_BAICa = "157";
    private static final String SC_BAICr = "351";
    private static final String SC_BAOC = "33";
    private static final String SC_BAOIC = "331";
    private static final String SC_BAOICxH = "332";
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
    private static Pattern sPatternSuppService;
    private static String[] sTwoDigitNumberPattern;
    private String mAction;
    private Context mContext;
    private String mDialingNumber;
    private IccRecords mIccRecords;
    private boolean mIsCallFwdReg;
    private boolean mIsPendingUSSD;
    private boolean mIsUssdRequest;
    private CharSequence mMessage;
    private ImsPhone mPhone;
    private String mPoundString;
    private String mPwd;
    private String mSc;
    private String mSia;
    private String mSib;
    private String mSic;
    private State mState;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.imsphone.ImsPhoneMmiCode.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.imsphone.ImsPhoneMmiCode.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhoneMmiCode.<clinit>():void");
    }

    static ImsPhoneMmiCode newFromDialString(String dialString, ImsPhone phone) {
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
                return 528;
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
            return 0;
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
            String[] barringMMI = resource.getStringArray(17236031);
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

    ImsPhoneMmiCode(ImsPhone phone) {
        super(phone.getHandler().getLooper());
        this.mState = State.PENDING;
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
                this.mPhone.cancelUSSD(obtainMessage(5, this));
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

    private static boolean isTwoDigitShortCode(Context context, String dialString) {
        Rlog.d(LOG_TAG, "isTwoDigitShortCode");
        if (dialString == null || dialString.length() > 2) {
            return false;
        }
        if (sTwoDigitNumberPattern == null) {
            sTwoDigitNumberPattern = context.getResources().getStringArray(17236018);
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

    /* JADX WARNING: Missing block: B:79:0x0146, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isSupportedOverImsPhone() {
        if (isShortCode()) {
            return true;
        }
        if (this.mDialingNumber != null) {
            return false;
        }
        if (!isServiceCodeCallForwarding(this.mSc) && !isServiceCodeCallBarring(this.mSc) && ((this.mSc == null || !this.mSc.equals(SC_WAIT)) && ((this.mSc == null || !this.mSc.equals(SC_CLIR)) && ((this.mSc == null || !this.mSc.equals(SC_CLIP)) && ((this.mSc == null || !this.mSc.equals(SC_COLR)) && ((this.mSc == null || !this.mSc.equals(SC_COLP)) && ((this.mSc == null || !this.mSc.equals(SC_BS_MT)) && (this.mSc == null || !this.mSc.equals(SC_BAICa))))))))) {
            return !isPinPukCommand() && ((this.mSc == null || !(this.mSc.equals(SC_PWD) || this.mSc.equals(SC_CLIP) || this.mSc.equals(SC_CLIR))) && this.mPoundString != null);
        } else {
            int serviceClass;
            if (is93MDSupport()) {
                try {
                    serviceClass = siToServiceClass(this.mSib);
                    if ((serviceClass & 1) == 0 && (serviceClass & 512) == 0 && serviceClass != 0) {
                        return false;
                    }
                    Rlog.d(LOG_TAG, "isSupportedOverImsPhone(), return true!");
                    return true;
                } catch (RuntimeException exc) {
                    Rlog.d(LOG_TAG, "Invalid service class " + exc);
                }
            } else {
                if (this.mPhone.isVolteEnabled() || (this.mPhone.isWifiCallingEnabled() && ((GsmCdmaPhone) this.mPhone.mDefaultPhone).isWFCUtSupport())) {
                    try {
                        serviceClass = siToServiceClass(this.mSib);
                        if (!((serviceClass & 1) == 0 && (serviceClass & 512) == 0 && serviceClass != 0)) {
                            Rlog.d(LOG_TAG, "isSupportedOverImsPhone(), return true!");
                            return true;
                        }
                    } catch (RuntimeException exc2) {
                        Rlog.d(LOG_TAG, "exc.toString() = " + exc2.toString());
                    }
                }
                return false;
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
            if (is93MDSupport() || this.mPhone.mDefaultPhone.getCsFallbackStatus() == 0) {
                CarrierConfigManager configLoader;
                boolean isUssiEnabled;
                PersistableBundle b;
                int serviceClass;
                Message msg;
                if (isShortCode()) {
                    Rlog.d(LOG_TAG, "isShortCode");
                    configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
                    isUssiEnabled = false;
                    if (configLoader != null) {
                        b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                        isUssiEnabled = b == null ? false : b.getBoolean("oppo_ussd_over_ims", false);
                        Rlog.d(LOG_TAG, "isUssiEnabled " + isUssiEnabled);
                    }
                    if (isUssiEnabled) {
                        Rlog.d(LOG_TAG, "Sending short code '" + this.mDialingNumber + "' over IMS.");
                        sendUssd(this.mDialingNumber);
                    } else {
                        Rlog.d(LOG_TAG, "Sending short code '" + this.mDialingNumber + "' over CS pipe.");
                        this.mPhone.removeMmi(this);
                        throw new CallStateException(Phone.CS_FALLBACK);
                    }
                } else if (isServiceCodeCallForwarding(this.mSc)) {
                    Rlog.d(LOG_TAG, "is CF");
                    String dialingNumber = this.mSia;
                    int reason = scToCallForwardReason(this.mSc);
                    serviceClass = siToServiceClass(this.mSib);
                    int time = siToTime(this.mSic);
                    if (isInterrogate()) {
                        if (serviceClass != 0 && (this.mPhone.mDefaultPhone instanceof GsmCdmaPhone)) {
                            ((GsmCdmaPhone) this.mPhone.mDefaultPhone).setServiceClass(serviceClass);
                        }
                        this.mPhone.getCallForwardingOption(reason, obtainMessage(1, this));
                    } else {
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
                        Rlog.d(LOG_TAG, "is CF setCallForward");
                        if (((GsmCdmaPhone) this.mPhone.mDefaultPhone).isOpReregisterForCF()) {
                            Rlog.i(LOG_TAG, "Set ims dereg to ON.");
                            SystemProperties.set(GsmCdmaPhone.IMS_DEREG_PROP, "1");
                        }
                        this.mPhone.setCallForwardingOption(cfAction, reason, dialingNumber, serviceClass, time, obtainMessage(4, isSettingUnconditional, isEnableDesired, this));
                    }
                } else if (isServiceCodeCallBarring(this.mSc)) {
                    String password = this.mSia;
                    serviceClass = siToServiceClass(this.mSib);
                    String facility = scToBarringFacility(this.mSc);
                    if (serviceClass != 0 && (this.mPhone.mDefaultPhone instanceof GsmCdmaPhone)) {
                        ((GsmCdmaPhone) this.mPhone.mDefaultPhone).setServiceClass(serviceClass);
                    }
                    if (isInterrogate()) {
                        this.mPhone.getCallBarring(facility, obtainMessage(7, this));
                    } else if (isActivate() || isDeactivate()) {
                        this.mPhone.setCallBarring(facility, isActivate(), password, obtainMessage(0, this));
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
                                        Rlog.d(LOG_TAG, "Could not get UT handle for ICB.");
                                    }
                                } else if (this.mSc != null && this.mSc.equals(SC_BAICa)) {
                                    int callAction = 0;
                                    try {
                                        if (isInterrogate()) {
                                            this.mPhone.mCT.getUtInterface().queryCallBarring(6, obtainMessage(10, this));
                                        } else {
                                            if (isActivate()) {
                                                callAction = 1;
                                            } else if (isDeactivate()) {
                                                callAction = 0;
                                            }
                                            this.mPhone.mCT.getUtInterface().updateCallBarring(6, callAction, obtainMessage(0, this), null);
                                        }
                                    } catch (ImsException e2) {
                                        Rlog.d(LOG_TAG, "Could not get UT handle for ICBa.");
                                    }
                                } else if (this.mSc != null && this.mSc.equals(SC_WAIT)) {
                                    serviceClass = siToServiceClass(this.mSib);
                                    String tbcwMode;
                                    if (is93MDSupport()) {
                                        if (isActivate() || isDeactivate()) {
                                            this.mPhone.setCallWaiting(isActivate(), serviceClass, obtainMessage(0, this));
                                        } else if (isInterrogate()) {
                                            this.mPhone.getCallWaiting(obtainMessage(3, this));
                                        } else {
                                            throw new RuntimeException("Invalid or Unsupported MMI Code");
                                        }
                                    } else if (isActivate() || isDeactivate()) {
                                        if (((GsmCdmaPhone) this.mPhone.mDefaultPhone).isOpNwCW()) {
                                            Rlog.d(LOG_TAG, "setCallWaiting() by Ut interface.");
                                            this.mPhone.setCallWaiting(isActivate(), serviceClass, obtainMessage(0, this));
                                        } else {
                                            tbcwMode = this.mPhone.mDefaultPhone.getSystemProperty("persist.radio.terminal-based.cw", "disabled_tbcw");
                                            Rlog.d(LOG_TAG, "setCallWaiting(): tbcwMode = " + tbcwMode + ", enable = " + isActivate());
                                            if ("enabled_tbcw_on".equals(tbcwMode)) {
                                                if (!isActivate()) {
                                                    this.mPhone.mDefaultPhone.setSystemProperty("persist.radio.terminal-based.cw", "enabled_tbcw_off");
                                                }
                                                msg = obtainMessage(0, null);
                                                AsyncResult.forMessage(msg, null, null);
                                                sendMessage(msg);
                                            } else if ("enabled_tbcw_off".equals(tbcwMode)) {
                                                if (isActivate()) {
                                                    this.mPhone.mDefaultPhone.setSystemProperty("persist.radio.terminal-based.cw", "enabled_tbcw_on");
                                                }
                                                msg = obtainMessage(0, null);
                                                AsyncResult.forMessage(msg, null, null);
                                                sendMessage(msg);
                                            } else {
                                                Rlog.d(LOG_TAG, "setCallWaiting() by Ut interface.");
                                                this.mPhone.setCallWaiting(isActivate(), serviceClass, obtainMessage(0, this));
                                            }
                                        }
                                    } else if (!isInterrogate()) {
                                        throw new RuntimeException("Invalid or Unsupported MMI Code");
                                    } else if (((GsmCdmaPhone) this.mPhone.mDefaultPhone).isOpNwCW()) {
                                        Rlog.d(LOG_TAG, "getCallWaiting() by Ut interface.");
                                        this.mPhone.getCallWaiting(obtainMessage(3, this));
                                    } else {
                                        tbcwMode = this.mPhone.mDefaultPhone.getSystemProperty("persist.radio.terminal-based.cw", "disabled_tbcw");
                                        Rlog.d(LOG_TAG, "SC_WAIT isInterrogate() tbcwMode = " + tbcwMode);
                                        int[] cwInfos;
                                        if ("enabled_tbcw_on".equals(tbcwMode)) {
                                            cwInfos = new int[2];
                                            cwInfos[0] = 1;
                                            cwInfos[1] = 1;
                                            msg = obtainMessage(3, null);
                                            AsyncResult.forMessage(msg, cwInfos, null);
                                            sendMessage(msg);
                                        } else if ("enabled_tbcw_off".equals(tbcwMode)) {
                                            cwInfos = new int[2];
                                            cwInfos[0] = 0;
                                            msg = obtainMessage(3, null);
                                            AsyncResult.forMessage(msg, cwInfos, null);
                                            sendMessage(msg);
                                        } else {
                                            Rlog.d(LOG_TAG, "getCallWaiting() by Ut interface.");
                                            this.mPhone.getCallWaiting(obtainMessage(3, this));
                                        }
                                    }
                                } else if (this.mPoundString != null) {
                                    configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
                                    isUssiEnabled = false;
                                    if (configLoader != null) {
                                        b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                                        isUssiEnabled = b == null ? false : b.getBoolean("oppo_ussd_over_ims", false);
                                        Rlog.d(LOG_TAG, "isUssiEnabled " + isUssiEnabled);
                                    }
                                    if (isUssiEnabled) {
                                        Rlog.d(LOG_TAG, "Sending pound string '" + this.mPoundString + "' over IMS.");
                                        sendUssd(this.mPoundString);
                                    } else {
                                        Rlog.d(LOG_TAG, "Sending pound string '" + this.mDialingNumber + "' over CS pipe.");
                                        this.mPhone.removeMmi(this);
                                        throw new CallStateException(Phone.CS_FALLBACK);
                                    }
                                } else {
                                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                                }
                            } else if (isActivate()) {
                                try {
                                    this.mPhone.mCT.getUtInterface().updateCOLR(1, obtainMessage(0, this));
                                } catch (ImsException e3) {
                                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLR.");
                                }
                            } else if (isDeactivate()) {
                                try {
                                    this.mPhone.mCT.getUtInterface().updateCOLR(0, obtainMessage(0, this));
                                } catch (ImsException e4) {
                                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLR.");
                                }
                            } else if (isInterrogate()) {
                                try {
                                    this.mPhone.mCT.getUtInterface().queryCOLR(obtainMessage(7, this));
                                } catch (ImsException e5) {
                                    Rlog.d(LOG_TAG, "Could not get UT handle for queryCOLR.");
                                }
                            } else {
                                throw new RuntimeException("Invalid or Unsupported MMI Code");
                            }
                        } else if (isInterrogate()) {
                            try {
                                this.mPhone.mCT.getUtInterface().queryCOLP(obtainMessage(7, this));
                            } catch (ImsException e6) {
                                Rlog.d(LOG_TAG, "Could not get UT handle for queryCOLP.");
                            }
                        } else if (isActivate() || isDeactivate()) {
                            try {
                                this.mPhone.mCT.getUtInterface().updateCOLP(isActivate(), obtainMessage(0, this));
                            } catch (ImsException e7) {
                                Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLP.");
                            }
                        } else {
                            throw new RuntimeException("Invalid or Unsupported MMI Code");
                        }
                    } else if (isInterrogate()) {
                        try {
                            this.mPhone.mCT.getUtInterface().queryCLIP(obtainMessage(7, this));
                        } catch (ImsException e8) {
                            Rlog.d(LOG_TAG, "Could not get UT handle for queryCLIP.");
                        }
                    } else if (isActivate() || isDeactivate()) {
                        try {
                            this.mPhone.mCT.getUtInterface().updateCLIP(isActivate(), obtainMessage(0, this));
                        } catch (ImsException e9) {
                            Rlog.d(LOG_TAG, "Could not get UT handle for updateCLIP.");
                        }
                    } else {
                        throw new RuntimeException("Invalid or Unsupported MMI Code");
                    }
                } else if (isActivate()) {
                    if (is93MDSupport() || !((GsmCdmaPhone) this.mPhone.mDefaultPhone).isOpTbClir()) {
                        try {
                            this.mPhone.mCT.getUtInterface().updateCLIR(1, obtainMessage(8, 1, 0, this));
                        } catch (ImsException e10) {
                            Rlog.d(LOG_TAG, "Could not get UT handle for updateCLIR.");
                        }
                    } else {
                        ((GsmCdmaPhone) this.mPhone.mDefaultPhone).mCi.setCLIR(1, obtainMessage(8, 1, 0, this));
                        return;
                    }
                } else if (isDeactivate()) {
                    if (is93MDSupport() || !((GsmCdmaPhone) this.mPhone.mDefaultPhone).isOpTbClir()) {
                        try {
                            this.mPhone.mCT.getUtInterface().updateCLIR(2, obtainMessage(8, 2, 0, this));
                        } catch (ImsException e11) {
                            Rlog.d(LOG_TAG, "Could not get UT handle for updateCLIR.");
                        }
                    } else {
                        ((GsmCdmaPhone) this.mPhone.mDefaultPhone).mCi.setCLIR(2, obtainMessage(8, 2, 0, this));
                        return;
                    }
                } else if (!isInterrogate()) {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                } else if (is93MDSupport() || !((GsmCdmaPhone) this.mPhone.mDefaultPhone).isOpTbClir()) {
                    try {
                        this.mPhone.mCT.getUtInterface().queryCLIR(obtainMessage(6, this));
                    } catch (ImsException e12) {
                        Rlog.d(LOG_TAG, "Could not get UT handle for queryCLIR.");
                    }
                } else {
                    msg = obtainMessage(6, this);
                    if (msg != null) {
                        int[] result = ((GsmCdmaPhone) this.mPhone.mDefaultPhone).getSavedClirSetting();
                        Bundle info = new Bundle();
                        info.putIntArray("queryClir", result);
                        AsyncResult.forMessage(msg, info, null);
                        msg.sendToTarget();
                    }
                    return;
                }
                return;
            }
            Rlog.d(LOG_TAG, "processCode(): getCsFallbackStatus(): CS Fallback!");
            this.mPhone.removeMmi(this);
            throw new CallStateException(Phone.CS_FALLBACK);
        } catch (RuntimeException e13) {
            this.mState = State.FAILED;
            this.mMessage = this.mContext.getText(17039524);
            this.mPhone.onMMIDone(this);
        }
    }

    void onUssdFinished(String ussdMessage, boolean isUssdRequest) {
        if (this.mState == State.PENDING) {
            if (ussdMessage == null) {
                this.mMessage = this.mContext.getText(17039532);
            } else {
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
            this.mMessage = this.mContext.getText(17039524);
            this.mPhone.onMMIDone(this);
        }
    }

    void sendUssd(String ussdMessage) {
        this.mIsPendingUSSD = true;
        this.mPhone.sendUSSD(ussdMessage, obtainMessage(2, this));
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        if (!is93MDSupport()) {
            ar = msg.obj;
            if (!(ar == null || ar.exception == null)) {
                if (ar.exception instanceof CommandException) {
                    CommandException cmdException = ar.exception;
                    if (cmdException.getCommandError() == Error.UT_XCAP_403_FORBIDDEN) {
                        Rlog.d(LOG_TAG, "handleMessage(): CommandException.Error.UT_XCAP_403_FORBIDDEN");
                        this.mPhone.handleMmiCodeCsfb(830, this);
                        return;
                    } else if (cmdException.getCommandError() == Error.UT_UNKNOWN_HOST) {
                        Rlog.d(LOG_TAG, "handleMessage(): CommandException.Error.UT_UNKNOWN_HOST");
                        this.mPhone.handleMmiCodeCsfb(831, this);
                        return;
                    }
                } else if (ar.exception instanceof ImsException) {
                    ImsException imsException = ar.exception;
                    if (imsException.getCode() == 830) {
                        Rlog.d(LOG_TAG, "handleMessage(): ImsReasonInfo.CODE_UT_XCAP_403_FORBIDDEN");
                        this.mPhone.handleMmiCodeCsfb(830, this);
                        return;
                    } else if (imsException.getCode() == 831) {
                        Rlog.d(LOG_TAG, "handleMessage(): ImsReasonInfo.CODE_UT_UNKNOWN_HOST");
                        this.mPhone.handleMmiCodeCsfb(831, this);
                        return;
                    }
                }
            }
        }
        switch (msg.what) {
            case 0:
                onSetComplete(msg, (AsyncResult) msg.obj);
                break;
            case 1:
                onQueryCfComplete((AsyncResult) msg.obj);
                break;
            case 2:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    this.mPhone.mUssiCSFB = true;
                    this.mState = State.FAILED;
                    this.mMessage = getErrorMessage(ar);
                    this.mPhone.onMMIDone(this);
                    break;
                }
                break;
            case 3:
                onQueryComplete((AsyncResult) msg.obj);
                break;
            case 4:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && msg.arg1 == 1) {
                    boolean cffEnabled = msg.arg2 == 1;
                    if (((GsmCdmaPhone) this.mPhone.mDefaultPhone).queryCFUAgainAfterSet()) {
                        if (ar.result != null) {
                            CallForwardInfo[] cfInfos = ar.result;
                            if (cfInfos == null || cfInfos.length == 0) {
                                Rlog.i(LOG_TAG, "cfInfo is null or length is 0.");
                            } else {
                                int i = 0;
                                while (i < cfInfos.length) {
                                    if ((cfInfos[i].serviceClass & 512) != 0) {
                                        this.mPhone.setVideoCallForwardingFlag(cfInfos[i].status == 1);
                                    }
                                    if ((cfInfos[i].serviceClass & 1) == 0) {
                                        i++;
                                    } else if (cfInfos[i].status == 1) {
                                        Rlog.i(LOG_TAG, "Set CF_ENABLE, serviceClass: " + cfInfos[i].serviceClass);
                                        cffEnabled = true;
                                    } else {
                                        Rlog.i(LOG_TAG, "Set CF_DISABLE, serviceClass: " + cfInfos[i].serviceClass);
                                        cffEnabled = false;
                                    }
                                }
                            }
                        } else {
                            Rlog.i(LOG_TAG, "ar.result is null.");
                        }
                    }
                    Rlog.i(LOG_TAG, "EVENT_SET_CFF_COMPLETE: cffEnabled:" + cffEnabled + ", mDialingNumber=" + this.mDialingNumber + ", mIccRecords=" + this.mIccRecords);
                    if (this.mIccRecords != null) {
                        ((GsmCdmaPhone) this.mPhone.mDefaultPhone).setVoiceCallForwardingFlag(1, cffEnabled, this.mDialingNumber);
                        this.mPhone.saveTimeSlot(null);
                    }
                    if (cffEnabled) {
                        TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_on");
                    } else {
                        TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
                    }
                }
                onSetComplete(msg, ar);
                break;
            case 5:
                this.mPhone.onMMIDone(this);
                break;
            case 6:
                onQueryClirComplete((AsyncResult) msg.obj);
                break;
            case 7:
                onSuppSvcQueryComplete((AsyncResult) msg.obj);
                break;
            case 8:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    ((GsmCdmaPhone) this.mPhone.mDefaultPhone).saveClirSetting(msg.arg1);
                }
                onSetComplete(msg, ar);
                break;
            case 10:
                onIcbQueryComplete((AsyncResult) msg.obj);
                break;
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
            Rlog.d(LOG_TAG, "Could not get UT handle for updating ICB.");
        }
    }

    private CharSequence getErrorMessage(AsyncResult ar) {
        return this.mContext.getText(17039524);
    }

    private CharSequence getScString() {
        if (this.mSc != null) {
            if (isServiceCodeCallBarring(this.mSc)) {
                return this.mContext.getText(17039549);
            }
            if (isServiceCodeCallForwarding(this.mSc)) {
                return this.mContext.getText(17039547);
            }
            if (this.mSc.equals(SC_PWD)) {
                return this.mContext.getText(17039550);
            }
            if (this.mSc.equals(SC_WAIT)) {
                return this.mContext.getText(17039548);
            }
            if (this.mSc.equals(SC_CLIP)) {
                return this.mContext.getText(17039543);
            }
            if (this.mSc.equals(SC_CLIR)) {
                return this.mContext.getText(17039544);
            }
            if (this.mSc.equals(SC_COLP)) {
                return this.mContext.getText(17039545);
            }
            if (this.mSc.equals(SC_COLR)) {
                return this.mContext.getText(17039546);
            }
            if (this.mSc.equals(SC_BS_MT)) {
                return IcbDnMmi;
            }
            if (this.mSc.equals(SC_BAICa)) {
                return IcbAnonymousMmi;
            }
        }
        return UsimPBMemInfo.STRING_NOT_SET;
    }

    private void onSetComplete(Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof CommandException) {
                CommandException err = ar.exception;
                if (err.getCommandError() == Error.PASSWORD_INCORRECT) {
                    sb.append(this.mContext.getText(17039531));
                } else if (err.getMessage() != null) {
                    sb.append(err.getMessage());
                } else {
                    sb.append(this.mContext.getText(17039524));
                }
            } else {
                ImsException error = ar.exception;
                if (error.getMessage() != null) {
                    sb.append(error.getMessage());
                } else {
                    sb.append(getErrorMessage(ar));
                }
            }
        } else if (isActivate()) {
            this.mState = State.COMPLETE;
            if (this.mIsCallFwdReg) {
                sb.append(this.mContext.getText(17039529));
            } else {
                sb.append(this.mContext.getText(17039526));
            }
        } else if (isDeactivate()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17039528));
        } else if (isRegister()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17039529));
        } else if (isErasure()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17039530));
        } else {
            this.mState = State.FAILED;
            sb.append(this.mContext.getText(17039524));
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private CharSequence serviceClassToCFString(int serviceClass) {
        switch (serviceClass) {
            case 1:
                return this.mContext.getText(17039576);
            case 2:
                return this.mContext.getText(17039577);
            case 4:
                return this.mContext.getText(17039578);
            case 8:
                return this.mContext.getText(17039579);
            case 16:
                return this.mContext.getText(17039581);
            case 32:
                return this.mContext.getText(17039580);
            case 64:
                return this.mContext.getText(17039582);
            case 128:
                return this.mContext.getText(17039583);
            default:
                return null;
        }
    }

    private CharSequence makeCFQueryResultMessage(CallForwardInfo info, int serviceClassMask) {
        CharSequence template;
        boolean z = true;
        String[] sources = new String[3];
        sources[0] = "{0}";
        sources[1] = "{1}";
        sources[2] = "{2}";
        CharSequence[] destinations = new CharSequence[3];
        boolean needTimeTemplate = info.reason == 2;
        if (info.status != 1 || isEmptyOrNull(info.number)) {
            if (isEmptyOrNull(info.number)) {
                template = this.mContext.getText(17039603);
            } else if (needTimeTemplate) {
                template = this.mContext.getText(17039607);
            } else {
                template = this.mContext.getText(17039606);
            }
        } else if (needTimeTemplate) {
            template = this.mContext.getText(17039605);
        } else {
            template = this.mContext.getText(17039604);
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
            if (info.status != 1) {
                z = false;
            }
            imsPhone.setVideoCallForwardingFlag(z);
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
                if (error.getMessage() != null) {
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
                sb.append(this.mContext.getText(17039528));
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
                            tb.append("\n");
                        }
                    }
                }
                sb.append(tb);
            }
            this.mState = State.COMPLETE;
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private void onSuppSvcQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                ImsException error = ar.exception;
                if (error.getMessage() != null) {
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
                Rlog.d(LOG_TAG, "Received CLIP/COLP/COLR Response.");
                ImsSsInfo ssInfo = (ImsSsInfo) ar.result.getParcelable(UT_BUNDLE_KEY_SSINFO);
                if (ssInfo != null) {
                    Rlog.d(LOG_TAG, "ImsSsInfo mStatus = " + ssInfo.mStatus);
                    if (ssInfo.mStatus == 0) {
                        sb.append(this.mContext.getText(17039528));
                        this.mState = State.COMPLETE;
                    } else if (ssInfo.mStatus == 1) {
                        sb.append(this.mContext.getText(17039526));
                        this.mState = State.COMPLETE;
                    } else {
                        sb.append(this.mContext.getText(17039524));
                    }
                } else {
                    sb.append(this.mContext.getText(17039524));
                }
            } else {
                Rlog.d(LOG_TAG, "Received Call Barring Response.");
                if (ar.result[0] == 1) {
                    sb.append(this.mContext.getText(17039526));
                    this.mState = State.COMPLETE;
                } else {
                    sb.append(this.mContext.getText(17039528));
                    this.mState = State.COMPLETE;
                }
            }
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private void onIcbQueryComplete(AsyncResult ar) {
        Rlog.d(LOG_TAG, "onIcbQueryComplete ");
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                ImsException error = ar.exception;
                if (error.getMessage() != null) {
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
                sb.append(this.mContext.getText(17039528));
            } else {
                int s = infos.length;
                for (int i = 0; i < s; i++) {
                    if (infos[i].mIcbNum != null) {
                        sb.append("Num: ").append(infos[i].mIcbNum).append(" status: ").append(infos[i].mStatus).append("\n");
                    } else if (infos[i].mStatus == 1) {
                        sb.append(this.mContext.getText(17039526));
                    } else {
                        sb.append(this.mContext.getText(17039528));
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
            int[] clirInfo = ar.result.getIntArray("queryClir");
            Rlog.d(LOG_TAG, "CLIR param n=" + clirInfo[0] + " m=" + clirInfo[1]);
            switch (clirInfo[1]) {
                case 0:
                    sb.append(this.mContext.getText(17039562));
                    this.mState = State.COMPLETE;
                    break;
                case 1:
                    sb.append(this.mContext.getText(17039563));
                    this.mState = State.COMPLETE;
                    break;
                case 3:
                    switch (clirInfo[0]) {
                        case 0:
                            sb.append(this.mContext.getText(17039558));
                            this.mState = State.COMPLETE;
                            break;
                        case 1:
                            sb.append(this.mContext.getText(17039558));
                            this.mState = State.COMPLETE;
                            break;
                        case 2:
                            sb.append(this.mContext.getText(17039559));
                            this.mState = State.COMPLETE;
                            break;
                        default:
                            sb.append(this.mContext.getText(17039524));
                            this.mState = State.FAILED;
                            break;
                    }
                case 4:
                    switch (clirInfo[0]) {
                        case 0:
                            sb.append(this.mContext.getText(17039561));
                            this.mState = State.COMPLETE;
                            break;
                        case 1:
                            sb.append(this.mContext.getText(17039560));
                            this.mState = State.COMPLETE;
                            break;
                        case 2:
                            sb.append(this.mContext.getText(17039561));
                            this.mState = State.COMPLETE;
                            break;
                        default:
                            sb.append(this.mContext.getText(17039524));
                            this.mState = State.FAILED;
                            break;
                    }
                default:
                    sb.append(this.mContext.getText(17039524));
                    this.mState = State.FAILED;
                    break;
            }
        } else if (ar.exception instanceof ImsException) {
            ImsException error = ar.exception;
            if (error.getMessage() != null) {
                sb.append(error.getMessage());
            } else {
                sb.append(getErrorMessage(ar));
            }
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private void onQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                ImsException error = ar.exception;
                if (error.getMessage() != null) {
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
                sb.append(this.mContext.getText(17039524));
            } else if (ints[0] == 0) {
                sb.append(this.mContext.getText(17039528));
            } else if (this.mSc.equals(SC_WAIT)) {
                sb.append(createQueryCallWaitingResultMessage(ints[1]));
            } else if (ints[0] == 1) {
                sb.append(this.mContext.getText(17039526));
            } else {
                sb.append(this.mContext.getText(17039524));
            }
            this.mState = State.COMPLETE;
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private CharSequence createQueryCallWaitingResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17039527));
        for (int classMask = 1; classMask <= 512; classMask <<= 1) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    public boolean getUserInitiatedMMI() {
        return false;
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
            sb.append(" poundString=").append(this.mPoundString);
        }
        if (this.mDialingNumber != null) {
            sb.append(" dialingNumber=").append(this.mDialingNumber);
        }
        if (this.mPwd != null) {
            sb.append(" pwd=").append(this.mPwd);
        }
        sb.append("}");
        return sb.toString();
    }

    /* JADX WARNING: Missing block: B:30:0x007b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isUssdNumber() {
        if (isTemporaryModeCLIR()) {
            return false;
        }
        if (isShortCode() || this.mDialingNumber != null) {
            return true;
        }
        return (this.mSc == null || !(this.mSc.equals(SC_CNAP) || this.mSc.equals(SC_CLIP) || this.mSc.equals(SC_CLIR) || this.mSc.equals(SC_COLP) || this.mSc.equals(SC_COLR) || isServiceCodeCallForwarding(this.mSc) || isServiceCodeCallBarring(this.mSc) || this.mSc.equals(SC_PWD) || this.mSc.equals(SC_WAIT) || isPinPukCommand())) && this.mPoundString != null;
    }

    public String getUssdDialString() {
        Rlog.d(LOG_TAG, "getUssdDialString(): mDialingNumber=" + this.mDialingNumber + ", mPoundString=" + this.mPoundString);
        if (this.mDialingNumber != null) {
            return this.mDialingNumber;
        }
        return this.mPoundString;
    }

    /* JADX WARNING: Missing block: B:4:0x000d, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:9:0x0018, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isUtMmiCode(String dialString, ImsPhone dialPhone) {
        ImsPhoneMmiCode mmi = newFromDialString(dialString, dialPhone);
        if (mmi == null || mmi.isTemporaryModeCLIR() || mmi.isShortCode() || mmi.mDialingNumber != null || mmi.mSc == null || (!mmi.mSc.equals(SC_CLIP) && !mmi.mSc.equals(SC_CLIR) && !mmi.mSc.equals(SC_COLP) && !mmi.mSc.equals(SC_COLR) && !isServiceCodeCallForwarding(mmi.mSc) && !isServiceCodeCallBarring(mmi.mSc) && !mmi.mSc.equals(SC_WAIT) && !mmi.mSc.equals(SC_BS_MT) && !mmi.mSc.equals(SC_BAICa))) {
            return false;
        }
        return true;
    }

    private boolean is93MDSupport() {
        if (SystemProperties.get("ro.md_auto_setup_ims").equals("1")) {
            return true;
        }
        return false;
    }
}
