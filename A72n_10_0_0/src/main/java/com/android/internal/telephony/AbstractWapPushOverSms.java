package com.android.internal.telephony;

import android.content.Intent;
import android.text.TextUtils;

public class AbstractWapPushOverSms {
    public String mScAddress = PhoneConfigurationManager.SSSS;

    /* access modifiers changed from: protected */
    public void oemSetScAddress(Intent intent) {
        if (!TextUtils.isEmpty(this.mScAddress) && intent != null) {
            intent.putExtra("service_center", this.mScAddress);
        }
    }
}
