package com.mediatek.internal.telephony.uicc;

public interface IMtkRsuSml {
    int deregisterCallback(Object obj);

    int registerCallback(Object obj);

    int remoteSimlockGenerateRequest(int i, int i2);

    int remoteSimlockGetSimlockStatus(int i);

    int remoteSimlockGetVersion(int i);

    int remoteSimlockProcessSimlockData(int i, byte[] bArr);

    int remoteSimlockUnlockTimer(int i, int i2);
}
