package com.mediatek.common.telephony.internationalroaming.strategy;

public interface INetworkSelectionStrategy extends IBaseStrategy {
    boolean needToBootOnCdma();

    boolean needToBootOnGsm();

    void onCdmaPlmnChanged(String str);

    void onGsmSuspend(String[] strArr, int i);

    void onNoService(int i);

    void onPostSwitchPhone();

    int onPreSwitchPhone();
}
