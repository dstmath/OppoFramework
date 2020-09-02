package com.mediatek.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.RadioConfig;

public class MtkRadioConfig extends RadioConfig {
    private static final String TAG = "MtkRadioConfig";

    public MtkRadioConfig(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public boolean isGetHidlServiceSync() {
        return false;
    }
}
