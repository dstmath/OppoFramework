package com.mediatek.op.ims.ril;

import vendor.mediatek.hardware.radio_op.V2_0.IImsRadioIndicationOp;

public class OpImsRadioIndication extends IImsRadioIndicationOp.Stub {
    private int mPhoneId;
    private OpImsRIL mRil;

    OpImsRadioIndication(OpImsRIL ril, int phoneId) {
        this.mRil = ril;
        this.mPhoneId = phoneId;
        OpImsRIL opImsRIL = this.mRil;
        opImsRIL.riljLogv("OpImsRadioIndication, phone = " + this.mPhoneId);
    }
}
