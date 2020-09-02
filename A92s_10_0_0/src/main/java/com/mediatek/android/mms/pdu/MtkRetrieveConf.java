package com.mediatek.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.RetrieveConf;

public class MtkRetrieveConf extends RetrieveConf {
    public MtkRetrieveConf() throws InvalidHeaderValueException {
    }

    MtkRetrieveConf(MtkPduHeaders headers) {
        super(headers);
    }

    MtkRetrieveConf(MtkPduHeaders headers, PduBody body) {
        super(headers, body);
    }

    public long getDateSent() {
        return this.mPduHeaders.getLongInteger(201);
    }
}
