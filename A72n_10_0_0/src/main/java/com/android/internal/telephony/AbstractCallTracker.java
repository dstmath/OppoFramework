package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ims.ImsReasonInfo;
import com.android.ims.ImsCall;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import com.android.internal.telephony.util.OemTelephonyUtils;

public abstract class AbstractCallTracker extends Handler {
    protected static final int AUTO_ANSWER_CDMA_TIMER = 2000;
    protected static final int AUTO_ANSWER_TIMER = 3;
    public static final int EVENT_ACCEPT_COMPLETE = 103;
    protected static final int EVENT_AUTO_ANSWER = 900;
    public static final int EVENT_DIAL_COMPLETE = 101;
    public static final int EVENT_HANGUP_COMPLETE = 102;
    public static final int EVENT_OPPO_END_INCOMING_CALL = 100;
    private static final String LOG_TAG = "AbstractTracker";
    public static final int TIME_END_INCOMING_CALL_DELAY = 65000;
    public static int TYPE_CALLIN = 0;
    public static int TYPE_CALLOUT = 1;
    public static int TYPE_PS = 4;
    public static int TYPE_SMSIN = 2;
    public static int TYPE_SMSOUT = 3;
    public boolean isOemSwitchAccept = false;
    public GsmCdmaCall mHangupCall = null;
    public GsmCdmaConnection mHangupConn = null;
    public CallTracker mImsPhoneCallTracker = null;
    public boolean mIsOemSwitcMO = false;
    public boolean mIsPeningAccept = false;
    public boolean mIsPeningSwitch = false;
    public int mOemLastMsg = -1;
    public String mPendingHangupAddr = null;
    public ImsPhoneCall mPendingHangupCall = null;
    protected IOppoCallTracker mReference;
    GsmCdmaConnection mRingingConnection = null;

    public void handleCallinControl(int index, DriverCall dc) {
        this.mReference.handleCallinControl(index, dc);
    }

    public void handleCallinControl(ImsCall imsCall) {
        this.mReference.handleCallinControl(imsCall);
    }

    public boolean handleCallinControl(CdmaCallWaitingNotification cw) {
        return this.mReference.handleCallinControl(cw);
    }

    public boolean handlDuplicateCdmaCW(CdmaCallWaitingNotification cw) {
        return this.mReference.handlDuplicateCdmaCW(cw);
    }

    public boolean isOemAutoAnswer(Phone phone) {
        AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone);
        if (!tmpPhone.is_test_card() || !tmpPhone.getOemAutoAnswer()) {
            return false;
        }
        Rlog.d(LOG_TAG, "isOemAutoAnswer for test card...");
        return true;
    }

    public void handleAutoAnswer(Phone phone) {
        this.mReference.handleAutoAnswer(phone);
    }

    public void oemNofiyNoService(Phone phone, boolean isIdle) {
        try {
            if (!SystemProperties.get("persist.radio.multisim.config", PhoneConfigurationManager.SSSS).equals(PhoneConfigurationManager.DSDS)) {
                return;
            }
            if (phone.getContext().getPackageManager().hasSystemFeature("oppo.ct.optr")) {
                int pid = 0;
                if (phone.getPhoneId() == 0) {
                    if (phone.getServiceStateTracker().mSS.getRoaming()) {
                        pid = 1;
                    } else {
                        return;
                    }
                }
                Phone oPhone = PhoneFactory.getPhone(pid);
                if (oPhone != null && pid == 0 && oPhone.getServiceStateTracker().mSS.getRoaming()) {
                }
            }
        } catch (Exception e) {
        }
    }

    public boolean isImsCallHangupPending() {
        return this.mPendingHangupCall != null;
    }

    public String getPendingHangupAddr() {
        return this.mPendingHangupAddr;
    }

    public synchronized boolean isOemInImsEmergencyCall() {
        return false;
    }

    public void setPendingHangupAddr(String addr) {
        this.mPendingHangupAddr = addr;
    }

    public void setPendingHangupCall(ImsPhoneCall pendingHangupCall) {
        this.mPendingHangupCall = pendingHangupCall;
    }

    public ImsPhoneCall getPendingHangupCall() {
        return this.mPendingHangupCall;
    }

    public void oemNotifySrvccComplete() {
        IOppoCallTracker iOppoCallTracker = this.mReference;
        if (iOppoCallTracker != null) {
            iOppoCallTracker.oemNotifySrvccComplete();
        }
    }

    public boolean isSrvccConfHost(Connection c) {
        if (!c.isConferenceHost()) {
            return false;
        }
        Rlog.e(LOG_TAG, "Conference Host from SRVCC");
        return true;
    }

    public void oemHandleFeatureCapabilityChanged() {
        this.mReference.oemHandleFeatureCapabilityChanged();
    }

    public boolean oemSRVCCWhenHangup(ImsPhoneCall call, ImsPhoneConnection pendingMO) {
        return this.mReference.oemSRVCCWhenHangup(call, pendingMO);
    }

    public int oemGetDisconnectCauseFromReasonInfo(int code) {
        return this.mReference.oemGetDisconnectCauseFromReasonInfo(code);
    }

    public synchronized void processPendingHangup(String msg) {
        this.mReference.processPendingHangup(msg);
    }

    public void oemRetryResumeAfterResumeFail(ImsReasonInfo reasonInfo) {
        this.mReference.oemRetryResumeAfterResumeFail(reasonInfo);
    }

    public boolean getVowifiRegStatus() {
        return this.mReference.getVowifiRegStatus();
    }

    public void oemResetImsCapabilities() {
        this.mReference.oemResetImsCapabilities();
    }

    public void handlePhoneStateChanged(GsmCdmaCallTracker mGCCT, PhoneConstants.State oldState, PhoneConstants.State mState) {
        this.mReference.handlePhoneStateChanged(mGCCT, oldState, mState);
    }

    public void handleDropedCall() {
        this.mReference.handleDropedCall();
    }

    public void handleCallStatePhoneIdle(Connection mPendingMO, int mPendingOperations) {
        this.mReference.handleCallStatePhoneIdle(mPendingMO, mPendingOperations);
    }

    public void handlePendingHangupSRVCC(CallTracker mImsPhoneCallTracker2, int index) {
        this.mReference.handlePendingHangupSRVCC(mImsPhoneCallTracker2, index);
    }

    public void handleExtraCallStateChanged(GsmCdmaConnection conn, DriverCall dc, int dcSize, boolean hasNonHangupStateChanged) {
        this.mReference.handleExtraCallStateChanged(conn, dc, dcSize, hasNonHangupStateChanged);
    }

    public void handleSwitchResult(GsmCdmaConnection mPendingMO, Message msg) {
        this.mReference.handleSwitchResult(mPendingMO, msg);
    }

    public void hanleEndIncomingCall(GsmCdmaConnection mRingingConnection2) {
        this.mReference.hanleEndIncomingCall(mRingingConnection2);
    }

    public void handleDialComplete(GsmCdmaConnection mPendingMO, AsyncResult ar) {
        this.mReference.handleDialComplete(mPendingMO, ar);
    }

    public void handleHangupComplete(Message msg) {
        this.mReference.handleHangupComplete(msg);
    }

    public void handleAcceptComplete(Message msg) {
        this.mReference.handleAcceptComplete(msg);
    }

    public boolean getHangupPendingMO() {
        return false;
    }

    public void setHangupPendingMO(boolean val) {
    }

    public void decPendingOperations() {
    }

    public void incPendingOperations() {
    }

    public void oppoOperationComplete() {
    }

    public Message oppoObtainCompleteMessage() {
        return null;
    }

    public void HangupLocal(GsmCdmaCall call) {
    }

    public void HangupLocal(ImsPhoneCall call) {
    }

    public void colorosSetCause(GsmCdmaCall call, int cause) {
    }

    public void handleImsPhoneStateChanged(PhoneConstants.State oldState, PhoneConstants.State mState) {
        this.mReference.handleImsPhoneStateChanged(oldState, mState);
    }

    public boolean oemProcessPendingHangup(ImsPhoneCall call, ImsPhoneConnection conn) {
        return this.mReference.oemProcessPendingHangup(call, conn);
    }
}
