package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.gsm.NetworkInfoWithAcT;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractGsmCdmaPhone extends Phone {
    private static final String LOG_TAG = "AbstractGsmCdmaPhone";
    protected IOppoGsmCdmaPhone mGsmCdmaPhoneEx;
    private final String mName;
    protected boolean mPendingSetConfig = false;
    private boolean mPhoneTypeSwitchPending = false;

    protected AbstractGsmCdmaPhone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode, int phoneId, TelephonyComponentFactory telephonyComponentFactory) {
        super(name, notifier, context, ci, unitTestMode, phoneId, telephonyComponentFactory);
        this.mName = name;
    }

    public boolean handleCalloutControl(boolean isEmergencyNumber) {
        return this.mGsmCdmaPhoneEx.handleCalloutControl(isEmergencyNumber);
    }

    public boolean handleImsForUtCheck(boolean useImsForUt, String dialString, AtomicReference<UiccCardApplication> mUiccApplication) {
        return this.mGsmCdmaPhoneEx.handleImsForUtCheck(useImsForUt, dialString, mUiccApplication);
    }

    public boolean isOemInCall() {
        return this.mGsmCdmaPhoneEx.isOemInCall();
    }

    /* access modifiers changed from: package-private */
    public boolean isManualSelectNetworksAllowed(ServiceStateTracker mSST) {
        return this.mGsmCdmaPhoneEx.isManualSelectNetworksAllowed(mSST);
    }

    public String oemGetFullIccSerialNumber(IccRecords mIccRecords, UiccController mUiccController) {
        return this.mGsmCdmaPhoneEx.oemGetFullIccSerialNumber(mIccRecords, mUiccController);
    }

    public void notifyFailure(Message onComplete, CommandException.Error error) {
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(error));
            onComplete.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public void getPreferedOperatorList(Message onComplete) {
        Rlog.d(LOG_TAG, "getPreferedOperatorList enter =====================");
        IccRecords records = getIccRecords();
        if (records != null) {
            ((AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, records)).getPreferedOperatorList(onComplete);
        } else {
            Rlog.d(LOG_TAG, "getPreferedOperatorList mIccRecords is null =====================");
        }
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public void setPOLEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
        Rlog.d(LOG_TAG, "setPOLEntry enter =====================");
        IccRecords records = getIccRecords();
        if (records != null) {
            ((AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, records)).setPOLEntry(networkWithAct, onComplete);
        } else {
            Rlog.d(LOG_TAG, "setPOLEntry mIccRecords is null =====================");
        }
    }

    public void setPhoneTypeSwitchPending() {
        this.mPhoneTypeSwitchPending = true;
    }

    public void clearPhoneTypeSwitchPending() {
        this.mPhoneTypeSwitchPending = false;
    }

    public boolean isPhoneTypeSwitchPending() {
        return this.mPhoneTypeSwitchPending;
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public String colorGetIccCardType() {
        return this.mGsmCdmaPhoneEx.colorGetIccCardType();
    }

    public void resetImsSS(Phone imsPhone) {
        this.mGsmCdmaPhoneEx.resetImsSS(imsPhone);
    }

    public boolean isUssdEnabledInVolteCall() {
        return this.mGsmCdmaPhoneEx.isUssdEnabledInVolteCall();
    }

    public boolean isInImsCall() {
        return this.mGsmCdmaPhoneEx.isInImsCall();
    }

    public String getDefaultVMByImsi(String imsi) {
        return this.mGsmCdmaPhoneEx.getDefaultVMByImsi(imsi);
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public void updateLteWifiCoexist(boolean enabled) {
        this.mGsmCdmaPhoneEx.updateLteWifiCoexist(enabled);
    }

    public void getLteWifiCoexistStatus() {
        this.mGsmCdmaPhoneEx.getLteWifiCoexistStatus();
    }
}
