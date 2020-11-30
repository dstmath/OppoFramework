package com.android.server.inputmethod;

import android.os.Message;
import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoInputMethodManagerServiceEx extends IOppoCommonManagerServiceEx {
    InputMethodManagerService getInputMethodManagerService();

    void handleMessage(Message message, int i);

    default int getLayoutResIdForInputmethodWwitch(int resId) {
        return resId;
    }
}
