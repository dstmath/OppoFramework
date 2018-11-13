package com.android.server.updates;

import com.android.server.pm.CompatibilityHelper;

public class CertPinInstallReceiver extends ConfigUpdateInstallReceiver {
    public CertPinInstallReceiver() {
        super("/data/misc/keychain/", "pins", "metadata/", CompatibilityHelper.VERSION_NAME);
    }
}
