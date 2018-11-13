package com.qualcomm.qti.internal.telephony;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.os.AsyncResult;
import android.os.Bundle;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ServiceStateTracker;

public class QtiServiceStateTracker extends ServiceStateTracker {
    private static final String ACTION_MANAGED_ROAMING_IND = "codeaurora.intent.action.ACTION_MANAGED_ROAMING_IND";
    private static final boolean DBG = true;
    private static final String LOG_TAG = "QtiServiceStateTracker";
    private static final boolean VDBG = false;
    private final String ACTION_RAC_CHANGED = "qualcomm.intent.action.ACTION_RAC_CHANGED";
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("qualcomm.intent.action.ACTION_RAC_CHANGED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    QtiServiceStateTracker.this.mRac = bundle.getInt("rac");
                    QtiServiceStateTracker.this.mRat = bundle.getInt("rat");
                }
            }
        }
    };
    private int mRac;
    private final String mRacChange = "rac";
    private int mRat;
    private final String mRatInfo = "rat";
    private int mTac = -1;

    public QtiServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        super(phone, ci);
        IntentFilter filter = new IntentFilter();
        filter.addAction("qualcomm.intent.action.ACTION_RAC_CHANGED");
        phone.getContext().registerReceiver(this.mIntentReceiver, filter);
    }

    protected void handlePollStateResultMessage(int what, AsyncResult ar) {
        switch (what) {
            case 4:
                super.handlePollStateResultMessage(what, ar);
                if (this.mPhone.isPhoneTypeGsm()) {
                    VoiceRegStateResult voiceRegStateResult = ar.result;
                    int regState = voiceRegStateResult.regState;
                    if ((regState == 3 || regState == 13) && voiceRegStateResult.reasonForDenial == 10) {
                        log(" Posting Managed roaming intent sub = " + this.mPhone.getSubId());
                        try {
                            Intent intent = new Intent(ACTION_MANAGED_ROAMING_IND);
                            intent.setComponent(new ComponentName("com.qualcomm.qti.networksetting", "com.qualcomm.qti.networksetting.ManagedRoaming"));
                            intent.addFlags(268435456);
                            intent.putExtra("subscription", this.mPhone.getSubId());
                            this.mPhone.getContext().startActivity(intent);
                            return;
                        } catch (ActivityNotFoundException e) {
                            loge("unable to start activity: " + e);
                            return;
                        }
                    }
                    return;
                }
                return;
            default:
                super.handlePollStateResultMessage(what, ar);
                return;
        }
    }

    public void pollState(boolean modemTriggered) {
        if (this.mPhone.mCi.getRadioState() == RadioState.RADIO_OFF && ((!isDeviceShuttingDown() || modemTriggered) && 18 != this.mSS.getRilDataRadioTechnology())) {
            this.mSS.setStateOutOfService();
        }
        super.pollState(modemTriggered);
    }

    public int[] getPollingContext() {
        return this.mPollingContext;
    }
}
