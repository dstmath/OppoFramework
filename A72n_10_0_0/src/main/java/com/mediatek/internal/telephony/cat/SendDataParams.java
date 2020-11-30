package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.TextMessage;

/* compiled from: BipCommandParams */
class SendDataParams extends CommandParams {
    byte[] channelData = null;
    int mSendDataCid = 0;
    int mSendMode = 0;
    TextMessage textMsg = new TextMessage();

    SendDataParams(CommandDetails cmdDet, byte[] data, int cid, TextMessage textMsg2, int sendMode) {
        super(cmdDet);
        this.channelData = data;
        this.textMsg = textMsg2;
        this.mSendDataCid = cid;
        this.mSendMode = sendMode;
    }
}
