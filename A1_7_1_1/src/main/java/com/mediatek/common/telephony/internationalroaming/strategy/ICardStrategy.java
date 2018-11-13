package com.mediatek.common.telephony.internationalroaming.strategy;

public interface ICardStrategy extends IBaseStrategy {
    void onSimImsiLoaded(int i, String str, String str2);

    int parseCardType(String str, String str2);
}
