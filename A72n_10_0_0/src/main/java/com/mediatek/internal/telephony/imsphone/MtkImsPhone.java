package com.mediatek.internal.telephony.imsphone;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.telephony.CallQuality;
import android.telephony.CarrierConfigManager;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsInfo;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.ims.MtkImsCallForwardInfo;
import com.mediatek.ims.MtkImsUt;
import com.mediatek.internal.telephony.MtkCallForwardInfo;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkRIL;
import com.mediatek.internal.telephony.MtkSuppSrvRequest;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import mediatek.telephony.MtkServiceState;

public class MtkImsPhone extends ImsPhone {
    private static final String CFU_TIME_SLOT = "persist.vendor.radio.cfu.timeslot.";
    public static final int EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE = 109;
    public static final int EVENT_SET_CALL_FORWARD_TIME_SLOT_DONE = 110;
    private static final String LOG_TAG = "MtkImsPhone";
    public static final String UT_BUNDLE_KEY_CLIR = "queryClir";
    private String mDialString;
    private boolean mIsBlindAssuredEctSupported;
    private boolean mIsConsultativeEctSupported;
    private boolean mIsDeviceSwitchSupported;
    private boolean mIsDigitsSupported;
    private boolean mIsWfcModeHomeForDomRoaming;
    protected BroadcastReceiver mReceiver;

    public enum FeatureType {
        VOLTE_ENHANCED_CONFERENCE,
        VIDEO_RESTRICTION,
        BLINDASSURED_ECT,
        CONSULTATIVE_ECT
    }

    public /* bridge */ /* synthetic */ void activateCellBroadcastSms(int x0, Message x1) {
        MtkImsPhone.super.activateCellBroadcastSms(x0, x1);
    }

    public /* bridge */ /* synthetic */ boolean disableDataConnectivity() {
        return MtkImsPhone.super.disableDataConnectivity();
    }

    public /* bridge */ /* synthetic */ void disableLocationUpdates() {
        MtkImsPhone.super.disableLocationUpdates();
    }

    public /* bridge */ /* synthetic */ boolean enableDataConnectivity() {
        return MtkImsPhone.super.enableDataConnectivity();
    }

    public /* bridge */ /* synthetic */ void enableLocationUpdates() {
        MtkImsPhone.super.enableLocationUpdates();
    }

    public /* bridge */ /* synthetic */ void getAvailableNetworks(Message x0) {
        MtkImsPhone.super.getAvailableNetworks(x0);
    }

    public /* bridge */ /* synthetic */ boolean getCallForwardingIndicator() {
        return MtkImsPhone.super.getCallForwardingIndicator();
    }

    public /* bridge */ /* synthetic */ void getCellBroadcastSmsConfig(Message x0) {
        MtkImsPhone.super.getCellBroadcastSmsConfig(x0);
    }

    public /* bridge */ /* synthetic */ List getCurrentDataConnectionList() {
        return MtkImsPhone.super.getCurrentDataConnectionList();
    }

    public /* bridge */ /* synthetic */ PhoneInternalInterface.DataActivityState getDataActivityState() {
        return MtkImsPhone.super.getDataActivityState();
    }

    public /* bridge */ /* synthetic */ PhoneConstants.DataState getDataConnectionState() {
        return MtkImsPhone.super.getDataConnectionState();
    }

    public /* bridge */ /* synthetic */ boolean getDataRoamingEnabled() {
        return MtkImsPhone.super.getDataRoamingEnabled();
    }

    public /* bridge */ /* synthetic */ String getDeviceId() {
        return MtkImsPhone.super.getDeviceId();
    }

    public /* bridge */ /* synthetic */ String getDeviceSvn() {
        return MtkImsPhone.super.getDeviceSvn();
    }

    public /* bridge */ /* synthetic */ String getEsn() {
        return MtkImsPhone.super.getEsn();
    }

    public /* bridge */ /* synthetic */ String getGroupIdLevel1() {
        return MtkImsPhone.super.getGroupIdLevel1();
    }

    public /* bridge */ /* synthetic */ String getGroupIdLevel2() {
        return MtkImsPhone.super.getGroupIdLevel2();
    }

    public /* bridge */ /* synthetic */ IccCard getIccCard() {
        return MtkImsPhone.super.getIccCard();
    }

    public /* bridge */ /* synthetic */ IccFileHandler getIccFileHandler() {
        return MtkImsPhone.super.getIccFileHandler();
    }

    public /* bridge */ /* synthetic */ IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return MtkImsPhone.super.getIccPhoneBookInterfaceManager();
    }

    public /* bridge */ /* synthetic */ boolean getIccRecordsLoaded() {
        return MtkImsPhone.super.getIccRecordsLoaded();
    }

    public /* bridge */ /* synthetic */ String getIccSerialNumber() {
        return MtkImsPhone.super.getIccSerialNumber();
    }

    public /* bridge */ /* synthetic */ String getImei() {
        return MtkImsPhone.super.getImei();
    }

    public /* bridge */ /* synthetic */ String getLine1AlphaTag() {
        return MtkImsPhone.super.getLine1AlphaTag();
    }

    public /* bridge */ /* synthetic */ String getLine1Number() {
        return MtkImsPhone.super.getLine1Number();
    }

    public /* bridge */ /* synthetic */ LinkProperties getLinkProperties(String x0) {
        return MtkImsPhone.super.getLinkProperties(x0);
    }

    public /* bridge */ /* synthetic */ String getMeid() {
        return MtkImsPhone.super.getMeid();
    }

    public /* bridge */ /* synthetic */ boolean getMessageWaitingIndicator() {
        return MtkImsPhone.super.getMessageWaitingIndicator();
    }

    public /* bridge */ /* synthetic */ int getPhoneType() {
        return MtkImsPhone.super.getPhoneType();
    }

    public /* bridge */ /* synthetic */ SignalStrength getSignalStrength() {
        return MtkImsPhone.super.getSignalStrength();
    }

    public /* bridge */ /* synthetic */ String getSubscriberId() {
        return MtkImsPhone.super.getSubscriberId();
    }

    public /* bridge */ /* synthetic */ String getVoiceMailAlphaTag() {
        return MtkImsPhone.super.getVoiceMailAlphaTag();
    }

    public /* bridge */ /* synthetic */ String getVoiceMailNumber() {
        return MtkImsPhone.super.getVoiceMailNumber();
    }

    public /* bridge */ /* synthetic */ boolean handlePinMmi(String x0) {
        return MtkImsPhone.super.handlePinMmi(x0);
    }

    public /* bridge */ /* synthetic */ boolean isDataAllowed(int x0) {
        return MtkImsPhone.super.isDataAllowed(x0);
    }

    public /* bridge */ /* synthetic */ boolean isUserDataEnabled() {
        return MtkImsPhone.super.isUserDataEnabled();
    }

    public /* bridge */ /* synthetic */ void migrateFrom(Phone x0) {
        MtkImsPhone.super.migrateFrom(x0);
    }

    public /* bridge */ /* synthetic */ boolean needsOtaServiceProvisioning() {
        return MtkImsPhone.super.needsOtaServiceProvisioning();
    }

    public /* bridge */ /* synthetic */ void notifyCallForwardingIndicator() {
        MtkImsPhone.super.notifyCallForwardingIndicator();
    }

    public /* bridge */ /* synthetic */ void notifyDisconnect(Connection x0) {
        MtkImsPhone.super.notifyDisconnect(x0);
    }

    public /* bridge */ /* synthetic */ void notifyImsReason(ImsReasonInfo x0) {
        MtkImsPhone.super.notifyImsReason(x0);
    }

    public /* bridge */ /* synthetic */ void notifyPhoneStateChanged() {
        MtkImsPhone.super.notifyPhoneStateChanged();
    }

    public /* bridge */ /* synthetic */ void notifyPreciseCallStateChanged() {
        MtkImsPhone.super.notifyPreciseCallStateChanged();
    }

    public /* bridge */ /* synthetic */ void notifySuppServiceFailed(PhoneInternalInterface.SuppService x0) {
        MtkImsPhone.super.notifySuppServiceFailed(x0);
    }

    public /* bridge */ /* synthetic */ void onCallQualityChanged(CallQuality x0, int x1) {
        MtkImsPhone.super.onCallQualityChanged(x0, x1);
    }

    public /* bridge */ /* synthetic */ void onTtyModeReceived(int x0) {
        MtkImsPhone.super.onTtyModeReceived(x0);
    }

    public /* bridge */ /* synthetic */ void registerForOnHoldTone(Handler x0, int x1, Object x2) {
        MtkImsPhone.super.registerForOnHoldTone(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void registerForRingbackTone(Handler x0, int x1, Object x2) {
        MtkImsPhone.super.registerForRingbackTone(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void registerForTtyModeReceived(Handler x0, int x1, Object x2) {
        MtkImsPhone.super.registerForTtyModeReceived(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void selectNetworkManually(OperatorInfo x0, boolean x1, Message x2) {
        MtkImsPhone.super.selectNetworkManually(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void setCellBroadcastSmsConfig(int[] x0, Message x1) {
        MtkImsPhone.super.setCellBroadcastSmsConfig(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setDataRoamingEnabled(boolean x0) {
        MtkImsPhone.super.setDataRoamingEnabled(x0);
    }

    public /* bridge */ /* synthetic */ boolean setLine1Number(String x0, String x1, Message x2) {
        return MtkImsPhone.super.setLine1Number(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void setNetworkSelectionModeAutomatic(Message x0) {
        MtkImsPhone.super.setNetworkSelectionModeAutomatic(x0);
    }

    public /* bridge */ /* synthetic */ void setVoiceMailNumber(String x0, String x1, Message x2) {
        MtkImsPhone.super.setVoiceMailNumber(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void startNetworkScan(NetworkScanRequest x0, Message x1) {
        MtkImsPhone.super.startNetworkScan(x0, x1);
    }

    @VisibleForTesting
    public /* bridge */ /* synthetic */ void startOnHoldTone(Connection x0) {
        MtkImsPhone.super.startOnHoldTone(x0);
    }

    public /* bridge */ /* synthetic */ void startRingbackTone() {
        MtkImsPhone.super.startRingbackTone();
    }

    public /* bridge */ /* synthetic */ void stopNetworkScan(Message x0) {
        MtkImsPhone.super.stopNetworkScan(x0);
    }

    public /* bridge */ /* synthetic */ void stopOnHoldTone(Connection x0) {
        MtkImsPhone.super.stopOnHoldTone(x0);
    }

    public /* bridge */ /* synthetic */ void stopRingbackTone() {
        MtkImsPhone.super.stopRingbackTone();
    }

    public /* bridge */ /* synthetic */ void unregisterForOnHoldTone(Handler x0) {
        MtkImsPhone.super.unregisterForOnHoldTone(x0);
    }

    public /* bridge */ /* synthetic */ void unregisterForRingbackTone(Handler x0) {
        MtkImsPhone.super.unregisterForRingbackTone(x0);
    }

    public /* bridge */ /* synthetic */ void unregisterForTtyModeReceived(Handler x0) {
        MtkImsPhone.super.unregisterForTtyModeReceived(x0);
    }

    public /* bridge */ /* synthetic */ void updateServiceLocation() {
        MtkImsPhone.super.updateServiceLocation();
    }

    public MtkImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone) {
        this(context, notifier, defaultPhone, false);
    }

    @VisibleForTesting
    public MtkImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone, boolean unitTestMode) {
        super(context, notifier, defaultPhone, unitTestMode);
        this.mIsDigitsSupported = MtkTelephonyManagerEx.getDefault().isDigitsSupported();
        this.mSS = new MtkServiceState();
        this.mReceiver = new BroadcastReceiver() {
            /* class com.mediatek.internal.telephony.imsphone.MtkImsPhone.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    int subId = intent.getIntExtra("subscription", -1);
                    if (subId == MtkImsPhone.this.getSubId()) {
                        MtkImsPhone mtkImsPhone = MtkImsPhone.this;
                        mtkImsPhone.logd("Receive carrierConfig changed: " + MtkImsPhone.this.mPhoneId);
                        MtkImsPhone.this.cacheCarrierConfiguration();
                    }
                    MtkImsPhone.this.handleCarrerConfigChanged(subId, intent);
                }
            }
        };
        logd("Start to create MtkImsPhone.");
        setPhoneName(LOG_TAG);
        cacheCarrierConfiguration();
        registerForListenCarrierConfigChanged();
    }

    public void dispose() {
        MtkImsPhone.super.dispose();
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
    }

    /* access modifiers changed from: protected */
    public Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs, ResultReceiver wrappedCallback) throws CallStateException {
        ImsPhone.ImsDialArgs.Builder imsDialArgsBuilder;
        boolean isUriNumber = PhoneNumberUtils.isUriNumber(dialString);
        String newDialString = dialString;
        if (!isUriNumber) {
            newDialString = PhoneNumberUtils.stripSeparators(dialString);
        }
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        if (!(dialArgs instanceof ImsPhone.ImsDialArgs)) {
            imsDialArgsBuilder = ImsPhone.ImsDialArgs.Builder.from(dialArgs);
        } else {
            imsDialArgsBuilder = ImsPhone.ImsDialArgs.Builder.from((ImsPhone.ImsDialArgs) dialArgs);
        }
        imsDialArgsBuilder.setClirMode(this.mCT.getClirMode());
        if (this.mDefaultPhone.getPhoneType() == 2) {
            return this.mCT.dial(dialString, imsDialArgsBuilder.build());
        }
        String networkPortion = dialString;
        if (!isUriNumber) {
            networkPortion = PhoneNumberUtils.extractNetworkPortionAlt(newDialString);
        }
        MtkImsPhoneMmiCode mmi = null;
        if (!PhoneNumberUtils.isUriNumber(dialString)) {
            mmi = MtkImsPhoneMmiCode.newFromDialString(networkPortion, this, wrappedCallback);
        } else {
            logd("dialInternal: url dial string, it must not be MMI");
        }
        boolean isEcc = MtkLocalPhoneNumberUtils.getIsEmergencyNumber();
        logd("dialInternal: dialing w/ mmi [" + mmi + "] isEcc: " + isEcc);
        this.mDialString = dialString;
        if (mmi == null || isEcc) {
            return this.mCT.dial(dialString, imsDialArgsBuilder.build());
        }
        if (mmi.isTemporaryModeCLIR()) {
            imsDialArgsBuilder.setClirMode(mmi.getCLIRMode());
            return this.mCT.dial(mmi.getDialingNumber(), imsDialArgsBuilder.build());
        } else if (mmi.isSupportedOverImsPhone()) {
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
            try {
                OpTelephonyCustomizationUtils.getOpFactory(getContext()).makeDigitsUssdManager().setUssdExtra(dialArgs.intentExtras);
                mmi.processCode();
            } catch (CallStateException cse) {
                if ("cs_fallback".equals(cse.getMessage())) {
                    logi("dialInternal: fallback to GSM required.");
                    this.mPendingMMIs.remove(mmi);
                    throw cse;
                }
            }
            return null;
        } else {
            logi("dialInternal: USSD not supported by IMS; fallback to CS.");
            throw new CallStateException("cs_fallback");
        }
    }

    public void explicitCallTransfer(String number, int type) {
        this.mCT.unattendedCallTransfer(number, type);
    }

    public void deviceSwitch(String number, String deviceId) {
        this.mCT.deviceSwitch(number, deviceId);
    }

    public void cancelDeviceSwitch() {
        this.mCT.cancelDeviceSwitch();
    }

    public void setImsRegistered(boolean value) {
        this.mImsRegistered = value;
        if (this.mImsRegistered) {
            ((NotificationManager) this.mContext.getSystemService("notification")).cancel("wifi_calling", 1);
        }
    }

    /* access modifiers changed from: protected */
    public void onIncomingUSSD(int ussdMode, String ussdMessage) {
        logd("onIncomingUSSD ussdMode=" + ussdMode);
        boolean isUssdError = false;
        boolean isUssdRequest = ussdMode == 1;
        if (!(ussdMode == 0 || ussdMode == 1)) {
            isUssdError = true;
        }
        ImsPhoneMmiCode found = null;
        int i = 0;
        int s = this.mPendingMMIs.size();
        while (true) {
            if (i >= s) {
                break;
            } else if (((ImsPhoneMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                found = (ImsPhoneMmiCode) this.mPendingMMIs.get(i);
                break;
            } else {
                i++;
            }
        }
        if (found != null) {
            if (isUssdError) {
                found.onUssdFinishedError();
            } else {
                found.onUssdFinished(ussdMessage, isUssdRequest);
            }
        } else if (!isUssdError && ussdMessage != null) {
            onNetworkInitiatedUssd(ImsPhoneMmiCode.newNetworkInitiatedUssd(ussdMessage, isUssdRequest, this));
        } else if (isUssdError) {
            onNetworkInitiatedUssd(MtkImsPhoneMmiCode.newNetworkInitiatedUssdError(ussdMessage, this));
        }
    }

    public Connection dial(List<String> numbers, int videoState) throws CallStateException {
        return this.mCT.dial(numbers, videoState);
    }

    public void hangupAll() throws CallStateException {
        logd("hangupAll");
        this.mCT.hangupAll();
    }

    public void removeMmi(ImsPhoneMmiCode mmi) {
        logd("removeMmi: " + mmi);
        dumpPendingMmi();
        this.mPendingMMIs.remove(mmi);
    }

    public void dumpPendingMmi() {
        int size = this.mPendingMMIs.size();
        if (size == 0) {
            logd("dumpPendingMmi: none");
            return;
        }
        for (int i = 0; i < size; i++) {
            logd("dumpPendingMmi: " + this.mPendingMMIs.get(i));
        }
    }

    public void handleMmiCodeCsfb(int reason, MtkImsPhoneMmiCode mmi) {
        logd("handleMmiCodeCsfb: reason = " + reason + ", mDialString = " + this.mDialString + ", mmi=" + mmi);
        removeMmi(mmi);
        if (reason == 61446) {
            this.mDefaultPhone.setCsFallbackStatus(2);
        } else if (reason == 61447) {
            this.mDefaultPhone.setCsFallbackStatus(1);
        }
        MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(15, null);
        ss.mParcel.writeString(this.mDialString);
        this.mDefaultPhone.sendMessage(this.mDefaultPhone.obtainMessage(2001, ss));
    }

    /* access modifiers changed from: protected */
    public boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public int getConditionFromCFReason(int reason) {
        switch (reason) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            default:
                return -1;
        }
    }

    /* access modifiers changed from: protected */
    public int getCFReasonFromCondition(int condition) {
        switch (condition) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            default:
                return 3;
        }
    }

    /* access modifiers changed from: private */
    public static class Cf {
        public final boolean mIsCfu;
        public final Message mOnComplete;
        public final int mServiceClass;
        public final String mSetCfNumber;

        public Cf(String cfNumber, boolean isCfu, Message onComplete, int serviceClass) {
            this.mSetCfNumber = cfNumber;
            this.mIsCfu = isCfu;
            this.mOnComplete = onComplete;
            this.mServiceClass = serviceClass;
        }
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        logd("getCallForwardingOption reason=" + commandInterfaceCFReason);
        if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            logd("requesting call forwarding query.");
            try {
                this.mCT.getUtInterface().queryCallForward(getConditionFromCFReason(commandInterfaceCFReason), (String) null, obtainMessage(13, onComplete));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
        String getNumber;
        logd("setCallForwardingOption action=" + commandInterfaceCFAction + ", reason=" + commandInterfaceCFReason + " serviceClass=" + serviceClass);
        if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            boolean z = true;
            if ((dialingNumber == null || dialingNumber.isEmpty()) && this.mDefaultPhone != null && this.mDefaultPhone.getPhoneType() == 1 && (this.mDefaultPhone instanceof MtkGsmCdmaPhone) && this.mDefaultPhone.isSupportSaveCFNumber() && isCfEnable(commandInterfaceCFAction) && (getNumber = this.mDefaultPhone.getCFPreviousDialNumber(commandInterfaceCFReason)) != null && !getNumber.isEmpty()) {
                dialingNumber = getNumber;
            }
            if (commandInterfaceCFReason != 0) {
                z = false;
            }
            try {
                this.mCT.getUtInterface().updateCallForward(getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber, serviceClass, timerSeconds, obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cf(dialingNumber, z, onComplete, serviceClass)));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    /* access modifiers changed from: protected */
    public int getCBTypeFromFacility(String facility) {
        if (MtkRIL.CB_FACILITY_BA_ACR.equals(facility)) {
            return 6;
        }
        return MtkImsPhone.super.getCBTypeFromFacility(facility);
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
        int action;
        logd("setCallBarring facility=" + facility + ", lockState=" + lockState + ", serviceClass = " + serviceClass);
        Message resp = obtainMessage(53, onComplete);
        if (lockState) {
            action = 1;
        } else {
            action = 0;
        }
        try {
            ((MtkImsUt) this.mCT.getUtInterface()).updateCallBarring(password, getCBTypeFromFacility(facility), action, resp, (String[]) null, serviceClass);
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    /* access modifiers changed from: private */
    public static class CfEx {
        final boolean mIsCfu;
        final Message mOnComplete;
        final String mSetCfNumber;
        final long[] mSetTimeSlot;

        CfEx(String cfNumber, long[] cfTimeSlot, boolean isCfu, Message onComplete) {
            this.mSetCfNumber = cfNumber;
            this.mSetTimeSlot = cfTimeSlot;
            this.mIsCfu = isCfu;
            this.mOnComplete = onComplete;
        }
    }

    public void saveTimeSlot(long[] timeSlot) {
        String timeSlotKey = CFU_TIME_SLOT + this.mPhoneId;
        String timeSlotString = "";
        if (timeSlot != null && timeSlot.length == 2) {
            timeSlotString = Long.toString(timeSlot[0]) + "," + Long.toString(timeSlot[1]);
        }
        SystemProperties.set(timeSlotKey, timeSlotString);
        logd("timeSlotString = " + timeSlotString);
    }

    public long[] getTimeSlot() {
        String timeSlotString = SystemProperties.get(CFU_TIME_SLOT + this.mPhoneId, "");
        long[] timeSlot = null;
        if (timeSlotString != null && !timeSlotString.equals("")) {
            String[] timeArray = timeSlotString.split(",");
            if (timeArray.length == 2) {
                timeSlot = new long[2];
                for (int i = 0; i < 2; i++) {
                    timeSlot[i] = Long.parseLong(timeArray[i]);
                    Calendar calenar = Calendar.getInstance(TimeZone.getDefault());
                    calenar.setTimeInMillis(timeSlot[i]);
                    int hour = calenar.get(11);
                    int min = calenar.get(12);
                    Calendar calenar2 = Calendar.getInstance(TimeZone.getDefault());
                    calenar2.set(11, hour);
                    calenar2.set(12, min);
                    timeSlot[i] = calenar2.getTimeInMillis();
                }
            }
        }
        logd("timeSlot = " + Arrays.toString(timeSlot));
        return timeSlot;
    }

    public void getCallForwardInTimeSlot(int commandInterfaceCFReason, Message onComplete) {
        logd("getCallForwardInTimeSlot reason = " + commandInterfaceCFReason);
        if (commandInterfaceCFReason == 0) {
            logd("requesting call forwarding in a time slot query.");
            try {
                ((MtkImsUt) this.mCT.getUtInterface()).queryCallForwardInTimeSlot(getConditionFromCFReason(commandInterfaceCFReason), obtainMessage(109, onComplete));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void setCallForwardInTimeSlot(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, long[] timeSlot, Message onComplete) {
        logd("setCallForwardInTimeSlot action = " + commandInterfaceCFAction + ", reason = " + commandInterfaceCFReason);
        if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && commandInterfaceCFReason == 0) {
            try {
                ((MtkImsUt) this.mCT.getUtInterface()).updateCallForwardInTimeSlot(getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber, timerSeconds, timeSlot, obtainMessage(110, commandInterfaceCFAction, 0, new CfEx(dialingNumber, timeSlot, true, onComplete)));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void getCallForwardingOptionForServiceClass(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
        logd("getCallForwardingOptionForServiceClass reason=" + commandInterfaceCFReason + ", service class= " + serviceClass);
        if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            logd("requesting call forwarding query.");
            try {
                ((MtkImsUt) this.mCT.getUtInterface()).queryCFForServiceClass(getConditionFromCFReason(commandInterfaceCFReason), (String) null, serviceClass, obtainMessage(13, onComplete));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    private MtkCallForwardInfo[] handleCfInTimeSlotQueryResult(MtkImsCallForwardInfo[] infos) {
        MtkCallForwardInfo[] cfInfos = null;
        if (supportMdAutoSetupIms()) {
            if (!(infos == null || infos.length == 0)) {
                cfInfos = new MtkCallForwardInfo[infos.length];
            }
        } else if (infos != null) {
            cfInfos = new MtkCallForwardInfo[infos.length];
        }
        IccRecords r = this.mDefaultPhone.getIccRecords();
        if (infos != null && infos.length != 0) {
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                if (!(infos[i].mCondition != 0 || (infos[i].mServiceClass & 1) == 0 || r == null)) {
                    setVoiceCallForwardingFlag(r, 1, infos[i].mStatus == 1, infos[i].mNumber);
                    saveTimeSlot(infos[i].mTimeSlot);
                }
                if (infos[i].mCondition == 0 && (infos[i].mServiceClass & 512) != 0) {
                    setVideoCallForwardingFlag(infos[i].mStatus == 1);
                }
                cfInfos[i] = getMtkCallForwardInfo(infos[i]);
            }
        } else if (r != null) {
            setVoiceCallForwardingFlag(r, 1, false, null);
            setVideoCallForwardingFlag(false);
        }
        return cfInfos;
    }

    private MtkCallForwardInfo getMtkCallForwardInfo(MtkImsCallForwardInfo info) {
        MtkCallForwardInfo cfInfo = new MtkCallForwardInfo();
        cfInfo.status = info.mStatus;
        cfInfo.reason = getCFReasonFromCondition(info.mCondition);
        cfInfo.serviceClass = info.mServiceClass;
        cfInfo.toa = info.mToA;
        cfInfo.number = info.mNumber;
        cfInfo.timeSeconds = info.mTimeSeconds;
        cfInfo.timeSlot = info.mTimeSlot;
        return cfInfo;
    }

    public void sendUssdResponse(String ussdMessge) {
        dumpPendingMmi();
        MtkImsPhone.super.sendUssdResponse(ussdMessge);
    }

    /* access modifiers changed from: protected */
    public CommandException getCommandException(int code, String errorString) {
        logd("getCommandException code= " + code + ", errorString= " + errorString);
        CommandException.Error error = CommandException.Error.GENERIC_FAILURE;
        if (code == 241) {
            error = CommandException.Error.FDN_CHECK_FAILURE;
        } else if (code == 801) {
            error = CommandException.Error.REQUEST_NOT_SUPPORTED;
        } else if (code != 802) {
            switch (code) {
                case 821:
                    error = CommandException.Error.PASSWORD_INCORRECT;
                    break;
                case 822:
                    error = CommandException.Error.SS_MODIFIED_TO_DIAL;
                    break;
                case 823:
                    error = CommandException.Error.SS_MODIFIED_TO_USSD;
                    break;
                case 824:
                    error = CommandException.Error.SS_MODIFIED_TO_SS;
                    break;
                case 825:
                    error = CommandException.Error.SS_MODIFIED_TO_DIAL_VIDEO;
                    break;
                default:
                    switch (code) {
                        case 61446:
                            error = CommandException.Error.OPERATION_NOT_ALLOWED;
                            break;
                        case 61447:
                            error = CommandException.Error.OEM_ERROR_3;
                            break;
                        default:
                            switch (code) {
                                case 61449:
                                    error = CommandException.Error.OEM_ERROR_25;
                                    break;
                                case 61450:
                                    error = CommandException.Error.OEM_ERROR_7;
                                    break;
                            }
                    }
            }
        } else {
            error = CommandException.Error.RADIO_NOT_AVAILABLE;
        }
        return new CommandException(error, errorString);
    }

    /* access modifiers changed from: protected */
    public CallForwardInfo getCallForwardInfo(ImsCallForwardInfo info) {
        CallForwardInfo cfInfo = new CallForwardInfo();
        cfInfo.status = info.mStatus;
        cfInfo.reason = getCFReasonFromCondition(info.mCondition);
        cfInfo.serviceClass = info.mServiceClass;
        cfInfo.toa = info.mToA;
        cfInfo.number = info.mNumber;
        cfInfo.timeSeconds = info.mTimeSeconds;
        return cfInfo;
    }

    public CallForwardInfo[] handleCfQueryResult(ImsCallForwardInfo[] infos) {
        CallForwardInfo[] cfInfos = null;
        if (supportMdAutoSetupIms()) {
            if (!(infos == null || infos.length == 0)) {
                cfInfos = new CallForwardInfo[infos.length];
            }
        } else if (infos != null) {
            cfInfos = new CallForwardInfo[infos.length];
        }
        IccRecords r = this.mDefaultPhone.getIccRecords();
        if (infos != null && infos.length != 0) {
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                if (!(infos[i].mCondition != 0 || (infos[i].mServiceClass & 1) == 0 || r == null)) {
                    setVoiceCallForwardingFlag(r, 1, infos[i].mStatus == 1, infos[i].mNumber);
                }
                cfInfos[i] = getCallForwardInfo(infos[i]);
                if (infos[i].mCondition == 0 && (infos[i].mServiceClass & 512) != 0) {
                    setVideoCallForwardingFlag(infos[i].mStatus == 1);
                    this.mDefaultPhone.notifyCallForwardingIndicator();
                }
            }
        } else if (r != null) {
            setVoiceCallForwardingFlag(r, 1, false, null);
            setVideoCallForwardingFlag(false);
        }
        return cfInfos;
    }

    /* access modifiers changed from: protected */
    public int[] handleCbQueryResult(ImsSsInfo[] infos) {
        return new int[]{infos[0].mStatus};
    }

    public void handleMessage(Message msg) {
        ImsException imsException;
        Message resp;
        ImsException imsException2;
        Message resp2;
        ImsException imsException3;
        ImsException imsException4;
        AsyncResult ar = (AsyncResult) msg.obj;
        logd("Mtk handleMessage what=" + msg.what);
        int i = msg.what;
        boolean z = false;
        if (i == 12) {
            IccRecords r = this.mDefaultPhone.getIccRecords();
            Cf cf = (Cf) ar.userObj;
            int cfAction = msg.arg1;
            int cfReason = msg.arg2;
            boolean isCfEnable = isCfEnable(cfAction);
            if (cf.mIsCfu && ar.exception == null && r != null) {
                if (!this.mDefaultPhone.queryCFUAgainAfterSet() || cfReason != 0) {
                    if ((cf.mServiceClass & 1) != 0) {
                        if (isCfEnable) {
                            z = true;
                        }
                        setVoiceCallForwardingFlag(r, 1, z, cf.mSetCfNumber);
                    }
                } else if (ar.result == null) {
                    logi("arResult is null.");
                } else {
                    logd("[EVENT_SET_CALL_FORWARD_DONE check cfinfo.");
                }
            }
            if (this.mDefaultPhone.getPhoneType() == 1 && (this.mDefaultPhone instanceof MtkGsmCdmaPhone) && this.mDefaultPhone.isSupportSaveCFNumber() && ar.exception == null) {
                if (isCfEnable && !this.mDefaultPhone.applyCFSharePreference(cfReason, cf.mSetCfNumber)) {
                    logd("applySharePreference false.");
                }
                if (cfAction == 4) {
                    this.mDefaultPhone.clearCFSharePreference(cfReason);
                }
            }
            sendResponse(cf.mOnComplete, null, ar.exception);
        } else if (i != 53) {
            if (i != 54) {
                if (i == 109) {
                    MtkCallForwardInfo[] mtkCfInfos = null;
                    if (ar.exception == null) {
                        mtkCfInfos = handleCfInTimeSlotQueryResult((MtkImsCallForwardInfo[]) ar.result);
                    }
                    if (ar.exception != null && (ar.exception instanceof ImsException) && (imsException3 = ar.exception) != null && imsException3.getCode() == 61446) {
                        this.mDefaultPhone.setCsFallbackStatus(2);
                        Message resp3 = (Message) ar.userObj;
                        if (resp3 != null) {
                            AsyncResult.forMessage(resp3, mtkCfInfos, new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED));
                            resp3.sendToTarget();
                            return;
                        }
                    }
                    sendResponse((Message) ar.userObj, mtkCfInfos, ar.exception);
                } else if (i != 110) {
                    MtkImsPhone.super.handleMessage(msg);
                } else {
                    IccRecords records = this.mDefaultPhone.getIccRecords();
                    CfEx cfEx = (CfEx) ar.userObj;
                    if (cfEx.mIsCfu && ar.exception == null && records != null) {
                        if (isCfEnable(msg.arg1)) {
                            z = true;
                        }
                        setVoiceCallForwardingFlag(records, 1, z, cfEx.mSetCfNumber);
                        saveTimeSlot(cfEx.mSetTimeSlot);
                    }
                    if (ar.exception != null && (ar.exception instanceof ImsException) && (imsException4 = ar.exception) != null && imsException4.getCode() == 61446) {
                        this.mDefaultPhone.setCsFallbackStatus(2);
                        Message resp4 = cfEx.mOnComplete;
                        if (resp4 != null) {
                            AsyncResult.forMessage(resp4, (Object) null, new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED));
                            resp4.sendToTarget();
                            return;
                        }
                    }
                    sendResponse(cfEx.mOnComplete, null, ar.exception);
                }
            } else if (supportMdAutoSetupIms() || !this.mDefaultPhone.isOpTransferXcap404() || ar.exception == null || !(ar.exception instanceof ImsException) || (imsException2 = ar.exception) == null || imsException2.getCode() != 61448 || (resp2 = (Message) ar.userObj) == null) {
                int[] ssInfos = null;
                if (ar.exception == null) {
                    ssInfos = handleCbQueryResult((ImsSsInfo[]) ar.result);
                }
                sendResponse((Message) ar.userObj, ssInfos, ar.exception);
            } else {
                AsyncResult.forMessage(resp2, (Object) null, new CommandException(CommandException.Error.NO_SUCH_ELEMENT));
                resp2.sendToTarget();
            }
        } else if (supportMdAutoSetupIms() || !this.mDefaultPhone.isOpTransferXcap404() || ar.exception == null || !(ar.exception instanceof ImsException) || (imsException = ar.exception) == null || imsException.getCode() != 61448 || (resp = (Message) ar.userObj) == null) {
            sendResponse((Message) ar.userObj, null, ar.exception);
        } else {
            AsyncResult.forMessage(resp, (Object) null, new CommandException(CommandException.Error.NO_SUCH_ELEMENT));
            resp.sendToTarget();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.mediatek.internal.telephony.imsphone.MtkImsPhone */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public void updateRoamingState(ServiceState ss) {
        if (ss == null) {
            loge("updateRoamingState: null ServiceState!");
            return;
        }
        boolean newRoamingState = ss.getRoaming();
        if (this.mRoaming != newRoamingState) {
            if (!(ss.getVoiceRegState() == 0 || ss.getDataRegState() == 0)) {
                logi("updateRoamingState: we are OUT_OF_SERVICE, ignoring roaming change.");
            } else if (isCsNotInServiceAndPsWwanReportingWlan(ss)) {
                logi("updateRoamingState: IWLAN masking roaming, ignore roaming change.");
            } else if (this.mCT.getState() == PhoneConstants.State.IDLE) {
                logd("updateRoamingState now: " + newRoamingState);
                this.mRoaming = newRoamingState;
                ImsManager imsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
                if (this.mIsWfcModeHomeForDomRoaming) {
                    int voiceRoamingType = ss.getVoiceRoamingType();
                    int dataRoamingType = ss.getDataRoamingType();
                    if (this.mRoaming && (voiceRoamingType == 2 || dataRoamingType == 2)) {
                        logd("Convert new roaming to HOME if it's domestic roaming,  voiceRoamingType: " + voiceRoamingType + " dataRoamingType: " + dataRoamingType);
                        imsManager.setWfcMode(imsManager.getWfcMode(false), false);
                        return;
                    }
                }
                imsManager.setWfcMode(imsManager.getWfcMode(newRoamingState), newRoamingState);
            } else {
                logd("updateRoamingState postponed: " + newRoamingState);
                this.mCT.registerForVoiceCallEnded(this, 61, (Object) null);
            }
        }
    }

    @VisibleForTesting
    public void setServiceState(int state) {
        MtkImsPhone.super.setServiceState(state);
        updateIsEmergencyOnly();
    }

    private boolean isSupportImsEcc() {
        return this.mCT.isSupportImsEcc();
    }

    public void updateIsEmergencyOnly() {
        ServiceState ss = getServiceState();
        logd("updateIsEmergencyOnly() sst: " + ss.getState() + " supportImsEcc: " + isSupportImsEcc());
        if (ss.getState() != 1 || !isSupportImsEcc()) {
            this.mSS.setEmergencyOnly(false);
        } else {
            this.mSS.setEmergencyOnly(true);
        }
    }

    private boolean supportMdAutoSetupIms() {
        if (SystemProperties.get("ro.vendor.md_auto_setup_ims").equals("1")) {
            return true;
        }
        return false;
    }

    private void registerForListenCarrierConfigChanged() {
        if (this.mContext == null) {
            logd("registerForListenCarrierConfigChanged failed");
            return;
        }
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, intentfilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cacheCarrierConfiguration() {
        int subId = getSubId();
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configMgr == null) {
            logd("cacheCarrierConfiguration failed: config mgr access failed");
            return;
        }
        PersistableBundle carrierConfig = configMgr.getConfigForSubId(subId);
        if (carrierConfig == null) {
            logd("cacheCarrierConfiguration failed: carrier config access failed");
            return;
        }
        this.mIsConsultativeEctSupported = carrierConfig.getBoolean("mtk_carrier_consultative_ect_supported");
        this.mIsBlindAssuredEctSupported = carrierConfig.getBoolean("mtk_carrier_blind_assured_ect_supported");
        this.mIsDeviceSwitchSupported = carrierConfig.getBoolean("mtk_carrier_device_switch_supported");
        this.mIsWfcModeHomeForDomRoaming = carrierConfig.getBoolean("mtk_carrier_wfc_mode_domestic_roaming_to_home");
        logd("cacheCarrierConfiguration, blindAssureEctSupported: " + this.mIsBlindAssuredEctSupported + " deviceSwitchSupported: " + this.mIsDeviceSwitchSupported + " WfcModeHomeForDomRoaming: " + this.mIsWfcModeHomeForDomRoaming);
    }

    public boolean isFeatureSupported(FeatureType feature) {
        if (feature == FeatureType.VIDEO_RESTRICTION) {
            List<String> videoRestriction = Arrays.asList("46003", "46011");
            IccRecords iccRecords = this.mDefaultPhone.getIccRecords();
            if (iccRecords == null) {
                logd("isFeatureSupported(" + feature + ") no iccRecords");
                return false;
            }
            String mccMnc = iccRecords.getOperatorNumeric();
            boolean ret = videoRestriction.contains(mccMnc);
            if (ret) {
                logd("isFeatureSupported(" + feature + "): ret = " + ret + " current mccMnc = " + mccMnc);
                return ret;
            }
        }
        if (feature == FeatureType.VOLTE_ENHANCED_CONFERENCE || feature == FeatureType.VIDEO_RESTRICTION || feature == FeatureType.BLINDASSURED_ECT) {
            List<String> voLteEnhancedConfMccMncList = Arrays.asList("46000", "46002", "46004", "46007", "46008", "00101");
            IccRecords iccRecords2 = this.mDefaultPhone.getIccRecords();
            if (iccRecords2 == null) {
                logd("isFeatureSupported(" + feature + ") no iccRecords");
                return false;
            }
            String mccMnc2 = iccRecords2.getOperatorNumeric();
            if (feature != FeatureType.BLINDASSURED_ECT) {
                boolean ret2 = voLteEnhancedConfMccMncList.contains(mccMnc2);
                logd("isFeatureSupported(" + feature + "): ret = " + ret2 + " current mccMnc = " + mccMnc2);
                return ret2;
            } else if (this.mIsBlindAssuredEctSupported) {
                logd("isFeatureSupported(" + feature + "): true current mccMnc = " + mccMnc2);
                return true;
            } else if (this.mIsDigitsSupported && this.mIsDeviceSwitchSupported) {
                logd("Digits device and TMO card, ECT supported: " + mccMnc2);
                return true;
            }
        } else if (feature == FeatureType.CONSULTATIVE_ECT) {
            if (this.mIsConsultativeEctSupported) {
                logd("isFeatureSupported(" + feature + "): true");
                return true;
            }
            logd("isFeatureSupported(" + feature + "): false");
            return false;
        }
        return false;
    }

    public boolean isWifiPdnOutOfService() {
        return this.mCT.isWifiPdnOutOfService();
    }

    public void setVoiceCallForwardingFlag(IccRecords r, int line, boolean enable, String number) {
        IccRecords record;
        MtkImsPhone.super.setVoiceCallForwardingFlag(r, line, enable, number);
        if (this.mDefaultPhone.getPhoneType() == 2 && (this.mDefaultPhone instanceof MtkGsmCdmaPhone) && this.mDefaultPhone.isGsmSsPrefer()) {
            UiccController uiccCtl = UiccController.getInstance();
            if (!(uiccCtl == null || (record = uiccCtl.getIccRecords(this.mPhoneId, 1)) == null)) {
                record.setVoiceCallForwardingFlag(line, enable, number);
            }
            this.mDefaultPhone.notifyCallForwardingIndicator();
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleEctIncallSupplementaryService(String dialString) {
        if (dialString.length() != 1) {
            return false;
        }
        if (!isFeatureSupported(FeatureType.CONSULTATIVE_ECT)) {
            return MtkImsPhone.super.handleEctIncallSupplementaryService(dialString);
        }
        Rlog.d(LOG_TAG, "supports explicit call transfer");
        this.mCT.explicitCallTransfer();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean needNotifySrvccState() {
        return true;
    }
}
