package com.mediatek.common.telephony;

public interface IGsmDCTExt {
    long getDisconnectDoneRetryTimer(String str, long j);

    long getIPv6Valid(Object obj);

    boolean isDataAllowedAsOff(String str);

    boolean isDomesticRoamingEnabled();

    boolean isFdnEnableSupport();

    boolean isIgnoredCause(Object obj);

    void onDcActivated(String[] strArr, String str);

    void onDcDeactivated(String[] strArr, String str);
}
