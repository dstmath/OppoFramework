package com.qualcomm.qti.internal.telephony;

import android.os.Handler;
import android.os.Message;

public interface BaseRilInterface {
    void getOmhCallProfile(int i, Message message, int i2);

    boolean isServiceReady();

    void registerForServiceReadyEvent(Handler handler, int i, Object obj);

    void sendPhoneStatus(int i, int i2);

    boolean setLocalCallHold(int i, boolean z);

    void unRegisterForServiceReadyEvent(Handler handler);
}
