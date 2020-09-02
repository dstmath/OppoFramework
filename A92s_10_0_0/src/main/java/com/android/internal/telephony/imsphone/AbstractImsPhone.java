package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.content.Intent;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.util.OemTelephonyUtils;

public abstract class AbstractImsPhone extends Phone {
    private static final String LOG_TAG = "AbstractImsPhone";
    protected IOppoImsPhone mImsPhoneEx;
    private final String mName;

    protected AbstractImsPhone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode) {
        super(name, notifier, context, ci, unitTestMode);
        this.mName = name;
    }

    /* access modifiers changed from: protected */
    public boolean handle1xInCallMmiCode(String dialString, ImsPhoneCall call) {
        return this.mImsPhoneEx.handle1xInCallMmiCode(dialString, call);
    }

    /* access modifiers changed from: protected */
    public int setCallForwardingTimer(Phone phone, int commandInterfaceCFReason, int timerSeconds) {
        return this.mImsPhoneEx.setCallForwardingTimer(phone, commandInterfaceCFReason, timerSeconds);
    }

    /* access modifiers changed from: protected */
    public int setCallForwardingServiceClass(int commandInterfaceServiceClass) {
        return this.mImsPhoneEx.setCallForwardingServiceClass(commandInterfaceServiceClass);
    }

    @Override // com.android.internal.telephony.Phone
    public void dispose() {
        this.mImsPhoneEx.dispose();
    }

    public void setRusConfig(boolean reset) {
        Rlog.e(LOG_TAG, "imsPhone setRusConfig for phoneId " + this.mPhoneId + ", reset " + reset);
    }

    public void handleCarrerConfigChanged(int subId, Intent intent) {
        this.mImsPhoneEx.handleCarrerConfigChanged(subId, intent);
    }

    public void handleInCallMmiForSpecificOp(ImsPhone mPhone, ImsPhoneMmiCode imsPhoneMmiCode, MmiCode.State mState, CharSequence mMessage, String mccMnc, String mDialingNumber, boolean isUssiEnabled, Context mContext) throws CallStateException {
        this.mImsPhoneEx.handleInCallMmiForSpecificOp(mPhone, imsPhoneMmiCode, mState, mMessage, mccMnc, mDialingNumber, isUssiEnabled, mContext);
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public void setVideoCallForwardingFlag(boolean enable) {
        getDefaultAbstractPhone().setVideoCallForwardingFlag(enable);
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public boolean getVideoCallForwardingFlag() {
        return getDefaultAbstractPhone().getVideoCallForwardingFlag();
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public String handlePreCheckCFDialingNumber(String dialingNumber) {
        return getDefaultAbstractPhone().handlePreCheckCFDialingNumber(dialingNumber);
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public int specifyServiceClassForOperator(int serviceClass) {
        return getDefaultAbstractPhone().specifyServiceClassForOperator(serviceClass);
    }

    private AbstractPhone getDefaultAbstractPhone() {
        return (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, getDefaultPhone());
    }
}
