package com.mediatek.internal.telephony;

import android.os.Handler;
import android.os.Message;

public interface IMtkRilOp {
    void exitSCBM(Message message);

    void getDisable2G(Message message);

    void getRxTestResult(Message message);

    void registerForModulation(Handler handler, int i, Object obj);

    void setDisable2G(boolean z, Message message);

    void setRxTestConfig(int i, Message message);

    void unregisterForModulation(Handler handler);
}
