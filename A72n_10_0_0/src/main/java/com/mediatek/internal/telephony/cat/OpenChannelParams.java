package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.TextMessage;

/* compiled from: BipCommandParams */
class OpenChannelParams extends CommandParams {
    public BearerDesc bearerDesc = null;
    public int bufferSize = 0;
    public OtherAddress dataDestinationAddress = null;
    public GprsParams gprsParams = null;
    public OtherAddress localAddress = null;
    public TextMessage textMsg = null;
    public TransportProtocol transportProtocol = null;

    OpenChannelParams(CommandDetails cmdDet, BearerDesc bearerDesc2, int size, OtherAddress localAddress2, TransportProtocol transportProtocol2, OtherAddress address, String apn, String login, String pwd, TextMessage textMsg2) {
        super(cmdDet);
        this.bearerDesc = bearerDesc2;
        this.bufferSize = size;
        this.localAddress = localAddress2;
        this.transportProtocol = transportProtocol2;
        this.dataDestinationAddress = address;
        this.textMsg = textMsg2;
        this.gprsParams = new GprsParams(apn, login, pwd);
    }

    /* compiled from: BipCommandParams */
    public class GprsParams {
        public String accessPointName = null;
        public String userLogin = null;
        public String userPwd = null;

        GprsParams(String apn, String login, String pwd) {
            this.accessPointName = apn;
            this.userLogin = login;
            this.userPwd = pwd;
        }
    }
}
