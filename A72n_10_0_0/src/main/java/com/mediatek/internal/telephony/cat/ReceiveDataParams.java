package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.TextMessage;

/* compiled from: BipCommandParams */
class ReceiveDataParams extends CommandParams {
    int channelDataLength = 0;
    int mReceiveDataCid = 0;
    TextMessage textMsg = new TextMessage();

    ReceiveDataParams(CommandDetails cmdDet, int length, int cid, TextMessage textMsg2) {
        super(cmdDet);
        this.channelDataLength = length;
        this.textMsg = textMsg2;
        this.mReceiveDataCid = cid;
    }
}
