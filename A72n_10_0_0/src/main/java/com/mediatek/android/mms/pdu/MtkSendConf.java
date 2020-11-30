package com.mediatek.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.SendConf;

public class MtkSendConf extends SendConf {
    public MtkSendConf() throws InvalidHeaderValueException {
    }

    MtkSendConf(MtkPduHeaders headers) {
        super(headers);
    }

    MtkSendConf(PduHeaders headers) {
        super(headers);
    }

    public EncodedStringValue getResponseText() {
        return this.mPduHeaders.getEncodedStringValue(147);
    }
}
