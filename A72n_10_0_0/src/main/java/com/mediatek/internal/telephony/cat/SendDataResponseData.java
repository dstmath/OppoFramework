package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.cat.ResponseData;
import java.io.ByteArrayOutputStream;

/* compiled from: BipResponseData */
class SendDataResponseData extends ResponseData {
    int mTxBufferSize = 0;

    SendDataResponseData(int size) {
        this.mTxBufferSize = size;
    }

    public void format(ByteArrayOutputStream buf) {
        if (buf != null) {
            buf.write(ComprehensionTlvTag.CHANNEL_DATA_LENGTH.value() | 128);
            buf.write(1);
            int i = this.mTxBufferSize;
            if (i >= 255) {
                buf.write(255);
            } else {
                buf.write(i);
            }
        }
    }
}
