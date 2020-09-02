package com.android.server.inputmethod;

import android.content.Context;
import android.os.Message;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class OppoDummyInputMethodManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoInputMethodManagerServiceEx {
    protected final InputMethodManagerService mImms;

    public OppoDummyInputMethodManagerServiceEx(Context context, InputMethodManagerService imms) {
        super(context);
        this.mImms = imms;
    }

    @Override // com.android.server.inputmethod.IOppoInputMethodManagerServiceEx
    public InputMethodManagerService getInputMethodManagerService() {
        return this.mImms;
    }

    @Override // com.android.server.inputmethod.IOppoInputMethodManagerServiceEx
    public void handleMessage(Message msg, int whichHandler) {
    }
}
