package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.Input;

public class MtkInput extends Input {
    MtkInput() {
    }

    public static Input getInstance() {
        return new MtkInput();
    }
}
