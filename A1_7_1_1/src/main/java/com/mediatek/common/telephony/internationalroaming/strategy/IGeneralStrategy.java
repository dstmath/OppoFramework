package com.mediatek.common.telephony.internationalroaming.strategy;

public interface IGeneralStrategy extends IBaseStrategy {
    boolean isHomeNetwork(String str);

    void onDualPhoneRadioAvailable();

    void onDualPhoneRadioOn(int i);

    void onNewSimInserted(int i);
}
