package com.mediatek.op.ims.ril;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Message;
import vendor.mediatek.hardware.radio_op.V2_0.IImsRadioResponseOp;

public class OpImsRadioResponse extends IImsRadioResponseOp.Stub {
    private int mPhoneId;
    private OpImsRIL mRil;

    OpImsRadioResponse(OpImsRIL ril, int phoneId) {
        this.mRil = ril;
        this.mPhoneId = phoneId;
        OpImsRIL opImsRIL = this.mRil;
        opImsRIL.riljLogv("OpImsRadioResponse, phone = " + this.mPhoneId);
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.sendToTarget();
        }
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IImsRadioResponseOp
    public void dialFromResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IImsRadioResponseOp
    public void sendUssiFromResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IImsRadioResponseOp
    public void cancelUssiFromResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IImsRadioResponseOp
    public void setEmergencyCallConfigResponse(RadioResponseInfo info) {
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IImsRadioResponseOp
    public void deviceSwitchResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IImsRadioResponseOp
    public void cancelDeviceSwitchResponse(RadioResponseInfo info) {
        responseVoid(info);
    }

    private void responseVoid(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, null);
            }
            this.mRil.processResponseDone(rr, responseInfo, null);
        }
    }

    private void responseString(RadioResponseInfo responseInfo, String str) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            String ret = null;
            if (responseInfo.error == 0) {
                ret = str;
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }
}
