package com.xxx.devicemanager;

import android.content.ComponentName;
import android.content.Context;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceControlerManager;

public class SecurityPolicy {
    public void shutdown(boolean isReboot) {
        DeviceControlerManager manager = DeviceControlerManager.getInstance((Context) null);
        if (isReboot) {
            manager.rebootDevice((ComponentName) null);
        } else {
            manager.shutdownDevice((ComponentName) null);
        }
    }
}
