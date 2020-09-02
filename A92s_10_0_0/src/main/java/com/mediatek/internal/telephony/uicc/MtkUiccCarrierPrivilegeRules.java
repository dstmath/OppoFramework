package com.mediatek.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.uicc.UiccCarrierPrivilegeRules;
import com.android.internal.telephony.uicc.UiccProfile;

public class MtkUiccCarrierPrivilegeRules extends UiccCarrierPrivilegeRules {
    public MtkUiccCarrierPrivilegeRules(UiccProfile uiccProfile, Message loadedCallback) {
        super(uiccProfile, loadedCallback);
    }

    public void handleMessage(Message msg) {
        if (msg.what != 1) {
            log("Handled by AOSP handleMessage" + msg.what);
            MtkUiccCarrierPrivilegeRules.super.handleMessage(msg);
            return;
        }
        log("M: EVENT_OPEN_LOGICAL_CHANNEL_DONE");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null && ar.result != null) {
            MtkUiccCarrierPrivilegeRules.super.handleMessage(msg);
        } else if (!(ar.exception instanceof CommandException) || ar.exception.getCommandError() != CommandException.Error.RADIO_NOT_AVAILABLE) {
            MtkUiccCarrierPrivilegeRules.super.handleMessage(msg);
        } else {
            updateState(2, "RADIO_NOT_AVAILABLE");
        }
    }
}
