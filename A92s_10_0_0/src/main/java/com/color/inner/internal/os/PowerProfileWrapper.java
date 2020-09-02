package com.color.inner.internal.os;

import android.content.Context;
import android.util.Log;
import com.android.internal.os.PowerProfile;

public class PowerProfileWrapper {
    private static final String TAG = "PowerProfileWrapper";

    private PowerProfileWrapper() {
    }

    public static double getBatteryCapacity(Context context) {
        try {
            return new PowerProfile(context).getBatteryCapacity();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0.0d;
        }
    }

    public static double getAveragePower(Context context, String type) {
        try {
            return new PowerProfile(context).getAveragePower(type);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0.0d;
        }
    }
}
