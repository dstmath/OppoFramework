package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.os.Registrant;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.GsmCdmaCall;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.UiccCardApplication;
import java.util.Iterator;
import vendor.mediatek.hardware.mtkradioex.V1_0.MtkApnTypes;

public class MtkGsmCdmaConnection extends GsmCdmaConnection {
    private static final int MO_CALL_VIBRATE_TIME = 200;
    private static final String PROP_LOG_TAG = "GsmCdmaConn";
    String mForwardingAddress;
    private boolean mIsIncomingCallGwsd;
    private boolean mIsRealConnected;
    private boolean mReceivedAccepted;
    String mRedirectingAddress;
    int mRejectCauseToRIL;

    public synchronized boolean isIncomingCallGwsd() {
        Rlog.d("GsmCdmaConnection", "isIncomingCallGwsd: " + this.mIsIncomingCallGwsd);
        return this.mIsIncomingCallGwsd;
    }

    public MtkGsmCdmaConnection(GsmCdmaPhone phone, String dialString, GsmCdmaCallTracker ct, GsmCdmaCall parent, boolean isEmergencyCall) {
        super(phone, dialString, ct, parent, isEmergencyCall);
        this.mRejectCauseToRIL = -1;
        this.mIsIncomingCallGwsd = false;
        this.mIsRealConnected = false;
        this.mReceivedAccepted = false;
    }

    public MtkGsmCdmaConnection(GsmCdmaPhone phone, DriverCall dc, GsmCdmaCallTracker ct, int index) {
        super(phone, dc, ct, index);
        this.mRejectCauseToRIL = -1;
        this.mIsIncomingCallGwsd = false;
        if (((MtkGsmCdmaCallTracker) ct).mHelper.isGwsdCall()) {
            this.mIsIncomingCallGwsd = true;
            ((MtkGsmCdmaCallTracker) ct).mHelper.setGwsdCall(false);
        }
        String origAddress = ((MtkGsmCdmaCallTracker) ct).mMtkGsmCdmaCallTrackerExt.convertAddress(this.mAddress);
        if (origAddress != null) {
            setConnectionExtras(((MtkGsmCdmaCallTracker) ct).mMtkGsmCdmaCallTrackerExt.getAddressExtras(this.mAddress));
            this.mNumberConverted = true;
            this.mConvertedNumber = this.mAddress;
            this.mAddress = origAddress;
        }
    }

    public MtkGsmCdmaConnection(Context context, CdmaCallWaitingNotification cw, GsmCdmaCallTracker ct, GsmCdmaCall parent) {
        super(context, cw, ct, parent);
        this.mRejectCauseToRIL = -1;
        this.mIsIncomingCallGwsd = false;
    }

    public boolean compareTo(DriverCall c) {
        if (!this.mIsIncoming && !c.isMT) {
            return true;
        }
        if (isPhoneTypeGsm() && this.mOrigConnection != null) {
            return true;
        }
        boolean addrChanged2 = this.mOwner.mMtkGsmCdmaCallTrackerExt.isAddressChanged(this.mNumberConverted, this.mAddress, PhoneNumberUtils.stringFromStringAndTOA(c.number, c.TOA));
        if (this.mIsIncoming != c.isMT || addrChanged2) {
            return false;
        }
        return true;
    }

    public String getForwardingAddress() {
        return this.mForwardingAddress;
    }

    public void setForwardingAddress(String address) {
        this.mForwardingAddress = address;
    }

    public String getRedirectingAddress() {
        return this.mRedirectingAddress;
    }

    public void setRedirectingAddress(String address) {
        this.mRedirectingAddress = address;
    }

    /* access modifiers changed from: protected */
    public int disconnectCauseFromCode(int causeCode) {
        IccCardApplicationStatus.AppState uiccAppState;
        GsmCdmaPhone phone = this.mOwner.getPhone();
        int serviceState = phone.getServiceState().getState();
        UiccCardApplication cardApp = phone.getUiccCardApplication();
        if (cardApp != null) {
            uiccAppState = cardApp.getState();
        } else {
            uiccAppState = IccCardApplicationStatus.AppState.APPSTATE_UNKNOWN;
        }
        Rlog.d(PROP_LOG_TAG, "disconnectCauseFromCode, causeCode:" + causeCode + ", cardApp:" + cardApp + ", serviceState:" + serviceState + ", uiccAppState:" + uiccAppState);
        if (causeCode == 0) {
            if (isEmergencyCall()) {
                return 2;
            }
            causeCode = MtkApnTypes.MTKALL;
        }
        if (!isEmergencyCall() || (causeCode != 31 && causeCode != 79)) {
            return MtkGsmCdmaConnection.super.disconnectCauseFromCode(causeCode);
        }
        return 2;
    }

    public boolean update(DriverCall dc) {
        boolean changed;
        boolean changed2 = false;
        boolean wasConnectingInOrOut = isConnectingInOrOut();
        boolean z = true;
        boolean wasHolding = getState() == Call.State.HOLDING;
        GsmCdmaCall newParent = parentFromDCState(dc.state);
        log("parent= " + this.mParent + ", newParent= " + newParent);
        if (isPhoneTypeGsm() && this.mOrigConnection != null) {
            log("update: mOrigConnection is not null");
        } else if (isIncoming()) {
            log(" mNumberConverted " + this.mNumberConverted);
            if (this.mOwner.mMtkGsmCdmaCallTrackerExt.isAddressChanged(this.mNumberConverted, dc.number, this.mAddress, this.mConvertedNumber)) {
                log("update: phone # changed!");
                this.mAddress = dc.number;
                changed2 = true;
            }
        }
        int newAudioQuality = getAudioQualityFromDC(dc.audioQuality);
        if (getAudioQuality() != newAudioQuality) {
            StringBuilder sb = new StringBuilder();
            sb.append("update: audioQuality # changed!:  ");
            sb.append(newAudioQuality == 2 ? "high" : "standard");
            log(sb.toString());
            setAudioQuality(newAudioQuality);
            changed2 = true;
        }
        if (!TextUtils.isEmpty(dc.name) && !dc.name.equals(this.mCnapName)) {
            changed2 = true;
            this.mCnapName = dc.name;
        }
        log("--dssds----" + this.mCnapName);
        this.mCnapNamePresentation = dc.namePresentation;
        this.mNumberPresentation = dc.numberPresentation;
        if (newParent != this.mParent) {
            if (this.mParent != null) {
                this.mParent.detach(this);
            }
            newParent.attach(this, dc);
            this.mParent = newParent;
            changed = true;
        } else {
            changed = changed2 || this.mParent.update(this, dc);
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("update: parent=");
        sb2.append(this.mParent);
        sb2.append(", hasNewParent=");
        if (newParent == this.mParent) {
            z = false;
        }
        sb2.append(z);
        sb2.append(", wasConnectingInOrOut=");
        sb2.append(wasConnectingInOrOut);
        sb2.append(", wasHolding=");
        sb2.append(wasHolding);
        sb2.append(", isConnectingInOrOut=");
        sb2.append(isConnectingInOrOut());
        sb2.append(", changed=");
        sb2.append(changed);
        log(sb2.toString());
        if (wasConnectingInOrOut && !isConnectingInOrOut()) {
            onConnectedInOrOut();
        }
        if (changed && !wasHolding && getState() == Call.State.HOLDING) {
            onStartedHolding();
        }
        if (!isPhoneTypeGsm()) {
            log("state=" + getState() + ", mReceivedAccepted=" + this.mReceivedAccepted);
            if (getState() == Call.State.ACTIVE && this.mReceivedAccepted) {
                onCdmaCallAccepted();
                this.mReceivedAccepted = false;
            }
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    public void processNextPostDialChar() {
        char c;
        Message notifyMessage;
        if (this.mPostDialState == Connection.PostDialState.CANCELLED) {
            releaseWakeLock();
            return;
        }
        if (this.mPostDialString == null || this.mPostDialString.length() <= this.mNextPostDialChar || this.mDisconnected) {
            setPostDialState(Connection.PostDialState.COMPLETE);
            releaseWakeLock();
            c = 0;
        } else {
            setPostDialState(Connection.PostDialState.STARTED);
            String str = this.mPostDialString;
            int i = this.mNextPostDialChar;
            this.mNextPostDialChar = i + 1;
            c = str.charAt(i);
            if (!processPostDialChar(c)) {
                this.mHandler.obtainMessage(3).sendToTarget();
                Rlog.e("GsmCdmaConnection", "processNextPostDialChar: c=" + c + " isn't valid!");
                return;
            }
        }
        notifyPostDialListenersNextChar(c);
        Registrant postDialHandler = this.mOwner.getPhone().getPostDialHandler();
        if (postDialHandler != null && (notifyMessage = postDialHandler.messageForRegistrant()) != null) {
            Connection.PostDialState state = this.mPostDialState;
            AsyncResult ar = AsyncResult.forMessage(notifyMessage);
            ar.result = this;
            ar.userObj = state;
            notifyMessage.arg1 = c;
            notifyMessage.sendToTarget();
        }
    }

    public boolean isRealConnected() {
        return this.mIsRealConnected;
    }

    /* access modifiers changed from: package-private */
    public boolean onCdmaCallAccepted() {
        log("onCdmaCallAccepted, mIsRealConnected=" + this.mIsRealConnected + ", state=" + getState());
        if (getState() != Call.State.ACTIVE) {
            this.mReceivedAccepted = true;
            return false;
        }
        if (!this.mIsRealConnected) {
            this.mIsRealConnected = true;
            processNextPostDialChar();
        }
        return true;
    }

    private boolean isInChina() {
        return !this.mOwner.getPhone().is_test_card();
    }

    private void vibrateForAccepted() {
        if ("0".equals(SystemProperties.get("persist.vendor.radio.telecom.vibrate", "1"))) {
            log("vibrateForAccepted, disabled by Engineer Mode");
        } else {
            ((Vibrator) this.mParent.getPhone().getContext().getSystemService("vibrator")).vibrate(200);
        }
    }

    public void onConnectedInOrOut() {
        this.mConnectTime = System.currentTimeMillis();
        this.mConnectTimeReal = SystemClock.elapsedRealtime();
        this.mDuration = 0;
        log("onConnectedInOrOut: connectTime=" + this.mConnectTime);
        if (this.mIsIncoming) {
            releaseWakeLock();
        } else if (isPhoneTypeGsm()) {
            processNextPostDialChar();
        } else {
            int count = this.mParent.mConnections.size();
            log("mParent.mConnections.size()=" + count);
            Iterator it = this.mParent.mConnections.iterator();
            while (it.hasNext()) {
                if (!((Connection) it.next()).isAlive()) {
                    count--;
                }
            }
            if (!isInChina() && !this.mIsRealConnected && count == 1) {
                this.mIsRealConnected = true;
                processNextPostDialChar();
                vibrateForAccepted();
            }
            if (count > 1) {
                this.mIsRealConnected = true;
                processNextPostDialChar();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resumeHoldAfterDialFailed() {
        if (this.mParent != null) {
            this.mParent.detach(this);
        }
        this.mParent = this.mOwner.mForegroundCall;
        this.mParent.attachFake(this, Call.State.ACTIVE);
    }

    /* access modifiers changed from: package-private */
    public void updateConferenceParticipantAddress(String address) {
        this.mAddress = address;
    }

    public boolean isMultiparty() {
        if (this.mParent != null) {
            return this.mParent.isMultiparty();
        }
        return false;
    }

    public void setRejectWithCause(int telephonyDisconnectCode) {
        if (this.mParent == null || this.mOwner == null) {
            Rlog.d(PROP_LOG_TAG, "setRejectWithCause fail. mParent(" + this.mParent + "), mOwner(" + this.mOwner + ")");
            return;
        }
        GsmCdmaPhone phone = this.mOwner.getPhone();
        if (MtkIncomingCallChecker.isMtkEnhancedCallBlockingEnabled(phone.getContext(), phone.getSubId())) {
            Rlog.d(PROP_LOG_TAG, "setRejectWithCause set (" + this.mRejectCauseToRIL + " to " + telephonyDisconnectCode + ")");
            this.mRejectCauseToRIL = telephonyDisconnectCode;
        }
    }

    public int getRejectWithCause() {
        return this.mRejectCauseToRIL;
    }

    public void clearRejectWithCause() {
        if (this.mRejectCauseToRIL != -1) {
            Rlog.d(PROP_LOG_TAG, "clearRejectWithCause (" + this.mRejectCauseToRIL + " to -1)");
            this.mRejectCauseToRIL = -1;
        }
    }

    public void onHangupLocal() {
        clearRejectWithCause();
        MtkGsmCdmaConnection.super.onHangupLocal();
    }
}
