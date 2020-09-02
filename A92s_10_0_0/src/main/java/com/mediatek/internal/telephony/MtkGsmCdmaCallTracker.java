package com.mediatek.internal.telephony;

import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.emergency.EmergencyNumber;
import android.text.TextUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.GsmCdmaCall;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IOppoCallManager;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyDevController;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.mediatek.internal.telephony.cdma.pluscode.PlusCodeProcessor;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.imsphone.MtkImsPhoneConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MtkGsmCdmaCallTracker extends GsmCdmaCallTracker {
    protected static final int EVENT_CALL_ADDITIONAL_INFO = 1006;
    protected static final int EVENT_CDMA_CALL_ACCEPTED = 1004;
    protected static final int EVENT_DIAL_CALL_RESULT = 1002;
    protected static final int EVENT_ECONF_SRVCC_INDICATION = 1005;
    protected static final int EVENT_HANG_UP_RESULT = 1003;
    protected static final int EVENT_INCOMING_CALL_INDICATION = 1000;
    protected static final int EVENT_MTK_BASE = 1000;
    protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 1001;
    private static final int MIN_CONNECTIONS_IN_CDMA_CONFERENCE = 2;
    private static final String PROP_LOG_TAG = "GsmCdmaCallTkr";
    private static int TYPE_CALLIN = 0;
    private static int TYPE_CALLOUT = 1;
    private static int TYPE_PS = 4;
    private static int TYPE_SMSIN = 2;
    private static int TYPE_SMSOUT = 3;
    private int[] mEconfSrvccConnectionIds = null;
    private boolean mHasPendingCheckAndEnableData = false;
    boolean mHasPendingSwapRequest = false;
    private boolean mHasPendingUpdatePhoneType = false;
    public MtkGsmCdmaCallTrackerHelper mHelper;
    protected Connection mImsConfHostConnection = null;
    private ArrayList<Connection> mImsConfParticipants = new ArrayList<>();
    public MtkRIL mMtkCi = null;
    protected IMtkGsmCdmaCallTrackerExt mMtkGsmCdmaCallTrackerExt = null;
    protected boolean mNeedWaitImsEConfSrvcc = false;
    private int mPhoneType = 0;
    TelephonyDevController mTelDevController = TelephonyDevController.getInstance();
    private OpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;
    WaitForHoldToHangup mWaitForHoldToHangupRequest = new WaitForHoldToHangup();
    WaitForHoldToRedial mWaitForHoldToRedialRequest = new WaitForHoldToRedial();

    private boolean hasC2kOverImsModem() {
        TelephonyDevController telephonyDevController = this.mTelDevController;
        if (telephonyDevController == null || telephonyDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasC2kOverImsModem()) {
            return false;
        }
        return true;
    }

    public int getMaxConnections() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return 19;
        }
        return 8;
    }

    class WaitForHoldToRedial {
        private int mClirMode = 0;
        private String mDialString = null;
        private EmergencyNumber mEmergencyNumberInfo;
        private boolean mHasKnownUserIntentEmergency = false;
        private boolean mIsEmergencyCall = false;
        private UUSInfo mUUSInfo = null;
        private boolean mWaitToRedial = false;

        WaitForHoldToRedial() {
            resetToRedial();
        }

        /* access modifiers changed from: package-private */
        public boolean isWaitToRedial() {
            return this.mWaitToRedial;
        }

        /* access modifiers changed from: package-private */
        public void setToRedial() {
            this.mWaitToRedial = true;
        }

        public void setToRedial(String dialSting, boolean isEmergencyCall, EmergencyNumber emergencyNumberInfo, boolean hasKnownUserIntentEmergency, int clir, UUSInfo uusinfo) {
            this.mWaitToRedial = true;
            this.mDialString = dialSting;
            this.mIsEmergencyCall = isEmergencyCall;
            this.mEmergencyNumberInfo = emergencyNumberInfo;
            this.mHasKnownUserIntentEmergency = hasKnownUserIntentEmergency;
            this.mClirMode = clir;
            this.mUUSInfo = uusinfo;
        }

        public void resetToRedial() {
            Rlog.d(MtkGsmCdmaCallTracker.PROP_LOG_TAG, "Reset mWaitForHoldToRedialRequest variables");
            this.mWaitToRedial = false;
            this.mDialString = null;
            this.mClirMode = 0;
            this.mUUSInfo = null;
            this.mIsEmergencyCall = false;
            this.mHasKnownUserIntentEmergency = false;
        }

        /* access modifiers changed from: private */
        public boolean resumeDialAfterHold() {
            Rlog.d(MtkGsmCdmaCallTracker.PROP_LOG_TAG, "resumeDialAfterHold begin");
            if (!this.mWaitToRedial) {
                return false;
            }
            MtkGsmCdmaCallTracker.this.mCi.dial(this.mDialString, this.mIsEmergencyCall, this.mEmergencyNumberInfo, this.mHasKnownUserIntentEmergency, this.mClirMode, this.mUUSInfo, MtkGsmCdmaCallTracker.this.obtainCompleteMessage(1002));
            resetToRedial();
            Rlog.d(MtkGsmCdmaCallTracker.PROP_LOG_TAG, "resumeDialAfterHold end");
            return true;
        }
    }

    class WaitForHoldToHangup {
        /* access modifiers changed from: private */
        public GsmCdmaCall mCall = null;
        private boolean mHoldDone = false;
        private boolean mWaitToHangup = false;

        WaitForHoldToHangup() {
            resetToHangup();
        }

        /* access modifiers changed from: package-private */
        public boolean isWaitToHangup() {
            return this.mWaitToHangup;
        }

        /* access modifiers changed from: package-private */
        public boolean isHoldDone() {
            return this.mHoldDone;
        }

        /* access modifiers changed from: package-private */
        public void setHoldDone() {
            this.mHoldDone = true;
        }

        /* access modifiers changed from: package-private */
        public void setToHangup() {
            this.mWaitToHangup = true;
        }

        public void setToHangup(GsmCdmaCall call) {
            this.mWaitToHangup = true;
            this.mCall = call;
        }

        public void resetToHangup() {
            Rlog.d(MtkGsmCdmaCallTracker.PROP_LOG_TAG, "Reset mWaitForHoldToHangupRequest variables");
            this.mWaitToHangup = false;
            this.mHoldDone = false;
            this.mCall = null;
        }

        /* access modifiers changed from: private */
        public boolean resumeHangupAfterHold() {
            Rlog.d(MtkGsmCdmaCallTracker.PROP_LOG_TAG, "resumeHangupAfterHold begin");
            if (!this.mWaitToHangup || this.mCall == null) {
                resetToHangup();
                return false;
            }
            Rlog.d(MtkGsmCdmaCallTracker.PROP_LOG_TAG, "resumeHangupAfterHold to hangup call");
            this.mWaitToHangup = false;
            this.mHoldDone = false;
            try {
                MtkGsmCdmaCallTracker.this.hangup(this.mCall);
            } catch (CallStateException ex) {
                ex.printStackTrace();
                Rlog.e(MtkGsmCdmaCallTracker.PROP_LOG_TAG, "unexpected error on hangup (" + ex.getMessage() + ")");
            }
            Rlog.d(MtkGsmCdmaCallTracker.PROP_LOG_TAG, "resumeHangupAfterHold end");
            this.mCall = null;
            return true;
        }
    }

    public MtkGsmCdmaCallTracker(GsmCdmaPhone phone) {
        super(phone);
        this.mRingingCall = new MtkGsmCdmaCall(this);
        this.mForegroundCall = new MtkGsmCdmaCall(this);
        this.mBackgroundCall = new MtkGsmCdmaCall(this);
        this.mMtkCi = this.mCi;
        this.mMtkCi.setOnIncomingCallIndication(this, 1000, null);
        this.mMtkCi.registerForCallAdditionalInfo(this, 1006, null);
        this.mMtkCi.registerForOffOrNotAvailable(this, 1001, null);
        this.mHelper = new MtkGsmCdmaCallTrackerHelper(phone.getContext(), this);
        this.mMtkCi.registerForEconfSrvcc(this, 1005, null);
        try {
            this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(this.mPhone.getContext());
            this.mMtkGsmCdmaCallTrackerExt = this.mTelephonyCustomizationFactory.makeMtkGsmCdmaCallTrackerExt(phone.getContext());
        } catch (Exception e) {
            Rlog.d("GsmCdmaCallTracker", "mMtkGsmCdmaCallTrackerExt init fail");
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void updatePhoneType(boolean duringInit) {
        updatePhoneType(duringInit, false);
    }

    private void updatePhoneType(boolean duringInit, boolean duringPollCallsResult) {
        if (this.mPhoneType != 2 || this.mPhone.isPhoneTypeGsm()) {
            if (!duringInit) {
                if (duringPollCallsResult || this.mState == PhoneConstants.State.IDLE) {
                    reset();
                    if (hasC2kOverImsModem()) {
                        Phone imsPhone = this.mPhone.getImsPhone();
                        if (imsPhone == null || imsPhone.getHandoverConnection() == null) {
                            pollCallsWhenSafe();
                        } else {
                            Rlog.d("GsmCdmaCallTracker", "not trigger pollCall since imsCall exists");
                        }
                    }
                } else {
                    this.mHasPendingUpdatePhoneType = true;
                    Rlog.d("GsmCdmaCallTracker", "[updatePhoneType]mHasPendingUpdatePhoneType = true");
                    if (this.mPhoneType == 2 && this.mPhone.isPhoneTypeGsm()) {
                        this.mHasPendingCheckAndEnableData = true;
                    }
                    if (this.mLastRelevantPoll == null) {
                        pollCallsWhenSafe();
                        return;
                    }
                    return;
                }
            }
            MtkGsmCdmaCallTracker.super.updatePhoneType(true);
            if (this.mPhone.isPhoneTypeGsm()) {
                if (this.mMtkCi == null) {
                    this.mMtkCi = this.mCi;
                }
                this.mMtkCi.unregisterForLineControlInfo(this);
                this.mPhoneType = 1;
                return;
            }
            if (this.mMtkCi == null) {
                this.mMtkCi = this.mCi;
            }
            this.mMtkCi.unregisterForLineControlInfo(this);
            this.mMtkCi.registerForLineControlInfo(this, 1004, null);
            this.mPhoneType = 2;
        }
    }

    /* access modifiers changed from: protected */
    public void reset() {
        this.mHelper.setGwsdCall(false);
        if (!hasC2kOverImsModem()) {
            MtkGsmCdmaCallTracker.super.reset();
            return;
        }
        Rlog.d("GsmCdmaCallTracker", "reset");
        GsmCdmaConnection[] gsmCdmaConnectionArr = this.mConnections;
        for (GsmCdmaConnection gsmCdmaConnection : gsmCdmaConnectionArr) {
            if (gsmCdmaConnection != null) {
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

    /* JADX INFO: Multiple debug info for r6v37 'dc'  com.android.internal.telephony.DriverCall: [D('dcSize' int), D('dc' com.android.internal.telephony.DriverCall)] */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.mediatek.internal.telephony.MtkGsmCdmaConnection.<init>(com.android.internal.telephony.GsmCdmaPhone, com.android.internal.telephony.DriverCall, com.android.internal.telephony.GsmCdmaCallTracker, int):void
     arg types: [com.android.internal.telephony.GsmCdmaPhone, com.android.internal.telephony.DriverCall, com.mediatek.internal.telephony.MtkGsmCdmaCallTracker, int]
     candidates:
      com.mediatek.internal.telephony.MtkGsmCdmaConnection.<init>(android.content.Context, com.android.internal.telephony.cdma.CdmaCallWaitingNotification, com.android.internal.telephony.GsmCdmaCallTracker, com.android.internal.telephony.GsmCdmaCall):void
      com.mediatek.internal.telephony.MtkGsmCdmaConnection.<init>(com.android.internal.telephony.GsmCdmaPhone, com.android.internal.telephony.DriverCall, com.android.internal.telephony.GsmCdmaCallTracker, int):void */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:338:0x08af, code lost:
        return;
     */
    public synchronized void handlePollCalls(AsyncResult ar) {
        List polledCalls;
        Phone imsPhone;
        int cause;
        int handoverConnectionsSize;
        boolean unknownConnectionAppeared;
        boolean hasAnyCallDisconnected;
        DriverCall dc;
        boolean needsPollDelay;
        int curDC;
        boolean noConnectionExists;
        AsyncResult asyncResult = ar;
        synchronized (this) {
            if (asyncResult.exception == null) {
                polledCalls = (List) asyncResult.result;
            } else if (isCommandExceptionRadioNotAvailable(asyncResult.exception)) {
                polledCalls = new ArrayList();
            } else if (!this.mNeedWaitImsEConfSrvcc || hasParsingCEPCapability()) {
                pollCallsAfterDelay();
                return;
            } else {
                Rlog.d(PROP_LOG_TAG, "SRVCC: +ECONFSRVCC is still not arrival, skip this poll call.");
                return;
            }
            Connection newRinging = null;
            ArrayList<Connection> newUnknownConnectionsGsm = new ArrayList<>();
            Connection dc2 = null;
            boolean hasNonHangupStateChanged = false;
            boolean hasAnyCallDisconnected2 = false;
            boolean needsPollDelay2 = false;
            boolean unknownConnectionAppeared2 = false;
            int handoverConnectionsSize2 = this.mHandoverConnections.size();
            boolean noConnectionExists2 = true;
            if (polledCalls.size() == 0) {
                this.mHelper.setGwsdCall(false);
            }
            int i = 0;
            int curDC2 = 0;
            int dcSize = polledCalls.size();
            while (true) {
                Connection newUnknownConnectionCdma = dc2;
                if (i < this.mConnections.length) {
                    Connection conn = this.mConnections[i];
                    if (curDC2 < dcSize) {
                        DriverCall dc3 = (DriverCall) polledCalls.get(curDC2);
                        if (!isPhoneTypeGsm()) {
                            dcSize = dcSize;
                            hasAnyCallDisconnected = hasAnyCallDisconnected2;
                            dc = dc3;
                            unknownConnectionAppeared = unknownConnectionAppeared2;
                            handoverConnectionsSize = handoverConnectionsSize2;
                            dc.number = processPlusCodeForDriverCall(dc.number, dc.isMT, dc.TOA);
                        } else {
                            dcSize = dcSize;
                            hasAnyCallDisconnected = hasAnyCallDisconnected2;
                            unknownConnectionAppeared = unknownConnectionAppeared2;
                            handoverConnectionsSize = handoverConnectionsSize2;
                            dc = dc3;
                        }
                        if (dc.index == i + 1) {
                            curDC2++;
                        } else {
                            dc = null;
                        }
                    } else {
                        dcSize = dcSize;
                        hasAnyCallDisconnected = hasAnyCallDisconnected2;
                        unknownConnectionAppeared = unknownConnectionAppeared2;
                        handoverConnectionsSize = handoverConnectionsSize2;
                        dc = null;
                    }
                    if (!(conn == null && dc == null)) {
                        noConnectionExists2 = false;
                    }
                    if (DBG_POLL) {
                        log("poll: conn[i=" + i + "]=" + conn + ", dc=" + dc);
                    }
                    if (conn != null || dc == null) {
                        needsPollDelay = needsPollDelay2;
                        noConnectionExists = noConnectionExists2;
                        curDC = curDC2;
                        if (conn != null && dc == null) {
                            if (DBG_POLL) {
                                Rlog.d(PROP_LOG_TAG, "case 2 : old Call disappear");
                            }
                            if (!isPhoneTypeGsm() || this.mPhoneType == 2) {
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
                            } else {
                                if ((conn.getCall() == this.mForegroundCall && this.mForegroundCall.mConnections.size() == 1 && this.mBackgroundCall.isIdle()) || (conn.getCall() == this.mBackgroundCall && this.mBackgroundCall.mConnections.size() == 1 && this.mForegroundCall.isIdle())) {
                                    this.mRingingCall.getState();
                                    Call.State state = Call.State.WAITING;
                                }
                                this.mDroppedDuringPoll.add(conn);
                                if (this.mIsEcmTimerCanceled) {
                                    handleEcmTimer(0);
                                }
                                this.mConnections[i] = null;
                                this.mHelper.CallIndicationEnd();
                                this.mHelper.clearForwardingAddressVariables(i);
                            }
                            this.mConnections[i] = null;
                        } else if (conn != null && dc != null && !conn.compareTo(dc) && isPhoneTypeGsm()) {
                            if (DBG_POLL) {
                                Rlog.d(PROP_LOG_TAG, "case 3 : old Call replaced");
                            }
                            this.mDroppedDuringPoll.add(conn);
                            if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc)) {
                                this.mConnections[i] = new MtkGsmCdmaConnection(this.mPhone, dc, (GsmCdmaCallTracker) this, i);
                            } else {
                                Rlog.d(PROP_LOG_TAG, "ringing disc not updated yet & replaced by pendingMo");
                                this.mConnections[i] = this.mPendingMO;
                                this.mPendingMO.mIndex = i;
                                this.mPendingMO.update(dc);
                                this.mPendingMO = null;
                            }
                            if (this.mConnections[i].getCall() == this.mRingingCall) {
                                newRinging = this.mConnections[i];
                            }
                            hasNonHangupStateChanged = true;
                            dc2 = newUnknownConnectionCdma;
                            unknownConnectionAppeared2 = unknownConnectionAppeared;
                        } else if (!(conn == null || dc == null)) {
                            if (isPhoneTypeGsm() || conn.isIncoming() == dc.isMT) {
                                if (DBG_POLL) {
                                    Rlog.d(PROP_LOG_TAG, "case 4 : old Call update");
                                }
                                hasNonHangupStateChanged = hasNonHangupStateChanged || conn.update(dc);
                                dc2 = newUnknownConnectionCdma;
                                unknownConnectionAppeared2 = unknownConnectionAppeared;
                            } else if (dc.isMT) {
                                this.mConnections[i] = new MtkGsmCdmaConnection(this.mPhone, dc, (GsmCdmaCallTracker) this, i);
                                this.mDroppedDuringPoll.add(conn);
                                newRinging = checkMtFindNewRinging(dc, i);
                                if (newRinging == null) {
                                    newUnknownConnectionCdma = conn;
                                    unknownConnectionAppeared = true;
                                }
                                checkAndEnableDataCallAfterEmergencyCallDropped();
                                dc2 = newUnknownConnectionCdma;
                                unknownConnectionAppeared2 = unknownConnectionAppeared;
                            } else {
                                Rlog.e("GsmCdmaCallTracker", "Error in RIL, Phantom call appeared " + dc);
                            }
                        }
                        dc2 = newUnknownConnectionCdma;
                        unknownConnectionAppeared2 = unknownConnectionAppeared;
                    } else {
                        if (DBG_POLL) {
                            log("case 1 : new Call appear");
                        }
                        if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc)) {
                            log("pendingMo=" + this.mPendingMO + ", dc=" + dc);
                            if (this.mPendingMO != null && !this.mPendingMO.compareTo(dc)) {
                                Rlog.d(PROP_LOG_TAG, "MO/MT conflict! MO should be hangup by MD");
                            }
                            this.mConnections[i] = new MtkGsmCdmaConnection(this.mPhone, dc, (GsmCdmaCallTracker) this, i);
                            if (isPhoneTypeGsm()) {
                                this.mHelper.setForwardingAddressToConnection(i, this.mConnections[i]);
                            }
                            Connection hoConnection = getHoConnection(dc);
                            if (hoConnection == null) {
                                needsPollDelay = needsPollDelay2;
                                noConnectionExists = noConnectionExists2;
                                curDC = curDC2;
                                MtkGsmCdmaCallTracker.super.handleCallinControl(i, dc);
                                newRinging = checkMtFindNewRinging(dc, i);
                                if (newRinging == null) {
                                    unknownConnectionAppeared2 = true;
                                    if (isPhoneTypeGsm()) {
                                        newUnknownConnectionsGsm.add(this.mConnections[i]);
                                    } else {
                                        newUnknownConnectionCdma = this.mConnections[i];
                                    }
                                } else {
                                    unknownConnectionAppeared2 = unknownConnectionAppeared;
                                }
                            } else if (!(hoConnection instanceof MtkImsPhoneConnection) || !((MtkImsPhoneConnection) hoConnection).isMultipartyBeforeHandover() || !((MtkImsPhoneConnection) hoConnection).isConfHostBeforeHandover() || hasParsingCEPCapability()) {
                                Rlog.i("GsmCdmaCallTracker", "SRVCC: goes to normal call case.");
                                this.mConnections[i].migrateFrom(hoConnection);
                                if (hoConnection.mPreHandoverState == Call.State.ACTIVE || hoConnection.mPreHandoverState == Call.State.HOLDING || dc.state != DriverCall.State.ACTIVE) {
                                    this.mConnections[i].onConnectedConnectionMigrated();
                                } else {
                                    this.mConnections[i].onConnectedInOrOut();
                                }
                                this.mHandoverConnections.remove(hoConnection);
                                if (this.mPhone.getImsPhone() != null) {
                                    this.mImsPhoneCallTracker = this.mPhone.getImsPhone().getCallTracker();
                                }
                                MtkGsmCdmaCallTracker.super.handlePendingHangupSRVCC(this.mImsPhoneCallTracker, i);
                                if (isPhoneTypeGsm()) {
                                    Iterator<Connection> it = this.mHandoverConnections.iterator();
                                    while (it.hasNext()) {
                                        Connection c = it.next();
                                        Rlog.i("GsmCdmaCallTracker", "HO Conn state is " + c.mPreHandoverState);
                                        if (c.mPreHandoverState == this.mConnections[i].getState()) {
                                            Rlog.i("GsmCdmaCallTracker", "Removing HO conn " + hoConnection + c.mPreHandoverState);
                                            it.remove();
                                        }
                                        noConnectionExists2 = noConnectionExists2;
                                        curDC2 = curDC2;
                                        needsPollDelay2 = needsPollDelay2;
                                    }
                                    needsPollDelay = needsPollDelay2;
                                    noConnectionExists = noConnectionExists2;
                                    curDC = curDC2;
                                } else {
                                    needsPollDelay = needsPollDelay2;
                                    noConnectionExists = noConnectionExists2;
                                    curDC = curDC2;
                                }
                                if (this.mIsInEmergencyCall && !this.mIsEcmTimerCanceled && this.mPhone.isInEcm()) {
                                    Rlog.i("GsmCdmaCallTracker", "Ecm timer has been canceled in IMS, so set mIsEcmTimerCanceled=true directly");
                                    this.mIsEcmTimerCanceled = true;
                                }
                                this.mPhone.oemMigrateFrom();
                                if (this.mPhone.hasHoRegistrants()) {
                                    this.mPhone.notifyHandoverStateChanged(this.mConnections[i]);
                                    this.mConnections[i].onConnectionEvent("android.telecom.event.CALL_REMOTELY_UNHELD", (Bundle) null);
                                } else {
                                    unknownConnectionAppeared2 = true;
                                    if (isPhoneTypeGsm()) {
                                        newUnknownConnectionsGsm.add(this.mConnections[i]);
                                    } else {
                                        newUnknownConnectionCdma = this.mConnections[i];
                                    }
                                }
                            } else {
                                Rlog.i("GsmCdmaCallTracker", "SRVCC: goes to conference case.");
                                this.mConnections[i].mOrigConnection = hoConnection;
                                this.mImsConfParticipants.add(this.mConnections[i]);
                                needsPollDelay = needsPollDelay2;
                                noConnectionExists = noConnectionExists2;
                                curDC = curDC2;
                            }
                            hasNonHangupStateChanged = true;
                            dc2 = newUnknownConnectionCdma;
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
                                if (this.mIsEcmTimerCanceled) {
                                    handleEcmTimer(0);
                                }
                                try {
                                    log("poll: hangupPendingMO, hangup conn " + i);
                                    hangup(this.mConnections[i]);
                                } catch (CallStateException e) {
                                    Rlog.e("GsmCdmaCallTracker", "unexpected error on hangup");
                                }
                            } else {
                                needsPollDelay = needsPollDelay2;
                                noConnectionExists = noConnectionExists2;
                                curDC = curDC2;
                            }
                        }
                        unknownConnectionAppeared2 = unknownConnectionAppeared;
                        hasNonHangupStateChanged = true;
                        dc2 = newUnknownConnectionCdma;
                    }
                    i++;
                    noConnectionExists2 = noConnectionExists;
                    hasAnyCallDisconnected2 = hasAnyCallDisconnected;
                    handoverConnectionsSize2 = handoverConnectionsSize;
                    curDC2 = curDC;
                    needsPollDelay2 = needsPollDelay;
                } else {
                    boolean hasAnyCallDisconnected3 = hasAnyCallDisconnected2;
                    boolean unknownConnectionAppeared3 = unknownConnectionAppeared2;
                    if (!isPhoneTypeGsm() && noConnectionExists2) {
                        checkAndEnableDataCallAfterEmergencyCallDropped();
                    }
                    if (this.mPendingMO != null) {
                        Rlog.d("GsmCdmaCallTracker", "Pending MO dropped before poll fg state:" + this.mForegroundCall.getState());
                        this.mDroppedDuringPoll.add(this.mPendingMO);
                        this.mPendingMO = null;
                        this.mHangupPendingMO = false;
                        if (this.mPendingCallInEcm) {
                            this.mPendingCallInEcm = false;
                        }
                        if (this.mIsEcmTimerCanceled) {
                            handleEcmTimer(0);
                        }
                        if (!isPhoneTypeGsm()) {
                            checkAndEnableDataCallAfterEmergencyCallDropped();
                        }
                    }
                    if (polledCalls.size() == 0 && this.mConnections.length == 0) {
                        log("check whether fgCall or ringCall have mConnections");
                        if (!isPhoneTypeGsm()) {
                            int count3 = this.mForegroundCall.mConnections.size();
                            for (int n3 = 0; n3 < count3; n3++) {
                                log("adding fgCall cn " + n3 + " to droppedDuringPoll");
                                this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mForegroundCall.mConnections.get(n3));
                            }
                            int count4 = this.mRingingCall.mConnections.size();
                            for (int n4 = 0; n4 < count4; n4++) {
                                log("adding rgCall cn " + n4 + " to droppedDuringPoll");
                                this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mRingingCall.mConnections.get(n4));
                            }
                        }
                    }
                    if (newRinging != null) {
                        IOppoCallManager oppoCallManager = OppoTelephonyFactory.getInstance().getFeature(IOppoCallManager.DEFAULT, new Object[0]);
                        oppoCallManager.checkVoocState("true");
                        if (!oppoCallManager.isRestricted(TYPE_CALLIN, this.mPhone.getPhoneId())) {
                            this.mPhone.notifyNewRingingConnection(newRinging);
                        }
                        handleAutoAnswer(this.mPhone);
                    }
                    int mDropSize = this.mDroppedDuringPoll.size();
                    ArrayList<GsmCdmaConnection> locallyDisconnectedConnections = new ArrayList<>();
                    int i2 = this.mDroppedDuringPoll.size() - 1;
                    Connection newUnknownConnectionCdma2 = newUnknownConnectionCdma;
                    while (i2 >= 0) {
                        Connection conn2 = (GsmCdmaConnection) this.mDroppedDuringPoll.get(i2);
                        boolean wasDisconnected = false;
                        if (isCommandExceptionRadioNotAvailable(asyncResult.exception)) {
                            this.mDroppedDuringPoll.remove(i2);
                            wasDisconnected = true;
                            hasAnyCallDisconnected3 |= conn2.onDisconnect(14);
                        } else if (conn2.isIncoming() && conn2.getConnectTime() == 0 && conn2.getState() != Call.State.ACTIVE) {
                            if (((GsmCdmaConnection) conn2).mCause == 3 || ((GsmCdmaConnection) conn2).mCause == 16) {
                                cause = 16;
                            } else {
                                cause = 1;
                            }
                            log("missed/rejected call, conn.cause=" + ((GsmCdmaConnection) conn2).mCause);
                            log("setting cause to " + cause);
                            this.mDroppedDuringPoll.remove(i2);
                            wasDisconnected = true;
                            locallyDisconnectedConnections.add(conn2);
                            hasAnyCallDisconnected3 |= conn2.onDisconnect(cause);
                        } else if (((GsmCdmaConnection) conn2).mCause == 3 || ((GsmCdmaConnection) conn2).mCause == 7) {
                            this.mDroppedDuringPoll.remove(i2);
                            wasDisconnected = true;
                            locallyDisconnectedConnections.add(conn2);
                            hasAnyCallDisconnected3 |= conn2.onDisconnect(((GsmCdmaConnection) conn2).mCause);
                        }
                        if (!isPhoneTypeGsm() && wasDisconnected && unknownConnectionAppeared3 && conn2 == newUnknownConnectionCdma2) {
                            newUnknownConnectionCdma2 = null;
                            unknownConnectionAppeared3 = false;
                        }
                        i2--;
                        asyncResult = ar;
                    }
                    if (locallyDisconnectedConnections.size() > 0) {
                        this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), locallyDisconnectedConnections, TelephonyManager.getDefault().getNetworkCountryIso());
                    }
                    if (this.mImsConfHostConnection != null) {
                        MtkImsPhoneConnection hostConn = this.mImsConfHostConnection;
                        if (this.mImsConfParticipants.size() >= 2) {
                            restoreConferenceParticipantAddress();
                            Rlog.d(PROP_LOG_TAG, "SRVCC: notify new participant connections");
                            hostConn.notifyConferenceConnectionsConfigured(this.mImsConfParticipants);
                        } else if (this.mImsConfParticipants.size() == 1) {
                            GsmCdmaConnection participant = this.mImsConfParticipants.get(0);
                            String address = hostConn.getConferenceParticipantAddress(0);
                            Rlog.d(PROP_LOG_TAG, "SRVCC: restore participant connection with address: " + Rlog.pii(PROP_LOG_TAG, address));
                            if (participant instanceof MtkGsmCdmaConnection) {
                                ((MtkGsmCdmaConnection) participant).updateConferenceParticipantAddress(address);
                            }
                            Rlog.d(PROP_LOG_TAG, "SRVCC: only one connection, consider it as a normal call SRVCC");
                            this.mPhone.notifyHandoverStateChanged(participant);
                        } else {
                            Rlog.e(PROP_LOG_TAG, "SRVCC: abnormal case, no participant connections.");
                        }
                        this.mImsConfParticipants.clear();
                        this.mImsConfHostConnection = null;
                        this.mEconfSrvccConnectionIds = null;
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
                    if (mDropSize > 0) {
                        this.mMtkCi.getLastCallFailCause(obtainNoPollCompleteMessage(5));
                    }
                    if (needsPollDelay2) {
                        pollCallsAfterDelay();
                    }
                    if ((newRinging != null || hasNonHangupStateChanged || hasAnyCallDisconnected3) && !this.mHasPendingSwapRequest) {
                        internalClearDisconnected();
                    }
                    updatePhoneState();
                    if (this.mState == PhoneConstants.State.IDLE && (this.mPhone instanceof MtkGsmCdmaPhone)) {
                        log("Phone in IDLE State, reset that CRSS msg");
                        if (this.mPhone.getCachedCrss() != null) {
                            this.mPhone.resetCachedCrss();
                        }
                    }
                    if (unknownConnectionAppeared3) {
                        IOppoCallManager oppoCallManager2 = OppoTelephonyFactory.getInstance().getFeature(IOppoCallManager.DEFAULT, new Object[0]);
                        if (isPhoneTypeGsm()) {
                            Iterator<Connection> it3 = newUnknownConnectionsGsm.iterator();
                            while (it3.hasNext()) {
                                Connection c2 = it3.next();
                                log("Notify unknown for " + c2);
                                if (!oppoCallManager2.isRestricted(TYPE_CALLIN, this.mPhone.getPhoneId())) {
                                    this.mPhone.notifyUnknownConnection(c2);
                                }
                            }
                        } else if (!oppoCallManager2.isRestricted(TYPE_CALLIN, this.mPhone.getPhoneId())) {
                            this.mPhone.notifyUnknownConnection(newUnknownConnectionCdma2);
                        }
                    }
                    if (hasNonHangupStateChanged || newRinging != null || hasAnyCallDisconnected3) {
                        this.mPhone.notifyPreciseCallStateChanged();
                        updateMetrics(this.mConnections);
                    }
                    if (handoverConnectionsSize2 > 0 && this.mHandoverConnections.size() == 0 && (imsPhone = this.mPhone.getImsPhone()) != null) {
                        imsPhone.callEndCleanupHandOverCallIfAny();
                    }
                    if (isPhoneTypeGsm() && this.mConnections != null && this.mConnections.length == 19 && this.mHelper.getCurrentTotalConnections() == 1 && this.mRingingCall.getState() == Call.State.WAITING) {
                        this.mRingingCall.mState = Call.State.INCOMING;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dumpState() {
        Rlog.i("GsmCdmaCallTracker", "Phone State:" + this.mState);
        Rlog.i("GsmCdmaCallTracker", "Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (int i = 0; i < s; i++) {
            Rlog.i("GsmCdmaCallTracker", l.get(i).toString());
        }
        Rlog.i("GsmCdmaCallTracker", "Foreground call: " + this.mForegroundCall.toString());
        List l2 = this.mForegroundCall.getConnections();
        int s2 = l2.size();
        for (int i2 = 0; i2 < s2; i2++) {
            Rlog.i("GsmCdmaCallTracker", l2.get(i2).toString());
        }
        Rlog.i("GsmCdmaCallTracker", "Background call: " + this.mBackgroundCall.toString());
        List l3 = this.mBackgroundCall.getConnections();
        int s3 = l3.size();
        for (int i3 = 0; i3 < s3; i3++) {
            Rlog.i("GsmCdmaCallTracker", l3.get(i3).toString());
        }
        if (isPhoneTypeGsm()) {
            this.mHelper.LogState();
        }
    }

    public void handleMessage(Message msg) {
        this.mHelper.LogerMessage(msg.what);
        int i = msg.what;
        if (i == 1) {
            Rlog.d("GsmCdmaCallTracker", "Event EVENT_POLL_CALLS_RESULT Received");
            if (msg == this.mLastRelevantPoll) {
                if (DBG_POLL) {
                    log("handle EVENT_POLL_CALL_RESULT: set needsPoll=F");
                }
                this.mNeedsPoll = false;
                this.mLastRelevantPoll = null;
                boolean bNoCallExists = noAnyCallFromModemExist((AsyncResult) msg.obj);
                if (!bNoCallExists && this.mHasPendingUpdatePhoneType) {
                    this.mHasPendingUpdatePhoneType = false;
                    updatePhoneType(false, true);
                    Rlog.d("GsmCdmaCallTracker", "[EVENT_POLL_CALLS_RESULT]!bNoCallExists");
                }
                handlePollCalls((AsyncResult) msg.obj);
                if (bNoCallExists && this.mHasPendingUpdatePhoneType) {
                    this.mHasPendingUpdatePhoneType = false;
                    updatePhoneType(false, true);
                    Rlog.d("GsmCdmaCallTracker", "[EVENT_POLL_CALLS_RESULT]bNoCallExists");
                }
                if (this.mHasPendingCheckAndEnableData) {
                    if (bNoCallExists) {
                        checkAndEnableDataCallAfterEmergencyCallDropped();
                    }
                    this.mHasPendingCheckAndEnableData = false;
                }
                if (this.mWaitForHoldToHangupRequest.isHoldDone()) {
                    Rlog.d(PROP_LOG_TAG, "Switch ends, and poll call done, then resume hangup");
                    boolean unused = this.mWaitForHoldToHangupRequest.resumeHangupAfterHold();
                }
            }
        } else if (i != 8) {
            if (i == 14) {
                Rlog.d(PROP_LOG_TAG, "Receives EVENT_EXIT_ECM_RESPONSE_CDMA");
                if (this.mPendingCallInEcm) {
                    String dialString = (String) ((AsyncResult) msg.obj).userObj;
                    if (this.mPendingMO == null) {
                        this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, false);
                    }
                    if (!isPhoneTypeGsm()) {
                        String tmpStr = this.mPendingMO.getAddress();
                        this.mCi.dial(tmpStr + "," + PhoneNumberUtils.extractNetworkPortionAlt(dialString), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), this.mPendingCallClirMode, obtainCompleteMessage());
                        if (needToConvert(dialString)) {
                            this.mPendingMO.setConverted(PhoneNumberUtils.extractNetworkPortionAlt(dialString));
                        }
                    } else {
                        Rlog.e("GsmCdmaCallTracker", "originally unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                        this.mCi.dial(this.mPendingMO.getAddress(), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), this.mPendingCallClirMode, (UUSInfo) null, obtainCompleteMessage());
                    }
                    this.mPendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
            } else if (i != 16) {
                if (i != 20) {
                    switch (i) {
                        case 1000:
                            this.mHelper.CallIndicationProcess((AsyncResult) msg.obj);
                            return;
                        case 1001:
                            Rlog.d(PROP_LOG_TAG, "Receives EVENT_RADIO_OFF_OR_NOT_AVAILABLE");
                            handlePollCalls(new AsyncResult((Object) null, (Object) null, new CommandException(CommandException.Error.RADIO_NOT_AVAILABLE)));
                            this.mLastRelevantPoll = null;
                            return;
                        case 1002:
                            if (((AsyncResult) msg.obj).exception != null) {
                                Rlog.d(PROP_LOG_TAG, "dial call failed!!");
                            }
                            operationComplete();
                            return;
                        case 1003:
                            operationComplete();
                            return;
                        case 1004:
                            Rlog.d(PROP_LOG_TAG, "Receives EVENT_CDMA_CALL_ACCEPTED");
                            if (((AsyncResult) msg.obj).exception == null) {
                                handleCallAccepted();
                                return;
                            }
                            return;
                        case 1005:
                            log("Receives EVENT_ECONF_SRVCC_INDICATION");
                            if (!hasParsingCEPCapability()) {
                                this.mEconfSrvccConnectionIds = (int[]) ((AsyncResult) msg.obj).result;
                                this.mNeedWaitImsEConfSrvcc = false;
                                pollCallsWhenSafe();
                                return;
                            }
                            return;
                        case 1006:
                            this.mHelper.handleCallAdditionalInfo((AsyncResult) msg.obj);
                            return;
                        default:
                            MtkGsmCdmaCallTracker.super.handleMessage(msg);
                            return;
                    }
                } else {
                    Rlog.d(PROP_LOG_TAG, "Receives EVENT_THREE_WAY_DIAL_BLANK_FLASH");
                    if (isPhoneTypeGsm()) {
                        Rlog.e("GsmCdmaCallTracker", "unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                    } else if (((AsyncResult) msg.obj).exception == null) {
                        final String dialString2 = (String) ((AsyncResult) msg.obj).userObj;
                        postDelayed(new Runnable() {
                            /* class com.mediatek.internal.telephony.MtkGsmCdmaCallTracker.AnonymousClass1 */

                            public void run() {
                                if (MtkGsmCdmaCallTracker.this.mPendingMO != null) {
                                    String tmpStr = MtkGsmCdmaCallTracker.this.mPendingMO.getAddress();
                                    MtkGsmCdmaCallTracker.this.mCi.sendCDMAFeatureCode(tmpStr + "," + PhoneNumberUtils.extractNetworkPortionAlt(dialString2), MtkGsmCdmaCallTracker.this.obtainMessage(16));
                                    if (MtkGsmCdmaCallTracker.this.needToConvert(dialString2)) {
                                        MtkGsmCdmaCallTracker.this.mPendingMO.setConverted(PhoneNumberUtils.extractNetworkPortionAlt(dialString2));
                                    }
                                }
                            }
                        }, (long) this.m3WayCallFlashDelay);
                    } else {
                        this.mPendingMO = null;
                        Rlog.w("GsmCdmaCallTracker", "exception happened on Blank Flash for 3-way call");
                    }
                }
            } else if (isPhoneTypeGsm()) {
                Rlog.e("GsmCdmaCallTracker", "unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
            } else if (((AsyncResult) msg.obj).exception == null && this.mPendingMO != null) {
                this.mPendingMO.onConnectedInOrOut();
                this.mPendingMO = null;
            }
        } else if (isPhoneTypeGsm()) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                if (this.mWaitForHoldToRedialRequest.isWaitToRedial()) {
                    if (this.mPendingMO != null) {
                        this.mPendingMO.mCause = 3;
                        this.mPendingMO.onDisconnect(3);
                        this.mPendingMO = null;
                        this.mHangupPendingMO = false;
                        updatePhoneState();
                    }
                    resumeBackgroundAfterDialFailed();
                    this.mWaitForHoldToRedialRequest.resetToRedial();
                }
                this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
            } else if (this.mWaitForHoldToRedialRequest.isWaitToRedial()) {
                Rlog.d(PROP_LOG_TAG, "Switch success, then resume dial");
                boolean unused2 = this.mWaitForHoldToRedialRequest.resumeDialAfterHold();
            }
            if (this.mWaitForHoldToHangupRequest.isWaitToHangup()) {
                if (ar.exception == null && this.mWaitForHoldToHangupRequest.mCall != null) {
                    Rlog.d(PROP_LOG_TAG, "Switch ends, found waiting hangup. switch fg/bg call.");
                    if (this.mWaitForHoldToHangupRequest.mCall == this.mForegroundCall) {
                        this.mWaitForHoldToHangupRequest.setToHangup(this.mBackgroundCall);
                    } else if (this.mWaitForHoldToHangupRequest.mCall == this.mBackgroundCall) {
                        this.mWaitForHoldToHangupRequest.setToHangup(this.mForegroundCall);
                    }
                }
                Rlog.d(PROP_LOG_TAG, "Switch ends, wait for poll call done to hangup");
                this.mWaitForHoldToHangupRequest.setHoldDone();
            }
            this.mHasPendingSwapRequest = false;
            operationComplete();
        }
    }

    public void hangupAll() throws CallStateException {
        Rlog.d(PROP_LOG_TAG, "hangupAll");
        this.mMtkCi.hangupAll(obtainCompleteMessage());
        if (!this.mRingingCall.isIdle()) {
            this.mRingingCall.onHangupLocal();
        }
        if (!this.mForegroundCall.isIdle()) {
            this.mForegroundCall.onHangupLocal();
        }
        if (!this.mBackgroundCall.isIdle()) {
            this.mBackgroundCall.onHangupLocal();
        }
    }

    public void hangup(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner == this) {
            if (conn == this.mPendingMO) {
                log("hangup: set hangupPendingMO to true");
                this.mHangupPendingMO = true;
            } else if (!isPhoneTypeGsm() && conn.getCall() == this.mRingingCall && this.mRingingCall.getState() == Call.State.WAITING) {
                conn.onLocalDisconnect();
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                return;
            } else {
                try {
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), conn, conn.getGsmCdmaIndex(), TelephonyManager.getDefault().getNetworkCountryIso());
                    if (!(conn instanceof MtkGsmCdmaConnection) || ((MtkGsmCdmaConnection) conn).getRejectWithCause() == -1) {
                        this.mCi.hangupConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage(1003));
                    } else {
                        this.mMtkCi.hangupConnectionWithCause(conn.getGsmCdmaIndex(), ((MtkGsmCdmaConnection) conn).getRejectWithCause(), obtainCompleteMessage(1003));
                    }
                } catch (CallStateException e) {
                    Rlog.w("GsmCdmaCallTracker", "GsmCdmaCallTracker WARN: hangup() on absent connection " + conn);
                }
            }
            conn.onHangupLocal();
            return;
        }
        throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
    }

    public void hangup(GsmCdmaCall call) throws CallStateException {
        if (call.getConnections().size() != 0) {
            if (call == this.mRingingCall) {
                log("(ringing) hangup waiting or background");
                logHangupEvent(call);
                this.mCi.hangupWaitingOrBackground(obtainCompleteMessage(1003));
            } else if (call == this.mForegroundCall) {
                if (call.isDialingOrAlerting()) {
                    log("(foregnd) hangup dialing or alerting...");
                    hangup((GsmCdmaConnection) call.getConnections().get(0));
                } else {
                    logHangupEvent(call);
                    log("(foregnd) hangup active");
                    if (isPhoneTypeGsm()) {
                        if (TelephonyManager.getDefault().isEmergencyNumber(((GsmCdmaConnection) call.getConnections().get(0)).getAddress())) {
                            Rlog.d(PROP_LOG_TAG, "(foregnd) hangup active ECC call by connection index");
                            hangup((GsmCdmaConnection) call.getConnections().get(0));
                        } else if (!this.mWaitForHoldToHangupRequest.isWaitToHangup()) {
                            hangupForegroundResumeBackground();
                        } else {
                            this.mWaitForHoldToHangupRequest.setToHangup(call);
                        }
                    } else {
                        hangupForegroundResumeBackground();
                    }
                }
            } else if (call != this.mBackgroundCall) {
                throw new RuntimeException("GsmCdmaCall " + call + "does not belong to GsmCdmaCallTracker " + this);
            } else if (this.mRingingCall.isRinging()) {
                log("hangup all conns in background call");
                hangupAllConnections(call);
            } else {
                log("(backgnd) hangup waiting/background");
                if (!this.mWaitForHoldToHangupRequest.isWaitToHangup()) {
                    hangupWaitingOrBackground();
                } else {
                    this.mWaitForHoldToHangupRequest.setToHangup(call);
                }
            }
            call.onHangupLocal();
            this.mPhone.notifyPreciseCallStateChanged();
            return;
        }
        throw new CallStateException("no connections in call");
    }

    public void hangupWaitingOrBackground() {
        log("hangupWaitingOrBackground");
        logHangupEvent(this.mBackgroundCall);
        this.mCi.hangupWaitingOrBackground(obtainCompleteMessage(1003));
    }

    public void hangupForegroundResumeBackground() {
        log("hangupForegroundResumeBackground");
        this.mCi.hangupForegroundResumeBackground(obtainCompleteMessage(1003));
    }

    private boolean noAnyCallFromModemExist(AsyncResult ar) {
        List polledCalls;
        if (ar.exception == null) {
            polledCalls = (List) ar.result;
        } else {
            polledCalls = new ArrayList();
        }
        return polledCalls.size() == 0;
    }

    private PersistableBundle getCarrierConfig() {
        return ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
    }

    public boolean canConference() {
        boolean bCrossLineConfSupport = false;
        PersistableBundle config = getCarrierConfig();
        if (config != null) {
            bCrossLineConfSupport = config.getBoolean("mtk_key_multiline_allow_cross_line_conference_bool");
        }
        if (!bCrossLineConfSupport) {
            return this.mMtkGsmCdmaCallTrackerExt.areConnectionsInSameLine(this.mConnections) && MtkGsmCdmaCallTracker.super.canConference();
        }
        return MtkGsmCdmaCallTracker.super.canConference();
    }

    public void conference() {
        boolean bCrossLineConfSupport = false;
        PersistableBundle config = getCarrierConfig();
        if (config != null) {
            bCrossLineConfSupport = config.getBoolean("mtk_key_multiline_allow_cross_line_conference_bool");
        }
        if (bCrossLineConfSupport || this.mMtkGsmCdmaCallTrackerExt.areConnectionsInSameLine(this.mConnections)) {
            MtkGsmCdmaCallTracker.super.conference();
        } else {
            Rlog.e(PROP_LOG_TAG, "conference fail. (not same line)");
        }
    }

    public synchronized Connection dialGsm(String dialString, int clirMode, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        clearDisconnected();
        boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
        checkForDialIssues(isEmergencyCall);
        String dialString2 = convertNumberIfNecessary(this.mPhone, dialString);
        if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
            this.mWaitForHoldToRedialRequest.setToRedial();
            switchWaitingOrHoldingAndActive();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            fakeHoldForegroundBeforeDial();
        }
        this.mPendingMO = new MtkGsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString2), this, this.mForegroundCall, isEmergencyCall);
        if (intentExtras != null) {
            Rlog.d("GsmCdmaCallTracker", "dialGsm - emergency dialer: " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
            this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
        }
        this.mHangupPendingMO = false;
        this.mMetrics.writeRilDial(this.mPhone.getPhoneId(), this.mPendingMO, clirMode, uusInfo);
        String newDialString = this.mMtkGsmCdmaCallTrackerExt.convertDialString(intentExtras, this.mPendingMO.getAddress());
        if (newDialString != null) {
            this.mPendingMO.setConnectionExtras(intentExtras);
        }
        if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
            this.mPendingMO.mCause = 7;
            this.mWaitForHoldToRedialRequest.resetToRedial();
            pollCallsWhenSafe();
        } else {
            setMute(false);
            if (!this.mWaitForHoldToRedialRequest.isWaitToRedial()) {
                if (newDialString != null) {
                    this.mNumberConverted = true;
                } else {
                    newDialString = this.mPendingMO.getAddress();
                }
                this.mCi.dial(newDialString, this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), clirMode, uusInfo, obtainCompleteMessage());
            } else {
                if (newDialString != null) {
                    this.mNumberConverted = true;
                } else {
                    newDialString = this.mPendingMO.getAddress();
                }
                this.mWaitForHoldToRedialRequest.setToRedial(newDialString, this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), clirMode, uusInfo);
            }
        }
        if (this.mNumberConverted) {
            this.mPendingMO.setConverted(dialString);
            this.mNumberConverted = false;
        }
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
        return this.mPendingMO;
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (isPhoneTypeGsm()) {
            if (!this.mHasPendingSwapRequest) {
                this.mWaitForHoldToHangupRequest.setToHangup();
                this.mCi.switchWaitingOrHoldingAndActive(obtainCompleteMessage(8));
                this.mHasPendingSwapRequest = true;
            }
        } else if (this.mForegroundCall.getConnections().size() > 1) {
            flashAndSetGenericTrue();
        } else {
            this.mCi.sendCDMAFeatureCode("", obtainMessage(8));
        }
    }

    private void resumeBackgroundAfterDialFailed() {
        List<Connection> connCopy = (List) this.mBackgroundCall.mConnections.clone();
        int s = connCopy.size();
        for (int i = 0; i < s; i++) {
            ((MtkGsmCdmaConnection) connCopy.get(i)).resumeHoldAfterDialFailed();
        }
    }

    private void disableDataCallInEmergencyCall(boolean isEmergencyCall) {
        if (isEmergencyCall) {
            log("disableDataCallInEmergencyCall");
            setIsInEmergencyCall();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c0  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c5  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x019f  */
    public Connection dialCdma(String dialString, int clirMode, Bundle intentExtras) throws CallStateException {
        boolean internationalRoaming;
        String dialString2;
        String dialString3;
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
            if ("OP20".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, "")) || !isPhoneInEcmMode || isEmergencyCall) {
                if (isPhoneInEcmMode && isEmergencyCall) {
                    handleEcmTimer(1);
                }
                if (this.mForegroundCall.getState() != Call.State.ACTIVE) {
                    return dialThreeWay(dialString2, intentExtras);
                }
                this.mPendingMO = new MtkGsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString2), this, this.mForegroundCall, isEmergencyCall);
                if (intentExtras != null) {
                    Rlog.d("GsmCdmaCallTracker", "dialGsm - emergency dialer: " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
                    this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
                }
                this.mHangupPendingMO = false;
                if (this.mPendingMO.getAddress() != null && this.mPendingMO.getAddress().length() != 0) {
                    if (this.mPendingMO.getAddress().indexOf(78) < 0) {
                        setMute(false);
                        disableDataCallInEmergencyCall(isEmergencyCall);
                        if (!isPhoneInEcmMode) {
                            dialString3 = dialString2;
                        } else if (!isPhoneInEcmMode || !isEmergencyCall) {
                            this.mPhone.exitEmergencyCallbackMode();
                            this.mPhone.setOnEcbModeExitResponse(this, 14, dialString2);
                            this.mPendingCallClirMode = clirMode;
                            this.mPendingCallInEcm = true;
                            if (this.mNumberConverted) {
                                this.mPendingMO.setConverted(dialString);
                                this.mNumberConverted = false;
                            }
                            updatePhoneState();
                            this.mPhone.notifyPreciseCallStateChanged();
                            return this.mPendingMO;
                        } else {
                            dialString3 = dialString2;
                        }
                        this.mCi.dial(this.mPendingMO.getAddress(), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), clirMode, obtainCompleteMessage());
                        if (needToConvert(dialString3)) {
                            this.mPendingMO.setConverted(PhoneNumberUtils.extractNetworkPortionAlt(dialString3));
                        }
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
            throw new CallStateException("cannot dial in ECBM");
        }
        internationalRoaming = internationalRoaming2;
        if (!internationalRoaming) {
        }
        boolean isPhoneInEcmMode2 = this.mPhone.isInEcm();
        if ("OP20".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""))) {
        }
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
        this.mPendingMO = new MtkGsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, this.mIsInEmergencyCall);
        if (intentExtras != null) {
            Rlog.d("GsmCdmaCallTracker", "dialThreeWay - emergency dialer " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
            this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
        }
        PersistableBundle bundle = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        if (bundle != null) {
            this.m3WayCallFlashDelay = bundle.getInt("cdma_3waycall_flash_delay_int");
        } else {
            this.m3WayCallFlashDelay = 0;
        }
        if (this.m3WayCallFlashDelay > 0) {
            this.mCi.sendCDMAFeatureCode("", obtainMessage(20, dialString));
        } else {
            String tmpStr = this.mPendingMO.getAddress();
            this.mCi.sendCDMAFeatureCode(tmpStr + "," + PhoneNumberUtils.extractNetworkPortionAlt(dialString), obtainMessage(16));
            if (needToConvert(dialString)) {
                this.mPendingMO.setConverted(PhoneNumberUtils.extractNetworkPortionAlt(dialString));
            }
        }
        return this.mPendingMO;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.mediatek.internal.telephony.MtkGsmCdmaConnection.<init>(android.content.Context, com.android.internal.telephony.cdma.CdmaCallWaitingNotification, com.android.internal.telephony.GsmCdmaCallTracker, com.android.internal.telephony.GsmCdmaCall):void
     arg types: [android.content.Context, com.android.internal.telephony.cdma.CdmaCallWaitingNotification, com.mediatek.internal.telephony.MtkGsmCdmaCallTracker, com.android.internal.telephony.GsmCdmaCall]
     candidates:
      com.mediatek.internal.telephony.MtkGsmCdmaConnection.<init>(com.android.internal.telephony.GsmCdmaPhone, com.android.internal.telephony.DriverCall, com.android.internal.telephony.GsmCdmaCallTracker, int):void
      com.mediatek.internal.telephony.MtkGsmCdmaConnection.<init>(android.content.Context, com.android.internal.telephony.cdma.CdmaCallWaitingNotification, com.android.internal.telephony.GsmCdmaCallTracker, com.android.internal.telephony.GsmCdmaCall):void */
    /* access modifiers changed from: protected */
    public void handleCallWaitingInfo(CdmaCallWaitingNotification cw) {
        if (!MtkGsmCdmaCallTracker.super.handleCallinControl(cw)) {
            processPlusCodeForWaitingCall(cw);
            if (shouldNotifyWaitingCall(cw)) {
                if (this.mForegroundCall.mConnections.size() > 2) {
                    Iterator it = this.mForegroundCall.mConnections.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Connection c = (Connection) it.next();
                        if (cw.number != null && cw.number.equals(c.getAddress())) {
                            c.onDisconnect(2);
                            break;
                        }
                    }
                }
                new MtkGsmCdmaConnection(this.mPhone.getContext(), cw, (GsmCdmaCallTracker) this, this.mRingingCall);
                updatePhoneState();
                notifyCallWaitingInfo(cw);
            }
        }
    }

    private void handleCallAccepted() {
        List connections = this.mForegroundCall.getConnections();
        int count = connections.size();
        Rlog.d(PROP_LOG_TAG, "handleCallAccepted, fgcall count=" + count);
        if (count == 1) {
            GsmCdmaConnection c = (GsmCdmaConnection) connections.get(0);
            if ((c instanceof MtkGsmCdmaConnection) && (this.mPhone instanceof MtkGsmCdmaPhone)) {
                ((MtkGsmCdmaConnection) c).onCdmaCallAccepted();
            }
        }
    }

    private String processPlusCodeForDriverCall(String number, boolean isMt, int typeOfAddress) {
        if (isMt && typeOfAddress == 145) {
            if (number != null && number.length() > 0 && number.charAt(0) == '+') {
                number = number.substring(1, number.length());
            }
            number = PlusCodeProcessor.getPlusCodeUtils().removeIddNddAddPlusCode(number);
        }
        return PhoneNumberUtils.stringFromStringAndTOA(number, typeOfAddress);
    }

    private void processPlusCodeForWaitingCall(CdmaCallWaitingNotification cw) {
        String address = cw.number;
        if (address != null && address.length() > 0) {
            cw.number = processPlusCodeForWaitingCall(address, cw.numberType);
        }
    }

    private String processPlusCodeForWaitingCall(String number, int numberType) {
        String format = PlusCodeProcessor.getPlusCodeUtils().removeIddNddAddPlusCode(number);
        if (format == null) {
            return number;
        }
        if (numberType != 1 || format.length() <= 0 || format.charAt(0) == '+') {
            return format;
        }
        return "+" + format;
    }

    /* access modifiers changed from: private */
    public boolean needToConvert(String source) {
        String target = GsmCdmaConnection.formatDialString(source);
        return (source == null || target == null || source.equals(target)) ? false : true;
    }

    private boolean shouldNotifyWaitingCall(CdmaCallWaitingNotification cw) {
        GsmCdmaConnection lastRingConn;
        String address = cw.number;
        Rlog.d(PROP_LOG_TAG, "shouldNotifyWaitingCall");
        if (address == null || address.length() <= 0 || (lastRingConn = this.mRingingCall.getLatestConnection()) == null || !address.equals(lastRingConn.getAddress())) {
            return true;
        }
        Rlog.d(PROP_LOG_TAG, "handleCallWaitingInfo, skip duplicate waiting call!");
        return false;
    }

    /* access modifiers changed from: protected */
    public void updatePhoneState() {
        PhoneConstants.State oldState = this.mState;
        if (this.mRingingCall.isRinging()) {
            this.mState = PhoneConstants.State.RINGING;
        } else if (this.mPendingMO != null || !this.mForegroundCall.isIdle() || !this.mBackgroundCall.isIdle()) {
            this.mState = PhoneConstants.State.OFFHOOK;
        } else {
            Phone imsPhone = this.mPhone.getImsPhone();
            if (imsPhone != null) {
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
            OemConstant.setOemCallState(oldState, this.mState);
            if (this.mState != PhoneConstants.State.RINGING) {
                this.mPhone.notifyPhoneStateChanged();
                this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
            }
            if (this.mState == PhoneConstants.State.IDLE && this.mPhone.isPhoneTypeSwitchPending()) {
                log("update phone state, to deal with pending PhoneType SWITCHING");
                this.mCi.getVoiceRadioTechnology(this.mPhone.obtainMessage(40));
                this.mPhone.clearPhoneTypeSwitchPending();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifySrvccState(Call.SrvccState state, ArrayList<Connection> c) {
        if (state == Call.SrvccState.STARTED && c != null) {
            this.mHandoverConnections.addAll(c);
            if (!hasParsingCEPCapability()) {
                Iterator it = this.mHandoverConnections.iterator();
                while (it.hasNext()) {
                    Connection conn = (Connection) it.next();
                    if (conn.isMultiparty() && (conn instanceof MtkImsPhoneConnection) && conn.isConferenceHost()) {
                        log("srvcc: mNeedWaitImsEConfSrvcc set True");
                        this.mNeedWaitImsEConfSrvcc = true;
                        this.mImsConfHostConnection = conn;
                    }
                }
            }
        } else if (state != Call.SrvccState.COMPLETED) {
            this.mHandoverConnections.clear();
        }
        log("notifySrvccState: mHandoverConnections= " + this.mHandoverConnections.toString());
    }

    /* access modifiers changed from: protected */
    public Connection getHoConnection(DriverCall dc) {
        if (dc == null) {
            return null;
        }
        int[] iArr = this.mEconfSrvccConnectionIds;
        if (iArr != null) {
            int numOfParticipants = iArr[0];
            int index = 1;
            while (true) {
                if (index > numOfParticipants) {
                    break;
                } else if (dc.index == this.mEconfSrvccConnectionIds[index]) {
                    Rlog.d(PROP_LOG_TAG, "SRVCC: getHoConnection for call-id:" + dc.index + " in a conference is found!");
                    if (this.mImsConfHostConnection == null) {
                        Rlog.d(PROP_LOG_TAG, "SRVCC: but mImsConfHostConnection is null, try to find by callState");
                    } else {
                        Rlog.v(PROP_LOG_TAG, "SRVCC: ret= " + this.mImsConfHostConnection);
                        return this.mImsConfHostConnection;
                    }
                } else {
                    index++;
                }
            }
        }
        if (dc.number != null && !dc.number.isEmpty()) {
            Iterator it = this.mHandoverConnections.iterator();
            while (it.hasNext()) {
                Connection hoConn = (Connection) it.next();
                log("getHoConnection - compare number: hoConn= " + hoConn.toString());
                if (hoConn.getAddress() != null && hoConn.getAddress().contains(dc.number)) {
                    log("getHoConnection: Handover connection match found = " + hoConn.toString());
                    return hoConn;
                }
            }
        }
        Iterator it2 = this.mHandoverConnections.iterator();
        while (it2.hasNext()) {
            Connection hoConn2 = (Connection) it2.next();
            log("getHoConnection: compare state hoConn= " + hoConn2.toString());
            if (hoConn2.getStateBeforeHandover() == Call.stateFromDCState(dc.state)) {
                log("getHoConnection: Handover connection match found = " + hoConn2.toString());
                return hoConn2;
            }
        }
        return null;
    }

    private synchronized boolean restoreConferenceParticipantAddress() {
        if (this.mEconfSrvccConnectionIds == null) {
            Rlog.d(PROP_LOG_TAG, "SRVCC: restoreConferenceParticipantAddress():ignore because mEconfSrvccConnectionIds is empty");
            return false;
        }
        boolean finishRestore = false;
        int numOfParticipants = this.mEconfSrvccConnectionIds[0];
        for (int index = 1; index <= numOfParticipants; index++) {
            GsmCdmaConnection participantConnection = this.mConnections[this.mEconfSrvccConnectionIds[index] - 1];
            if (participantConnection != null) {
                Rlog.d(PROP_LOG_TAG, "SRVCC: found conference connections!");
                if (participantConnection.mOrigConnection instanceof MtkImsPhoneConnection) {
                    MtkImsPhoneConnection hostConnection = participantConnection.mOrigConnection;
                    if (hostConnection == null) {
                        Rlog.v(PROP_LOG_TAG, "SRVCC: no host, ignore connection: " + participantConnection);
                    } else {
                        String address = hostConnection.getConferenceParticipantAddress(index - 1);
                        if (participantConnection instanceof MtkGsmCdmaConnection) {
                            ((MtkGsmCdmaConnection) participantConnection).updateConferenceParticipantAddress(address);
                        }
                        finishRestore = true;
                        Rlog.v(PROP_LOG_TAG, "SRVCC: restore Connection=" + participantConnection + " with address:" + address);
                    }
                } else {
                    Rlog.v(PROP_LOG_TAG, "SRVCC: host is abnormal, ignore connection: " + participantConnection);
                }
            }
        }
        return finishRestore;
    }

    /* access modifiers changed from: package-private */
    public boolean hasParsingCEPCapability() {
        MtkHardwareConfig modem = this.mTelDevController.getModem(0);
        if (modem == null) {
            return false;
        }
        return modem.hasParsingCEPCapability();
    }

    public int getHandoverConnectionSize() {
        return this.mHandoverConnections.size();
    }
}
