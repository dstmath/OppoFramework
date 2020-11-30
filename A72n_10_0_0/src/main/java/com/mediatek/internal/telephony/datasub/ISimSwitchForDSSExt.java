package com.mediatek.internal.telephony.datasub;

public interface ISimSwitchForDSSExt {
    boolean checkCapSwitch(int i);

    void init(DataSubSelector dataSubSelector);

    int isNeedSimSwitch();
}
