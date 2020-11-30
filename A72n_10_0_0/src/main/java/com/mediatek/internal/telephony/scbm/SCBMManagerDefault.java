package com.mediatek.internal.telephony.scbm;

import android.content.Context;
import android.os.Handler;
import com.android.internal.telephony.CommandsInterface;

public class SCBMManagerDefault extends Handler implements ISCBMManager {
    protected static final int EVENT_EXIT_SCBM_RESPONSE = 2;
    protected static final int EVENT_SCBM_ENTER = 1;

    public SCBMManagerDefault(Context context, int phoneId, CommandsInterface ci) {
    }

    @Override // com.mediatek.internal.telephony.scbm.ISCBMManager
    public boolean isInScm() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.scbm.ISCBMManager
    public void exitSCBM() {
    }
}
