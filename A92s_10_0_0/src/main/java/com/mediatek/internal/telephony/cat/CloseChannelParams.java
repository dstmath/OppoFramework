package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.TextMessage;

/* compiled from: BipCommandParams */
class CloseChannelParams extends CommandParams {
    boolean mBackToTcpListen = false;
    int mCloseCid = 0;
    TextMessage textMsg = new TextMessage();

    CloseChannelParams(CommandDetails cmdDet, int cid, TextMessage textMsg2, boolean backToTcpListen) {
        super(cmdDet);
        this.textMsg = textMsg2;
        this.mCloseCid = cid;
        this.mBackToTcpListen = backToTcpListen;
    }
}
