package com.color.inner.telephony;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;

public class CarrierConfigManagerWrapper {
    private static final String TAG = "CarrierConfigManagerWrapper";

    public static PersistableBundle getDefaultConfig() {
        return CarrierConfigManager.getDefaultConfig();
    }
}
