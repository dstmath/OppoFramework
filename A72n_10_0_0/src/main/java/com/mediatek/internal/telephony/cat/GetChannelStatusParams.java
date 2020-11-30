package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.TextMessage;

/* compiled from: BipCommandParams */
class GetChannelStatusParams extends CommandParams {
    TextMessage textMsg = new TextMessage();

    GetChannelStatusParams(CommandDetails cmdDet, TextMessage textMsg2) {
        super(cmdDet);
        this.textMsg = textMsg2;
    }
}
