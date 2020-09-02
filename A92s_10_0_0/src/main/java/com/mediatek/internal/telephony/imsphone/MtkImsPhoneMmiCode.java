package com.mediatek.internal.telephony.imsphone;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import com.android.ims.ImsException;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.IOppoCallManager;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.android.internal.util.ArrayUtils;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkSuppServHelper;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.worldphone.WorldMode;
import java.util.regex.Matcher;

public final class MtkImsPhoneMmiCode extends ImsPhoneMmiCode {
    static final String LOG_TAG = "MtkImsPhoneMmiCode";
    private static final String SC_CFNotRegister = "68";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER);

    public static MtkImsPhoneMmiCode newFromDialString(String dialString, ImsPhone phone) {
        return newFromDialString(dialString, phone, null);
    }

    public static MtkImsPhoneMmiCode newFromDialString(String dialString, ImsPhone phone, ResultReceiver wrappedCallback) {
        Rlog.d(LOG_TAG, "newFromDialString, dialstring = " + MtkSuppServHelper.encryptString(dialString));
        if ((dialString.startsWith("*") || dialString.startsWith("#") || dialString.endsWith("#")) || dialString.length() <= 2) {
            Matcher m = sPatternSuppService.matcher(dialString);
            if (m.matches()) {
                MtkImsPhoneMmiCode ret = new MtkImsPhoneMmiCode(phone);
                ret.mPoundString = makeEmptyNull(m.group(1));
                ret.mAction = makeEmptyNull(m.group(2));
                ret.mSc = makeEmptyNull(m.group(3));
                ret.mSia = makeEmptyNull(m.group(5));
                ret.mSib = makeEmptyNull(m.group(7));
                ret.mSic = makeEmptyNull(m.group(9));
                ret.mPwd = makeEmptyNull(m.group(11));
                ret.mDialingNumber = makeEmptyNull(m.group(12));
                ret.mCallbackReceiver = wrappedCallback;
                if (ret.mDialingNumber != null && ret.mDialingNumber.endsWith("#") && dialString.endsWith("#")) {
                    MtkImsPhoneMmiCode ret2 = new MtkImsPhoneMmiCode(phone);
                    ret2.mPoundString = dialString;
                    return ret2;
                } else if (ret.isFacToDial()) {
                    return null;
                } else {
                    return ret;
                }
            } else if (dialString.endsWith("#")) {
                MtkImsPhoneMmiCode ret3 = new MtkImsPhoneMmiCode(phone);
                ret3.mPoundString = dialString;
                return ret3;
            } else if (isTwoDigitShortCode(phone.getContext(), dialString) || !isShortCode(dialString, phone)) {
                return null;
            } else {
                MtkImsPhoneMmiCode ret4 = new MtkImsPhoneMmiCode(phone);
                ret4.mDialingNumber = dialString;
                return ret4;
            }
        } else {
            Rlog.d(LOG_TAG, "Not belong to MMI format.");
            return null;
        }
    }

    public static MtkImsPhoneMmiCode newNetworkInitiatedUssd(String ussdMessage, boolean isUssdRequest, MtkImsPhone phone) {
        MtkImsPhoneMmiCode ret = new MtkImsPhoneMmiCode(phone);
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

    public static MtkImsPhoneMmiCode newNetworkInitiatedUssdError(String ussdMessage, MtkImsPhone phone) {
        MtkImsPhoneMmiCode ret = new MtkImsPhoneMmiCode(phone);
        if (ussdMessage == null || ussdMessage.length() <= 0) {
            ret.mMessage = ret.mContext.getText(17040445);
        } else {
            ret.mMessage = ussdMessage;
        }
        ret.mIsUssdRequest = false;
        ret.mState = MmiCode.State.FAILED;
        return ret;
    }

    public static MtkImsPhoneMmiCode newFromUssdUserInput(String ussdMessge, MtkImsPhone phone) {
        MtkImsPhoneMmiCode ret = new MtkImsPhoneMmiCode(phone);
        ret.mMessage = ussdMessge;
        ret.mState = MmiCode.State.PENDING;
        ret.mIsPendingUSSD = true;
        return ret;
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

    public MtkImsPhoneMmiCode(ImsPhone phone) {
        super(phone);
    }

    public boolean isSupportedOverImsPhone() {
        if (isShortCode()) {
            return true;
        }
        if (this.mDialingNumber != null) {
            return false;
        }
        if (this.mSc != null && this.mSc.equals("300")) {
            return false;
        }
        if (isServiceCodeCallForwarding(this.mSc) || isServiceCodeCallBarring(this.mSc) || ((this.mSc != null && this.mSc.equals("43")) || ((this.mSc != null && this.mSc.equals("31")) || ((this.mSc != null && this.mSc.equals("30")) || ((this.mSc != null && this.mSc.equals("77")) || ((this.mSc != null && this.mSc.equals("76")) || ((this.mSc != null && this.mSc.equals("156")) || (this.mSc != null && this.mSc.equals("157"))))))))) {
            if (supportMdAutoSetupIms()) {
                try {
                    int serviceClass = siToServiceClass(this.mSib);
                    if ((serviceClass & 1) == 0 && (serviceClass & 512) == 0) {
                        if (serviceClass != 0) {
                            return false;
                        }
                    }
                    Rlog.d(LOG_TAG, "isSupportedOverImsPhone(), return true!");
                    return true;
                } catch (RuntimeException exc) {
                    Rlog.d(LOG_TAG, "Invalid service class " + exc);
                }
            } else {
                if (this.mPhone.isVolteEnabled() || (this.mPhone.isWifiCallingEnabled() && this.mPhone.mDefaultPhone.isWFCUtSupport())) {
                    try {
                        int serviceClass2 = siToServiceClass(this.mSib);
                        if ((serviceClass2 & 1) == 0 && (serviceClass2 & 512) == 0) {
                            if (serviceClass2 == 0) {
                            }
                        }
                        Rlog.d(LOG_TAG, "isSupportedOverImsPhone(), return true!");
                        return true;
                    } catch (RuntimeException exc2) {
                        Rlog.d(LOG_TAG, "exc.toString() = " + exc2.toString());
                    }
                }
                return false;
            }
        } else if (isPinPukCommand() || ((this.mSc != null && (this.mSc.equals("03") || this.mSc.equals("30") || this.mSc.equals("31"))) || this.mPoundString == null)) {
            return false;
        } else {
            return true;
        }
    }

    public void processCode() throws CallStateException {
        MtkImsPhoneMmiCode mtkImsPhoneMmiCode;
        String str;
        try {
            if (!supportMdAutoSetupIms()) {
                if (this.mPhone.mDefaultPhone.getCsFallbackStatus() != 0) {
                    Rlog.d(LOG_TAG, "processCode(): getCsFallbackStatus(): CS Fallback!");
                    this.mPhone.removeMmi(this);
                    throw new CallStateException("cs_fallback");
                }
            }
            boolean isUssiEnabled = OppoTelephonyFactory.getInstance().getFeature(IOppoCallManager.DEFAULT, new Object[0]).isUssiEnabled(this.mPhone);
            String mccMnc = TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "gsm.sim.operator.numeric", "");
            if (isShortCode()) {
                Rlog.d(LOG_TAG, "processCode: isShortCode");
                this.mPhone.handleInCallMmiForSpecificOp(this.mPhone, this, this.mState, this.mMessage, mccMnc, this.mDialingNumber, isUssiEnabled, this.mContext);
            } else if (isServiceCodeCallForwarding(this.mSc)) {
                handleCallForward();
            } else if (isServiceCodeCallBarring(this.mSc)) {
                handleCallBarring();
            } else if (this.mSc != null && this.mSc.equals("31")) {
                handleCLIR();
            } else if (this.mSc != null && this.mSc.equals("30")) {
                handleCLIP();
            } else if (this.mSc != null && this.mSc.equals("76")) {
                handleCOLP();
            } else if (this.mSc != null && this.mSc.equals("77")) {
                handleCOLR();
            } else if (this.mSc != null && this.mSc.equals("156")) {
                handleCallBarringSpecificMT();
            } else if (this.mSc != null && this.mSc.equals("157")) {
                handleCallBarringACR();
            } else if (this.mSc != null && this.mSc.equals("43")) {
                handleCW();
            } else if (this.mPoundString != null) {
                ImsPhone imsPhone = this.mPhone;
                ImsPhone imsPhone2 = this.mPhone;
                MmiCode.State state = this.mState;
                CharSequence charSequence = this.mMessage;
                String str2 = this.mPoundString;
                Context context = this.mContext;
                str = LOG_TAG;
                mtkImsPhoneMmiCode = this;
                try {
                    imsPhone.handleInCallMmiForSpecificOp(imsPhone2, this, state, charSequence, mccMnc, str2, isUssiEnabled, context);
                } catch (RuntimeException e) {
                    exc = e;
                    exc.printStackTrace();
                    Rlog.d(str, "procesCode: mState = FAILED");
                    mtkImsPhoneMmiCode.mState = MmiCode.State.FAILED;
                    mtkImsPhoneMmiCode.mMessage = mtkImsPhoneMmiCode.mContext.getText(17040445);
                    mtkImsPhoneMmiCode.mPhone.onMMIDone(mtkImsPhoneMmiCode);
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        } catch (RuntimeException e2) {
            exc = e2;
            str = LOG_TAG;
            mtkImsPhoneMmiCode = this;
            exc.printStackTrace();
            Rlog.d(str, "procesCode: mState = FAILED");
            mtkImsPhoneMmiCode.mState = MmiCode.State.FAILED;
            mtkImsPhoneMmiCode.mMessage = mtkImsPhoneMmiCode.mContext.getText(17040445);
            mtkImsPhoneMmiCode.mPhone.onMMIDone(mtkImsPhoneMmiCode);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCallForward() {
        int cfAction;
        Rlog.d(LOG_TAG, "processCode: is CF");
        String dialingNumber = this.mSia;
        int reason = scToCallForwardReason(this.mSc);
        int serviceClass = siToServiceClass(this.mSib);
        int time = siToTime(this.mSic);
        int serviceClass2 = this.mPhone.specifyServiceClassForOperator(serviceClass);
        int isEnableDesired = 1;
        if (isInterrogate()) {
            if (serviceClass2 != 0 && (this.mPhone.mDefaultPhone instanceof MtkGsmCdmaPhone)) {
                this.mPhone.mDefaultPhone.setServiceClass(serviceClass2);
            }
            this.mPhone.getCallForwardingOption(reason, obtainMessage(1, this));
            return;
        }
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
        int isSettingUnconditional = ((reason == 0 || reason == 4) && ((serviceClass2 & 1) != 0 || serviceClass2 == 0)) ? 1 : 0;
        if (!(cfAction == 1 || cfAction == 3)) {
            isEnableDesired = 0;
        }
        Rlog.d(LOG_TAG, "is CF setCallForward");
        if (this.mPhone.mDefaultPhone.isOpReregisterForCF()) {
            Rlog.i(LOG_TAG, "Set ims dereg to ON.");
            SystemProperties.set(MtkGsmCdmaPhone.IMS_DEREG_PROP, "1");
        }
        this.mPhone.setCallForwardingOption(cfAction, reason, dialingNumber, serviceClass2, time, obtainMessage(4, isSettingUnconditional, isEnableDesired, this));
    }

    /* access modifiers changed from: package-private */
    public void handleCallBarring() {
        Rlog.d(LOG_TAG, "processCode: is CB");
        String password = this.mSia;
        String facility = scToBarringFacility(this.mSc);
        int serviceClass = siToServiceClass(this.mSib);
        if (isInterrogate()) {
            this.mPhone.getCallBarring(facility, password, obtainMessage(7, this), serviceClass);
        } else if (isActivate() || isDeactivate()) {
            this.mPhone.setCallBarring(facility, isActivate(), password, obtainMessage(0, this), serviceClass);
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCW() {
        Rlog.d(LOG_TAG, "processCode: is CW");
        int serviceClass = siToServiceClass(this.mSia);
        if (!supportMdAutoSetupIms()) {
            int tbcwMode = this.mPhone.mDefaultPhone.getTbcwMode();
            if (isActivate() || isDeactivate()) {
                if (this.mPhone.mDefaultPhone.isOpNwCW()) {
                    Rlog.d(LOG_TAG, "setCallWaiting() by Ut interface.");
                    this.mPhone.setCallWaiting(isActivate(), serviceClass, obtainMessage(0, this));
                } else if (tbcwMode == 3) {
                    this.mPhone.mDefaultPhone.mCi.setCallWaiting(isActivate(), serviceClass, obtainMessage(0, isActivate() ? 1 : 0, -1, this));
                } else {
                    String tbcwStatus = TelephonyManager.getTelephonyProperty(this.mPhone.mDefaultPhone.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
                    Rlog.d(LOG_TAG, "setTerminal-based CallWaiting(): tbcwStatus = " + tbcwStatus + ", enable = " + isActivate());
                    if (!tbcwStatus.equals("disabled_tbcw")) {
                        this.mPhone.mDefaultPhone.setTerminalBasedCallWaiting(isActivate(), obtainMessage(0, this));
                        return;
                    }
                    Rlog.d(LOG_TAG, "setCallWaiting() by Ut interface.");
                    this.mPhone.setCallWaiting(isActivate(), serviceClass, obtainMessage(0, this));
                }
            } else if (isInterrogate()) {
                int tbcwMode2 = this.mPhone.mDefaultPhone.getTbcwMode();
                if (this.mPhone.mDefaultPhone.isOpNwCW()) {
                    Rlog.d(LOG_TAG, "getCallWaiting() by Ut interface.");
                    this.mPhone.getCallWaiting(obtainMessage(3, this));
                } else if (tbcwMode2 == 3) {
                    this.mPhone.mDefaultPhone.mCi.queryCallWaiting(serviceClass, obtainMessage(3, this));
                } else {
                    String tbcwStatus2 = TelephonyManager.getTelephonyProperty(this.mPhone.mDefaultPhone.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
                    Rlog.d(LOG_TAG, "SC_WAIT isInterrogate() tbcwStatus = " + tbcwStatus2);
                    if ("enabled_tbcw_on".equals(tbcwStatus2)) {
                        Message msg = obtainMessage(3, null);
                        AsyncResult.forMessage(msg, new int[]{1, 1}, (Throwable) null);
                        sendMessage(msg);
                    } else if ("enabled_tbcw_off".equals(tbcwStatus2)) {
                        int[] cwInfos = new int[2];
                        cwInfos[0] = 0;
                        Message msg2 = obtainMessage(3, null);
                        AsyncResult.forMessage(msg2, cwInfos, (Throwable) null);
                        sendMessage(msg2);
                    } else {
                        Rlog.d(LOG_TAG, "getCallWaiting() by Ut interface.");
                        this.mPhone.getCallWaiting(obtainMessage(3, this));
                    }
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        } else if (isActivate() || isDeactivate()) {
            this.mPhone.setCallWaiting(isActivate(), serviceClass, obtainMessage(0, this));
        } else if (isInterrogate()) {
            this.mPhone.getCallWaiting(obtainMessage(3, this));
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCLIP() {
        Rlog.d(LOG_TAG, "processCode: is CLIP");
        if (checkIfOPSupportCallerID()) {
            if (isInterrogate()) {
                try {
                    this.mPhone.mCT.getUtInterface().queryCLIP(obtainMessage(7, this));
                } catch (ImsException e) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for queryCLIP.");
                }
            } else if (isActivate() || isDeactivate()) {
                try {
                    this.mPhone.mCT.getUtInterface().updateCLIP(isActivate(), obtainMessage(0, this));
                } catch (ImsException e2) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCLIP.");
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCLIR() {
        Rlog.d(LOG_TAG, "processCode: is CLIR");
        if (checkIfOPSupportCallerID()) {
            if (isActivate()) {
                if (supportMdAutoSetupIms() || !this.mPhone.mDefaultPhone.isOpTbClir()) {
                    try {
                        this.mPhone.mCT.getUtInterface().updateCLIR(1, obtainMessage(0, 1, 0, this));
                    } catch (ImsException e) {
                        Rlog.d(LOG_TAG, "Could not get UT handle for updateCLIR.");
                    }
                } else {
                    this.mPhone.mDefaultPhone.mCi.setCLIR(1, obtainMessage(0, 1, 0, this));
                }
            } else if (isDeactivate()) {
                if (supportMdAutoSetupIms() || !this.mPhone.mDefaultPhone.isOpTbClir()) {
                    try {
                        this.mPhone.mCT.getUtInterface().updateCLIR(2, obtainMessage(0, 2, 0, this));
                    } catch (ImsException e2) {
                        Rlog.d(LOG_TAG, "Could not get UT handle for updateCLIR.");
                    }
                } else {
                    this.mPhone.mDefaultPhone.mCi.setCLIR(2, obtainMessage(0, 2, 0, this));
                }
            } else if (!isInterrogate()) {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            } else if (supportMdAutoSetupIms() || !this.mPhone.mDefaultPhone.isOpTbClir()) {
                try {
                    this.mPhone.mCT.getUtInterface().queryCLIR(obtainMessage(6, this));
                } catch (ImsException e3) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for queryCLIR.");
                }
            } else {
                Message msg = obtainMessage(6, this);
                if (msg != null) {
                    int[] result = this.mPhone.mDefaultPhone.getSavedClirSetting();
                    Bundle info = new Bundle();
                    info.putIntArray(MtkImsPhone.UT_BUNDLE_KEY_CLIR, result);
                    AsyncResult.forMessage(msg, info, (Throwable) null);
                    msg.sendToTarget();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCOLP() {
        Rlog.d(LOG_TAG, "processCode: is COLP");
        if (checkIfOPSupportCallerID()) {
            if (isInterrogate()) {
                try {
                    this.mPhone.mCT.getUtInterface().queryCOLP(obtainMessage(7, this));
                } catch (ImsException e) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for queryCOLP.");
                }
            } else if (isActivate() || isDeactivate()) {
                try {
                    this.mPhone.mCT.getUtInterface().updateCOLP(isActivate(), obtainMessage(0, this));
                } catch (ImsException e2) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLP.");
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCOLR() {
        Rlog.d(LOG_TAG, "processCode: is COLR");
        if (checkIfOPSupportCallerID()) {
            if (isActivate()) {
                try {
                    this.mPhone.mCT.getUtInterface().updateCOLR(1, obtainMessage(0, this));
                } catch (ImsException e) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLR.");
                }
            } else if (isDeactivate()) {
                try {
                    this.mPhone.mCT.getUtInterface().updateCOLR(0, obtainMessage(0, this));
                } catch (ImsException e2) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLR.");
                }
            } else if (isInterrogate()) {
                try {
                    this.mPhone.mCT.getUtInterface().queryCOLR(obtainMessage(7, this));
                } catch (ImsException e3) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for queryCOLR.");
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCallBarringSpecificMT() {
        Rlog.d(LOG_TAG, "processCode: is CB (specifc MT)");
        try {
            if (isInterrogate()) {
                this.mPhone.mCT.getUtInterface().queryCallBarring(10, obtainMessage(10, this));
            } else {
                processIcbMmiCodeForUpdate();
            }
        } catch (ImsException e) {
            Rlog.d(LOG_TAG, "Could not get UT handle for ICB.");
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCallBarringACR() {
        Rlog.d(LOG_TAG, "processCode: is CB (ACR)");
        int callAction = 0;
        String password = this.mSia;
        int serviceClass = siToServiceClass(this.mSib);
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
            this.mPhone.mCT.getUtInterface().updateCallBarring(password, 6, callAction, obtainMessage(0, this), (String[]) null, serviceClass);
        } catch (ImsException e) {
            Rlog.d(LOG_TAG, "Could not get UT handle for ICBa.");
        }
    }

    public void handleMessage(Message msg) {
        if (!triggerMmiCodeCsfb(msg)) {
            int i = msg.what;
            boolean enable = false;
            if (i == 0) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (!supportMdAutoSetupIms() && this.mSc.equals("43") && this.mPhone.mDefaultPhone.getTbcwMode() == 3 && ar.exception == null) {
                    if (msg.arg1 == 1) {
                        enable = true;
                    }
                    this.mPhone.mDefaultPhone.setTerminalBasedCallWaiting(enable, null);
                }
                onSetComplete(msg, ar);
            } else if (i != 4) {
                MtkImsPhoneMmiCode.super.handleMessage(msg);
            } else {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null && msg.arg1 == 1) {
                    boolean cffEnabled = msg.arg2 == 1;
                    if (this.mPhone.mDefaultPhone.queryCFUAgainAfterSet()) {
                        if (ar2.result != null) {
                            CallForwardInfo[] cfInfos = (CallForwardInfo[]) ar2.result;
                            if (cfInfos == null || cfInfos.length == 0) {
                                Rlog.i(LOG_TAG, "cfInfo is null or length is 0.");
                            } else {
                                int i2 = 0;
                                while (true) {
                                    if (i2 >= cfInfos.length) {
                                        break;
                                    } else if ((cfInfos[i2].serviceClass & 1) == 0) {
                                        if ((cfInfos[i2].serviceClass & 512) != 0) {
                                            this.mPhone.setVideoCallForwardingFlag(cfInfos[i2].status == 1);
                                        }
                                        i2++;
                                    } else if (cfInfos[i2].status == 1) {
                                        Rlog.i(LOG_TAG, "Set CF_ENABLE, serviceClass: " + cfInfos[i2].serviceClass);
                                        cffEnabled = true;
                                    } else {
                                        Rlog.i(LOG_TAG, "Set CF_DISABLE, serviceClass: " + cfInfos[i2].serviceClass);
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
                        try {
                            Rlog.i(LOG_TAG, "EVENT_SET_CFF_COMPLETE: mSib:" + this.mSib);
                            if (!(this.mPhone == null || this.mPhone.mDefaultPhone == null || (siToServiceClass(this.mSib) != 0 && (siToServiceClass(this.mSib) & 512) == 0))) {
                                this.mPhone.setVideoCallForwardingFlag(cffEnabled);
                            }
                        } catch (RuntimeException exc) {
                            Rlog.d(LOG_TAG, "Invalid service class " + exc);
                        }
                        ((ImsPhone) this.mPhone).mDefaultPhone.setVoiceCallForwardingFlag(1, cffEnabled, this.mDialingNumber);
                        this.mPhone.saveTimeSlot(null);
                    }
                }
                onSetComplete(msg, ar2);
            }
        }
    }

    public static boolean isUtMmiCode(String dialString, ImsPhone dialPhone) {
        MtkImsPhoneMmiCode mmi = newFromDialString(dialString, dialPhone);
        if (mmi == null || mmi.isTemporaryModeCLIR() || mmi.isShortCode() || mmi.mDialingNumber != null || mmi.mSc == null || (!mmi.mSc.equals("30") && !mmi.mSc.equals("31") && !mmi.mSc.equals("76") && !mmi.mSc.equals("77") && !isServiceCodeCallForwarding(mmi.mSc) && !isServiceCodeCallBarring(mmi.mSc) && !mmi.mSc.equals("43") && !mmi.mSc.equals("156") && !mmi.mSc.equals("157"))) {
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
    public void onQueryCfComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            MtkImsPhoneMmiCode.super.onQueryCfComplete(ar);
            return;
        }
        CallForwardInfo[] infos = (CallForwardInfo[]) ar.result;
        if (infos == null || infos.length == 0) {
            sb.append(this.mContext.getText(17040998));
            if (this.mIccRecords != null) {
                this.mPhone.setVoiceCallForwardingFlag(1, false, (String) null);
                if (this.mPhone != null) {
                    this.mPhone.setVideoCallForwardingFlag(false);
                }
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
    public void onSuppSvcQueryComplete(AsyncResult ar) {
        if (!isServiceCodeCallBarring(this.mSc) || ar.exception != null || (ar.result instanceof Bundle)) {
            MtkImsPhoneMmiCode.super.onSuppSvcQueryComplete(ar);
            return;
        }
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        Rlog.d(LOG_TAG, "onSuppSvcQueryComplete: Received Call Barring Response.");
        int[] cbInfos = (int[]) ar.result;
        if (cbInfos[0] == 0) {
            sb.append(this.mContext.getText(17040998));
        } else {
            sb.append(createQueryCallBarringResultMessage(cbInfos[0]));
        }
        this.mState = MmiCode.State.COMPLETE;
        this.mMessage = sb;
        Rlog.d(LOG_TAG, "onSuppSvcQueryComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private CharSequence createQueryCallBarringResultMessage(int serviceClass) {
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
    public CharSequence serviceClassToCFString(int serviceClass) {
        Rlog.d(LOG_TAG, "serviceClassToCFString, serviceClass = " + serviceClass);
        if (serviceClass == 256 || serviceClass == 512) {
            return this.mContext.getText(134545476);
        }
        return MtkImsPhoneMmiCode.super.serviceClassToCFString(serviceClass);
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
    public CharSequence getMmiErrorMessage(AsyncResult ar) {
        if (ar.exception instanceof ImsException) {
            Rlog.d(LOG_TAG, "getMmiErrorMessage, ims error code = " + ar.exception.getCode());
            int code = ar.exception.getCode();
            if (code == 241) {
                return this.mContext.getText(17040447);
            }
            if (code != 61449) {
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
                        return this.mContext.getText(17040445);
                }
            } else {
                String errorMsg = ar.exception.getMessage();
                if (errorMsg == null || errorMsg.isEmpty()) {
                    return this.mContext.getText(17040445);
                }
                String errorMsg2 = removeLastErrorCode(errorMsg);
                Rlog.d(LOG_TAG, "Ims errorMessage = " + errorMsg2);
                return errorMsg2;
            }
        } else {
            if (ar.exception instanceof CommandException) {
                CommandException err = ar.exception;
                Rlog.d(LOG_TAG, "getMmiErrorMessage, error code = " + err.getCommandError());
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
                if (err.getCommandError() == CommandException.Error.OEM_ERROR_25) {
                    String errorMsg3 = err.getMessage();
                    if (errorMsg3 == null || errorMsg3.isEmpty()) {
                        return this.mContext.getText(17040445);
                    }
                    String errorMsg4 = removeLastErrorCode(errorMsg3);
                    Rlog.d(LOG_TAG, "errorMessage = " + errorMsg4);
                    return errorMsg4;
                }
            }
            return this.mContext.getText(17040445);
        }
    }

    /* access modifiers changed from: protected */
    public CharSequence getImsErrorMessage(AsyncResult ar) {
        ImsException error = ar.exception;
        CharSequence errorMessage = getMmiErrorMessage(ar);
        if (errorMessage != null) {
            return errorMessage;
        }
        if (error.getMessage() == null) {
            return getErrorMessage(ar);
        }
        String errorMsg = removeLastErrorCode(error.getMessage());
        Rlog.d(LOG_TAG, "getImsErrorMessage, errorMsg = " + errorMsg);
        return errorMsg;
    }

    /* access modifiers changed from: protected */
    public CharSequence getErrorMessage(AsyncResult ar) {
        if (ar.exception instanceof CommandException) {
            CommandException err = ar.exception;
            String errorMsg = err.getMessage();
            if (err.getCommandError() == CommandException.Error.OEM_ERROR_1 && errorMsg != null && !errorMsg.isEmpty()) {
                String errorMsg2 = removeLastErrorCode(errorMsg);
                Rlog.d(LOG_TAG, "getErrorMessage, errorMsg = " + errorMsg2);
                return errorMsg2;
            }
        }
        return MtkImsPhoneMmiCode.super.getErrorMessage(ar);
    }

    private String removeLastErrorCode(CharSequence str) {
        return new StringBuilder(new StringBuilder(str).reverse().toString().replaceFirst("\\)[0-9]{5}\\(", "")).reverse().toString();
    }

    protected static boolean isServiceCodeCallForwarding(String sc) {
        return sc != null && (sc.equals("21") || sc.equals("67") || sc.equals("61") || sc.equals("62") || sc.equals("002") || sc.equals("004") || sc.equals(SC_CFNotRegister));
    }

    protected static int scToCallForwardReason(String sc) {
        if (sc == null) {
            throw new RuntimeException("invalid call forward sc");
        } else if (sc.equals("002")) {
            return 4;
        } else {
            if (sc.equals("21")) {
                return 0;
            }
            if (sc.equals("67")) {
                return 1;
            }
            if (sc.equals("62")) {
                return 3;
            }
            if (sc.equals("61")) {
                return 2;
            }
            if (sc.equals("004")) {
                return 5;
            }
            if (sc.equals(SC_CFNotRegister)) {
                return 6;
            }
            throw new RuntimeException("invalid call forward sc");
        }
    }

    private boolean triggerMmiCodeCsfb(Message msg) {
        AsyncResult ar;
        if (supportMdAutoSetupIms() || this.mPhone.mDefaultPhone.isNotSupportUtToCS() || (ar = (AsyncResult) msg.obj) == null || ar.exception == null) {
            return false;
        }
        if (ar.exception instanceof CommandException) {
            CommandException cmdException = ar.exception;
            if (cmdException.getCommandError() == CommandException.Error.OPERATION_NOT_ALLOWED) {
                Rlog.d(LOG_TAG, "handleMessage(): CommandException.Error.UT_XCAP_403_FORBIDDEN");
                this.mPhone.handleMmiCodeCsfb(61446, this);
                return true;
            } else if (cmdException.getCommandError() != CommandException.Error.OEM_ERROR_3) {
                return false;
            } else {
                Rlog.d(LOG_TAG, "handleMessage(): CommandException.Error.UT_UNKNOWN_HOST");
                this.mPhone.handleMmiCodeCsfb(61447, this);
                return true;
            }
        } else if (!(ar.exception instanceof ImsException)) {
            return false;
        } else {
            ImsException imsException = ar.exception;
            if (imsException.getCode() == 61446) {
                Rlog.d(LOG_TAG, "handleMessage(): ImsReasonInfo.CODE_UT_XCAP_403_FORBIDDEN");
                this.mPhone.handleMmiCodeCsfb(61446, this);
                return true;
            } else if (imsException.getCode() != 61447) {
                return false;
            } else {
                Rlog.d(LOG_TAG, "handleMessage(): ImsReasonInfo.CODE_UT_UNKNOWN_HOST");
                this.mPhone.handleMmiCodeCsfb(61447, this);
                return true;
            }
        }
    }

    private boolean checkIfOPSupportCallerID() {
        if (supportMdAutoSetupIms() || !this.mPhone.mDefaultPhone.isOpNotSupportCallIdentity()) {
            return true;
        }
        handleGeneralError();
        return false;
    }

    private void handleGeneralError() {
        this.mState = MmiCode.State.FAILED;
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        sb.append(this.mContext.getText(17040445));
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private boolean isFacToDial() {
        PersistableBundle b = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        if (b != null) {
            String[] dialFacList = b.getStringArray("feature_access_codes_string_array");
            if (!ArrayUtils.isEmpty(dialFacList)) {
                for (String fac : dialFacList) {
                    if (fac.equals(this.mSc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MtkImsPhoneMmiCode {");
        sb.append("State=" + getState());
        if (this.mAction != null) {
            sb.append(" action=" + this.mAction);
        }
        if (this.mSc != null) {
            sb.append(" sc=" + this.mSc);
        }
        if (this.mSia != null) {
            sb.append(" sia=" + MtkSuppServHelper.encryptString(this.mSia));
        }
        if (this.mSib != null) {
            sb.append(" sib=" + MtkSuppServHelper.encryptString(this.mSib));
        }
        if (this.mSic != null) {
            sb.append(" sic=" + MtkSuppServHelper.encryptString(this.mSic));
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
