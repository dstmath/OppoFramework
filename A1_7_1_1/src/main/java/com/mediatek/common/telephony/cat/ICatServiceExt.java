package com.mediatek.common.telephony.cat;

public interface ICatServiceExt {
    void init(int i);

    boolean unInstallIfNoSim();

    void updateMenuTitleFromEf(String str);
}
