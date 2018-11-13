package com.android.server.hdmi;

import android.net.arp.OppoArpPeer;

final class RequestArcInitiationAction extends RequestArcAction {
    private static final String TAG = "RequestArcInitiationAction";

    RequestArcInitiationAction(HdmiCecLocalDevice source, int avrAddress) {
        super(source, avrAddress);
    }

    boolean start() {
        this.mState = 1;
        addTimer(this.mState, OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT);
        sendCommand(HdmiCecMessageBuilder.buildRequestArcInitiation(getSourceAddress(), this.mAvrAddress), new SendMessageCallback() {
            public void onSendCompleted(int error) {
                if (error != 0) {
                    RequestArcInitiationAction.this.tv().setArcStatus(false);
                    RequestArcInitiationAction.this.finish();
                }
            }
        });
        return true;
    }
}
