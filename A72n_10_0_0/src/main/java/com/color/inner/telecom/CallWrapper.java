package com.color.inner.telecom;

import android.telecom.Call;
import android.telecom.OppoMirrorCall;

public class CallWrapper {
    private static final String TAG = "CallWrapper";

    public static String internalGetCallId(Call call) {
        if (OppoMirrorCall.internalGetCallId != null) {
            return (String) OppoMirrorCall.internalGetCallId.call(call, new Object[0]);
        }
        return null;
    }
}
