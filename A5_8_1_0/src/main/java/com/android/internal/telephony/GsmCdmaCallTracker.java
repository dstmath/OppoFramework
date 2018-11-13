package com.android.internal.telephony;

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
import android.os.SystemClock;
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
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.SpnOverride;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GsmCdmaCallTracker extends CallTracker {
    private static final int EVENT_ACCEPT_COMPLETE = 103;
    private static final int EVENT_CDMA_INFO_REC = 105;
    private static final int EVENT_DIAL_COMPLETE = 101;
    private static final int EVENT_HANGUP_COMPLETE = 102;
    private static final int EVENT_OPPO_END_INCOMING_CALL = 100;
    private static final String LOG_TAG = "GsmCdmaCallTracker";
    private static final int MAX_CONNECTIONS_CDMA = 8;
    public static final int MAX_CONNECTIONS_GSM = 19;
    private static final int MAX_CONNECTIONS_PER_CALL_CDMA = 1;
    private static final int MAX_CONNECTIONS_PER_CALL_GSM = 5;
    private static final boolean REPEAT_POLLING = false;
    private static final int TIME_END_INCOMING_CALL_DELAY = 65000;
    private static final int TIME_SLOT_IGNORE_CDMA_CW = 6500;
    private static final boolean VDBG = false;
    private boolean isOemSwitchAccept = false;
    private int m3WayCallFlashDelay;
    public GsmCdmaCall mBackgroundCall = new GsmCdmaCall(this);
    private RegistrantList mCallWaitingRegistrants = new RegistrantList();
    public GsmCdmaConnection[] mConnections;
    private boolean mDesiredMute = false;
    private ArrayList<GsmCdmaConnection> mDroppedDuringPoll = new ArrayList(19);
    private BroadcastReceiver mEcmExitReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                boolean isInEcm = intent.getBooleanExtra("phoneinECMState", false);
                GsmCdmaCallTracker.this.log("Received ACTION_EMERGENCY_CALLBACK_MODE_CHANGED isInEcm = " + isInEcm);
                if (!isInEcm) {
                    List<Connection> toNotify = new ArrayList();
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
    public GsmCdmaCall mForegroundCall = new GsmCdmaCall(this);
    private GsmCdmaCall mHangupCall = null;
    private GsmCdmaConnection mHangupConn = null;
    private boolean mHangupPendingMO;
    private CallTracker mImsPhoneCallTracker = null;
    private boolean mIsEcmTimerCanceled;
    private boolean mIsInEmergencyCall;
    private boolean mIsOemSwitcMO = false;
    private boolean mIsPeningAccept = false;
    private boolean mIsPeningSwitch = false;
    private String mLastCdmaCWAddr = null;
    private long mLastCdmaCWTime = 0;
    private TelephonyMetrics mMetrics = TelephonyMetrics.getInstance();
    private int mOemLastMsg = -1;
    private int mPendingCallClirMode;
    private boolean mPendingCallInEcm;
    private GsmCdmaConnection mPendingMO;
    private GsmCdmaPhone mPhone;
    public GsmCdmaCall mRingingCall = new GsmCdmaCall(this);
    GsmCdmaConnection mRingingConnection = null;
    public State mState = State.IDLE;
    private RegistrantList mVoiceCallEndedRegistrants = new RegistrantList();
    private RegistrantList mVoiceCallStartedRegistrants = new RegistrantList();

    public GsmCdmaCallTracker(GsmCdmaPhone phone) {
        this.mPhone = phone;
        this.mCi = phone.mCi;
        this.mCi.registerForCallStateChanged(this, 2, null);
        this.mCi.registerForOn(this, 9, null);
        this.mCi.registerForNotAvailable(this, 10, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mEcmExitReceiver, filter);
        this.mCi.registerForLineControlInfo(this, EVENT_CDMA_INFO_REC, null);
        updatePhoneType(true);
    }

    public void updatePhoneType() {
        updatePhoneType(false);
    }

    private void updatePhoneType(boolean duringInit) {
        if (!duringInit) {
            reset();
            pollCallsWhenSafe();
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            this.mConnections = new GsmCdmaConnection[19];
            this.mCi.unregisterForCallWaitingInfo(this);
            if (this.mIsInEmergencyCall) {
                this.mPhone.mDcTracker.setInternalDataEnabled(true);
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

    private void reset() {
        Rlog.d(LOG_TAG, "reset");
        for (GsmCdmaConnection gsmCdmaConnection : this.mConnections) {
            if (gsmCdmaConnection != null) {
                gsmCdmaConnection.onDisconnect(36);
                gsmCdmaConnection.dispose();
            }
        }
        if (this.mPendingMO != null) {
            this.mPendingMO.dispose();
        }
        this.mConnections = null;
        this.mPendingMO = null;
        clearDisconnected();
    }

    protected void finalize() {
        Rlog.d(LOG_TAG, "GsmCdmaCallTracker finalized");
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceCallStartedRegistrants.add(r);
        if (this.mState != State.IDLE) {
            r.notifyRegistrant(new AsyncResult(null, null, null));
        }
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCallWaitingRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallWaiting(Handler h) {
        this.mCallWaitingRegistrants.remove(h);
    }

    private void fakeHoldForegroundBeforeDial() {
        List<Connection> connCopy = (List) this.mForegroundCall.mConnections.clone();
        int s = connCopy.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) connCopy.get(i)).fakeHoldBeforeDial();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00bb A:{Catch:{ InterruptedException -> 0x006f }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Connection dial(String dialString, int clirMode, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        clearDisconnected();
        if (this.mHangupConn != null) {
            throw new CallStateException("cannot dial in hangup state");
        } else if (canDial()) {
            String origNumber = dialString;
            dialString = convertNumberIfNecessary(this.mPhone, dialString);
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                this.mIsOemSwitcMO = true;
                switchWaitingOrHoldingAndActive();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                fakeHoldForegroundBeforeDial();
            }
            if (this.mForegroundCall.getState() != Call.State.IDLE) {
                throw new CallStateException("cannot dial in current state");
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString));
            this.mHangupPendingMO = false;
            this.mMetrics.writeRilDial(this.mPhone.getPhoneId(), this.mPendingMO, clirMode, uusInfo);
            if (!(this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0)) {
                if (this.mPendingMO.getAddress().indexOf(78) < 0) {
                    setMute(false);
                    this.mCi.dial(this.mPendingMO.getAddress(), clirMode, uusInfo, obtainCompleteMessage(101));
                    if (this.mNumberConverted) {
                        this.mPendingMO.setConverted(origNumber);
                        this.mNumberConverted = false;
                    }
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                }
            }
            this.mPendingMO.mCause = 7;
            pollCallsWhenSafe();
            if (this.mNumberConverted) {
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            if (this.mPendingOperations > 0) {
                this.mPendingOperations--;
                pollCallsWhenSafe();
                if (DBG_POLL) {
                    Rlog.w(LOG_TAG, "pollCallsWhenSafe one more time");
                }
            }
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case 0:
                this.mIsEcmTimerCanceled = false;
                return;
            case 1:
                this.mIsEcmTimerCanceled = true;
                return;
            default:
                Rlog.e(LOG_TAG, "handleEcmTimer, unsupported action " + action);
                return;
        }
    }

    private void disableDataCallInEmergencyCall(String dialString) {
        if (PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString)) {
            log("disableDataCallInEmergencyCall");
            setIsInEmergencyCall();
        }
    }

    public void setIsInEmergencyCall() {
        this.mIsInEmergencyCall = true;
        this.mPhone.mDcTracker.setInternalDataEnabled(false);
        this.mPhone.notifyEmergencyCallRegistrants(true);
        this.mPhone.sendEmergencyCallStateChange(true);
    }

    private Connection dial(String dialString, int clirMode) throws CallStateException {
        clearDisconnected();
        if (this.mHangupConn != null) {
            throw new CallStateException("cannot dial in hangup state");
        } else if (canDial()) {
            int internationalRoaming;
            TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
            String origNumber = dialString;
            String operatorIsoContry = tm.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
            String simIsoContry = tm.getSimCountryIsoForPhone(this.mPhone.getPhoneId());
            if (TextUtils.isEmpty(operatorIsoContry) || (TextUtils.isEmpty(simIsoContry) ^ 1) == 0) {
                internationalRoaming = 0;
            } else {
                internationalRoaming = simIsoContry.equals(operatorIsoContry) ^ 1;
            }
            if (internationalRoaming != 0) {
                if ("us".equals(simIsoContry)) {
                    internationalRoaming = internationalRoaming != 0 ? "vi".equals(operatorIsoContry) ^ 1 : 0;
                } else if ("vi".equals(simIsoContry)) {
                    internationalRoaming = internationalRoaming != 0 ? "us".equals(operatorIsoContry) ^ 1 : 0;
                }
            }
            if (internationalRoaming != 0) {
                dialString = convertNumberIfNecessary(this.mPhone, dialString);
            }
            boolean isPhoneInEcmMode = this.mPhone.isInEcm();
            boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
            if (isPhoneInEcmMode && isEmergencyCall) {
                handleEcmTimer(1);
            }
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                return dialThreeWay(dialString);
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyCall);
            this.mHangupPendingMO = false;
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
            } else {
                setMute(false);
                disableDataCallInEmergencyCall(dialString);
                if (isPhoneInEcmMode) {
                    this.mPhone.exitEmergencyCallbackMode();
                    this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                    this.mPendingCallClirMode = clirMode;
                    this.mPendingCallInEcm = true;
                } else {
                    this.mCi.dial(this.mPendingMO.getAddress(), clirMode, obtainCompleteMessage(101));
                }
            }
            if (this.mNumberConverted) {
                this.mPendingMO.setConverted(origNumber);
                this.mNumberConverted = false;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return this.mPendingMO;
        } else {
            throw new CallStateException("cannot dial in current state");
        }
    }

    private Connection dialThreeWay(String dialString) {
        if (this.mForegroundCall.isIdle()) {
            return null;
        }
        disableDataCallInEmergencyCall(dialString);
        this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, this.mIsInEmergencyCall);
        PersistableBundle bundle = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfig();
        if (bundle != null) {
            this.m3WayCallFlashDelay = bundle.getInt("cdma_3waycall_flash_delay_int");
        } else {
            this.m3WayCallFlashDelay = 0;
        }
        if (this.m3WayCallFlashDelay > 0) {
            this.mCi.sendCDMAFeatureCode(SpnOverride.MVNO_TYPE_NONE, obtainMessage(20));
        } else {
            this.mCi.sendCDMAFeatureCode(this.mPendingMO.getAddress(), obtainMessage(16));
        }
        return this.mPendingMO;
    }

    public Connection dial(String dialString) throws CallStateException {
        if (isPhoneTypeGsm()) {
            return dial(dialString, 0, null);
        }
        return dial(dialString, 0);
    }

    public Connection dial(String dialString, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        return dial(dialString, 0, uusInfo, intentExtras);
    }

    private Connection dial(String dialString, int clirMode, Bundle intentExtras) throws CallStateException {
        return dial(dialString, clirMode, null, intentExtras);
    }

    public void acceptCall() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            Rlog.i("phone", "acceptCall: incoming...");
            setMute(false);
            this.mIsPeningAccept = true;
            this.mCi.acceptCall(obtainCompleteMessage(EVENT_ACCEPT_COMPLETE));
        } else if (this.mRingingCall.getState() != Call.State.WAITING) {
            throw new CallStateException("phone not ringing");
        } else if (this.mHangupConn == null) {
            this.isOemSwitchAccept = true;
            this.mOemLastMsg = -1;
            if (DBG_POLL) {
                log("accept the call now1!");
            }
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
            this.mIsPeningSwitch = true;
        }
    }

    public void rejectCall() throws CallStateException {
        if (this.mRingingCall.getState().isRinging()) {
            this.mCi.rejectCall(obtainCompleteMessage());
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    private void flashAndSetGenericTrue() {
        this.mCi.sendCDMAFeatureCode(SpnOverride.MVNO_TYPE_NONE, obtainMessage(8));
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (isPhoneTypeGsm()) {
            this.mCi.switchWaitingOrHoldingAndActive(obtainCompleteMessage(8));
        } else if (this.mForegroundCall.getConnections().size() > 1) {
            flashAndSetGenericTrue();
        } else {
            this.mCi.sendCDMAFeatureCode(SpnOverride.MVNO_TYPE_NONE, obtainMessage(8));
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

    public void clearDisconnected() {
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        if (this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING && (this.mBackgroundCall.isFull() ^ 1) != 0) {
            return this.mForegroundCall.isFull() ^ 1;
        }
        return false;
    }

    private boolean canDial() {
        boolean z = true;
        int serviceState = this.mPhone.getServiceState().getState();
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        boolean ret = (serviceState == 3 || this.mPendingMO != null || (this.mRingingCall.isRinging() ^ 1) == 0 || (disableCall.equals("true") ^ 1) == 0) ? false : (this.mForegroundCall.getState().isAlive() && (this.mBackgroundCall.getState().isAlive() ^ 1) == 0) ? !isPhoneTypeGsm() ? this.mForegroundCall.getState() == Call.State.ACTIVE : false : true;
        ret = ret ? this.mPhone.isSRVCC() ^ 1 : false;
        if (!ret) {
            boolean z2;
            String str = "canDial is false\n((serviceState=%d) != ServiceState.STATE_POWER_OFF)::=%s\n&& pendingMO == null::=%s\n&& !ringingCall.isRinging()::=%s\n&& !disableCall.equals(\"true\")::=%s\n&& (!foregroundCall.getState().isAlive()::=%s\n   || foregroundCall.getState() == GsmCdmaCall.State.ACTIVE::=%s\n   ||!backgroundCall.getState().isAlive())::=%s)";
            Object[] objArr = new Object[8];
            objArr[0] = Integer.valueOf(serviceState);
            if (serviceState != 3) {
                z2 = true;
            } else {
                z2 = false;
            }
            objArr[1] = Boolean.valueOf(z2);
            if (this.mPendingMO == null) {
                z2 = true;
            } else {
                z2 = false;
            }
            objArr[2] = Boolean.valueOf(z2);
            objArr[3] = Boolean.valueOf(this.mRingingCall.isRinging() ^ 1);
            objArr[4] = Boolean.valueOf(disableCall.equals("true") ^ 1);
            objArr[5] = Boolean.valueOf(this.mForegroundCall.getState().isAlive() ^ 1);
            if (this.mForegroundCall.getState() != Call.State.ACTIVE) {
                z = false;
            }
            objArr[6] = Boolean.valueOf(z);
            objArr[7] = Boolean.valueOf(this.mBackgroundCall.getState().isAlive() ^ 1);
            log(String.format(str, objArr));
            log("!mPhone.isSRVCC() = " + (this.mPhone.isSRVCC() ^ 1));
        }
        return ret;
    }

    public boolean canTransfer() {
        boolean z = false;
        if (isPhoneTypeGsm()) {
            if ((this.mForegroundCall.getState() == Call.State.ACTIVE || this.mForegroundCall.getState() == Call.State.ALERTING || this.mForegroundCall.getState() == Call.State.DIALING) && this.mBackgroundCall.getState() == Call.State.HOLDING) {
                z = true;
            }
            return z;
        }
        Rlog.e(LOG_TAG, "canTransfer: not possible in CDMA");
        return false;
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
    }

    private Message obtainCompleteMessage() {
        return obtainCompleteMessage(4);
    }

    private Message obtainCompleteMessage(int what) {
        this.mPendingOperations++;
        this.mLastRelevantPoll = null;
        this.mNeedsPoll = true;
        if (DBG_POLL) {
            log("obtainCompleteMessage: pendingOperations=" + this.mPendingOperations + ", needsPoll=" + this.mNeedsPoll);
        }
        return obtainMessage(what);
    }

    private void operationComplete() {
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

    private void updatePhoneState() {
        State oldState = this.mState;
        if (this.mRingingCall.isRinging()) {
            this.mState = State.RINGING;
        } else {
            if (this.mPendingMO == null) {
                int isIdle;
                if (this.mForegroundCall.isIdle()) {
                    isIdle = this.mBackgroundCall.isIdle();
                } else {
                    isIdle = 0;
                }
                if ((isIdle ^ 1) == 0) {
                    Phone imsPhone = this.mPhone.getImsPhone();
                    if (this.mState == State.OFFHOOK && imsPhone != null) {
                        imsPhone.callEndCleanupHandOverCallIfAny();
                    }
                    this.mState = State.IDLE;
                }
            }
            this.mState = State.OFFHOOK;
        }
        if (this.mState == State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else if (oldState == State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        log("update phone state, old=" + oldState + " new=" + this.mState);
        if (this.mState != oldState) {
            OemConstant.setOemVoocState(oldState, this.mState);
            oemNofiyNoService(this.mPhone, this.mState == State.IDLE);
            if (!isPhoneTypeGsm() && this.mState == State.IDLE) {
                this.isOemSwitchAccept = false;
                this.mOemLastMsg = -1;
            }
            this.mPhone.notifyPhoneStateChanged();
            this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
            if (this.mState == State.IDLE) {
                checkAndEnableDataCallAfterEmergencyCallDropped();
                this.mHangupConn = null;
                this.mHangupCall = null;
                if (this.mConnections != null) {
                    for (int i = 0; i < this.mConnections.length; i++) {
                        if (this.mConnections[i] != null) {
                            this.mConnections[i].onDisconnect(3);
                            this.mConnections[i] = null;
                        }
                    }
                }
            }
        } else if (this.mState == State.IDLE) {
            if (this.mPendingMO != null) {
                log("update phone state, clear pending mo");
                this.mPendingMO.onDisconnect(3);
                this.mPendingMO = null;
                if (this.mPendingOperations > 0) {
                    this.mPendingOperations--;
                }
            }
            if (this.mHangupConn != null) {
                log("update phone state, clear pending hangup");
                this.mHangupConn.onDisconnect(3);
                this.mHangupConn = null;
                this.mHangupCall = null;
                if (this.mPendingOperations > 0) {
                    this.mPendingOperations--;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:322:0x0bd5, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void handlePollCalls(AsyncResult ar) {
        List polledCalls;
        Connection hoConnection;
        Iterator<Connection> it;
        Connection c;
        if (ar.exception == null) {
            polledCalls = ar.result;
        } else {
            if (isCommandExceptionRadioNotAvailable(ar.exception)) {
                polledCalls = new ArrayList();
            } else {
                pollCallsAfterDelay();
                return;
            }
        }
        Connection newRinging = null;
        ArrayList<Connection> newUnknownConnectionsGsm = new ArrayList();
        Connection newUnknownConnectionCdma = null;
        boolean hasNonHangupStateChanged = false;
        int hasAnyCallDisconnected = 0;
        boolean unknownConnectionAppeared = false;
        int handoverConnectionsSize = this.mHandoverConnections.size();
        boolean noConnectionExists = true;
        boolean isSrvccCase = false;
        int i = 0;
        int curDC = 0;
        int dcSize = polledCalls.size();
        while (i < this.mConnections.length) {
            GsmCdmaConnection conn = this.mConnections[i];
            DriverCall dc = null;
            if (curDC < dcSize) {
                dc = (DriverCall) polledCalls.get(curDC);
                if (dc.index == i + 1) {
                    curDC++;
                } else {
                    dc = null;
                }
            }
            if (!(conn == null && dc == null)) {
                noConnectionExists = false;
            }
            if (DBG_POLL) {
                log("poll: conn[i=" + i + "]=" + conn + ", dc=" + dc);
            }
            if (conn == null && dc != null) {
                if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc)) {
                    log("pendingMo=" + this.mPendingMO + ", dc=" + dc);
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    hoConnection = getHoConnection(dc);
                    if (hoConnection != null) {
                        this.mImsPhoneCallTracker = null;
                        if (this.mPhone.getImsPhone() != null) {
                            this.mImsPhoneCallTracker = this.mPhone.getImsPhone().getCallTracker();
                        }
                        isSrvccCase = true;
                        this.mConnections[i].migrateFrom(hoConnection);
                        if (!(hoConnection.mPreHandoverState == Call.State.ACTIVE || hoConnection.mPreHandoverState == Call.State.HOLDING || dc.state != DriverCall.State.ACTIVE)) {
                            this.mConnections[i].onConnectedInOrOut();
                        }
                        this.mHandoverConnections.remove(hoConnection);
                        if (this.mImsPhoneCallTracker != null && this.mImsPhoneCallTracker.isImsCallHangupPending()) {
                            log("There is pending hangup before SRVCC " + this.mImsPhoneCallTracker.getPendingHangupAddr());
                            String HOConnectionAddr = this.mConnections[i].getAddress();
                            if (HOConnectionAddr != null && HOConnectionAddr.equals(this.mImsPhoneCallTracker.getPendingHangupAddr())) {
                                try {
                                    log("Pending hang up in SRVCC Case");
                                    hangup(this.mConnections[i]);
                                } catch (CallStateException e) {
                                    Rlog.e(LOG_TAG, "unexpected error on hangup of SRVCC Case");
                                }
                            }
                        }
                        if (isPhoneTypeGsm()) {
                            it = this.mHandoverConnections.iterator();
                            while (it.hasNext()) {
                                c = (Connection) it.next();
                                Rlog.i(LOG_TAG, "HO Conn state is " + c.mPreHandoverState);
                                if (c.mPreHandoverState == this.mConnections[i].getState()) {
                                    Rlog.i(LOG_TAG, "Removing HO conn " + hoConnection + c.mPreHandoverState);
                                    it.remove();
                                }
                            }
                        }
                        this.mPhone.oemMigrateFrom();
                        if (!this.mPhone.hasHoRegistrants() || (this.mConnections[i].isConferenceHost() ^ 1) == 0) {
                            unknownConnectionAppeared = true;
                            if (isPhoneTypeGsm()) {
                                newUnknownConnectionsGsm.add(this.mConnections[i]);
                            } else {
                                newUnknownConnectionCdma = this.mConnections[i];
                            }
                        } else {
                            this.mPhone.notifyHandoverStateChanged(this.mConnections[i]);
                            isSrvccCase = false;
                        }
                    } else {
                        if ((DriverCall.State.INCOMING == dc.state || DriverCall.State.WAITING == dc.state) && !OemConstant.isCallInEnable(this.mPhone)) {
                            log("ctmm vi block");
                            try {
                                hangup(this.mConnections[i]);
                            } catch (CallStateException e2) {
                                log("Exception in hangup call");
                            }
                        }
                        newRinging = checkMtFindNewRinging(dc, i);
                        if (newRinging == null) {
                            unknownConnectionAppeared = true;
                            if (isPhoneTypeGsm()) {
                                newUnknownConnectionsGsm.add(this.mConnections[i]);
                            } else {
                                newUnknownConnectionCdma = this.mConnections[i];
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
                            if (this.mHangupConn == null || this.mHangupConn.mIndex != this.mConnections[i].mIndex) {
                                hangup(this.mConnections[i]);
                            }
                        } catch (CallStateException e3) {
                            Rlog.e(LOG_TAG, "unexpected error on hangup");
                        }
                    }
                }
                hasNonHangupStateChanged = true;
            } else if (conn != null && dc == null) {
                if (isPhoneTypeGsm()) {
                    if (conn.getState() == Call.State.DISCONNECTING) {
                        Rlog.d("leon", "DISCONNECTING hangup " + conn.getCall().colorosGetCause());
                        conn.onDisconnect(conn.getCall().colorosGetCause());
                    }
                    this.mDroppedDuringPoll.add(conn);
                } else {
                    int n;
                    if (conn.getState() == Call.State.HOLDING) {
                        Rlog.d("leon", "HOLDING hangup normal");
                        conn.onDisconnect(2);
                        updatePhoneState();
                        this.mPhone.notifyPreciseCallStateChanged();
                    } else {
                        if (conn.getState() == Call.State.DISCONNECTING) {
                            Rlog.d("leon", "DISCONNECTING hangup");
                            conn.onDisconnect(conn.getCall().colorosGetCause());
                        }
                    }
                    int count = this.mForegroundCall.mConnections.size();
                    for (n = 0; n < count; n++) {
                        log("adding fgCall cn " + n + " to droppedDuringPoll");
                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mForegroundCall.mConnections.get(n));
                    }
                    count = this.mRingingCall.mConnections.size();
                    for (n = 0; n < count; n++) {
                        log("adding rgCall cn " + n + " to droppedDuringPoll");
                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mRingingCall.mConnections.get(n));
                    }
                    count = this.mBackgroundCall.mConnections.size();
                    for (n = 0; n < count; n++) {
                        log("adding bgCall cn " + n + " to droppedDuringPoll");
                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mBackgroundCall.mConnections.get(n));
                    }
                    if (this.mIsEcmTimerCanceled) {
                        handleEcmTimer(0);
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                }
                this.mConnections[i] = null;
            } else if (conn != null && dc != null && (conn.compareTo(dc) ^ 1) != 0 && isPhoneTypeGsm()) {
                this.mDroppedDuringPoll.add(conn);
                this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                if (this.mConnections[i].getCall() == this.mRingingCall) {
                    newRinging = this.mConnections[i];
                }
                hasNonHangupStateChanged = true;
            } else if (!(conn == null || dc == null)) {
                if (isPhoneTypeGsm() || conn.isIncoming() == dc.isMT) {
                    if (DBG_POLL) {
                        log("accept the call,conn:" + conn.getState() + "/dc:" + dc.state);
                    }
                    if (this.isOemSwitchAccept && conn.getState() == Call.State.WAITING) {
                        if (dc.state == DriverCall.State.INCOMING) {
                            this.isOemSwitchAccept = false;
                            this.mOemLastMsg = -1;
                            log("accept the call!");
                            setMute(false);
                            this.mCi.acceptCall(obtainCompleteMessage());
                        }
                    } else {
                        if (conn.getState() == Call.State.DISCONNECTED && dc.state == DriverCall.State.DIALING && (isPhoneTypeGsm() ^ 1) != 0 && (this.mForegroundCall.isMultiparty() ^ 1) != 0 && this.mBackgroundCall.isIdle()) {
                            conn.update(dc);
                            try {
                                hangup(conn);
                                log("hangup the dialing dc!");
                                return;
                            } catch (Exception ex) {
                                log("hangup the dialing dc!" + ex.getMessage());
                                hasNonHangupStateChanged = true;
                            }
                        } else {
                            if (conn.getState() == Call.State.ACTIVE && dcSize == 1 && conn.isConferenceHost()) {
                                log("hangup the lonely conference host!");
                                try {
                                    hangup(conn);
                                } catch (Exception ex2) {
                                    log("hangup the lonly conference host!" + ex2.getMessage());
                                }
                            }
                        }
                    }
                    hasNonHangupStateChanged = !hasNonHangupStateChanged ? conn.update(dc) : true;
                } else if (dc.isMT) {
                    this.mDroppedDuringPoll.add(conn);
                    newRinging = checkMtFindNewRinging(dc, i);
                    if (newRinging == null) {
                        unknownConnectionAppeared = true;
                        newUnknownConnectionCdma = conn;
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                } else {
                    Rlog.e(LOG_TAG, "Error in RIL, Phantom call appeared " + dc);
                }
            }
            i++;
        }
        if (!isPhoneTypeGsm() && noConnectionExists) {
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
            OemConstant.checkVoocState("true");
            this.mPhone.notifyNewRingingConnection(newRinging);
            this.mRingingConnection = (GsmCdmaConnection) newRinging;
            removeMessages(100);
            sendEmptyMessageDelayed(100, 65000);
            if (isOemAutoAnswer(this.mPhone)) {
                Rlog.d(LOG_TAG, "acceptCall: for test card...");
                sendEmptyMessageDelayed(900, (long) (isPhoneTypeGsm() ? 3 : ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT));
            }
        }
        ArrayList<GsmCdmaConnection> locallyDisconnectedConnections = new ArrayList();
        for (i = this.mDroppedDuringPoll.size() - 1; i >= 0; i--) {
            Connection conn2 = (GsmCdmaConnection) this.mDroppedDuringPoll.get(i);
            boolean wasDisconnected = false;
            if (conn2.isIncoming() && conn2.getConnectTime() == 0) {
                int cause;
                if (conn2.mCause == 16) {
                    cause = 16;
                } else if (conn2.mCause == 3) {
                    cause = 16;
                } else {
                    cause = 1;
                }
                log("missed/rejected call, conn.cause=" + conn2.mCause);
                log("setting cause to " + cause);
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn2.onDisconnect(cause);
                if (conn2.isConnectionDisconnect()) {
                    hasAnyCallDisconnected = 1;
                }
                wasDisconnected = true;
                locallyDisconnectedConnections.add(conn2);
            } else if (conn2.mCause == 3 || conn2.mCause == 7) {
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn2.onDisconnect(conn2.mCause);
                if (conn2.isConnectionDisconnect()) {
                    hasAnyCallDisconnected = 1;
                }
                wasDisconnected = true;
                locallyDisconnectedConnections.add(conn2);
            }
            if (!isPhoneTypeGsm() && wasDisconnected && unknownConnectionAppeared && conn2 == newUnknownConnectionCdma) {
                unknownConnectionAppeared = false;
                newUnknownConnectionCdma = null;
            }
        }
        if (locallyDisconnectedConnections.size() > 0) {
            this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), locallyDisconnectedConnections);
        }
        if (this.isOemSwitchAccept) {
            if (this.mOemLastMsg != -1) {
                this.mPhone.notifySuppServiceFailed(getFailedService(this.mOemLastMsg));
            }
            this.isOemSwitchAccept = false;
            this.mOemLastMsg = -1;
        }
        it = this.mHandoverConnections.iterator();
        while (it.hasNext()) {
            hoConnection = (Connection) it.next();
            log("handlePollCalls - disconnect hoConn= " + hoConnection + " hoConn.State= " + hoConnection.getState());
            if (hoConnection.getState().isRinging()) {
                hoConnection.onDisconnect(1);
            } else {
                hoConnection.onDisconnect(-1);
            }
            it.remove();
        }
        if (this.mDroppedDuringPoll.size() > 0) {
            this.mCi.getLastCallFailCause(obtainNoPollCompleteMessage(5));
        }
        if (false) {
            pollCallsAfterDelay();
        }
        if (!(newRinging == null && !hasNonHangupStateChanged && hasAnyCallDisconnected == 0)) {
            internalClearDisconnected();
        }
        updatePhoneState();
        if (unknownConnectionAppeared) {
            if (isPhoneTypeGsm()) {
                if (isSrvccCase) {
                    log("notifyHandoverStateChanged in SRVCC case");
                    if (newUnknownConnectionsGsm.size() > 0) {
                        this.mPhone.notifyHandoverStateChanged((Connection) newUnknownConnectionsGsm.get(0));
                        newUnknownConnectionsGsm.remove(0);
                    }
                }
                for (Connection c2 : newUnknownConnectionsGsm) {
                    if (!isSrvccConfHost(c2)) {
                        log("Notify unknown for " + c2);
                        this.mPhone.notifyUnknownConnection(c2);
                    }
                }
            } else {
                this.mPhone.notifyUnknownConnection(newUnknownConnectionCdma);
            }
        }
        if (!(!hasNonHangupStateChanged && newRinging == null && hasAnyCallDisconnected == 0)) {
            this.mPhone.notifyPreciseCallStateChanged();
            updateMetrics(this.mConnections);
        }
        if (handoverConnectionsSize > 0 && this.mHandoverConnections.size() == 0) {
            Phone imsPhone = this.mPhone.getImsPhone();
            if (imsPhone != null) {
                imsPhone.callEndCleanupHandOverCallIfAny();
            }
        }
    }

    private void updateMetrics(GsmCdmaConnection[] connections) {
        ArrayList<GsmCdmaConnection> activeConnections = new ArrayList();
        for (GsmCdmaConnection conn : connections) {
            if (conn != null) {
                activeConnections.add(conn);
            }
        }
        this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), activeConnections);
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        int i;
        Rlog.i(LOG_TAG, "Phone State:" + this.mState);
        Rlog.i(LOG_TAG, "Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Foreground call: " + this.mForegroundCall.toString());
        l = this.mForegroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Background call: " + this.mBackgroundCall.toString());
        l = this.mBackgroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
    }

    public void hangup(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner != this) {
            throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
        }
        if (conn == this.mPendingMO) {
            if (this.mIsEcmTimerCanceled) {
                handleEcmTimer(0);
            }
            if (isPhoneTypeGsm()) {
                log("oppo.leon.hangup: set hangupPendingMO to true");
                this.mHangupPendingMO = true;
            } else {
                log("oem hangup conn with callId '-1' as there is no DIAL response yet ");
                this.mCi.hangupConnection(-1, obtainCompleteMessage());
                this.mPendingMO.onDisconnect(3);
                this.mPendingMO = null;
            }
        } else if (!isPhoneTypeGsm() && conn.getCall() == this.mRingingCall && this.mRingingCall.getState() == Call.State.WAITING) {
            conn.onDisconnect(16);
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return;
        } else {
            try {
                this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), conn, conn.getGsmCdmaIndex());
                if (this.mHangupConn == null || this.mHangupConn.mIndex != conn.mIndex) {
                    this.mHangupConn = conn;
                    conn.getCall().colorosSetCause(3);
                    if (conn.getState() == Call.State.DIALING && this.mBackgroundCall.getConnections().size() > 0) {
                        if (DBG_POLL) {
                            log("hangup the foreground dialing call");
                        }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                        }
                    }
                    this.mCi.hangupConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage(102));
                    if (!isPhoneTypeGsm() && conn.getState() == Call.State.DIALING) {
                        conn.onDisconnect(3);
                    }
                } else {
                    return;
                }
            } catch (CallStateException e2) {
                Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: hangup() on absent connection " + conn);
                if (isPhoneTypeGsm()) {
                    this.mHangupConn = null;
                } else {
                    this.mCi.hangupConnection(-1, obtainCompleteMessage(102));
                }
            }
        }
        conn.onHangupLocal();
    }

    public void separate(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner != this) {
            throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
        }
        try {
            this.mCi.separateConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage(12));
        } catch (CallStateException e) {
            Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: separate() on absent connection " + conn);
        }
    }

    public void setMute(boolean mute) {
        this.mDesiredMute = mute;
        this.mCi.setMute(this.mDesiredMute, null);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void hangup(GsmCdmaCall call) throws CallStateException {
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections in call");
        }
        if (call == this.mRingingCall) {
            if (this.mIsPeningAccept) {
                this.mHangupCall = call;
                return;
            }
            log("(ringing) hangup waiting or background");
            logHangupEvent(call);
            this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
            call.colorosSetCause(16);
        } else if (call == this.mForegroundCall) {
            if (call.isDialingOrAlerting()) {
                log("(foregnd) hangup dialing or alerting...");
                hangup((GsmCdmaConnection) call.getConnections().get(0));
            } else if (isPhoneTypeGsm() && this.mRingingCall.isRinging()) {
                log("hangup all conns in active/background call, without affecting ringing call");
                hangupAllConnections(call);
            } else if (!this.mForegroundCall.isMultiparty() && this.mBackgroundCall.isIdle() && ((this.mRingingCall.getState() == Call.State.IDLE || this.mRingingCall.getState() == Call.State.WAITING) && call.getConnections().size() == 1)) {
                log("(foregnd) leon hangup waiting/idle call");
                hangup((GsmCdmaConnection) call.getConnections().get(0));
            } else {
                logHangupEvent(call);
                hangupForegroundResumeBackground();
            }
        } else if (call != this.mBackgroundCall) {
            throw new RuntimeException("GsmCdmaCall " + call + "does not belong to GsmCdmaCallTracker " + this);
        } else if (this.mRingingCall.isRinging()) {
            log("hangup all conns in background call");
            hangupAllConnections(call);
        } else {
            hangupWaitingOrBackground();
        }
        call.onHangupLocal();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    private void logHangupEvent(GsmCdmaCall call) {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            int call_index;
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            try {
                call_index = cn.getGsmCdmaIndex();
            } catch (CallStateException e) {
                call_index = -1;
            }
            this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, call_index);
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
        int i = 0;
        while (i < count) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            if (cn.mDisconnected || cn.getGsmCdmaIndex() != index) {
                i++;
            } else {
                this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, cn.getGsmCdmaIndex());
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
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, cn.getGsmCdmaIndex());
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

    private void notifyCallWaitingInfo(CdmaCallWaitingNotification obj) {
        if (this.mCallWaitingRegistrants != null) {
            this.mCallWaitingRegistrants.notifyRegistrants(new AsyncResult(null, obj, null));
        }
    }

    private void handleCallWaitingInfo(CdmaCallWaitingNotification cw) {
        if (cw == null || OemConstant.isCallInEnable(this.mPhone)) {
            String mWaittingCallAddr = cw.number;
            long mWaittingCallTime = SystemClock.elapsedRealtime();
            log("mWaittingCallAddr = " + mWaittingCallAddr + " , mWaittingCallTime = " + mWaittingCallTime + " , mLastCdmaCWTime = " + this.mLastCdmaCWTime);
            if (mWaittingCallAddr == null || !mWaittingCallAddr.equals(this.mLastCdmaCWAddr) || mWaittingCallTime - this.mLastCdmaCWTime >= 6500) {
                this.mLastCdmaCWAddr = mWaittingCallAddr;
                this.mLastCdmaCWTime = mWaittingCallTime;
                GsmCdmaConnection gsmCdmaConnection = new GsmCdmaConnection(this.mPhone.getContext(), cw, this, this.mRingingCall);
                updatePhoneState();
                notifyCallWaitingInfo(cw);
                return;
            }
            this.mLastCdmaCWAddr = mWaittingCallAddr;
            this.mLastCdmaCWTime = mWaittingCallTime;
            log("Ignore duplicate cdma flash info of call waitting");
            return;
        }
        log("oppo.leon handleCallWaitingInfo block the second incoming and hangup it ok!" + cw);
        if (cw.isPresent == 1) {
            this.mCi.hangupWaitingOrBackground(null);
        }
    }

    private SuppService getFailedService(int what) {
        switch (what) {
            case 8:
                return SuppService.SWITCH;
            case 11:
                return SuppService.CONFERENCE;
            case 12:
                return SuppService.SEPARATE;
            case 13:
                return SuppService.TRANSFER;
            default:
                return SuppService.UNKNOWN;
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                Rlog.d(LOG_TAG, "Event EVENT_POLL_CALLS_RESULT Received");
                if (msg == this.mLastRelevantPoll) {
                    if (DBG_POLL) {
                        log("handle EVENT_POLL_CALL_RESULT: set needsPoll=F");
                    }
                    this.mNeedsPoll = false;
                    this.mLastRelevantPoll = null;
                    handlePollCalls((AsyncResult) msg.obj);
                    return;
                }
                return;
            case 2:
            case 3:
                if ((this.mHangupPendingMO || this.mHangupConn != null) && this.mPendingOperations > 0) {
                    if (DBG_POLL) {
                        Rlog.w(LOG_TAG, "Ignore pending hangup request when call status change");
                    }
                    this.mPendingOperations--;
                }
                if (this.mPhone.isSRVCC()) {
                    this.mPhone.setPeningSRVCC(true);
                    return;
                } else {
                    pollCallsWhenSafe();
                    return;
                }
            case 4:
                operationComplete();
                return;
            case 5:
                int causeCode;
                String vendorCause = null;
                ar = (AsyncResult) msg.obj;
                operationComplete();
                if (ar.exception != null) {
                    causeCode = 16;
                    Rlog.i(LOG_TAG, "Exception during getLastCallFailCause, assuming normal disconnect");
                } else {
                    LastCallFailCause failCause = ar.result;
                    causeCode = failCause.causeCode;
                    vendorCause = failCause.vendorCause;
                }
                if (causeCode == 34 || causeCode == 41 || causeCode == 42 || causeCode == 44 || causeCode == 49 || causeCode == 58 || causeCode == 65535) {
                    CellLocation loc = this.mPhone.getCellLocation();
                    int cid = -1;
                    if (loc != null) {
                        if (isPhoneTypeGsm()) {
                            cid = ((GsmCellLocation) loc).getCid();
                        } else {
                            cid = ((CdmaCellLocation) loc).getBaseStationId();
                        }
                    }
                    EventLog.writeEvent(EventLogTags.CALL_DROP, new Object[]{Integer.valueOf(causeCode), Integer.valueOf(cid), Integer.valueOf(TelephonyManager.getDefault().getNetworkType())});
                }
                int s = this.mDroppedDuringPoll.size();
                for (int i = 0; i < s; i++) {
                    ((GsmCdmaConnection) this.mDroppedDuringPoll.get(i)).onRemoteDisconnect(causeCode, vendorCause);
                }
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), this.mDroppedDuringPoll);
                this.mDroppedDuringPoll.clear();
                return;
            case 8:
                if (isPhoneTypeGsm()) {
                    if (((AsyncResult) msg.obj).exception != null) {
                        if (this.mIsOemSwitcMO && this.mPendingMO != null) {
                            this.mHangupPendingMO = true;
                        }
                        if (this.isOemSwitchAccept) {
                            this.mOemLastMsg = msg.what;
                        } else {
                            this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                        }
                    } else {
                        if (DBG_POLL) {
                            log("accept the call 2!");
                        }
                        this.isOemSwitchAccept = false;
                        this.mOemLastMsg = -1;
                    }
                    this.mIsOemSwitcMO = false;
                    operationComplete();
                    return;
                } else if (msg.what != 8) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                } else {
                    return;
                }
            case 9:
                handleRadioAvailable();
                return;
            case 10:
                handleRadioNotAvailable();
                return;
            case 11:
                if (isPhoneTypeGsm() && msg.obj.exception != null) {
                    Connection connection = this.mForegroundCall.getLatestConnection();
                    if (connection != null) {
                        connection.onConferenceMergeFailed();
                        break;
                    }
                }
                break;
            case 12:
            case 13:
                break;
            case 14:
                Rlog.d(LOG_TAG, "Event EVENT_EXIT_ECM_RESPONSE_CDMA Received");
                if (this.mPendingCallInEcm) {
                    this.mCi.dial(this.mPendingMO.getAddress(), this.mPendingCallClirMode, obtainCompleteMessage(101));
                    this.mPendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
                return;
            case 15:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                }
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    handleCallWaitingInfo((CdmaCallWaitingNotification) ar.result);
                    Rlog.d(LOG_TAG, "Event EVENT_CALL_WAITING_INFO_CDMA Received");
                    return;
                }
                return;
            case 16:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                } else if (((AsyncResult) msg.obj).exception == null) {
                    this.mPendingMO.onConnectedInOrOut();
                    this.mPendingMO = null;
                    return;
                } else {
                    return;
                }
            case 20:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                } else if (((AsyncResult) msg.obj).exception == null) {
                    postDelayed(new Runnable() {
                        public void run() {
                            if (GsmCdmaCallTracker.this.mPendingMO != null) {
                                GsmCdmaCallTracker.this.mCi.sendCDMAFeatureCode(GsmCdmaCallTracker.this.mPendingMO.getAddress(), GsmCdmaCallTracker.this.obtainMessage(16));
                            }
                        }
                    }, (long) this.m3WayCallFlashDelay);
                    return;
                } else {
                    this.mPendingMO = null;
                    Rlog.w(LOG_TAG, "exception happened on Blank Flash for 3-way call");
                    return;
                }
            case 100:
                GsmCdmaConnection ringing = (GsmCdmaConnection) this.mRingingCall.getEarliestConnection();
                if (ringing != null) {
                    try {
                        if (ringing == this.mRingingConnection) {
                            hangup(this.mRingingConnection);
                            this.mRingingConnection.onDisconnect(1);
                            log("EVENT_OPPO_END_INCOMING_CALL");
                            return;
                        }
                        return;
                    } catch (Exception e) {
                        return;
                    }
                }
                return;
            case 101:
                ar = (AsyncResult) msg.obj;
                this.mPendingOperations--;
                if (ar.exception != null) {
                    if (!(this.mPendingMO == null || this.mHangupPendingMO)) {
                        int cause = 36;
                        Error error = ((CommandException) ar.exception).getCommandError();
                        if (error == Error.FDN_CHECK_FAILURE) {
                            cause = 21;
                        }
                        if (error == Error.GENERIC_FAILURE || error == Error.FDN_CHECK_FAILURE) {
                            this.mPendingMO.onDisconnect(cause);
                            this.mHangupPendingMO = true;
                        }
                    }
                    this.mPendingOperations++;
                    operationComplete();
                    return;
                }
                return;
            case 102:
                ar = (AsyncResult) msg.obj;
                if ((ar == null || ar.exception == null) && this.mHangupConn != null) {
                    this.mHangupConn.onDisconnect(3);
                }
                if (this.mIsPeningSwitch) {
                    setMute(false);
                    try {
                        switchWaitingOrHoldingAndActive();
                    } catch (Exception e2) {
                    }
                }
                this.mIsPeningSwitch = false;
                this.mHangupConn = null;
                operationComplete();
                return;
            case EVENT_ACCEPT_COMPLETE /*103*/:
                this.mIsPeningAccept = false;
                if (this.mHangupCall != null) {
                    this.mHangupCall.colorosSetCause(16);
                    this.mHangupCall.onHangupLocal();
                    this.mPhone.notifyPreciseCallStateChanged();
                    this.mPendingOperations--;
                    log("(ringing) hangup waiting or background");
                    try {
                        this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
                    } catch (Exception e3) {
                        Rlog.w(LOG_TAG, "GsmCallTracker WARN: (ringing) hangup waiting or background " + this.mHangupCall);
                    } catch (Throwable th) {
                        this.mHangupCall = null;
                    }
                    this.mHangupCall = null;
                    return;
                }
                operationComplete();
                return;
            case EVENT_CDMA_INFO_REC /*105*/:
                Rlog.d(LOG_TAG, "leon EVENT_CDMA_INFO_REC");
                GsmCdmaConnection c = (GsmCdmaConnection) this.mForegroundCall.getLatestConnection();
                if (c != null) {
                    c.setStatusForOutgoingCall(true);
                    return;
                }
                return;
            case 900:
                Rlog.d(LOG_TAG, "acceptCall: for test card...OK");
                try {
                    acceptCall();
                    return;
                } catch (Exception e4) {
                    return;
                }
            default:
                Rlog.d(LOG_TAG, "unexpected event not handled:" + msg.what);
                return;
        }
        if (((AsyncResult) msg.obj).exception != null) {
            this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
        }
        operationComplete();
    }

    private void checkAndEnableDataCallAfterEmergencyCallDropped() {
        if (this.mIsInEmergencyCall) {
            this.mIsInEmergencyCall = false;
            boolean inEcm = this.mPhone.isInEcm();
            log("checkAndEnableDataCallAfterEmergencyCallDropped,inEcm=" + inEcm);
            if (!inEcm) {
                this.mPhone.mDcTracker.setInternalDataEnabled(true);
                this.mPhone.notifyEmergencyCallRegistrants(false);
            }
            this.mPhone.sendEmergencyCallStateChange(false);
        }
    }

    private Connection checkMtFindNewRinging(DriverCall dc, int i) {
        Connection newRinging;
        if (this.mConnections[i].getCall() == this.mRingingCall) {
            newRinging = this.mConnections[i];
            log("Notify new ring " + dc);
            return newRinging;
        }
        Rlog.e(LOG_TAG, "Phantom call appeared " + dc);
        Call call = this.mConnections[i].getCall();
        if (!isPhoneTypeGsm() && call != null && ((call.getState() == Call.State.DISCONNECTED || call.getState() == Call.State.ACTIVE) && dc.state == DriverCall.State.INCOMING)) {
            this.mConnections[i].onDisconnect(2);
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            this.mConnections[i] = null;
            this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
            newRinging = this.mConnections[i];
            Rlog.e(LOG_TAG, "leon replace conn");
            return newRinging;
        } else if (dc.state == DriverCall.State.ALERTING || dc.state == DriverCall.State.DIALING) {
            return null;
        } else {
            this.mConnections[i].onConnectedInOrOut();
            if (dc.state != DriverCall.State.HOLDING) {
                return null;
            }
            this.mConnections[i].onStartedHolding();
            return null;
        }
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    private boolean isPhoneTypeGsm() {
        return this.mPhone.getPhoneType() == 1;
    }

    public GsmCdmaPhone getPhone() {
        return this.mPhone;
    }

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[GsmCdmaCallTracker] " + msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("GsmCdmaCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println("mConnections: length=" + this.mConnections.length);
        for (i = 0; i < this.mConnections.length; i++) {
            pw.printf("  mConnections[%d]=%s\n", new Object[]{Integer.valueOf(i), this.mConnections[i]});
        }
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        if (!isPhoneTypeGsm()) {
            pw.println(" mCallWaitingRegistrants=" + this.mCallWaitingRegistrants);
        }
        pw.println(" mDroppedDuringPoll: size=" + this.mDroppedDuringPoll.size());
        for (i = 0; i < this.mDroppedDuringPoll.size(); i++) {
            pw.printf("  mDroppedDuringPoll[%d]=%s\n", new Object[]{Integer.valueOf(i), this.mDroppedDuringPoll.get(i)});
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

    public State getState() {
        return this.mState;
    }

    public int getMaxConnectionsPerCall() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return 5;
        }
        return 1;
    }

    public void cleanupCalls() {
        pollCallsWhenSafe();
    }

    public boolean isOemInEcm() {
        return this.mPendingCallInEcm;
    }

    public boolean isOemInEmergencyCall() {
        boolean mIsEmergencyCallPending = false;
        boolean mIsEmergencyConn = false;
        if (this.mPendingMO != null) {
            mIsEmergencyCallPending = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), this.mPendingMO.getAddress());
        }
        for (GsmCdmaConnection gsmCdmaConnection : this.mConnections) {
            if (gsmCdmaConnection != null && PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), gsmCdmaConnection.getAddress())) {
                Rlog.e(LOG_TAG, "emergency call conn = " + gsmCdmaConnection);
                mIsEmergencyConn = true;
                break;
            }
        }
        Rlog.e(LOG_TAG, "mIsInEmergencyCall = " + this.mIsInEmergencyCall + " , mIsEmergencyCallPending = " + mIsEmergencyCallPending + " , mIsEmergencyConn = " + mIsEmergencyConn);
        return (this.mIsInEmergencyCall || mIsEmergencyCallPending) ? true : mIsEmergencyConn;
    }

    public void oemClearConn() {
        if (this.mState != State.IDLE) {
            try {
                internalClearDisconnected();
                for (GsmCdmaConnection gsmCdmaConnection : this.mConnections) {
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
            this.mHangupCall = null;
            this.mHangupConn = null;
            this.mHangupPendingMO = false;
            this.mRingingConnection = null;
            this.mState = State.IDLE;
            Rlog.e(LOG_TAG, "Phantom ims call appeared");
        }
    }

    private boolean isSrvccConfHost(Connection c) {
        if (!c.isConferenceHost()) {
            return false;
        }
        Rlog.e(LOG_TAG, "Conference Host from SRVCC");
        return true;
    }
}
