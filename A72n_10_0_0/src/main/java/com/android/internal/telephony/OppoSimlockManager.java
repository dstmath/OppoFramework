package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;

public abstract class OppoSimlockManager extends Handler {
    public OppoSimlockManager(Phone[] phone, CommandsInterface[] ci, Context context) {
    }

    public void init(Phone[] phone, CommandsInterface[] ci, Context context) {
    }

    public void handleOppoSimlocked(int slotId) {
    }

    public void handleOppoLoaded(int slotId) {
    }

    public void handleOppoAbsentOrError(int slotId) {
    }

    public void onSmlSlotLoclInfoChaned(AsyncResult ar, Integer index) {
    }

    public void handleRebroadcastSimlock() {
    }

    public boolean isSimlockRebroadcast(Intent intent, int slotId) {
        return false;
    }
}
