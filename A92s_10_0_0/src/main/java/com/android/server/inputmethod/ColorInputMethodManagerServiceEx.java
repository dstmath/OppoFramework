package com.android.server.inputmethod;

import android.content.Context;

public class ColorInputMethodManagerServiceEx extends ColorDummyInputMethodManagerServiceEx {
    public ColorInputMethodManagerServiceEx(Context context, InputMethodManagerService imms) {
        super(context, imms);
    }

    public int getLayoutResIdForInputmethodWwitch(int resId) {
        return 201917490;
    }
}
