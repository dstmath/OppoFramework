package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.TextMessage;

public class MtkTextMessage extends TextMessage {
    MtkTextMessage() {
    }

    public static MtkTextMessage getInstance() {
        return new MtkTextMessage();
    }
}
