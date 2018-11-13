package com.android.server.updates;

import com.android.server.pm.CompatibilityHelper;

public class SmsShortCodesInstallReceiver extends ConfigUpdateInstallReceiver {
    public SmsShortCodesInstallReceiver() {
        super("/data/misc/sms/", "codes", "metadata/", CompatibilityHelper.VERSION_NAME);
    }
}
