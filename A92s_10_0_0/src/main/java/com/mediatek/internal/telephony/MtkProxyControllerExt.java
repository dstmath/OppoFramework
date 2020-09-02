package com.mediatek.internal.telephony;

import android.content.Context;
import android.telephony.Rlog;

public class MtkProxyControllerExt implements IMtkProxyControllerExt {
    static final String TAG = "MtkProxyControllerExt";
    protected Context mContext;

    public MtkProxyControllerExt() {
    }

    public MtkProxyControllerExt(Context context) {
        this.mContext = context;
    }

    public void log(String text) {
        Rlog.d(TAG, text);
    }

    @Override // com.mediatek.internal.telephony.IMtkProxyControllerExt
    public boolean isNeedSimSwitch(int majorPhoneId, int phoneNum) {
        log("OMisNeedSimSwitch, majorPhoneId = " + majorPhoneId);
        return !RadioCapabilitySwitchUtil.isSkipCapabilitySwitch(majorPhoneId, phoneNum, this.mContext);
    }
}
