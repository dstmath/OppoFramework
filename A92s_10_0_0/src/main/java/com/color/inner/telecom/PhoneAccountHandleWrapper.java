package com.color.inner.telecom;

import android.telecom.OppoBasePhoneAccountHandle;
import android.telecom.PhoneAccountHandle;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class PhoneAccountHandleWrapper {
    private static final String TAG = "PhoneAccountHandleWrapper";

    private PhoneAccountHandleWrapper() {
    }

    public static int getSubId(PhoneAccountHandle phoneAccountHandle) {
        try {
            return typeCasting(phoneAccountHandle).getSubId();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int getSlotId(PhoneAccountHandle phoneAccountHandle) {
        try {
            return typeCasting(phoneAccountHandle).getSlotId();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    private static OppoBasePhoneAccountHandle typeCasting(PhoneAccountHandle phoneAccountHandle) {
        return (OppoBasePhoneAccountHandle) ColorTypeCastingHelper.typeCasting(OppoBasePhoneAccountHandle.class, phoneAccountHandle);
    }
}
