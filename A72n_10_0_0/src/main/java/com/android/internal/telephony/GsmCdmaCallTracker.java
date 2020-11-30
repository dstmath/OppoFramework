package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GsmCdmaCallTracker extends CallTracker {
    protected static final boolean DBG_POLL;
    protected static final String LOG_TAG = "GsmCdmaCallTracker";
    protected static final int MAX_CONNECTIONS_CDMA = 8;
    public static final int MAX_CONNECTIONS_GSM = 19;
    private static final int MAX_CONNECTIONS_PER_CALL_CDMA = 1;
    private static final int MAX_CONNECTIONS_PER_CALL_GSM = 5;
    protected static final boolean REPEAT_POLLING = false;
    protected static final boolean VDBG = false;
    protected int m3WayCallFlashDelay;
    @UnsupportedAppUsage
    public GsmCdmaCall mBackgroundCall = new GsmCdmaCall(this);
    private RegistrantList mCallWaitingRegistrants = new RegistrantList();
    @VisibleForTesting
    public GsmCdmaConnection[] mConnections;
    private boolean mDesiredMute = false;
    protected ArrayList<GsmCdmaConnection> mDroppedDuringPoll = new ArrayList<>(19);
    private BroadcastReceiver mEcmExitReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.GsmCdmaCallTracker.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                boolean isInEcm = intent.getBooleanExtra("phoneinECMState", false);
                GsmCdmaCallTracker gsmCdmaCallTracker = GsmCdmaCallTracker.this;
                gsmCdmaCallTracker.log("Received ACTION_EMERGENCY_CALLBACK_MODE_CHANGED isInEcm = " + isInEcm);
                if (!isInEcm) {
                    List<Connection> toNotify = new ArrayList<>();
                    toNotify.addAll(GsmCdmaCallTracker.this.mRingingCall.getConnections());
                    toNotify.addAll(GsmCdmaCallTracker.this.mForegroundCall.getConnections());
                    toNotify.addAll(GsmCdmaCallTracker.this.mBackgroundCall.getConnections());
                    if (GsmCdmaCallTracker.this.mPendingMO != null) {
                        toNotify.add(GsmCdmaCallTracker.this.mPendingMO);
                    }
                    for (Connection connection : toNotify) {
                        if (connection != null) {
                            connection.onExitedEcmMode();
                        }
                    }
                }
            }
        }
    };
    @UnsupportedAppUsage
    public GsmCdmaCall mForegroundCall = new GsmCdmaCall(this);
    protected boolean mHangupPendingMO;
    protected boolean mIsEcmTimerCanceled;
    protected boolean mIsInEmergencyCall;
    protected TelephonyMetrics mMetrics = TelephonyMetrics.getInstance();
    protected int mPendingCallClirMode;
    protected boolean mPendingCallInEcm;
    @UnsupportedAppUsage
    protected GsmCdmaConnection mPendingMO;
    @UnsupportedAppUsage
    public GsmCdmaPhone mPhone;
    @UnsupportedAppUsage
    public GsmCdmaCall mRingingCall = new GsmCdmaCall(this);
    @UnsupportedAppUsage
    public PhoneConstants.State mState = PhoneConstants.State.IDLE;
    protected RegistrantList mVoiceCallEndedRegistrants = new RegistrantList();
    protected RegistrantList mVoiceCallStartedRegistrants = new RegistrantList();

    static {
        boolean z = false;
        if (SystemProperties.getInt("persist.log.tag.tel_dbg", 0) == 1) {
            z = true;
        }
        DBG_POLL = z;
    }

    public GsmCdmaCallTracker(GsmCdmaPhone phone) {
        this.mPhone = phone;
        this.mCi = phone.mCi;
        this.mCi.registerForCallStateChanged(this, 2, null);
        this.mCi.registerForOn(this, 9, null);
        this.mCi.registerForNotAvailable(this, 10, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mEcmExitReceiver, filter);
        updatePhoneType(true);
        this.mReference = (IOppoCallTracker) OppoTelephonyFactory.getInstance().getFeature(IOppoCallTracker.DEFAULT, this);
    }

    public void updatePhoneType() {
        updatePhoneType(false);
    }

    /* access modifiers changed from: protected */
    public void updatePhoneType(boolean duringInit) {
        if (!duringInit) {
            reset();
            pollCallsWhenSafe();
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            this.mConnections = new GsmCdmaConnection[19];
            this.mCi.unregisterForCallWaitingInfo(this);
            if (this.mIsInEmergencyCall) {
                this.mPhone.getDataEnabledSettings().setInternalDataEnabled(true);
                return;
            }
            return;
        }
        this.mConnections = new GsmCdmaConnection[8];
        this.mPendingCallInEcm = false;
        this.mIsInEmergencyCall = false;
        this.mPendingCallClirMode = 0;
        this.mIsEcmTimerCanceled = false;
        this.m3WayCallFlashDelay = 0;
        this.mCi.registerForCallWaitingInfo(this, 15, null);
    }

    /* access modifiers changed from: protected */
    public void reset() {
        Rlog.d(LOG_TAG, "reset");
        GsmCdmaConnection[] gsmCdmaConnectionArr = this.mConnections;
        for (GsmCdmaConnection gsmCdmaConnection : gsmCdmaConnectionArr) {
            if (gsmCdmaConnection != null) {
                gsmCdmaConnection.onDisconnect(36);
                gsmCdmaConnection.dispose();
            }
        }
        GsmCdmaConnection gsmCdmaConnection2 = this.mPendingMO;
        if (gsmCdmaConnection2 != null) {
            gsmCdmaConnection2.onDisconnect(36);
            this.mPendingMO.dispose();
        }
        this.mConnections = null;
        this.mPendingMO = null;
        clearDisconnected();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        Rlog.d(LOG_TAG, "GsmCdmaCallTracker finalized");
    }

    @Override // com.android.internal.telephony.CallTracker
    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceCallStartedRegistrants.add(r);
        if (this.mState != PhoneConstants.State.IDLE) {
            r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // com.android.internal.telephony.CallTracker
    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CallTracker
    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CallTracker
    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCallWaitingRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallWaiting(Handler h) {
        this.mCallWaitingRegistrants.remove(h);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void fakeHoldForegroundBeforeDial() {
        List<Connection> connCopy = (List) this.mForegroundCall.mConnections.clone();
        int s = connCopy.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) connCopy.get(i)).fakeHoldBeforeDial();
        }
    }

    public synchronized Connection dialGsm(String dialString, int clirMode, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        clearDisconnected();
        boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
        checkForDialIssues(isEmergencyCall);
        String dialString2 = convertNumberIfNecessary(this.mPhone, dialString);
        if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
            switchWaitingOrHoldingAndActive();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            fakeHoldForegroundBeforeDial();
        }
        if (this.mForegroundCall.getState() == Call.State.IDLE) {
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString2), this, this.mForegroundCall, isEmergencyCall);
            if (intentExtras != null) {
                Rlog.d(LOG_TAG, "dialGsm - emergency dialer: " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
                this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
            }
            this.mHangupPendingMO = false;
            this.mMetrics.writeRilDial(this.mPhone.getPhoneId(), this.mPendingMO, clirMode, uusInfo);
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
            } else {
                setMute(false);
                this.mCi.dial(this.mPendingMO.getAddress(), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), clirMode, uusInfo, obtainCompleteMessage());
            }
            if (this.mNumberConverted) {
                this.mPendingMO.setConverted(dialString);
                this.mNumberConverted = false;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        if (action == 0) {
            this.mIsEcmTimerCanceled = false;
        } else if (action != 1) {
            Rlog.e(LOG_TAG, "handleEcmTimer, unsupported action " + action);
        } else {
            this.mIsEcmTimerCanceled = true;
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void disableDataCallInEmergencyCall(String dialString) {
        if (PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString)) {
            log("disableDataCallInEmergencyCall");
            setIsInEmergencyCall();
        }
    }

    public void setIsInEmergencyCall() {
        this.mIsInEmergencyCall = true;
        this.mPhone.getDataEnabledSettings().setInternalDataEnabled(false);
        this.mPhone.notifyEmergencyCallRegistrants(true);
        this.mPhone.sendEmergencyCallStateChange(true);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0170  */
    public Connection dialCdma(String dialString, int clirMode, Bundle intentExtras) throws CallStateException {
        boolean internationalRoaming;
        String dialString2;
        clearDisconnected();
        boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
        checkForDialIssues(isEmergencyCall);
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        String operatorIsoContry = tm.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
        String simIsoContry = tm.getSimCountryIsoForPhone(this.mPhone.getPhoneId());
        boolean internationalRoaming2 = !TextUtils.isEmpty(operatorIsoContry) && !TextUtils.isEmpty(simIsoContry) && !simIsoContry.equals(operatorIsoContry);
        if (internationalRoaming2) {
            if ("us".equals(simIsoContry)) {
                internationalRoaming = internationalRoaming2 && !"vi".equals(operatorIsoContry);
            } else if ("vi".equals(simIsoContry)) {
                internationalRoaming = internationalRoaming2 && !"us".equals(operatorIsoContry);
            }
            if (!internationalRoaming) {
                dialString2 = convertNumberIfNecessary(this.mPhone, dialString);
            } else {
                dialString2 = dialString;
            }
            boolean isPhoneInEcmMode = this.mPhone.isInEcm();
            if (isPhoneInEcmMode && isEmergencyCall) {
                handleEcmTimer(1);
            }
            if (this.mForegroundCall.getState() != Call.State.ACTIVE) {
                return dialThreeWay(dialString2, intentExtras);
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString2), this, this.mForegroundCall, isEmergencyCall);
            if (intentExtras != null) {
                Rlog.d(LOG_TAG, "dialGsm - emergency dialer: " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
                this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
            }
            this.mHangupPendingMO = false;
            if (this.mPendingMO.getAddress() != null && this.mPendingMO.getAddress().length() != 0) {
                if (this.mPendingMO.getAddress().indexOf(78) < 0) {
                    setMute(false);
                    disableDataCallInEmergencyCall(dialString2);
                    if (isPhoneInEcmMode) {
                        if (!isPhoneInEcmMode || !isEmergencyCall) {
                            this.mPhone.exitEmergencyCallbackMode();
                            this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                            this.mPendingCallClirMode = clirMode;
                            this.mPendingCallInEcm = true;
                            if (this.mNumberConverted) {
                                this.mPendingMO.setConverted(dialString);
                                this.mNumberConverted = false;
                            }
                            updatePhoneState();
                            this.mPhone.notifyPreciseCallStateChanged();
                            return this.mPendingMO;
                        }
                    }
                    this.mCi.dial(this.mPendingMO.getAddress(), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), clirMode, obtainCompleteMessage());
                    if (this.mNumberConverted) {
                    }
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                    return this.mPendingMO;
                }
            }
            this.mPendingMO.mCause = 7;
            pollCallsWhenSafe();
            if (this.mNumberConverted) {
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return this.mPendingMO;
        }
        internationalRoaming = internationalRoaming2;
        if (!internationalRoaming) {
        }
        boolean isPhoneInEcmMode2 = this.mPhone.isInEcm();
        handleEcmTimer(1);
        if (this.mForegroundCall.getState() != Call.State.ACTIVE) {
        }
    }

    /* access modifiers changed from: protected */
    public Connection dialThreeWay(String dialString, Bundle intentExtras) {
        if (this.mForegroundCall.isIdle()) {
            return null;
        }
        disableDataCallInEmergencyCall(dialString);
        this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, this.mIsInEmergencyCall);
        if (intentExtras != null) {
            Rlog.d(LOG_TAG, "dialThreeWay - emergency dialer " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
            this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
        }
        PersistableBundle bundle = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        if (bundle != null) {
            this.m3WayCallFlashDelay = bundle.getInt("cdma_3waycall_flash_delay_int");
        } else {
            this.m3WayCallFlashDelay = 0;
        }
        if (this.m3WayCallFlashDelay > 0) {
            this.mCi.sendCDMAFeatureCode(PhoneConfigurationManager.SSSS, obtainMessage(20));
        } else {
            this.mCi.sendCDMAFeatureCode(this.mPendingMO.getAddress(), obtainMessage(16));
        }
        return this.mPendingMO;
    }

    public Connection dial(String dialString, Bundle intentExtras) throws CallStateException {
        if (isPhoneTypeGsm()) {
            return dialGsm(dialString, 0, intentExtras);
        }
        return dialCdma(dialString, 0, intentExtras);
    }

    public Connection dialGsm(String dialString, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        return dialGsm(dialString, 0, uusInfo, intentExtras);
    }

    private Connection dialGsm(String dialString, int clirMode, Bundle intentExtras) throws CallStateException {
        return dialGsm(dialString, clirMode, null, intentExtras);
    }

    public void acceptCall() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            Rlog.i("phone", "acceptCall: incoming...");
            setMute(false);
            this.mCi.acceptCall(obtainCompleteMessage());
        } else if (this.mRingingCall.getState() == Call.State.WAITING) {
            if (isPhoneTypeGsm()) {
                setMute(false);
            } else {
                GsmCdmaConnection cwConn = (GsmCdmaConnection) this.mRingingCall.getLatestConnection();
                cwConn.updateParent(this.mRingingCall, this.mForegroundCall);
                cwConn.onConnectedInOrOut();
                updatePhoneState();
            }
            switchWaitingOrHoldingAndActive();
        } else {
            throw new CallStateException("phone not ringing");
        }
    }

    public void rejectCall() throws CallStateException {
        if (this.mRingingCall.getState().isRinging()) {
            this.mCi.rejectCall(obtainCompleteMessage());
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    /* access modifiers changed from: protected */
    public void flashAndSetGenericTrue() {
        this.mCi.sendCDMAFeatureCode(PhoneConfigurationManager.SSSS, obtainMessage(8));
        this.mPhone.notifyPreciseCallStateChanged();
    }

    @UnsupportedAppUsage
    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (isPhoneTypeGsm()) {
            this.mCi.switchWaitingOrHoldingAndActive(obtainCompleteMessage(8));
        } else if (this.mForegroundCall.getConnections().size() > 1) {
            flashAndSetGenericTrue();
        } else {
            this.mCi.sendCDMAFeatureCode(PhoneConfigurationManager.SSSS, obtainMessage(8));
        }
    }

    public void conference() {
        if (isPhoneTypeGsm()) {
            this.mCi.conference(obtainCompleteMessage(11));
        } else {
            flashAndSetGenericTrue();
        }
    }

    public void explicitCallTransfer() {
        this.mCi.explicitCallTransfer(obtainCompleteMessage(13));
    }

    @UnsupportedAppUsage
    public void clearDisconnected() {
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        return this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING && !this.mBackgroundCall.isFull() && !this.mForegroundCall.isFull();
    }

    public void checkForDialIssues(boolean isEmergencyCall) throws CallStateException {
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        if (this.mCi.getRadioState() != 1) {
            throw new CallStateException(2, "Modem not powered");
        } else if (disableCall.equals("true")) {
            throw new CallStateException(5, "Calling disabled via ro.telephony.disable-call property");
        } else if (this.mPendingMO != null) {
            throw new CallStateException(3, "A call is already dialing.");
        } else if (this.mRingingCall.isRinging()) {
            throw new CallStateException(4, "Can't call while a call is ringing.");
        } else if (isPhoneTypeGsm() && this.mForegroundCall.getState().isAlive() && this.mBackgroundCall.getState().isAlive()) {
            throw new CallStateException(6, "There is already a foreground and background call.");
        } else if (!isPhoneTypeGsm() && this.mForegroundCall.getState().isAlive() && this.mForegroundCall.getState() != Call.State.ACTIVE && this.mBackgroundCall.getState().isAlive()) {
            throw new CallStateException(6, "There is already a foreground and background call.");
        } else if (!isEmergencyCall && isInOtaspCall()) {
            throw new CallStateException(7, "OTASP provisioning is in process.");
        }
    }

    public boolean canTransfer() {
        if (!isPhoneTypeGsm()) {
            Rlog.e(LOG_TAG, "canTransfer: not possible in CDMA");
            return false;
        } else if ((this.mForegroundCall.getState() == Call.State.ACTIVE || this.mForegroundCall.getState() == Call.State.ALERTING || this.mForegroundCall.getState() == Call.State.DIALING) && this.mBackgroundCall.getState() == Call.State.HOLDING) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
    }

    @UnsupportedAppUsage
    public Message obtainCompleteMessage() {
        return obtainCompleteMessage(4);
    }

    @UnsupportedAppUsage
    public Message obtainCompleteMessage(int what) {
        this.mPendingOperations++;
        this.mLastRelevantPoll = null;
        this.mNeedsPoll = true;
        if (DBG_POLL) {
            log("obtainCompleteMessage: pendingOperations=" + this.mPendingOperations + ", needsPoll=" + this.mNeedsPoll);
        }
        return obtainMessage(what);
    }

    /* access modifiers changed from: protected */
    public void operationComplete() {
        this.mPendingOperations--;
        if (DBG_POLL) {
            log("operationComplete: pendingOperations=" + this.mPendingOperations + ", needsPoll=" + this.mNeedsPoll);
        }
        if (this.mPendingOperations == 0 && this.mNeedsPoll) {
            this.mLastRelevantPoll = obtainMessage(1);
            this.mCi.getCurrentCalls(this.mLastRelevantPoll);
        } else if (this.mPendingOperations < 0) {
            Rlog.e(LOG_TAG, "GsmCdmaCallTracker.pendingOperations < 0");
            this.mPendingOperations = 0;
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void updatePhoneState() {
        PhoneConstants.State oldState = this.mState;
        if (this.mRingingCall.isRinging()) {
            this.mState = PhoneConstants.State.RINGING;
        } else if (this.mPendingMO != null || !this.mForegroundCall.isIdle() || !this.mBackgroundCall.isIdle()) {
            this.mState = PhoneConstants.State.OFFHOOK;
        } else {
            Phone imsPhone = this.mPhone.getImsPhone();
            if (this.mState == PhoneConstants.State.OFFHOOK && imsPhone != null) {
                imsPhone.callEndCleanupHandOverCallIfAny();
            }
            this.mState = PhoneConstants.State.IDLE;
        }
        if (this.mState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else if (oldState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
        log("update phone state, old=" + oldState + " new=" + this.mState);
        if (this.mState != oldState) {
            this.mPhone.notifyPhoneStateChanged();
            this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.CallTracker
    public synchronized void handlePollCalls(AsyncResult ar) {
        List polledCalls;
        Phone imsPhone;
        int cause;
        DriverCall dc;
        boolean hasAnyCallDisconnected;
        int dcSize;
        int curDC;
        boolean noConnectionExists;
        boolean unknownConnectionAppeared;
        if (ar.exception == null) {
            polledCalls = (List) ar.result;
        } else if (isCommandExceptionRadioNotAvailable(ar.exception)) {
            polledCalls = new ArrayList();
        } else {
            pollCallsAfterDelay();
            return;
        }
        ArrayList<Connection> newUnknownConnectionsGsm = new ArrayList<>();
        boolean hasNonHangupStateChanged = false;
        boolean hasAnyCallDisconnected2 = false;
        boolean unknownConnectionAppeared2 = false;
        int handoverConnectionsSize = this.mHandoverConnections.size();
        boolean noConnectionExists2 = true;
        int i = 0;
        int curDC2 = 0;
        int dcSize2 = polledCalls.size();
        Connection newUnknownConnectionCdma = null;
        Connection newRinging = null;
        while (i < this.mConnections.length) {
            GsmCdmaConnection conn = this.mConnections[i];
            if (curDC2 < dcSize2) {
                DriverCall dc2 = (DriverCall) polledCalls.get(curDC2);
                if (dc2.index == i + 1) {
                    curDC2++;
                    dc = dc2;
                } else {
                    dc = null;
                }
            } else {
                dc = null;
            }
            if (!(conn == null && dc == null)) {
                noConnectionExists2 = false;
            }
            if (DBG_POLL) {
                StringBuilder sb = new StringBuilder();
                hasAnyCallDisconnected = hasAnyCallDisconnected2;
                sb.append("poll: conn[i=");
                sb.append(i);
                sb.append("]=");
                sb.append(conn);
                sb.append(", dc=");
                sb.append(dc);
                log(sb.toString());
            } else {
                hasAnyCallDisconnected = hasAnyCallDisconnected2;
            }
            if (conn != null || dc == null) {
                unknownConnectionAppeared = unknownConnectionAppeared2;
                noConnectionExists = noConnectionExists2;
                curDC = curDC2;
                dcSize = dcSize2;
                if (conn != null && dc == null) {
                    if (isPhoneTypeGsm()) {
                        this.mDroppedDuringPoll.add(conn);
                    } else {
                        int count = this.mForegroundCall.mConnections.size();
                        for (int n = 0; n < count; n++) {
                            log("adding fgCall cn " + n + " to droppedDuringPoll");
                            this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mForegroundCall.mConnections.get(n));
                        }
                        int count2 = this.mRingingCall.mConnections.size();
                        for (int n2 = 0; n2 < count2; n2++) {
                            log("adding rgCall cn " + n2 + " to droppedDuringPoll");
                            this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mRingingCall.mConnections.get(n2));
                        }
                        if (this.mIsEcmTimerCanceled) {
                            handleEcmTimer(0);
                        }
                        checkAndEnableDataCallAfterEmergencyCallDropped();
                    }
                    this.mConnections[i] = null;
                } else if (conn != null && dc != null && !conn.compareTo(dc) && isPhoneTypeGsm()) {
                    this.mDroppedDuringPoll.add(conn);
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    if (this.mConnections[i].getCall() == this.mRingingCall) {
                        newRinging = this.mConnections[i];
                    }
                    hasNonHangupStateChanged = true;
                } else if (!(conn == null || dc == null)) {
                    if (isPhoneTypeGsm() || conn.isIncoming() == dc.isMT) {
                        hasNonHangupStateChanged = hasNonHangupStateChanged || conn.update(dc);
                    } else if (dc.isMT) {
                        this.mDroppedDuringPoll.add(conn);
                        Connection newRinging2 = checkMtFindNewRinging(dc, i);
                        if (newRinging2 == null) {
                            newUnknownConnectionCdma = conn;
                            unknownConnectionAppeared = true;
                        }
                        checkAndEnableDataCallAfterEmergencyCallDropped();
                        newRinging = newRinging2;
                    } else {
                        Rlog.e(LOG_TAG, "Error in RIL, Phantom call appeared " + dc);
                    }
                }
            } else {
                if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc)) {
                    log("pendingMo=" + this.mPendingMO + ", dc=" + dc);
                    unknownConnectionAppeared = unknownConnectionAppeared2;
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    Connection hoConnection = getHoConnection(dc);
                    if (hoConnection != null) {
                        this.mConnections[i].migrateFrom(hoConnection);
                        if (hoConnection.mPreHandoverState == Call.State.ACTIVE || hoConnection.mPreHandoverState == Call.State.HOLDING || dc.state != DriverCall.State.ACTIVE) {
                            this.mConnections[i].onConnectedConnectionMigrated();
                        } else {
                            this.mConnections[i].onConnectedInOrOut();
                        }
                        this.mHandoverConnections.remove(hoConnection);
                        if (isPhoneTypeGsm()) {
                            Iterator<Connection> it = this.mHandoverConnections.iterator();
                            while (it.hasNext()) {
                                Connection c = it.next();
                                Rlog.i(LOG_TAG, "HO Conn state is " + c.mPreHandoverState);
                                if (c.mPreHandoverState == this.mConnections[i].getState()) {
                                    Rlog.i(LOG_TAG, "Removing HO conn " + hoConnection + c.mPreHandoverState);
                                    it.remove();
                                }
                                noConnectionExists2 = noConnectionExists2;
                                curDC2 = curDC2;
                                dcSize2 = dcSize2;
                            }
                            noConnectionExists = noConnectionExists2;
                            curDC = curDC2;
                            dcSize = dcSize2;
                        } else {
                            noConnectionExists = noConnectionExists2;
                            curDC = curDC2;
                            dcSize = dcSize2;
                        }
                        this.mPhone.notifyHandoverStateChanged(this.mConnections[i]);
                    } else {
                        noConnectionExists = noConnectionExists2;
                        curDC = curDC2;
                        dcSize = dcSize2;
                        newRinging = checkMtFindNewRinging(dc, i);
                        if (newRinging == null) {
                            if (isPhoneTypeGsm()) {
                                newUnknownConnectionsGsm.add(this.mConnections[i]);
                                unknownConnectionAppeared = true;
                            } else {
                                newUnknownConnectionCdma = this.mConnections[i];
                                unknownConnectionAppeared = true;
                            }
                        }
                    }
                } else {
                    if (DBG_POLL) {
                        log("poll: pendingMO=" + this.mPendingMO);
                    }
                    this.mConnections[i] = this.mPendingMO;
                    this.mPendingMO.mIndex = i;
                    this.mPendingMO.update(dc);
                    this.mPendingMO = null;
                    if (this.mHangupPendingMO) {
                        this.mHangupPendingMO = false;
                        if (!isPhoneTypeGsm() && this.mIsEcmTimerCanceled) {
                            handleEcmTimer(0);
                        }
                        try {
                            log("poll: hangupPendingMO, hangup conn " + i);
                            hangup(this.mConnections[i]);
                        } catch (CallStateException e) {
                            Rlog.e(LOG_TAG, "unexpected error on hangup");
                        }
                        return;
                    }
                    unknownConnectionAppeared = unknownConnectionAppeared2;
                    noConnectionExists = noConnectionExists2;
                    curDC = curDC2;
                    dcSize = dcSize2;
                }
                hasNonHangupStateChanged = true;
            }
            unknownConnectionAppeared2 = unknownConnectionAppeared;
            i++;
            hasAnyCallDisconnected2 = hasAnyCallDisconnected;
            polledCalls = polledCalls;
            noConnectionExists2 = noConnectionExists;
            curDC2 = curDC;
            dcSize2 = dcSize;
        }
        boolean hasAnyCallDisconnected3 = hasAnyCallDisconnected2;
        boolean unknownConnectionAppeared3 = unknownConnectionAppeared2;
        if (!isPhoneTypeGsm() && noConnectionExists2) {
            checkAndEnableDataCallAfterEmergencyCallDropped();
        }
        if (this.mPendingMO != null) {
            Rlog.d(LOG_TAG, "Pending MO dropped before poll fg state:" + this.mForegroundCall.getState());
            this.mDroppedDuringPoll.add(this.mPendingMO);
            this.mPendingMO = null;
            this.mHangupPendingMO = false;
            if (!isPhoneTypeGsm()) {
                if (this.mPendingCallInEcm) {
                    this.mPendingCallInEcm = false;
                }
                checkAndEnableDataCallAfterEmergencyCallDropped();
            }
        }
        if (newRinging != null) {
            this.mPhone.notifyNewRingingConnection(newRinging);
            if (isOemAutoAnswer(this.mPhone)) {
                Rlog.d(LOG_TAG, "acceptCall: for test card...");
                sendEmptyMessageDelayed(900, (long) (isPhoneTypeGsm() ? 3 : TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_REASON_UNSPECIFIED));
            }
        }
        ArrayList<GsmCdmaConnection> locallyDisconnectedConnections = new ArrayList<>();
        for (int i2 = this.mDroppedDuringPoll.size() - 1; i2 >= 0; i2--) {
            GsmCdmaConnection conn2 = this.mDroppedDuringPoll.get(i2);
            boolean wasDisconnected = false;
            if (conn2.isIncoming() && conn2.getConnectTime() == 0) {
                if (conn2.mCause == 3) {
                    cause = 16;
                } else {
                    cause = 1;
                }
                log("missed/rejected call, conn.cause=" + conn2.mCause);
                log("setting cause to " + cause);
                this.mDroppedDuringPoll.remove(i2);
                wasDisconnected = true;
                locallyDisconnectedConnections.add(conn2);
                hasAnyCallDisconnected3 |= conn2.onDisconnect(cause);
            } else if (conn2.mCause == 3 || conn2.mCause == 7) {
                this.mDroppedDuringPoll.remove(i2);
                wasDisconnected = true;
                locallyDisconnectedConnections.add(conn2);
                hasAnyCallDisconnected3 |= conn2.onDisconnect(conn2.mCause);
            }
            if (!isPhoneTypeGsm() && wasDisconnected && unknownConnectionAppeared3 && conn2 == newUnknownConnectionCdma) {
                unknownConnectionAppeared3 = false;
                newUnknownConnectionCdma = null;
            }
        }
        if (locallyDisconnectedConnections.size() > 0) {
            this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), locallyDisconnectedConnections, getNetworkCountryIso());
        }
        Iterator<Connection> it2 = this.mHandoverConnections.iterator();
        while (it2.hasNext()) {
            Connection hoConnection2 = it2.next();
            log("handlePollCalls - disconnect hoConn= " + hoConnection2 + " hoConn.State= " + hoConnection2.getState());
            if (hoConnection2.getState().isRinging()) {
                hoConnection2.onDisconnect(1);
            } else {
                hoConnection2.onDisconnect(-1);
            }
            it2.remove();
        }
        if (this.mDroppedDuringPoll.size() > 0) {
            this.mCi.getLastCallFailCause(obtainNoPollCompleteMessage(5));
        }
        if (0 != 0) {
            pollCallsAfterDelay();
        }
        if (newRinging != null || hasNonHangupStateChanged || hasAnyCallDisconnected3) {
            internalClearDisconnected();
        }
        updatePhoneState();
        if (unknownConnectionAppeared3) {
            if (isPhoneTypeGsm()) {
                Iterator<Connection> it3 = newUnknownConnectionsGsm.iterator();
                while (it3.hasNext()) {
                    Connection c2 = it3.next();
                    log("Notify unknown for " + c2);
                    this.mPhone.notifyUnknownConnection(c2);
                }
            } else {
                this.mPhone.notifyUnknownConnection(newUnknownConnectionCdma);
            }
        }
        if (hasNonHangupStateChanged || newRinging != null || hasAnyCallDisconnected3) {
            this.mPhone.notifyPreciseCallStateChanged();
            updateMetrics(this.mConnections);
        }
        if (handoverConnectionsSize > 0 && this.mHandoverConnections.size() == 0 && (imsPhone = this.mPhone.getImsPhone()) != null) {
            imsPhone.callEndCleanupHandOverCallIfAny();
        }
    }

    /* access modifiers changed from: protected */
    public void updateMetrics(GsmCdmaConnection[] connections) {
        ArrayList<GsmCdmaConnection> activeConnections = new ArrayList<>();
        for (GsmCdmaConnection conn : connections) {
            if (conn != null) {
                activeConnections.add(conn);
            }
        }
        this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), activeConnections, getNetworkCountryIso());
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    /* access modifiers changed from: protected */
    public void dumpState() {
        Rlog.i(LOG_TAG, "Phone State:" + this.mState);
        Rlog.i(LOG_TAG, "Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (int i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Foreground call: " + this.mForegroundCall.toString());
        List l2 = this.mForegroundCall.getConnections();
        int s2 = l2.size();
        for (int i2 = 0; i2 < s2; i2++) {
            Rlog.i(LOG_TAG, l2.get(i2).toString());
        }
        Rlog.i(LOG_TAG, "Background call: " + this.mBackgroundCall.toString());
        List l3 = this.mBackgroundCall.getConnections();
        int s3 = l3.size();
        for (int i3 = 0; i3 < s3; i3++) {
            Rlog.i(LOG_TAG, l3.get(i3).toString());
        }
    }

    public void hangup(GsmCdmaConnection conn) throws CallStateException {
        GsmCdmaCall gsmCdmaCall;
        if (conn.mOwner == this) {
            if (conn == this.mPendingMO) {
                log("hangup: set hangupPendingMO to true");
                this.mHangupPendingMO = true;
            } else if (!isPhoneTypeGsm() && conn.getCall() == (gsmCdmaCall = this.mRingingCall) && gsmCdmaCall.getState() == Call.State.WAITING) {
                conn.onLocalDisconnect();
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                return;
            } else {
                try {
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), conn, conn.getGsmCdmaIndex(), getNetworkCountryIso());
                    this.mCi.hangupConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage());
                } catch (CallStateException e) {
                    Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: hangup() on absent connection " + conn);
                }
            }
            conn.onHangupLocal();
            return;
        }
        throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
    }

    public void separate(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner == this) {
            try {
                this.mCi.separateConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage(12));
            } catch (CallStateException e) {
                Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: separate() on absent connection " + conn);
            }
        } else {
            throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
        }
    }

    @UnsupportedAppUsage
    public void setMute(boolean mute) {
        this.mDesiredMute = mute;
        this.mCi.setMute(this.mDesiredMute, null);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void hangup(GsmCdmaCall call) throws CallStateException {
        if (call.getConnections().size() != 0) {
            GsmCdmaCall gsmCdmaCall = this.mRingingCall;
            if (call == gsmCdmaCall) {
                log("(ringing) hangup waiting or background");
                logHangupEvent(call);
                this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
            } else if (call == this.mForegroundCall) {
                if (call.isDialingOrAlerting()) {
                    log("(foregnd) hangup dialing or alerting...");
                    hangup((GsmCdmaConnection) call.getConnections().get(0));
                } else if (!isPhoneTypeGsm() || !this.mRingingCall.isRinging()) {
                    logHangupEvent(call);
                    hangupForegroundResumeBackground();
                } else {
                    log("hangup all conns in active/background call, without affecting ringing call");
                    hangupAllConnections(call);
                }
            } else if (call != this.mBackgroundCall) {
                throw new RuntimeException("GsmCdmaCall " + call + "does not belong to GsmCdmaCallTracker " + this);
            } else if (gsmCdmaCall.isRinging()) {
                log("hangup all conns in background call");
                hangupAllConnections(call);
            } else {
                hangupWaitingOrBackground();
            }
            call.onHangupLocal();
            this.mPhone.notifyPreciseCallStateChanged();
            return;
        }
        throw new CallStateException("no connections in call");
    }

    /* access modifiers changed from: protected */
    public void logHangupEvent(GsmCdmaCall call) {
        int call_index;
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            try {
                call_index = cn.getGsmCdmaIndex();
            } catch (CallStateException e) {
                call_index = -1;
            }
            this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, call_index, getNetworkCountryIso());
        }
    }

    public void hangupWaitingOrBackground() {
        log("hangupWaitingOrBackground");
        logHangupEvent(this.mBackgroundCall);
        this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
    }

    public void hangupForegroundResumeBackground() {
        log("hangupForegroundResumeBackground");
        this.mCi.hangupForegroundResumeBackground(obtainCompleteMessage());
    }

    public void hangupConnectionByIndex(GsmCdmaCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            if (!cn.mDisconnected && cn.getGsmCdmaIndex() == index) {
                this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, cn.getGsmCdmaIndex(), getNetworkCountryIso());
                this.mCi.hangupConnection(index, obtainCompleteMessage());
                return;
            }
        }
        throw new CallStateException("no GsmCdma index found");
    }

    public void hangupAllConnections(GsmCdmaCall call) {
        try {
            int count = call.mConnections.size();
            for (int i = 0; i < count; i++) {
                GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
                if (!cn.mDisconnected) {
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, cn.getGsmCdmaIndex(), getNetworkCountryIso());
                    this.mCi.hangupConnection(cn.getGsmCdmaIndex(), obtainCompleteMessage());
                }
            }
        } catch (CallStateException ex) {
            Rlog.e(LOG_TAG, "hangupConnectionByIndex caught " + ex);
        }
    }

    public GsmCdmaConnection getConnectionByIndex(GsmCdmaCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            if (!cn.mDisconnected && cn.getGsmCdmaIndex() == index) {
                return cn;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void notifyCallWaitingInfo(CdmaCallWaitingNotification obj) {
        RegistrantList registrantList = this.mCallWaitingRegistrants;
        if (registrantList != null) {
            registrantList.notifyRegistrants(new AsyncResult((Object) null, obj, (Throwable) null));
        }
    }

    /* access modifiers changed from: protected */
    public void handleCallWaitingInfo(CdmaCallWaitingNotification cw) {
        new GsmCdmaConnection(this.mPhone.getContext(), cw, this, this.mRingingCall);
        updatePhoneState();
        notifyCallWaitingInfo(cw);
    }

    /* access modifiers changed from: protected */
    public PhoneInternalInterface.SuppService getFailedService(int what) {
        if (what == 8) {
            return PhoneInternalInterface.SuppService.SWITCH;
        }
        switch (what) {
            case 11:
                return PhoneInternalInterface.SuppService.CONFERENCE;
            case 12:
                return PhoneInternalInterface.SuppService.SEPARATE;
            case 13:
                return PhoneInternalInterface.SuppService.TRANSFER;
            default:
                return PhoneInternalInterface.SuppService.UNKNOWN;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.android.internal.telephony.CallTracker
    public void handleMessage(Message msg) {
        int causeCode;
        int causeCode2;
        Connection connection;
        int i = msg.what;
        if (i == 1) {
            Rlog.d(LOG_TAG, "Event EVENT_POLL_CALLS_RESULT Received");
            if (msg == this.mLastRelevantPoll) {
                if (DBG_POLL) {
                    log("handle EVENT_POLL_CALL_RESULT: set needsPoll=F");
                }
                this.mNeedsPoll = false;
                this.mLastRelevantPoll = null;
                handlePollCalls((AsyncResult) msg.obj);
            }
        } else if (i == 2 || i == 3) {
            if (this.mPhone.isSRVCC()) {
                this.mPhone.setPeningSRVCC(true);
            } else {
                pollCallsWhenSafe();
            }
        } else if (i == 4) {
            operationComplete();
        } else if (i == 5) {
            String vendorCause = null;
            AsyncResult ar = (AsyncResult) msg.obj;
            operationComplete();
            if (ar.exception == null) {
                LastCallFailCause failCause = (LastCallFailCause) ar.result;
                causeCode = failCause.causeCode;
                vendorCause = failCause.vendorCause;
            } else if (ar.exception instanceof CommandException) {
                CommandException commandException = (CommandException) ar.exception;
                int i2 = AnonymousClass3.$SwitchMap$com$android$internal$telephony$CommandException$Error[commandException.getCommandError().ordinal()];
                if (i2 == 1 || i2 == 2 || i2 == 3 || i2 == 4) {
                    vendorCause = commandException.getCommandError().toString();
                    causeCode2 = 65535;
                } else {
                    causeCode2 = 16;
                }
                causeCode = causeCode2;
            } else {
                causeCode = 16;
                Rlog.i(LOG_TAG, "Exception during getLastCallFailCause, assuming normal disconnect");
            }
            if (causeCode == 34 || causeCode == 41 || causeCode == 42 || causeCode == 44 || causeCode == 49 || causeCode == 58 || causeCode == 65535) {
                CellLocation loc = this.mPhone.getCellLocation();
                int cid = -1;
                if (loc != null) {
                    if (loc instanceof GsmCellLocation) {
                        cid = ((GsmCellLocation) loc).getCid();
                    } else if (loc instanceof CdmaCellLocation) {
                        cid = ((CdmaCellLocation) loc).getBaseStationId();
                    }
                }
                EventLog.writeEvent((int) EventLogTags.CALL_DROP, Integer.valueOf(causeCode), Integer.valueOf(cid), Integer.valueOf(TelephonyManager.getDefault().getNetworkType()));
            }
            int s = this.mDroppedDuringPoll.size();
            for (int i3 = 0; i3 < s; i3++) {
                this.mDroppedDuringPoll.get(i3).onRemoteDisconnect(causeCode, vendorCause);
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), this.mDroppedDuringPoll, getNetworkCountryIso());
            this.mDroppedDuringPoll.clear();
        } else if (i != 20) {
            if (i != 900) {
                switch (i) {
                    case 8:
                    case 12:
                    case 13:
                        break;
                    case 9:
                        handleRadioAvailable();
                        return;
                    case 10:
                        handleRadioNotAvailable();
                        return;
                    case 11:
                        if (!(!isPhoneTypeGsm() || ((AsyncResult) msg.obj).exception == null || (connection = this.mForegroundCall.getLatestConnection()) == null)) {
                            connection.onConferenceMergeFailed();
                            break;
                        }
                    case 14:
                        if (!isPhoneTypeGsm()) {
                            if (this.mPendingCallInEcm) {
                                this.mCi.dial(this.mPendingMO.getAddress(), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), this.mPendingCallClirMode, obtainCompleteMessage());
                                this.mPendingCallInEcm = false;
                            }
                            this.mPhone.unsetOnEcbModeExitResponse(this);
                            return;
                        }
                        throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                    case 15:
                        if (!isPhoneTypeGsm()) {
                            AsyncResult ar2 = (AsyncResult) msg.obj;
                            if (ar2.exception == null) {
                                handleCallWaitingInfo((CdmaCallWaitingNotification) ar2.result);
                                Rlog.d(LOG_TAG, "Event EVENT_CALL_WAITING_INFO_CDMA Received");
                                return;
                            }
                            return;
                        }
                        throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                    case 16:
                        if (isPhoneTypeGsm()) {
                            throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                        } else if (((AsyncResult) msg.obj).exception == null) {
                            this.mPendingMO.onConnectedInOrOut();
                            this.mPendingMO = null;
                            return;
                        } else {
                            return;
                        }
                    default:
                        throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                }
                if (isPhoneTypeGsm()) {
                    if (((AsyncResult) msg.obj).exception != null) {
                        this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                    }
                    operationComplete();
                } else if (msg.what != 8) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                }
            } else {
                Rlog.d(LOG_TAG, "acceptCall: for test card...OK");
                try {
                    acceptCall();
                } catch (Exception e) {
                }
            }
        } else if (isPhoneTypeGsm()) {
            throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
        } else if (((AsyncResult) msg.obj).exception == null) {
            postDelayed(new Runnable() {
                /* class com.android.internal.telephony.GsmCdmaCallTracker.AnonymousClass2 */

                public void run() {
                    if (GsmCdmaCallTracker.this.mPendingMO != null) {
                        GsmCdmaCallTracker.this.mCi.sendCDMAFeatureCode(GsmCdmaCallTracker.this.mPendingMO.getAddress(), GsmCdmaCallTracker.this.obtainMessage(16));
                    }
                }
            }, (long) this.m3WayCallFlashDelay);
        } else {
            this.mPendingMO = null;
            Rlog.w(LOG_TAG, "exception happened on Blank Flash for 3-way call");
        }
    }

    /* renamed from: com.android.internal.telephony.GsmCdmaCallTracker$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$CommandException$Error = new int[CommandException.Error.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.RADIO_NOT_AVAILABLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.NO_MEMORY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.INTERNAL_ERR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.NO_RESOURCES.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public void dispatchCsCallRadioTech(int vrat) {
        GsmCdmaConnection[] gsmCdmaConnectionArr = this.mConnections;
        if (gsmCdmaConnectionArr == null) {
            log("dispatchCsCallRadioTech: mConnections is null");
            return;
        }
        for (GsmCdmaConnection gsmCdmaConnection : gsmCdmaConnectionArr) {
            if (gsmCdmaConnection != null) {
                gsmCdmaConnection.setCallRadioTech(vrat);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkAndEnableDataCallAfterEmergencyCallDropped() {
        if (this.mIsInEmergencyCall) {
            this.mIsInEmergencyCall = false;
            boolean inEcm = this.mPhone.isInEcm();
            log("checkAndEnableDataCallAfterEmergencyCallDropped,inEcm=" + inEcm);
            if (!inEcm) {
                this.mPhone.getDataEnabledSettings().setInternalDataEnabled(true);
                this.mPhone.notifyEmergencyCallRegistrants(false);
            }
            this.mPhone.sendEmergencyCallStateChange(false);
        }
    }

    /* access modifiers changed from: protected */
    public Connection checkMtFindNewRinging(DriverCall dc, int i) {
        if (this.mConnections[i].getCall() == this.mRingingCall) {
            Connection newRinging = this.mConnections[i];
            log("Notify new ring " + dc);
            return newRinging;
        }
        Rlog.e(LOG_TAG, "Phantom call appeared " + dc);
        if (dc.state == DriverCall.State.ALERTING || dc.state == DriverCall.State.DIALING) {
            return null;
        }
        this.mConnections[i].onConnectedInOrOut();
        if (dc.state != DriverCall.State.HOLDING) {
            return null;
        }
        this.mConnections[i].onStartedHolding();
        return null;
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    public boolean isInOtaspCall() {
        GsmCdmaConnection gsmCdmaConnection = this.mPendingMO;
        return (gsmCdmaConnection != null && gsmCdmaConnection.isOtaspCall()) || this.mForegroundCall.getConnections().stream().filter($$Lambda$GsmCdmaCallTracker$wkXwCyVPcnlqyXzSJdP2cQlpZxg.INSTANCE).count() > 0;
    }

    static /* synthetic */ boolean lambda$isInOtaspCall$0(Connection connection) {
        return (connection instanceof GsmCdmaConnection) && ((GsmCdmaConnection) connection).isOtaspCall();
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isPhoneTypeGsm() {
        return this.mPhone.getPhoneType() == 1;
    }

    @Override // com.android.internal.telephony.CallTracker
    @UnsupportedAppUsage
    public GsmCdmaPhone getPhone() {
        return this.mPhone;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.CallTracker
    @UnsupportedAppUsage
    public void log(String msg) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    @Override // com.android.internal.telephony.CallTracker
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("GsmCdmaCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println("mConnections: length=" + this.mConnections.length);
        for (int i = 0; i < this.mConnections.length; i++) {
            pw.printf("  mConnections[%d]=%s\n", Integer.valueOf(i), this.mConnections[i]);
        }
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        if (!isPhoneTypeGsm()) {
            pw.println(" mCallWaitingRegistrants=" + this.mCallWaitingRegistrants);
        }
        pw.println(" mDroppedDuringPoll: size=" + this.mDroppedDuringPoll.size());
        for (int i2 = 0; i2 < this.mDroppedDuringPoll.size(); i2++) {
            pw.printf("  mDroppedDuringPoll[%d]=%s\n", Integer.valueOf(i2), this.mDroppedDuringPoll.get(i2));
        }
        pw.println(" mRingingCall=" + this.mRingingCall);
        pw.println(" mForegroundCall=" + this.mForegroundCall);
        pw.println(" mBackgroundCall=" + this.mBackgroundCall);
        pw.println(" mPendingMO=" + this.mPendingMO);
        pw.println(" mHangupPendingMO=" + this.mHangupPendingMO);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDesiredMute=" + this.mDesiredMute);
        pw.println(" mState=" + this.mState);
        if (!isPhoneTypeGsm()) {
            pw.println(" mPendingCallInEcm=" + this.mPendingCallInEcm);
            pw.println(" mIsInEmergencyCall=" + this.mIsInEmergencyCall);
            pw.println(" mPendingCallClirMode=" + this.mPendingCallClirMode);
            pw.println(" mIsEcmTimerCanceled=" + this.mIsEcmTimerCanceled);
        }
    }

    @Override // com.android.internal.telephony.CallTracker
    public PhoneConstants.State getState() {
        return this.mState;
    }

    public int getMaxConnectionsPerCall() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return 5;
        }
        return 1;
    }

    private String getNetworkCountryIso() {
        ServiceStateTracker sst;
        LocaleTracker lt;
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        if (gsmCdmaPhone == null || (sst = gsmCdmaPhone.getServiceStateTracker()) == null || (lt = sst.getLocaleTracker()) == null) {
            return PhoneConfigurationManager.SSSS;
        }
        return lt.getCurrentCountry();
    }

    @Override // com.android.internal.telephony.CallTracker
    public void cleanupCalls() {
        pollCallsWhenSafe();
    }

    public boolean isOemInEcm() {
        return this.mPendingCallInEcm;
    }

    public boolean isOemInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    @Override // com.android.internal.telephony.CallTracker
    public void oemClearConn() {
        if (this.mState != PhoneConstants.State.IDLE) {
            try {
                internalClearDisconnected();
                GsmCdmaConnection[] gsmCdmaConnectionArr = this.mConnections;
                for (GsmCdmaConnection gsmCdmaConnection : gsmCdmaConnectionArr) {
                    if (gsmCdmaConnection != null) {
                        gsmCdmaConnection.dispose();
                    }
                }
                if (this.mPendingMO != null) {
                    this.mPendingMO.dispose();
                }
            } catch (Exception e) {
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mConnections = new GsmCdmaConnection[19];
            } else {
                this.mConnections = new GsmCdmaConnection[8];
            }
            this.mPendingMO = null;
            this.mHangupPendingMO = false;
            this.mState = PhoneConstants.State.IDLE;
            Rlog.e(LOG_TAG, "Phantom ims call appeared");
        }
    }
}
