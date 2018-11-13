package com.mediatek.common.telephony.internationalroaming.strategy;

public interface IDataStrategy extends IBaseStrategy {
    void onRegisterHomeNetwork(String str);

    void onRegisterRoamingNetwork(String str);

    void onSimImsiLoaded(String str, String str2);
}
