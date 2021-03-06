package com.android.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.deprecated.V1_0.IOemHookResponse;
import java.util.ArrayList;

public class OemHookResponse extends IOemHookResponse.Stub {
    RIL mRil;

    public OemHookResponse(RIL ril) {
        this.mRil = ril;
    }

    @Override // android.hardware.radio.deprecated.V1_0.IOemHookResponse
    public void sendRequestRawResponse(RadioResponseInfo responseInfo, ArrayList<Byte> data) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            byte[] ret = null;
            if (responseInfo.error == 0) {
                ret = RIL.arrayListToPrimitiveArray(data);
                RadioResponse.sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    @Override // android.hardware.radio.deprecated.V1_0.IOemHookResponse
    public void sendRequestStringsResponse(RadioResponseInfo responseInfo, ArrayList<String> data) {
        RadioResponse.responseStringArrayList(this.mRil, responseInfo, data);
    }
}
