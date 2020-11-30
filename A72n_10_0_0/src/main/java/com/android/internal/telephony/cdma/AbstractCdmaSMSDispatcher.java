package com.android.internal.telephony.cdma;

import com.android.internal.telephony.AbstractSMSDispatcher;
import com.android.internal.telephony.IOppoSMSDispatcher;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.util.OemTelephonyUtils;

public abstract class AbstractCdmaSMSDispatcher extends SMSDispatcher {
    private String LOG_TAG;
    public IOppoSMSDispatcher mReferenceCdma;

    protected AbstractCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone, smsDispatchersController);
        this.mReferenceCdma = null;
        this.LOG_TAG = "AbstractCdmaSMSDispatcher";
        this.mReferenceCdma = (IOppoSMSDispatcher) OppoTelephonyFactory.getInstance().getFeature(IOppoSMSDispatcher.DEFAULT, this);
        ((AbstractSMSDispatcher) OemTelephonyUtils.typeCasting(AbstractSMSDispatcher.class, this)).mReference = this.mReferenceCdma;
        if (phone != null) {
            this.LOG_TAG = "AbstractCdmaSMSDispatcher[" + phone.getPhoneId() + "]";
        }
        String str = this.LOG_TAG;
        OppoRlog.Rlog.d(str, "mReferenceCdma=" + this.mReferenceCdma);
    }
}
