package com.mediatek.android.mms.pdu;

import com.google.android.mms.pdu.MultimediaMessagePdu;

public class MtkMultimediaMessagePdu extends MultimediaMessagePdu {
    public long getDateSent() {
        return this.mPduHeaders.getLongInteger(201);
    }
}
