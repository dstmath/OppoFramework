package com.mediatek.common.telephony.cdma;

public interface ICdmaCallTrackerExt {
    boolean needToConvert(String str, String str2);

    String processPlusCodeForDriverCall(String str, boolean z, int i);

    String processPlusCodeForWaitingCall(String str, int i);
}
