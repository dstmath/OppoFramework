package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.RadioCapability;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.qualcomm.qti.internal.telephony.primarycard.SubsidyLockSettingsObserver;

public class QtiGsmCdmaPhone extends GsmCdmaPhone {
    private static final int EVENT_OEM_HOOK_SERVICE_READY = 46;
    private static final String LOG_TAG = "QtiGsmCdmaPhone";
    private static final int PROP_EVENT_START = 45;
    private static int READY = 1;
    private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    CdmaSubscriptionSourceManager mCdmaNVSSM;
    int mCdmaNVSubscriptionSource;
    private boolean mIsPhoneReadyPending;
    private boolean mIsPhoneReadySent;
    private BaseRilInterface mQtiRilInterface;

    public QtiGsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        this(context, ci, notifier, false, phoneId, precisePhoneType, telephonyComponentFactory);
    }

    public QtiGsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, boolean unitTestMode, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        super(context, ci, notifier, unitTestMode, phoneId, precisePhoneType, telephonyComponentFactory);
        this.mIsPhoneReadySent = false;
        this.mIsPhoneReadyPending = false;
        this.mCdmaNVSubscriptionSource = -1;
        Rlog.d(LOG_TAG, "Constructor");
        this.mQtiRilInterface = getQtiRilInterface();
        this.mQtiRilInterface.registerForServiceReadyEvent(this, EVENT_OEM_HOOK_SERVICE_READY, null);
        this.mCdmaNVSSM = CdmaSubscriptionSourceManager.getInstance(context, this.mCi, null, -1, null);
    }

    private void handleCdmaNVSubscriptionSource(int newSubscriptionSource) {
        Rlog.d(LOG_TAG, " mCdmaNVSubscriptionSource:  " + this.mCdmaNVSubscriptionSource + " newSubscriptionSource:  " + newSubscriptionSource);
        if (newSubscriptionSource != this.mCdmaNVSubscriptionSource) {
            this.mCdmaNVSubscriptionSource = newSubscriptionSource;
            handleNVChange(newSubscriptionSource);
        }
    }

    private void handleNVChange(int newSubscriptionSource) {
        boolean isNVReady = newSubscriptionSource == 1;
        Rlog.e(LOG_TAG, " handleNVChanged: isNVReady: " + isNVReady);
        QtiSubscriptionInfoUpdater.getInstance().updateNVRecord(isNVReady, this.mPhoneId);
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        QtiRadioCapabilityController radioCapController = QtiRadioCapabilityController.getInstance();
        if (radioCapController != null) {
            radioCapController.setPreferredNetworkType(getPhoneId(), networkType, response);
        } else {
            Rlog.e(LOG_TAG, " Error: Received null QtiRadioCapabilityController instante ");
        }
    }

    private void updatePhoneReady(int phoneId) {
        if (!this.mIsPhoneReadySent && SystemProperties.getInt("persist.vendor.radio.poweron_opt", 0) == 1) {
            if (this.mQtiRilInterface.isServiceReady()) {
                logd("Sending Phone Ready to RIL.");
                this.mQtiRilInterface.sendPhoneStatus(READY, phoneId);
                this.mIsPhoneReadySent = true;
                this.mIsPhoneReadyPending = false;
            } else {
                this.mIsPhoneReadyPending = true;
            }
        }
    }

    protected void phoneObjectUpdater(int newVoiceTech) {
        super.phoneObjectUpdater(newVoiceTech);
        updatePhoneReady(this.mPhoneId);
    }

    public void radioCapabilityUpdated(RadioCapability rc) {
        this.mRadioCapability.set(rc);
        QtiRadioCapabilityController radioCapController = QtiRadioCapabilityController.getInstance();
        if (radioCapController != null) {
            radioCapController.radioCapabilityUpdated(getPhoneId(), rc);
        }
    }

    public boolean getCallForwardingIndicator() {
        if (isCurrentSubValid()) {
            return super.getCallForwardingIndicator();
        }
        return false;
    }

    private boolean isCurrentSubValid() {
        int provisionStatus;
        SubscriptionManager subscriptionManager = SubscriptionManager.from(this.mContext);
        try {
            provisionStatus = QtiUiccCardProvisioner.getInstance().getCurrentUiccCardProvisioningStatus(this.mPhoneId);
        } catch (NullPointerException e) {
            provisionStatus = 0;
        }
        Rlog.d(LOG_TAG, "ProvisionStatus: " + provisionStatus + " phone id:" + this.mPhoneId);
        if (!subscriptionManager.isActiveSubId(getSubId())) {
            return false;
        }
        if (provisionStatus == 1) {
            return true;
        }
        return false;
    }

    public boolean setLocalCallHold(boolean enable) {
        if (this.mQtiRilInterface.isServiceReady()) {
            return this.mQtiRilInterface.setLocalCallHold(this.mPhoneId, enable);
        }
        Rlog.e(LOG_TAG, "mQtiRilInterface is not ready yet");
        return false;
    }

    public void fetchIMEI() {
        Rlog.d(LOG_TAG, "fetching device id");
        this.mCi.getDeviceIdentity(obtainMessage(21));
    }

    public void dispose() {
        this.mQtiRilInterface.unRegisterForServiceReadyEvent(this);
        this.mQtiRilInterface = null;
        super.dispose();
    }

    public void handleMessage(Message msg) {
        Rlog.d(LOG_TAG, "handleMessage: Event: " + msg.what);
        switch (msg.what) {
            case 1:
                this.mIsPhoneReadySent = false;
                updatePhoneReady(this.mPhoneId);
                super.handleMessage(msg);
                return;
            case 3:
                if (isPhoneTypeGsm()) {
                    Rlog.d(LOG_TAG, "notify call forward indication, phone id:" + this.mPhoneId);
                    notifyCallForwardingIndicator();
                }
                super.handleMessage(msg);
                return;
            case 5:
            case 27:
                if (mNumPhones == 1) {
                    handleCdmaNVSubscriptionSource(this.mCdmaNVSSM.getCdmaSubscriptionSource());
                }
                super.handleMessage(msg);
                return;
            case 23:
                Rlog.d(LOG_TAG, "Event EVENT_NV_READY Received");
                prepareEri();
                Rlog.d(LOG_TAG, "notifyMessageWaitingChanged");
                this.mNotifier.notifyMessageWaitingChanged(this);
                updateVoiceMail();
                return;
            case 41:
                this.mIsPhoneReadySent = false;
                super.handleMessage(msg);
                return;
            case EVENT_OEM_HOOK_SERVICE_READY /*46*/:
                AsyncResult ar = msg.obj;
                if (ar == null || ar.result == null) {
                    Rlog.e(LOG_TAG, "Error: empty result, EVENT_OEM_HOOK_SERVICE_READY");
                    return;
                } else if (((Boolean) ar.result).booleanValue()) {
                    if (this.mIsPhoneReadyPending) {
                        updatePhoneReady(this.mPhoneId);
                    }
                    Rlog.d(LOG_TAG, "EVENT_OEM_HOOK_SERVICE_READY received");
                    return;
                } else {
                    return;
                }
            default:
                super.handleMessage(msg);
                return;
        }
    }

    public void startDtmf(char c) {
        Object obj = 1;
        if (!(PhoneNumberUtils.is12Key(c) || c == 'D')) {
            obj = null;
        }
        if (obj == null) {
            Rlog.e(LOG_TAG, "startDtmf called with invalid character '" + c + "'");
            return;
        }
        if (isPhoneTypeCdma() && c == 'D') {
            c = '#';
        }
        this.mCi.startDtmf(c, null);
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
        Character c = Character.valueOf(dtmfString.charAt(0));
        if (dtmfString.length() == 1 && c.charValue() == 'D') {
            dtmfString = c.toString();
        }
        super.sendBurstDtmf(dtmfString, on, off, onComplete);
    }

    public ServiceState getServiceState() {
        if (this.mSST == null || this.mSST.mSS.getState() != 0) {
            boolean isImsEnabled = this.mImsPhone != null ? (this.mImsPhone.isVolteEnabled() || this.mImsPhone.isVideoEnabled()) ? true : this.mImsPhone.isWifiCallingEnabled() : false;
            if (isImsEnabled) {
                return ServiceState.mergeServiceStates(this.mSST == null ? new ServiceState() : this.mSST.mSS, this.mImsPhone.getServiceState());
            }
        }
        if (this.mSST != null) {
            return this.mSST.mSS;
        }
        return new ServiceState();
    }

    public void sendSubscriptionSettings(boolean restoreNetworkSelection) {
        ExtTelephonyServiceImpl serviceImpl = ExtTelephonyServiceImpl.getInstance();
        if (SubsidyLockSettingsObserver.isSubsidyLockFeatureEnabled() && (SubsidyLockSettingsObserver.isSubsidyUnlocked(this.mContext) ^ 1) != 0) {
            if (((serviceImpl != null ? 1 : 0) & serviceImpl.isPrimaryCarrierSlotId(getPhoneId())) != 0) {
                setPreferredNetworkType(PhoneFactory.calculatePreferredNetworkType(this.mContext, getSubId()), null);
                logd(" settings network selection mode to AUTO ");
                setNetworkSelectionModeAutomatic(null);
                return;
            }
        }
        super.sendSubscriptionSettings(restoreNetworkSelection);
    }

    private BaseRilInterface getQtiRilInterface() {
        if (!getUnitTestMode()) {
            return QtiRilInterface.getInstance(this.mContext);
        }
        logd("getQtiRilInterface, unitTestMode = true");
        return SimulatedQtiRilInterface.getInstance(this.mContext);
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, "[" + this.mPhoneId + " ] " + msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, "[" + this.mPhoneId + " ] " + msg);
    }
}
