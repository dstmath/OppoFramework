package com.oppo.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractCallTracker;
import com.android.internal.telephony.AbstractGsmCdmaPhone;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.GsmCdmaCall;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IOppoCallTracker;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import com.oppo.internal.telephony.utils.OppoPolicyController;

public class OppoGsmCdmaCallTrackerReference extends Handler implements IOppoCallTracker {
    private static final int AUTO_ANSWER_CDMA_TIMER = 2000;
    private static final int AUTO_ANSWER_GSM_TIMER = 3;
    private static final int EVENT_REQUEST_VOICE_RADIO_TECH_DONE = 40;
    protected static final String LOG_TAG = "OppoGsmCdmaCallTracker";
    private static final int TIME_SLOT_IGNORE_CDMA_CW = 6500;
    protected GsmCdmaCall mBackgroundCall;
    protected CommandsInterface mCi;
    protected GsmCdmaCall mForegroundCall;
    private GsmCdmaCallTracker mGsmCdmaCallTracker;
    private String mLastCdmaCWAddr = null;
    private long mLastCdmaCWTime = 0;
    protected GsmCdmaCall mRingingCall;

    public OppoGsmCdmaCallTrackerReference(AbstractCallTracker callTracker) {
        this.mGsmCdmaCallTracker = (GsmCdmaCallTracker) OemTelephonyUtils.typeCasting(GsmCdmaCallTracker.class, callTracker);
        this.mRingingCall = this.mGsmCdmaCallTracker.mRingingCall;
        this.mForegroundCall = this.mGsmCdmaCallTracker.mForegroundCall;
        this.mBackgroundCall = this.mGsmCdmaCallTracker.mBackgroundCall;
        this.mCi = this.mGsmCdmaCallTracker.mCi;
    }

    public GsmCdmaPhone getPhone() {
        return this.mGsmCdmaCallTracker.getPhone();
    }

    public void handleCallinControl(int index, DriverCall dc) {
        logd("handleCallinControl - index = " + index + " , DriverCall = " + dc);
        if ((DriverCall.State.INCOMING == dc.state || DriverCall.State.WAITING == dc.state) && !OppoPolicyController.isCallInEnable(getPhone())) {
            logd("ctmm vi block");
            try {
                this.mGsmCdmaCallTracker.hangup(this.mGsmCdmaCallTracker.mConnections[index]);
            } catch (CallStateException e) {
                loge("Exception in hangup call");
            }
        }
    }

    public boolean handleCallinControl(CdmaCallWaitingNotification cw) {
        if (cw == null) {
            return false;
        }
        logd("handleCallinControl - cw = " + cw);
        if (OppoPolicyController.isCallInEnable(getPhone()) || cw.isPresent != 1) {
            return false;
        }
        this.mCi.hangupWaitingOrBackground((Message) null);
        return true;
    }

    public boolean handlDuplicateCdmaCW(CdmaCallWaitingNotification cw) {
        GsmCdmaConnection lastRingConn;
        String mWaittingCallAddr = cw.number;
        logd("handlDuplicateCdmaCW cwNumber = " + mWaittingCallAddr);
        if (mWaittingCallAddr == null || mWaittingCallAddr.length() <= 0 || (lastRingConn = this.mRingingCall.getLatestConnection()) == null || !mWaittingCallAddr.equals(lastRingConn.getAddress())) {
            return false;
        }
        logd("handlDuplicateCdmaCW, skip duplicate waiting call!");
        return true;
    }

    public void handleAutoAnswer(Phone phone) {
        if (phone != null && ((AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker)).isOemAutoAnswer(phone)) {
            int delayTime = phone.getPhoneType() == 1 ? 3 : AUTO_ANSWER_CDMA_TIMER;
            logd("handleAutoAnswer...");
            this.mGsmCdmaCallTracker.postDelayed(new Runnable() {
                /* class com.oppo.internal.telephony.OppoGsmCdmaCallTrackerReference.AnonymousClass1 */

                public void run() {
                    try {
                        OppoGsmCdmaCallTrackerReference.this.mGsmCdmaCallTracker.acceptCall();
                    } catch (Exception e) {
                        OppoGsmCdmaCallTrackerReference oppoGsmCdmaCallTrackerReference = OppoGsmCdmaCallTrackerReference.this;
                        oppoGsmCdmaCallTrackerReference.loge("EVENT_AUTO_ANSWER: e " + e);
                    }
                }
            }, (long) delayTime);
        }
    }

    public void handlePhoneStateChanged(GsmCdmaCallTracker mGCCT, PhoneConstants.State oldState, PhoneConstants.State mState) {
        logd("handlePhoneStateChanged");
        OppoPhoneUtil.setOemVoocState(oldState, mState);
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker);
        tmpCallTracker.oemNofiyNoService(getPhone(), mState == PhoneConstants.State.IDLE);
        if (!getPhone().isPhoneTypeGsm() && mState == PhoneConstants.State.IDLE) {
            tmpCallTracker.isOemSwitchAccept = false;
            tmpCallTracker.mOemLastMsg = -1;
        }
    }

    public void handleDropedCall() {
        logd("handleDropedCall");
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker);
        tmpCallTracker.mHangupConn = null;
        tmpCallTracker.mHangupCall = null;
        if (this.mGsmCdmaCallTracker.mConnections != null) {
            for (int i = 0; i < this.mGsmCdmaCallTracker.mConnections.length; i++) {
                if (this.mGsmCdmaCallTracker.mConnections[i] != null) {
                    this.mGsmCdmaCallTracker.mConnections[i].onDisconnect(3);
                    this.mGsmCdmaCallTracker.mConnections[i] = null;
                }
            }
        }
    }

    public void handleCallStatePhoneIdle(Connection mPendingMO, int mPendingOperations) {
        logd("handleCallStatePhoneIdle");
        if (mPendingMO != null) {
            logd("update phone state, clear pending mo");
            mPendingMO.onDisconnect(3);
            if (mPendingOperations > 0) {
                mPendingOperations--;
            }
        }
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker);
        if (tmpCallTracker.mHangupConn != null) {
            logd("update phone state, clear pending hangup");
            tmpCallTracker.mHangupConn.onDisconnect(3);
            tmpCallTracker.mHangupConn = null;
            tmpCallTracker.mHangupCall = null;
            if (mPendingOperations > 0) {
                int mPendingOperations2 = mPendingOperations - 1;
            }
        }
        ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, getPhone())).clearSRVCC();
        AbstractGsmCdmaPhone tmpGsmCdmaPhone = (AbstractGsmCdmaPhone) OemTelephonyUtils.typeCasting(AbstractGsmCdmaPhone.class, getPhone());
        if (tmpGsmCdmaPhone.isPhoneTypeSwitchPending()) {
            logd("update phone state, to deal with pending PhoneType SWITCHING");
            getPhone().mCi.getVoiceRadioTechnology(getPhone().obtainMessage(40));
            tmpGsmCdmaPhone.clearPhoneTypeSwitchPending();
        }
    }

    public void handlePendingHangupSRVCC(CallTracker mImsPhoneCallTracker, int index) {
        logd("handlePendingHangupSRVCC");
        if (mImsPhoneCallTracker != null) {
            AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, mImsPhoneCallTracker);
            if (tmpCallTracker.isImsCallHangupPending()) {
                logd("There is pending hangup before SRVCC " + tmpCallTracker.getPendingHangupAddr());
                String HOConnectionAddr = this.mGsmCdmaCallTracker.mConnections[index].getAddress();
                if (HOConnectionAddr != null && HOConnectionAddr.equals(tmpCallTracker.getPendingHangupAddr())) {
                    try {
                        logd("Pending hang up in SRVCC Case");
                        this.mGsmCdmaCallTracker.hangup(this.mGsmCdmaCallTracker.mConnections[index]);
                        tmpCallTracker.setPendingHangupCall((ImsPhoneCall) null);
                        tmpCallTracker.setPendingHangupAddr((String) null);
                    } catch (CallStateException e) {
                        logd("unexpected error on hangup of SRVCC Case");
                    }
                }
            }
        }
    }

    public void handleExtraCallStateChanged(GsmCdmaConnection conn, DriverCall dc, int dcSize, boolean hasNonHangupStateChanged) {
        logd("handleExtraCallStateChanged");
        logd("accept the call,conn:" + conn.getState() + "/dc:" + dc.state);
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker);
        if (!tmpCallTracker.isOemSwitchAccept || conn.getState() != Call.State.WAITING) {
            if (conn.getState() == Call.State.DISCONNECTED && dc.state == DriverCall.State.DIALING && !getPhone().isPhoneTypeGsm() && !this.mForegroundCall.isMultiparty() && this.mBackgroundCall.isIdle()) {
                conn.update(dc);
                try {
                    this.mGsmCdmaCallTracker.hangup(conn);
                    logd("hangup the dialing dc!");
                } catch (Exception ex) {
                    logd("hangup the dialing dc!" + ex.getMessage());
                }
            } else if (conn.getState() == Call.State.ACTIVE && dcSize == 1 && conn.isConferenceHost()) {
                logd("hangup the lonely conference host!");
                try {
                    this.mGsmCdmaCallTracker.hangup(conn);
                } catch (Exception ex2) {
                    logd("hangup the lonly conference host!" + ex2.getMessage());
                }
            }
        } else if (dc.state == DriverCall.State.INCOMING) {
            tmpCallTracker.isOemSwitchAccept = false;
            tmpCallTracker.mOemLastMsg = -1;
            logd("accept the call!");
            this.mGsmCdmaCallTracker.setMute(false);
            try {
                this.mGsmCdmaCallTracker.acceptCall();
            } catch (Exception e) {
            }
        }
    }

    public void handleSwitchResult(GsmCdmaConnection mPendingMO, Message msg) {
        logd("handleSwitchResult");
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker);
        if (((AsyncResult) msg.obj).exception != null) {
            if (tmpCallTracker.mIsOemSwitcMO && mPendingMO != null) {
                tmpCallTracker.setHangupPendingMO(true);
            }
            if (tmpCallTracker.isOemSwitchAccept) {
                tmpCallTracker.mOemLastMsg = msg.what;
            } else {
                getPhone().notifySuppServiceFailed(PhoneInternalInterface.SuppService.SWITCH);
            }
        } else {
            logd("accept the call 2!");
            tmpCallTracker.isOemSwitchAccept = false;
            tmpCallTracker.mOemLastMsg = -1;
        }
        tmpCallTracker.mIsOemSwitcMO = false;
    }

    public void hanleEndIncomingCall(GsmCdmaConnection mRingingConnection) {
        logd("hanleEndIncomingCall");
        GsmCdmaConnection ringing = (GsmCdmaConnection) this.mRingingCall.getEarliestConnection();
        if (ringing != null && ringing == mRingingConnection) {
            try {
                this.mGsmCdmaCallTracker.hangup(mRingingConnection);
                mRingingConnection.onDisconnect(1);
                logd("EVENT_OPPO_END_INCOMING_CALL");
            } catch (Exception e) {
            }
        }
    }

    public void handleDialComplete(GsmCdmaConnection mPendingMO, AsyncResult ar) {
        logd("handleDialComplete");
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker);
        tmpCallTracker.oppoOperationComplete();
        if (ar.exception != null) {
            if (mPendingMO != null && !tmpCallTracker.getHangupPendingMO()) {
                int cause = 36;
                CommandException.Error error = ar.exception.getCommandError();
                if (error == CommandException.Error.FDN_CHECK_FAILURE) {
                    cause = 21;
                }
                if (error == CommandException.Error.GENERIC_FAILURE || error == CommandException.Error.FDN_CHECK_FAILURE) {
                    mPendingMO.onDisconnect(cause);
                }
            }
            tmpCallTracker.incPendingOperations();
            tmpCallTracker.oppoOperationComplete();
        }
    }

    public void handleHangupComplete(Message msg) {
        logd("handleHangupComplete");
        AsyncResult ar = (AsyncResult) msg.obj;
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker);
        if ((ar == null || ar.exception == null) && tmpCallTracker.mHangupConn != null) {
            tmpCallTracker.mHangupConn.onDisconnect(3);
        }
        if (tmpCallTracker.mIsPeningSwitch) {
            this.mGsmCdmaCallTracker.setMute(false);
            try {
                this.mGsmCdmaCallTracker.switchWaitingOrHoldingAndActive();
            } catch (Exception e) {
            }
        }
        tmpCallTracker.mIsPeningSwitch = false;
        tmpCallTracker.mHangupConn = null;
        tmpCallTracker.oppoOperationComplete();
        getPhone().notifyPreciseCallStateChanged();
    }

    public void handleAcceptComplete(Message msg) {
        logd("handleAcceptComplete");
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mGsmCdmaCallTracker);
        tmpCallTracker.mIsPeningAccept = false;
        if (tmpCallTracker.mHangupCall != null) {
            tmpCallTracker.colorosSetCause(tmpCallTracker.mHangupCall, 16);
            tmpCallTracker.HangupLocal(tmpCallTracker.mHangupCall);
            getPhone().notifyPreciseCallStateChanged();
            tmpCallTracker.decPendingOperations();
            logd("(ringing) hangup waiting or background");
            try {
                this.mCi.hangupWaitingOrBackground(tmpCallTracker.oppoObtainCompleteMessage());
            } catch (Exception e) {
                Rlog.w(LOG_TAG, "GsmCallTracker WARN: (ringing) hangup waiting or background " + tmpCallTracker.mHangupCall);
            } catch (Throwable th) {
                tmpCallTracker.mHangupCall = null;
                throw th;
            }
            tmpCallTracker.mHangupCall = null;
            return;
        }
        tmpCallTracker.oppoOperationComplete();
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d("OppoGsmCdmaCallTracker/" + getPhone().getPhoneId(), s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e("OppoGsmCdmaCallTracker/" + getPhone().getPhoneId(), s);
    }
}
