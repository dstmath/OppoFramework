package com.android.server.updates;

import com.android.server.pm.CompatibilityHelper;

public class CarrierProvisioningUrlsInstallReceiver extends ConfigUpdateInstallReceiver {
    public CarrierProvisioningUrlsInstallReceiver() {
        super("/data/misc/radio/", "provisioning_urls.xml", "metadata/", CompatibilityHelper.VERSION_NAME);
    }
}
