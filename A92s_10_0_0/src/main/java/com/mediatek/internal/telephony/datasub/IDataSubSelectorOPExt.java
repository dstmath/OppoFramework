package com.mediatek.internal.telephony.datasub;

import android.content.Intent;

public interface IDataSubSelectorOPExt {
    boolean enableAospDefaultDataUpdate();

    boolean enableAospDisableDataSwitch();

    void handleAirPlaneModeOff(Intent intent);

    void handleBootCompleteAction();

    void handleConnectivityAction();

    void handleDefaultDataChanged(Intent intent);

    void handlePlmnChanged(Intent intent);

    void handleSimMeLock(Intent intent);

    void handleSimStateChanged(Intent intent);

    void handleSubinfoRecordUpdated(Intent intent);

    void handleSubsidyLockStateAction(Intent intent);

    void init(DataSubSelector dataSubSelector, ISimSwitchForDSSExt iSimSwitchForDSSExt);

    void subSelector(Intent intent);
}
