package com.mediatek.internal.telephony.devreg;

public interface IDeviceRegisterExt {
    void handleAutoRegMessage(int i, String str, byte[] bArr);

    void handleAutoRegMessage(byte[] bArr);

    void setCdmaCardEsnOrMeid(String str);
}
