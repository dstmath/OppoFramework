package com.oppo.internal.telephony.imsphone;

import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ims.ImsReasonInfo;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.AbstractCallTracker;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.IOppoCallTracker;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import com.oppo.internal.telephony.utils.OppoPolicyController;

public class OppoImsPhoneCallTrackerReference extends Handler implements IOppoCallTracker {
    private static final int AUTO_ANSWER_IMS_TIMER = 500;
    private static final int EVENT_OPPO_PENGDING_HANGUP = 101;
    private static final int EVENT_RESUME_BACKGROUND = 102;
    private static final String IMS_VOLTE_ENABLE = "volte";
    private static final String IMS_VOWIFI_ENABLE = "vowifi";
    protected static final String LOG_TAG = "OppoImsPhoneCallTracker";
    private static final int MO_RAMDOM_DELAY = 190;
    private static final int MO_RAMDOM_DELAY_BASE = 2100;
    private static final int MT_RAMDOM_DELAY = 200;
    private static final String PRO_IMS_TYPE = "gsm.ims.type";
    private static final int TIME_OPPO_PENGDING_HANGUP = 500;
    private static final int UNITS_STEPS = 10;
    private long mDelayToResume = 0;
    /* access modifiers changed from: private */
    public ImsPhoneCallTracker mImsPhoneCallTracker;
    private boolean mIsVowifiRegistered = false;
    private ImsPhone mPhone;
    private boolean mTmpIsUtEnabled = false;
    private boolean mTmpIsVolteEnabled = false;
    private boolean mTmpIsVowifiEnabled = false;

    public OppoImsPhoneCallTrackerReference(AbstractCallTracker callTracker) {
        this.mImsPhoneCallTracker = (ImsPhoneCallTracker) OemTelephonyUtils.typeCasting(ImsPhoneCallTracker.class, callTracker);
        this.mPhone = this.mImsPhoneCallTracker.getPhone();
    }

    public void handleCallinControl(ImsCall imsCall) {
        logd("handleCallinControl - imsCall = " + imsCall);
        if (!OppoPolicyController.isCallInEnable(getImsPhone().getDefaultPhone())) {
            logd("ctmm vi block");
            try {
                imsCall.reject(504);
            } catch (Exception e) {
                loge("Exception in terminate call");
            }
        }
    }

    public ImsPhone getImsPhone() {
        return this.mImsPhoneCallTracker.getPhone();
    }

    public void handleAutoAnswer(Phone phone) {
        AbstractCallTracker tmpCallTracker;
        if (phone != null && (tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mImsPhoneCallTracker)) != null && tmpCallTracker.isOemAutoAnswer(phone.getDefaultPhone())) {
            logd("handleAutoAnswer...");
            this.mImsPhoneCallTracker.postDelayed(new Runnable() {
                /* class com.oppo.internal.telephony.imsphone.OppoImsPhoneCallTrackerReference.AnonymousClass1 */

                public void run() {
                    try {
                        OppoImsPhoneCallTrackerReference.this.mImsPhoneCallTracker.acceptCall(0);
                    } catch (Exception e) {
                        OppoImsPhoneCallTrackerReference oppoImsPhoneCallTrackerReference = OppoImsPhoneCallTrackerReference.this;
                        oppoImsPhoneCallTrackerReference.loge("EVENT_AUTO_ANSWER: e " + e);
                    }
                }
            }, 500);
        }
    }

    public void oemHandleFeatureCapabilityChanged() {
        boolean isUtEnabled = this.mImsPhoneCallTracker.isUtEnabled();
        boolean isVolteEnabled = this.mImsPhoneCallTracker.isVolteEnabled();
        boolean isVoWifiEnabled = this.mImsPhoneCallTracker.isVowifiEnabled();
        if (this.mTmpIsUtEnabled != isUtEnabled) {
            logd("onFeatureCapabilityChanged: isUtEnabled Change");
            this.mPhone.notifyCallForwardingIndicator();
            this.mTmpIsUtEnabled = isUtEnabled;
        }
        boolean z = false;
        this.mPhone.setImsRegistered(isVolteEnabled || isVoWifiEnabled);
        String registerImsType = "";
        if (isVolteEnabled) {
            registerImsType = IMS_VOLTE_ENABLE;
        } else if (isVoWifiEnabled) {
            registerImsType = IMS_VOWIFI_ENABLE;
        }
        this.mIsVowifiRegistered = registerImsType == IMS_VOWIFI_ENABLE;
        SystemProperties.set(PRO_IMS_TYPE + this.mPhone.getPhoneId(), registerImsType);
        if (isVolteEnabled || isVoWifiEnabled) {
            z = true;
        }
        int status = z ? 4 : 5;
        boolean isVoLTESwitchOn = ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId()).isEnhanced4gLteModeSettingEnabledByUser();
        boolean isWfcSwitchOn = ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId()).isWfcEnabledByUser();
        logd("handleFeatureCapabilityChanged: mTmpIsVolteEnabled = " + this.mTmpIsVolteEnabled + ", mTmpIsVowifiEnabled = " + this.mTmpIsVowifiEnabled + ", isVoLTESwitchOn = " + isVoLTESwitchOn + ", isWfcSwitchOn = " + isWfcSwitchOn);
        if (!isVolteEnabled || isVoLTESwitchOn || !isVoWifiEnabled || isWfcSwitchOn) {
            if (!(this.mTmpIsVolteEnabled == isVolteEnabled && this.mTmpIsVowifiEnabled == isVoWifiEnabled)) {
                if (this.mTmpIsVolteEnabled != isVolteEnabled) {
                    this.mTmpIsVolteEnabled = isVolteEnabled;
                }
                if (this.mTmpIsVowifiEnabled != isVoWifiEnabled) {
                    this.mTmpIsVowifiEnabled = isVoWifiEnabled;
                }
            }
            logd("handleFeatureCapabilityChanged: registerImsType = " + registerImsType + ",, status " + status);
        }
    }

    public boolean getVowifiRegStatus() {
        return this.mIsVowifiRegistered;
    }

    public boolean oemSRVCCWhenHangup(ImsPhoneCall call, ImsPhoneConnection pendingMO) {
        String pendingHangupAddr;
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mImsPhoneCallTracker);
        AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone.getDefaultPhone());
        ImsPhoneCall pendingHangupCall = tmpCallTracker.getPendingHangupCall();
        if (!tmpPhone.isSRVCC() && (pendingHangupCall != null || pendingMO == null || pendingMO.getState().isAlive())) {
            return false;
        }
        logd("(foregnd) hangup dialing or alerting pending...");
        tmpCallTracker.setPendingHangupCall(call);
        if (tmpPhone.isSRVCC()) {
            pendingHangupAddr = call.getFirstConnection().getAddress();
        } else {
            pendingHangupAddr = pendingMO.getAddress();
        }
        tmpCallTracker.setPendingHangupAddr(pendingHangupAddr);
        tmpCallTracker.HangupLocal(call);
        sendEmptyMessageDelayed(101, 500);
        return true;
    }

    public boolean oemProcessPendingHangup(ImsPhoneCall call, ImsPhoneConnection pendingMO) {
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mImsPhoneCallTracker);
        AbstractPhone abstractPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, this.mPhone.getDefaultPhone());
        if (tmpCallTracker.getPendingHangupCall() != null || pendingMO == null || !pendingMO.getState().isDialing() || !this.mImsPhoneCallTracker.mBackgroundCall.isIdle()) {
            return false;
        }
        logd("(foregnd) hangup dialing or alerting pending...");
        tmpCallTracker.setPendingHangupCall(call);
        tmpCallTracker.setPendingHangupAddr(pendingMO.getAddress());
        tmpCallTracker.HangupLocal(call);
        sendEmptyMessageDelayed(101, 500);
        return true;
    }

    public void handleMessage(Message msg) {
        logd("handleMessage what=" + msg.what);
        int i = msg.what;
        if (i == 101) {
            processPendingHangup("handler");
        } else if (i == 102) {
            try {
                ImsCall imsCall = this.mImsPhoneCallTracker.mForegroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume();
                    TelephonyMetrics.getInstance().writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
                }
            } catch (ImsException e) {
            }
        }
    }

    public void handleImsPhoneStateChanged(PhoneConstants.State oldState, PhoneConstants.State mState) {
        OppoPhoneUtil.setOemVoocState(oldState, mState);
    }

    public synchronized void processPendingHangup(String msg) {
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mImsPhoneCallTracker);
        if (tmpCallTracker.getPendingHangupCall() != null) {
            logd("processPendingHangup. for " + msg);
            removeMessages(101);
            try {
                ImsCall imsCall = tmpCallTracker.getPendingHangupCall().getImsCall();
                if (imsCall != null) {
                    imsCall.terminate(501);
                }
            } catch (Exception ex) {
                loge("processPendingHangup. ex:" + ex.getMessage());
            }
            tmpCallTracker.setPendingHangupCall((ImsPhoneCall) null);
            tmpCallTracker.setPendingHangupAddr((String) null);
        }
    }

    public void oemNotifySrvccComplete() {
        AbstractCallTracker tmpCallTracker = (AbstractCallTracker) OemTelephonyUtils.typeCasting(AbstractCallTracker.class, this.mImsPhoneCallTracker);
        if (tmpCallTracker != null && tmpCallTracker.isImsCallHangupPending()) {
            logd("oemNotifySrvccComplete:there is still an pending hangup after SRVCC,remove the msg and hangup in GSM.");
            removeMessages(101);
        }
    }

    public int oemGetDisconnectCauseFromReasonInfo(int code) {
        if (code != 143) {
            return 36;
        }
        return 3;
    }

    public void oemRetryResumeAfterResumeFail(ImsReasonInfo reasonInfo) {
        if (this.mImsPhoneCallTracker.mForegroundCall.getState() == Call.State.HOLDING && this.mImsPhoneCallTracker.mBackgroundCall.getState() == Call.State.IDLE && this.mImsPhoneCallTracker.mRingingCall.getState() == Call.State.IDLE && reasonInfo.getExtraCode() == 491) {
            if (this.mImsPhoneCallTracker.mForegroundCall.getFirstConnection() != null) {
                if (this.mImsPhoneCallTracker.mForegroundCall.getFirstConnection().isIncoming()) {
                    this.mDelayToResume = (long) (Math.random() * 200.0d * 10.0d);
                } else {
                    this.mDelayToResume = (long) ((Math.random() * 190.0d * 10.0d) + 2100.0d);
                }
            }
            logd("onCallResumeFailed : resume failure because of re-invite COLLISION " + this.mDelayToResume);
            sendEmptyMessageDelayed(102, this.mDelayToResume);
        }
    }

    public void oemResetImsCapabilities() {
        logd("Resetting Capabilities... ");
        if (this.mPhone != null) {
            SystemProperties.set(PRO_IMS_TYPE + this.mPhone.getPhoneId(), "");
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d("OppoImsPhoneCallTracker/" + getImsPhone().getPhoneId(), s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e("OppoImsPhoneCallTracker/" + getImsPhone().getPhoneId(), s);
    }
}
