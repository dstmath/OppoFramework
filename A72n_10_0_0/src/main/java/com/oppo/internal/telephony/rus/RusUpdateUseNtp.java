package com.oppo.internal.telephony.rus;

import com.android.internal.telephony.ServiceStateTracker;
import com.oppo.internal.telephony.OppoNewNitzStateMachine;
import java.util.HashMap;

public final class RusUpdateUseNtp extends RusBase {
    private static final String TAG = "RusUpdateUseNtp";
    private final ServiceStateTracker mSst = this.mPhone.getServiceStateTracker();

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey(OppoNewNitzStateMachine.TAG_ENABLE) && rusData.containsKey(OppoNewNitzStateMachine.TAG_INTERVAL)) {
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",use_ntp_enable:" + rusData.get(OppoNewNitzStateMachine.TAG_ENABLE) + ",use_ntp_interval:" + rusData.get(OppoNewNitzStateMachine.TAG_INTERVAL));
        }
    }
}
