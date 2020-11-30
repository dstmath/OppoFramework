package com.android.internal.telephony;

import android.telephony.Rlog;
import com.android.internal.telephony.util.OemTelephonyUtils;

public abstract class AbstractImsSmsDispatcher extends SMSDispatcher {
    private String LOG_TAG;
    public IOppoSMSDispatcher mReferenceIms;

    protected AbstractImsSmsDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone, smsDispatchersController);
        this.mReferenceIms = null;
        this.LOG_TAG = "AbstractImsSmsDispatcher";
        this.mReferenceIms = (IOppoSMSDispatcher) OppoTelephonyFactory.getInstance().getFeature(IOppoSMSDispatcher.DEFAULT, this);
        ((AbstractSMSDispatcher) OemTelephonyUtils.typeCasting(AbstractSMSDispatcher.class, this)).mReference = this.mReferenceIms;
        if (phone != null) {
            this.LOG_TAG = "AbstractImsSmsDispatcher[" + phone.getPhoneId() + "]";
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "mReferenceIms=" + this.mReferenceIms);
    }
}
