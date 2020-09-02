package com.android.internal.telephony.gsm;

import com.android.internal.telephony.AbstractSMSDispatcher;
import com.android.internal.telephony.IOppoSMSDispatcher;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.util.OemTelephonyUtils;

public abstract class AbstractGsmSMSDispatcher extends SMSDispatcher {
    private String LOG_TAG;
    public IOppoSMSDispatcher mReferenceGsm;

    protected AbstractGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone, smsDispatchersController);
        this.mReferenceGsm = null;
        this.LOG_TAG = "AbstractGsmSMSDispatcher";
        this.mReferenceGsm = (IOppoSMSDispatcher) OppoTelephonyFactory.getInstance().getFeature(IOppoSMSDispatcher.DEFAULT, this);
        ((AbstractSMSDispatcher) OemTelephonyUtils.typeCasting(AbstractSMSDispatcher.class, this)).mReference = this.mReferenceGsm;
        if (phone != null) {
            this.LOG_TAG = "AbstractGsmSMSDispatcher[" + phone.getPhoneId() + "]";
        }
        String str = this.LOG_TAG;
        OppoRlog.Rlog.d(str, "mReferenceGsm=" + this.mReferenceGsm);
    }
}
