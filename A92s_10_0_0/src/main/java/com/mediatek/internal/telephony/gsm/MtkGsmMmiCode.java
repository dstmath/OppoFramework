package com.mediatek.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Build;
import android.os.Message;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkSSRequestDecisionMaker;
import com.mediatek.internal.telephony.MtkSuppServHelper;
import com.mediatek.internal.telephony.MtkSuppServManager;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.worldphone.WorldMode;
import java.util.regex.Matcher;

public final class MtkGsmMmiCode extends GsmMmiCode {
    static final String CNAPMmi = "Calling Name Presentation";
    static final int EVENT_GET_COLP_COMPLETE = 9;
    static final int EVENT_GET_COLR_COMPLETE = 8;
    static final String LOG_TAG = "MtkGsmMmiCode";
    static final String SC_CNAP = "300";
    static final String SC_COLP = "76";
    static final String SC_COLR = "77";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER);
    private MtkSSRequestDecisionMaker mMtkSSReqDecisionMaker;
    MtkGsmCdmaPhone mPhone;
    private boolean mUserInitiatedMMI = false;

    public static MtkGsmMmiCode newFromDialString(String dialString, MtkGsmCdmaPhone phone, UiccCardApplication app) {
        return newFromDialString(dialString, phone, app, null);
    }

    public static MtkGsmMmiCode newFromDialString(String dialString, MtkGsmCdmaPhone phone, UiccCardApplication app, ResultReceiver wrappedCallback) {
        MtkGsmMmiCode ret = null;
        Rlog.d(LOG_TAG, "newFromDialString, dialstring = " + MtkSuppServHelper.encryptString(dialString));
        String dialPart = PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.stripSeparators(dialString));
        if ((dialPart.startsWith("*") || dialPart.startsWith("#") || dialPart.endsWith("#")) || dialPart.length() <= 2) {
            if (phone.getServiceState().getVoiceRoaming() && phone.supportsConversionOfCdmaCallerIdMmiCodesWhileRoaming()) {
                dialString = convertCdmaMmiCodesTo3gppMmiCodes(dialString);
            }
            Matcher m = sPatternSuppService.matcher(dialString);
            if (m.matches()) {
                ret = new MtkGsmMmiCode(phone, app);
                ret.mPoundString = makeEmptyNull(m.group(1));
                ret.mAction = makeEmptyNull(m.group(2));
                ret.mSc = makeEmptyNull(m.group(3));
                ret.mSia = makeEmptyNull(m.group(5));
                ret.mSib = makeEmptyNull(m.group(7));
                ret.mSic = makeEmptyNull(m.group(9));
                ret.mPwd = makeEmptyNull(m.group(11));
                ret.mDialingNumber = makeEmptyNull(m.group(12));
                if (ret.mDialingNumber != null && ret.mDialingNumber.endsWith("#") && dialString.endsWith("#")) {
                    ret = new MtkGsmMmiCode(phone, app);
                    ret.mPoundString = dialString;
                } else if (ret.isFacToDial()) {
                    ret = null;
                }
            } else if (dialString.endsWith("#")) {
                ret = new MtkGsmMmiCode(phone, app);
                ret.mPoundString = dialString;
            } else if (isTwoDigitShortCode(phone.getContext(), dialString)) {
                ret = null;
            } else if (isShortCode(dialString, phone)) {
                ret = new MtkGsmMmiCode(phone, app);
                ret.mDialingNumber = dialString;
            }
            if (ret != null) {
                ret.mCallbackReceiver = wrappedCallback;
            }
            return ret;
        }
        Rlog.d(LOG_TAG, "Not belong to MMI format.");
        return null;
    }

    public static MtkGsmMmiCode newFromUssdUserInput(String ussdMessge, MtkGsmCdmaPhone phone, UiccCardApplication app) {
        MtkGsmMmiCode ret = new MtkGsmMmiCode(phone, app);
        ret.mMessage = ussdMessge;
        ret.mState = MmiCode.State.PENDING;
        ret.mIsPendingUSSD = true;
        return ret;
    }

    private int specifyServiceClassForOperator() {
        String[][] serviceCodeForOp = {new String[]{"50501", "voice"}, new String[]{"50511", "voice"}, new String[]{"50571", "voice"}};
        int serviceCode = -1;
        String mccMnc = TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "gsm.sim.operator.numeric", "");
        if (mccMnc != null) {
            int i = 0;
            while (true) {
                if (i >= serviceCodeForOp.length) {
                    break;
                } else if (!mccMnc.equals(serviceCodeForOp[i][0])) {
                    i++;
                } else if (serviceCodeForOp[i][1].equals("voice")) {
                    serviceCode = 1;
                } else if (serviceCodeForOp[i][1].equals("video")) {
                    serviceCode = 512;
                } else if (serviceCodeForOp[i][1].equals("both")) {
                    serviceCode = 0;
                }
            }
        }
        Rlog.d(LOG_TAG, "specifyServiceClassForOperator(): mccMnc = " + mccMnc + " serviceCode = " + serviceCode);
        return serviceCode;
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
                    case WorldMode.MD_WORLD_MODE_LTWCG /*{ENCODED_INT: 19}*/:
                        return 5;
                    case 20:
                        return 48;
                    case WorldMode.MD_WORLD_MODE_LFCTG /*{ENCODED_INT: 21}*/:
                        return 160;
                    case 22:
                        return 80;
                    default:
                        switch (serviceCode) {
                            case 24:
                                return 528;
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

    public MtkGsmMmiCode(MtkGsmCdmaPhone phone, UiccCardApplication app) {
        super(phone, app);
        this.mPhone = phone;
        this.mMtkSSReqDecisionMaker = phone.getMtkSSRequestDecisionMaker();
    }

    public void setUserInitiatedMMI(boolean userinit) {
        this.mUserInitiatedMMI = userinit;
    }

    public boolean getUserInitiatedMMI() {
        return this.mUserInitiatedMMI;
    }

    public void processCode() throws CallStateException {
        try {
            if (supportMdAutoSetupIms() || (!this.mPhone.isDuringVoLteCall() && !this.mPhone.isDuringImsEccCall())) {
                if (isShortCode()) {
                    Rlog.d(LOG_TAG, "isShortCode");
                    sendUssd(this.mDialingNumber);
                } else if (this.mDialingNumber != null) {
                    Rlog.w(LOG_TAG, "Special USSD Support:" + this.mPoundString + this.mDialingNumber);
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.mPoundString);
                    sb.append(this.mDialingNumber);
                    sendUssd(sb.toString());
                } else if (this.mSc == null || !this.mSc.equals(SC_CNAP) || !isInterrogate()) {
                    if (this.mSc != null && this.mSc.equals("30")) {
                        handleCLIP();
                    } else if (this.mSc != null && this.mSc.equals("31")) {
                        handleCLIR();
                    } else if (this.mSc != null && this.mSc.equals(SC_COLP)) {
                        handleCOLP();
                    } else if (this.mSc != null && this.mSc.equals(SC_COLR)) {
                        handleCOLR();
                    } else if (isServiceCodeCallForwarding(this.mSc)) {
                        handleCallForward();
                    } else if (isServiceCodeCallBarring(this.mSc)) {
                        handleCallBarring();
                    } else if (this.mSc != null && this.mSc.equals("03")) {
                        handleChangeBarringPassward();
                    } else if (this.mSc != null && this.mSc.equals("43")) {
                        handleCW();
                    } else if (isPinPukCommand()) {
                        String oldPinOrPuk = this.mSia;
                        String newPinOrPuk = this.mSib;
                        int pinLen = newPinOrPuk.length();
                        if (!isRegister()) {
                            throw new RuntimeException("Ivalid register/action=" + this.mAction);
                        } else if (!newPinOrPuk.equals(this.mSic)) {
                            handlePasswordError(17040435);
                        } else {
                            if (pinLen >= 4) {
                                if (pinLen <= 8) {
                                    if (this.mSc.equals("04") && this.mUiccApplication != null && this.mUiccApplication.getState() == IccCardApplicationStatus.AppState.APPSTATE_PUK) {
                                        handlePasswordError(17040453);
                                    } else if (this.mUiccApplication != null) {
                                        Rlog.d(LOG_TAG, "process mmi service code using UiccApp sc=" + this.mSc);
                                        if (this.mSc.equals("04")) {
                                            this.mUiccApplication.changeIccLockPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                                        } else if (this.mSc.equals("042")) {
                                            this.mUiccApplication.changeIccFdnPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                                        } else if (this.mSc.equals("05")) {
                                            this.mUiccApplication.supplyPuk(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                                        } else if (this.mSc.equals("052")) {
                                            this.mUiccApplication.supplyPuk2(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                                        } else {
                                            throw new RuntimeException("uicc unsupported service code=" + this.mSc);
                                        }
                                    } else {
                                        throw new RuntimeException("No application mUiccApplicaiton is null");
                                    }
                                }
                            }
                            handlePasswordError(17040141);
                        }
                    } else if (this.mPoundString != null) {
                        if (this.mPhone.getCsFallbackStatus() == 1) {
                            this.mPhone.setCsFallbackStatus(0);
                        }
                        sendUssd(this.mPoundString);
                    } else {
                        throw new RuntimeException("Invalid or Unsupported MMI Code");
                    }
                } else if (this.mPoundString != null) {
                    handleCNAP(this.mPoundString);
                }
                return;
            }
            Rlog.d(LOG_TAG, "Stop CS MMI during IMS Ecc Call or VoLTE call");
            if (this.mPhone.getCsFallbackStatus() == 1) {
                this.mPhone.setCsFallbackStatus(0);
            }
            StringBuilder sb2 = new StringBuilder(getScString());
            sb2.append("\n");
            this.mState = MmiCode.State.FAILED;
            sb2.append(this.mContext.getText(17040445));
            this.mMessage = sb2;
            this.mPhone.onMMIDone(this);
        } catch (RuntimeException exc) {
            this.mState = MmiCode.State.FAILED;
            exc.printStackTrace();
            Rlog.d(LOG_TAG, "exc.toString() = " + exc.toString());
            Rlog.d(LOG_TAG, "procesCode: mState = FAILED");
            this.mMessage = this.mContext.getText(17040445);
            this.mPhone.onMMIDone(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCNAP(String cnapssMessage) {
        Rlog.d(LOG_TAG, "processCode: is CNAP");
        if (isInterrogate()) {
            if (this.mPhone.getCsFallbackStatus() == 1) {
                this.mPhone.setCsFallbackStatus(0);
            }
            this.mPhone.mMtkCi.sendCNAP(cnapssMessage, obtainMessage(5, this));
            return;
        }
        throw new RuntimeException("Invalid or Unsupported MMI Code");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: com.mediatek.internal.telephony.MtkSSRequestDecisionMaker} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: com.mediatek.internal.telephony.MtkRIL} */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v2, types: [boolean, int] */
    /* access modifiers changed from: package-private */
    public void handleCLIP() {
        Rlog.d(LOG_TAG, "processCode: is CLIP");
        if (isActivate() || isDeactivate()) {
            ? isActivate = isActivate();
            if (supportMdAutoSetupIms()) {
                this.mPhone.mMtkCi.setCLIP(isActivate, obtainMessage(1, this));
            } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
                if (this.mPhone.getCsFallbackStatus() == 1) {
                    this.mPhone.setCsFallbackStatus(0);
                }
                this.mPhone.mMtkCi.setCLIP(isActivate == true ? 1 : 0, obtainMessage(1, this));
            } else {
                this.mMtkSSReqDecisionMaker.setCLIP(isActivate, obtainMessage(1, this));
            }
        } else if (!isInterrogate()) {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        } else if (supportMdAutoSetupIms()) {
            this.mPhone.mMtkCi.queryCLIP(obtainMessage(5, this));
        } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
            if (this.mPhone.getCsFallbackStatus() == 1) {
                this.mPhone.setCsFallbackStatus(0);
            }
            this.mPhone.mCi.queryCLIP(obtainMessage(5, this));
        } else {
            this.mMtkSSReqDecisionMaker.getCLIP(obtainMessage(5, this));
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCLIR() {
        Rlog.d(LOG_TAG, "processCode: is CLIR");
        int clirAction = 2;
        if (isActivate() || isDeactivate()) {
            if (isActivate()) {
                clirAction = 1;
            }
            if (supportMdAutoSetupIms()) {
                this.mPhone.mCi.setCLIR(clirAction, obtainMessage(1, this));
            } else if (this.mPhone.isOpTbClir()) {
                this.mPhone.mCi.setCLIR(clirAction, obtainMessage(1, this));
            } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
                if (this.mPhone.getCsFallbackStatus() == 1) {
                    this.mPhone.setCsFallbackStatus(0);
                }
                this.mPhone.mCi.setCLIR(clirAction, obtainMessage(1, this));
            } else {
                this.mMtkSSReqDecisionMaker.setCLIR(1, obtainMessage(1, this));
            }
        } else if (!isInterrogate()) {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        } else if (supportMdAutoSetupIms()) {
            this.mPhone.mCi.getCLIR(obtainMessage(2, this));
        } else if (this.mPhone.isOpTbClir()) {
            this.mPhone.mCi.getCLIR(obtainMessage(2, this));
        } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
            if (this.mPhone.getCsFallbackStatus() == 1) {
                this.mPhone.setCsFallbackStatus(0);
            }
            this.mPhone.mCi.getCLIR(obtainMessage(2, this));
        } else {
            this.mMtkSSReqDecisionMaker.getCLIR(obtainMessage(2, this));
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCOLP() {
        Rlog.d(LOG_TAG, "processCode: is COLP");
        if (!isInterrogate()) {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        } else if (supportMdAutoSetupIms()) {
            this.mPhone.mMtkCi.getCOLP(obtainMessage(9, this));
        } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
            if (this.mPhone.getCsFallbackStatus() == 1) {
                this.mPhone.setCsFallbackStatus(0);
            }
            this.mPhone.mMtkCi.getCOLP(obtainMessage(9, this));
        } else {
            this.mMtkSSReqDecisionMaker.getCOLP(obtainMessage(9, this));
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCOLR() {
        Rlog.d(LOG_TAG, "processCode: is COLR");
        if (!isInterrogate()) {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        } else if (supportMdAutoSetupIms()) {
            this.mPhone.mMtkCi.getCOLR(obtainMessage(8, this));
        } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
            if (this.mPhone.getCsFallbackStatus() == 1) {
                this.mPhone.setCsFallbackStatus(0);
            }
            this.mPhone.mMtkCi.getCOLR(obtainMessage(8, this));
        } else {
            this.mMtkSSReqDecisionMaker.getCOLR(obtainMessage(8, this));
        }
    }

    /* JADX WARN: Type inference failed for: r2v4, types: [boolean, int] */
    /* access modifiers changed from: package-private */
    public void handleCallForward() {
        int cfAction;
        Rlog.d(LOG_TAG, "processCode: is CF");
        String dialingNumber = this.mSia;
        int serviceClass = siToServiceClass(this.mSib);
        int reason = scToCallForwardReason(this.mSc);
        int time = siToTime(this.mSic);
        int serviceClass2 = this.mPhone.specifyServiceClassForOperator(serviceClass);
        if (!isInterrogate()) {
            if (isActivate()) {
                if (isEmptyOrNull(dialingNumber)) {
                    this.mIsCallFwdReg = false;
                    cfAction = 1;
                } else {
                    this.mIsCallFwdReg = true;
                    cfAction = 3;
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
            ? isVoiceUnconditionalForwarding = isVoiceUnconditionalForwarding(reason, serviceClass2);
            int isEnableDesired = (cfAction == 1 || cfAction == 3) ? 1 : 0;
            Rlog.d(LOG_TAG, "is CF setCallForward");
            if (supportMdAutoSetupIms()) {
                this.mPhone.mCi.setCallForward(cfAction, reason, serviceClass2, dialingNumber, time, obtainMessage(6, isVoiceUnconditionalForwarding, isEnableDesired, this));
            } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
                if (this.mPhone.getCsFallbackStatus() == 1) {
                    this.mPhone.setCsFallbackStatus(0);
                }
                this.mPhone.mCi.setCallForward(cfAction, reason, serviceClass2, dialingNumber, time, obtainMessage(6, isVoiceUnconditionalForwarding == true ? 1 : 0, isEnableDesired, this));
            } else {
                this.mMtkSSReqDecisionMaker.setCallForward(cfAction, reason, serviceClass2, dialingNumber, time, obtainMessage(6, isVoiceUnconditionalForwarding, isEnableDesired, this));
            }
        } else if (supportMdAutoSetupIms()) {
            this.mPhone.mCi.queryCallForwardStatus(reason, serviceClass2, dialingNumber, obtainMessage(3, this));
        } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
            if (this.mPhone.getCsFallbackStatus() == 1) {
                this.mPhone.setCsFallbackStatus(0);
            }
            this.mPhone.mCi.queryCallForwardStatus(reason, serviceClass2, dialingNumber, obtainMessage(3, this));
        } else {
            this.mMtkSSReqDecisionMaker.queryCallForwardStatus(reason, serviceClass2, dialingNumber, obtainMessage(3, this));
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCallBarring() {
        Rlog.d(LOG_TAG, "processCode: is CB");
        String password = this.mSia;
        int serviceClass = siToServiceClass(this.mSib);
        String facility = scToBarringFacility(this.mSc);
        if (isInterrogate()) {
            if (supportMdAutoSetupIms()) {
                this.mPhone.mCi.queryFacilityLock(facility, password, serviceClass, obtainMessage(5, this));
            } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
                if (this.mPhone.getCsFallbackStatus() == 1) {
                    this.mPhone.setCsFallbackStatus(0);
                }
                if (this.mPhone.getUiccCardApplication() == null) {
                    Rlog.d(LOG_TAG, "handleCallBarring: getUiccCardApplication() == null");
                    Message msg = obtainMessage(5, this);
                    AsyncResult.forMessage(msg, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                    msg.sendToTarget();
                    return;
                }
                this.mPhone.mCi.queryFacilityLockForApp(facility, password, serviceClass, this.mPhone.getUiccCardApplication().getAid(), obtainMessage(5, this));
            } else {
                this.mMtkSSReqDecisionMaker.queryFacilityLock(facility, password, serviceClass, obtainMessage(5, this));
            }
        } else if (!isActivate() && !isDeactivate()) {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        } else if (supportMdAutoSetupIms()) {
            this.mPhone.mCi.setFacilityLock(facility, isActivate(), password, serviceClass, obtainMessage(1, this));
        } else if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
            if (this.mPhone.getCsFallbackStatus() == 1) {
                this.mPhone.setCsFallbackStatus(0);
            }
            if (this.mPhone.getUiccCardApplication() == null) {
                Rlog.d(LOG_TAG, "handleCallBarring: getUiccCardApplication() == null");
                Message msg2 = obtainMessage(1, this);
                AsyncResult.forMessage(msg2, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                msg2.sendToTarget();
                return;
            }
            this.mPhone.mCi.setFacilityLockForApp(facility, isActivate(), password, serviceClass, this.mPhone.getUiccCardApplication().getAid(), obtainMessage(1, this));
        } else {
            this.mMtkSSReqDecisionMaker.setFacilityLock(facility, isActivate(), password, serviceClass, obtainMessage(1, this));
        }
    }

    /* access modifiers changed from: package-private */
    public void handleChangeBarringPassward() {
        String facility;
        Rlog.d(LOG_TAG, "processCode: is Change PWD");
        String oldPwd = this.mSib;
        String newPwd = this.mSic;
        if (isActivate() || isRegister()) {
            this.mAction = "**";
            if (this.mSia == null) {
                facility = "AB";
            } else {
                facility = scToBarringFacility(this.mSia);
            }
            if (oldPwd == null || newPwd == null || this.mPwd == null) {
                handlePasswordError(17040538);
            } else if (this.mPwd.length() != newPwd.length() || oldPwd.length() != 4 || this.mPwd.length() != 4) {
                handlePasswordError(17040538);
            } else if (this.mPhone.isDuringImsCall()) {
                Message msg = obtainMessage(1, this);
                AsyncResult.forMessage(msg, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                msg.sendToTarget();
            } else {
                this.mPhone.mMtkCi.changeBarringPassword(facility, oldPwd, newPwd, this.mPwd, obtainMessage(1, this));
            }
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCW() {
        Rlog.d(LOG_TAG, "processCode: is CW");
        int serviceClass = siToServiceClass(this.mSia);
        Rlog.d(LOG_TAG, "CW serviceClass = " + serviceClass);
        if (!supportMdAutoSetupIms()) {
            int tbcwMode = this.mPhone.getTbcwMode();
            if (isActivate() || isDeactivate()) {
                if (tbcwMode == 1 && !this.mPhone.isOpNwCW()) {
                    String tbcwStatus = TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
                    Rlog.d(LOG_TAG, "setTerminal-based CallWaiting(): tbcwStatus = " + tbcwStatus + ", enable = " + isActivate());
                    if (!tbcwStatus.equals("disabled_tbcw")) {
                        this.mPhone.setTerminalBasedCallWaiting(isActivate(), obtainMessage(1, this));
                        return;
                    }
                    Rlog.d(LOG_TAG, "setCallWaiting() by NW.");
                    this.mPhone.mCi.setCallWaiting(isActivate(), serviceClass, obtainMessage(1, this));
                } else if (tbcwMode == 2 || tbcwMode == 3) {
                    if (this.mPhone.getCsFallbackStatus() == 1) {
                        this.mPhone.setCsFallbackStatus(0);
                    }
                    this.mPhone.mCi.setCallWaiting(isActivate(), serviceClass, obtainMessage(1, isActivate() ? 1 : 0, -1, this));
                } else {
                    Rlog.d(LOG_TAG, "processCode setCallWaiting");
                    if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
                        this.mPhone.mCi.setCallWaiting(isActivate(), serviceClass, obtainMessage(1, this));
                    } else {
                        this.mMtkSSReqDecisionMaker.setCallWaiting(isActivate(), serviceClass, obtainMessage(1, this));
                    }
                }
            } else if (!isInterrogate()) {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            } else if (tbcwMode == 1 && !this.mPhone.isOpNwCW()) {
                this.mPhone.getTerminalBasedCallWaiting(obtainMessage(5, this));
            } else if (tbcwMode == 2 || tbcwMode == 3) {
                if (this.mPhone.getCsFallbackStatus() == 1) {
                    this.mPhone.setCsFallbackStatus(0);
                }
                this.mPhone.mCi.queryCallWaiting(serviceClass, obtainMessage(5, this));
            } else {
                Rlog.d(LOG_TAG, "processCode getCallWaiting");
                if (this.mPhone.getCsFallbackStatus() != 0 || !this.mPhone.isGsmUtSupport()) {
                    this.mPhone.mCi.queryCallWaiting(serviceClass, obtainMessage(5, this));
                } else {
                    this.mMtkSSReqDecisionMaker.queryCallWaiting(serviceClass, obtainMessage(5, this));
                }
            }
        } else if (isActivate() || isDeactivate()) {
            this.mPhone.mCi.setCallWaiting(isActivate(), serviceClass, obtainMessage(1, this));
        } else if (isInterrogate()) {
            this.mPhone.mCi.queryCallWaiting(serviceClass, obtainMessage(5, this));
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        boolean isSettingUnconditionalVideo = false;
        if (i == 1) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (!supportMdAutoSetupIms() && this.mSc.equals("43") && this.mPhone.getTbcwMode() == 3 && ar.exception == null) {
                if (msg.arg1 == 1) {
                    isSettingUnconditionalVideo = true;
                }
                this.mPhone.setTerminalBasedCallWaiting(isSettingUnconditionalVideo, null);
            }
            onSetComplete(msg, ar);
        } else if (i == 5) {
            AsyncResult ar2 = (AsyncResult) msg.obj;
            if (!supportMdAutoSetupIms() && this.mSc.equals("43") && this.mPhone.getTbcwMode() == 3) {
                Rlog.d(LOG_TAG, "TBCW_WITH_CS");
                if (ar2.exception == null) {
                    int[] cwArray = (int[]) ar2.result;
                    try {
                        Rlog.d(LOG_TAG, "EVENT_GET_CALL_WAITING_FOR_CS_TB cwArray[0]:cwArray[1] = " + cwArray[0] + ":" + cwArray[1]);
                        if (cwArray[0] == 1 && (cwArray[1] & 1) == 1) {
                            isSettingUnconditionalVideo = true;
                        }
                        this.mPhone.setTerminalBasedCallWaiting(isSettingUnconditionalVideo, null);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Rlog.e(LOG_TAG, "EVENT_GET_CALL_WAITING_FOR_CS_TB: improper result: err =" + e.getMessage());
                    }
                }
            }
            onQueryComplete(ar2);
        } else if (i == 6) {
            AsyncResult ar3 = (AsyncResult) msg.obj;
            if (ar3.exception == null && msg.arg1 == 1) {
                boolean cffEnabled = msg.arg2 == 1;
                if (this.mIccRecords != null) {
                    try {
                        int serviceClass = siToServiceClass(this.mSib);
                        int reason = scToCallForwardReason(this.mSc);
                        if ((reason == 0 || reason == 4) && (serviceClass == 0 || (serviceClass & 512) != 0)) {
                            isSettingUnconditionalVideo = true;
                        }
                        if (this.mPhone != null && isSettingUnconditionalVideo) {
                            this.mPhone.setVideoCallForwardingFlag(cffEnabled);
                        }
                    } catch (RuntimeException e2) {
                        Rlog.d(LOG_TAG, "EVENT_SET_CFF_COMPLETE Exception " + e2);
                    }
                    this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, this.mDialingNumber);
                    this.mPhone.saveTimeSlot(null);
                }
            }
            onSetComplete(msg, ar3);
        } else if (i == 8) {
            onGetColrComplete((AsyncResult) msg.obj);
        } else if (i != 9) {
            MtkGsmMmiCode.super.handleMessage(msg);
        } else {
            onGetColpComplete((AsyncResult) msg.obj);
        }
    }

    /* access modifiers changed from: protected */
    public void onSetComplete(Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            if ((ar.exception instanceof CommandException) && ar.exception.getCommandError() == CommandException.Error.REQUEST_NOT_SUPPORTED && (this.mSc.equals("31") || this.mSc.equals("30"))) {
                sb.append(this.mContext.getText(17040445));
                this.mMessage = sb;
                this.mPhone.onMMIDone(this);
                return;
            }
        }
        MtkGsmMmiCode.super.onSetComplete(msg, ar);
    }

    /* access modifiers changed from: protected */
    public CharSequence serviceClassToCFString(int serviceClass) {
        Rlog.d(LOG_TAG, "serviceClassToCFString, serviceClass = " + serviceClass);
        if (serviceClass == 256 || serviceClass == 512) {
            return this.mContext.getText(134545476);
        }
        return MtkGsmMmiCode.super.serviceClassToCFString(serviceClass);
    }

    /* access modifiers changed from: protected */
    public CharSequence makeCFQueryResultMessage(CallForwardInfo info, int serviceClassMask) {
        CharSequence template;
        String[] sources = {"{0}", "{1}", "{2}"};
        CharSequence[] destinations = new CharSequence[3];
        boolean cffEnabled = false;
        boolean needTimeTemplate = info.reason == 2 && info.timeSeconds >= 0;
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
            if (info.status == 1) {
                cffEnabled = true;
            }
            if (this.mIccRecords != null) {
                this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, info.number);
            }
        }
        return TextUtils.replace(template, sources, destinations);
    }

    /* access modifiers changed from: protected */
    public void onQueryCfComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            MtkGsmMmiCode.super.onQueryCfComplete(ar);
            return;
        }
        CallForwardInfo[] infos = (CallForwardInfo[]) ar.result;
        if (infos.length == 0) {
            sb.append(this.mContext.getText(17040998));
            if (this.mIccRecords != null) {
                this.mPhone.setVoiceCallForwardingFlag(1, false, null);
            }
        } else {
            SpannableStringBuilder tb = new SpannableStringBuilder();
            for (int serviceClassMask = 1; serviceClassMask <= 512; serviceClassMask <<= 1) {
                if (serviceClassMask != 256) {
                    int s = infos.length;
                    for (int i = 0; i < s; i++) {
                        if ((infos[i].serviceClass & serviceClassMask) != 0) {
                            tb.append(makeCFQueryResultMessage(infos[i], serviceClassMask));
                            tb.append((CharSequence) "\n");
                            if (infos[i].reason == 0 && (infos[i].serviceClass & serviceClassMask) == 1) {
                                if (this.mIccRecords != null) {
                                    this.mPhone.setVoiceCallForwardingFlag(1, infos[i].status == 1, null);
                                }
                                if (infos[i].reason == 0 && (infos[i].serviceClass & serviceClassMask) == 512) {
                                    this.mPhone.setVideoCallForwardingFlag(infos[i].status == 1);
                                }
                            }
                        }
                    }
                }
            }
            sb.append((CharSequence) tb);
        }
        this.mState = MmiCode.State.COMPLETE;
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onQueryCfComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    /* access modifiers changed from: protected */
    public void onQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception == null) {
            int[] ints = (int[]) ar.result;
            if (!(ints.length == 0 || ints[0] == 0 || !this.mSc.equals(SC_CNAP))) {
                Rlog.d(LOG_TAG, "onQueryComplete_CNAP");
                sb.append(createQueryCnapResultMessage(ints[1]));
                this.mMessage = sb;
                this.mState = MmiCode.State.COMPLETE;
                this.mPhone.onMMIDone(this);
                return;
            }
        }
        MtkGsmMmiCode.super.onQueryComplete(ar);
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
    public CharSequence createQueryCallBarringResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17041000));
        for (int classMask = 1; classMask <= 512; classMask <<= 1) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    private CharSequence createQueryCnapResultMessage(int serviceProvisioned) {
        Rlog.d(LOG_TAG, "createQueryCnapResultMessage");
        StringBuilder sb = new StringBuilder(this.mContext.getText(17041000));
        sb.append("\n");
        if (serviceProvisioned == 0) {
            sb.append(this.mContext.getText(17041002));
        } else if (serviceProvisioned != 1) {
            sb.append(this.mContext.getText(17041002));
        } else {
            sb.append(this.mContext.getText(134545420));
        }
        Rlog.d(LOG_TAG, "CNAP_sb = " + ((Object) sb));
        return sb;
    }

    public static MtkGsmMmiCode newNetworkInitiatedUssdError(String ussdMessage, boolean isUssdRequest, MtkGsmCdmaPhone phone, UiccCardApplication app) {
        MtkGsmMmiCode ret = new MtkGsmMmiCode(phone, app);
        if (ussdMessage == null || ussdMessage.length() <= 0) {
            ret.mMessage = ret.mContext.getText(17040445);
        } else {
            ret.mMessage = ussdMessage;
        }
        ret.mIsUssdRequest = isUssdRequest;
        ret.mState = MmiCode.State.FAILED;
        return ret;
    }

    private void onGetColpComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            sb.append(getErrorMessage(ar));
        } else {
            int i = ((int[]) ar.result)[1];
            if (i == 0) {
                sb.append(this.mContext.getText(17041002));
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 1) {
                sb.append(this.mContext.getText(134545420));
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 2) {
                sb.append(this.mContext.getText(134545421));
                this.mState = MmiCode.State.COMPLETE;
            }
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private void onGetColrComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            sb.append(getErrorMessage(ar));
        } else {
            int i = ((int[]) ar.result)[0];
            if (i == 0) {
                sb.append(this.mContext.getText(17041002));
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 1) {
                sb.append(this.mContext.getText(134545420));
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 2) {
                sb.append(this.mContext.getText(17040445));
                this.mState = MmiCode.State.FAILED;
            }
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    public static boolean isUtMmiCode(String dialString, MtkGsmCdmaPhone dialPhone, UiccCardApplication iccApp) {
        MtkGsmMmiCode mmi = newFromDialString(dialString, dialPhone, iccApp);
        if (mmi == null || mmi.isTemporaryModeCLIR() || mmi.isShortCode() || mmi.mDialingNumber != null || mmi.mSc == null || (!mmi.mSc.equals("30") && !mmi.mSc.equals("31") && !mmi.mSc.equals(SC_COLP) && !mmi.mSc.equals(SC_COLR) && !isServiceCodeCallForwarding(mmi.mSc) && !isServiceCodeCallBarring(mmi.mSc) && !mmi.mSc.equals("43"))) {
            return false;
        }
        return true;
    }

    private boolean supportMdAutoSetupIms() {
        if (SystemProperties.get("ro.vendor.md_auto_setup_ims").equals("1")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public CharSequence getScString() {
        if (this.mSc == null || !this.mSc.equals(SC_CNAP)) {
            return MtkGsmMmiCode.super.getScString();
        }
        return CNAPMmi;
    }

    /* access modifiers changed from: protected */
    public CharSequence getErrorMessage(AsyncResult ar) {
        if (ar.exception instanceof CommandException) {
            CommandException.Error err = ar.exception.getCommandError();
            if (err == CommandException.Error.OEM_ERROR_25) {
                if (!supportMdAutoSetupIms()) {
                    return this.mContext.getText(17040445);
                }
                Rlog.i(LOG_TAG, "getErrorMessage, OEM_ERROR_25 409_CONFLICT");
                MtkSuppServHelper ssHelper = MtkSuppServManager.getSuppServHelper(this.mPhone.getPhoneId());
                if (ssHelper != null) {
                    String errorMsg = ssHelper.getXCAPErrorMessageFromSysProp(CommandException.Error.OEM_ERROR_25);
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        return this.mContext.getText(17040445);
                    }
                    return errorMsg;
                }
            } else if (err == CommandException.Error.OEM_ERROR_5) {
                Rlog.i(LOG_TAG, "getErrorMessage, OEM_ERROR_5 CALL_BARRED");
                return this.mContext.getText(134545416);
            } else if (err == CommandException.Error.FDN_CHECK_FAILURE) {
                Rlog.i(LOG_TAG, "getErrorMessage, FDN_CHECK_FAILURE");
                return this.mContext.getText(134545415);
            }
        }
        return MtkGsmMmiCode.super.getErrorMessage(ar);
    }
}
