package com.android.server.inputmethod;

import android.content.Context;

public class ColorDummyInputMethodManagerServiceEx extends OppoDummyInputMethodManagerServiceEx implements IColorInputMethodManagerServiceEx {
    public ColorDummyInputMethodManagerServiceEx(Context context, InputMethodManagerService imms) {
        super(context, imms);
    }
}
