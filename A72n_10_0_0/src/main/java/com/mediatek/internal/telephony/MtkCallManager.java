package com.mediatek.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.mediatek.android.mms.pdu.MtkCharacterSets;

public class MtkCallManager extends CallManager {

    public class MtkCallManagerHandler extends CallManager.CallManagerHandler {
        public MtkCallManagerHandler() {
            super(MtkCallManager.this);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 102) {
                MtkCallManager.super.handleMessage(msg);
                return;
            }
            Connection c = (Connection) ((AsyncResult) msg.obj).result;
            int subId = c.getCall().getPhone().getSubId();
            if (!MtkCallManager.this.getActiveFgCallState(subId).isDialing() && !MtkCallManager.this.hasMoreThanOneRingingCall()) {
                if (MtkCallManager.this.getActiveFgCallState(subId) != Call.State.ACTIVE || !"0".equals(SystemProperties.get("persist.sys.oem_t_vi", "-1"))) {
                    MtkCallManager.this.mNewRingingConnectionRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    return;
                }
                try {
                    Rlog.d("CallManager", "silently drop incoming call: " + c.getCall());
                    c.getCall().hangup();
                } catch (Exception e) {
                    Rlog.w("CallManager", "new ringing connection", e);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerForPhoneStates(Phone phone) {
        if (((CallManager.CallManagerHandler) this.mHandlerMap.get(phone)) != null) {
            Rlog.d("CallManager", "This phone has already been registered.");
            return;
        }
        CallManager.CallManagerHandler handler = new MtkCallManagerHandler();
        this.mHandlerMap.put(phone, handler);
        phone.registerForPreciseCallStateChanged(handler, 101, this.mRegistrantidentifier);
        phone.registerForDisconnect(handler, 100, this.mRegistrantidentifier);
        phone.registerForNewRingingConnection(handler, 102, this.mRegistrantidentifier);
        phone.registerForUnknownConnection(handler, 103, this.mRegistrantidentifier);
        phone.registerForIncomingRing(handler, (int) MtkCharacterSets.ISO_2022_CN, this.mRegistrantidentifier);
        phone.registerForRingbackTone(handler, (int) MtkCharacterSets.ISO_2022_CN_EXT, this.mRegistrantidentifier);
        phone.registerForInCallVoicePrivacyOn(handler, 106, this.mRegistrantidentifier);
        phone.registerForInCallVoicePrivacyOff(handler, 107, this.mRegistrantidentifier);
        phone.registerForDisplayInfo(handler, 109, this.mRegistrantidentifier);
        phone.registerForSignalInfo(handler, 110, this.mRegistrantidentifier);
        phone.registerForResendIncallMute(handler, (int) MtkCharacterSets.ISO_8859_16, this.mRegistrantidentifier);
        phone.registerForMmiInitiate(handler, (int) MtkCharacterSets.GBK, this.mRegistrantidentifier);
        phone.registerForMmiComplete(handler, (int) MtkCharacterSets.GB18030, this.mRegistrantidentifier);
        phone.registerForSuppServiceFailed(handler, 117, this.mRegistrantidentifier);
        phone.registerForServiceStateChanged(handler, 118, this.mRegistrantidentifier);
        phone.setOnPostDialCharacter(handler, 119, (Object) null);
        phone.registerForCdmaOtaStatusChange(handler, 111, (Object) null);
        phone.registerForSubscriptionInfoReady(handler, 116, (Object) null);
        phone.registerForCallWaiting(handler, 108, (Object) null);
        phone.registerForEcmTimerReset(handler, 115, (Object) null);
        phone.registerForOnHoldTone(handler, 120, (Object) null);
        phone.registerForSuppServiceFailed(handler, 117, (Object) null);
        phone.registerForTtyModeReceived(handler, 122, (Object) null);
    }
}
