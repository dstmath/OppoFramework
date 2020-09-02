package com.mediatek.internal.telephony.devreg;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.mediatek.internal.telephony.MtkRIL;

public class DeviceRegisterHandler extends Handler {
    private static final int EVENT_CDMA_CARD_INITIAL_ESN_OR_MEID = 107;
    private final CommandsInterface mCi;
    private final DeviceRegisterController mController;
    private final Phone mPhone;

    public DeviceRegisterHandler(Phone phone, DeviceRegisterController controller) {
        this.mPhone = phone;
        this.mCi = phone.mCi;
        this.mController = controller;
        MtkRIL mtkRIL = this.mCi;
    }

    public void handleMessage(Message msg) {
        if (msg.what != EVENT_CDMA_CARD_INITIAL_ESN_OR_MEID) {
            super.handleMessage(msg);
            return;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null && ar.exception == null && ar.result != null) {
            try {
                this.mController.setCdmaCardEsnOrMeid((String) ar.result);
            } catch (ClassCastException e) {
            }
        }
    }
}
