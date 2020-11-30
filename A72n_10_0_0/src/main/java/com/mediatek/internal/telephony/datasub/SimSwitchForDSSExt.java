package com.mediatek.internal.telephony.datasub;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

public class SimSwitchForDSSExt implements ISimSwitchForDSSExt {
    public static boolean DBG = true;
    public static final boolean USER_BUILD = TextUtils.equals(Build.TYPE, DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER);
    protected static Context mContext = null;
    private static DataSubSelector mDataSubSelector = null;

    public SimSwitchForDSSExt(Context context) {
    }

    @Override // com.mediatek.internal.telephony.datasub.ISimSwitchForDSSExt
    public void init(DataSubSelector dataSubSelector) {
        mDataSubSelector = dataSubSelector;
    }

    @Override // com.mediatek.internal.telephony.datasub.ISimSwitchForDSSExt
    public boolean checkCapSwitch(int policy) {
        return false;
    }

    @Override // com.mediatek.internal.telephony.datasub.ISimSwitchForDSSExt
    public int isNeedSimSwitch() {
        return 2;
    }
}
