package com.android.server.hdmi;

import com.android.server.hdmi.HdmiControlService;

final class RequestArcInitiationAction extends RequestArcAction {
    private static final String TAG = "RequestArcInitiationAction";

    RequestArcInitiationAction(HdmiCecLocalDevice source, int avrAddress) {
        super(source, avrAddress);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.hdmi.HdmiCecFeatureAction
    public boolean start() {
        this.mState = 1;
        addTimer(this.mState, 2000);
        sendCommand(HdmiCecMessageBuilder.buildRequestArcInitiation(getSourceAddress(), this.mAvrAddress), new HdmiControlService.SendMessageCallback() {
            /* class com.android.server.hdmi.RequestArcInitiationAction.AnonymousClass1 */

            @Override // com.android.server.hdmi.HdmiControlService.SendMessageCallback
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
