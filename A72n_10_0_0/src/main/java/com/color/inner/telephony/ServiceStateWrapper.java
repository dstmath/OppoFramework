package com.color.inner.telephony;

import android.os.Bundle;
import android.telephony.ServiceState;
import android.util.Log;

public class ServiceStateWrapper {
    private static final String TAG = "ServiceStateWrapper";

    private ServiceStateWrapper() {
    }

    public static ServiceState newFromBundle(Bundle bundle) {
        try {
            return ServiceState.newFromBundle(bundle);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static int getDataRegState(ServiceState serviceState) {
        try {
            return serviceState.getDataRegState();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }
}
