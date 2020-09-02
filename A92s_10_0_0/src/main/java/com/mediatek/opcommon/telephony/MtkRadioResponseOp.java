package com.mediatek.opcommon.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.telephony.Rlog;
import com.android.internal.telephony.RILRequest;
import com.android.internal.telephony.RadioResponse;
import java.util.ArrayList;
import vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp;
import vendor.mediatek.hardware.radio_op.V2_0.RsuResponseInfo;

public class MtkRadioResponseOp extends IRadioResponseOp.Stub {
    static final String TAG = "MtkRadioResponseOp";
    private MtkRilOp mMtkRilOp;
    RadioResponse mRadioResponse;

    public MtkRadioResponseOp(MtkRilOp ril) {
        this.mRadioResponse = new RadioResponse(ril);
        this.mMtkRilOp = ril;
        this.mMtkRilOp.log("MtkRadioResponseOp constructor");
    }

    public void log(String text) {
        Rlog.d(TAG, text);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp
    public void setIncomingVirtualLineResponse(RadioResponseInfo responseInfo) {
        this.mMtkRilOp.log("setIncomingVirtualLineResponse");
        this.mRadioResponse.responseVoid(responseInfo);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp
    public void setRxTestConfigResponse(RadioResponseInfo responseInfo, ArrayList<Integer> respAntConf) {
        this.mMtkRilOp.log("setRxTestConfigResponse");
        this.mRadioResponse.responseIntArrayList(responseInfo, respAntConf);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp
    public void getRxTestResultResponse(RadioResponseInfo responseInfo, ArrayList<Integer> respAntInfo) {
        this.mMtkRilOp.log("getRxTestResultResponse");
        this.mRadioResponse.responseIntArrayList(responseInfo, respAntInfo);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp
    public void setDisable2GResponse(RadioResponseInfo responseInfo) {
        this.mMtkRilOp.log("setDisable2GResponse");
        this.mRadioResponse.responseVoid(responseInfo);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp
    public void getDisable2GResponse(RadioResponseInfo responseInfo, int mode) {
        this.mMtkRilOp.log("getDisable2GResponse");
        this.mRadioResponse.responseInts(responseInfo, new int[]{mode});
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp
    public void exitSCBMResponse(RadioResponseInfo responseInfo) {
        this.mRadioResponse.responseVoid(responseInfo);
    }

    @Override // vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp
    public void sendRsuRequestResponse(RadioResponseInfo responseInfo, RsuResponseInfo data) {
        RILRequest rr = this.mMtkRilOp.processResponse(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                AsyncResult.forMessage(rr.mResult, data, (Throwable) null);
                rr.mResult.sendToTarget();
            }
            this.mMtkRilOp.processResponseDone(rr, responseInfo, data);
        }
    }
}
