package com.mediatek.android.mms.pdu;

import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.SendReq;

public class MtkSendReq extends SendReq {
    private static final String TAG = "MtkSendReq";

    public MtkSendReq() {
    }

    public MtkSendReq(byte[] contentType, MtkEncodedStringValue from, int mmsVersion, byte[] transactionId) throws InvalidHeaderValueException {
        super(contentType, from, mmsVersion, transactionId);
    }

    MtkSendReq(MtkPduHeaders headers) {
        super(headers);
    }

    MtkSendReq(MtkPduHeaders headers, PduBody body) {
        super(headers, body);
    }

    public long getDateSent() {
        return this.mPduHeaders.getLongInteger(201);
    }
}
