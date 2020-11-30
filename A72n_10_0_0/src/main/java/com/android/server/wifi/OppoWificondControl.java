package com.android.server.wifi;

import android.app.AlarmManager;
import android.os.Looper;

public class OppoWificondControl extends WificondControl {
    private static final String TAG = OppoWificondControl.class.getSimpleName();

    OppoWificondControl(WifiInjector wifiInjector, WifiMonitor wifiMonitor, CarrierNetworkConfig carrierNetworkConfig, AlarmManager alarmManager, Looper looper, Clock clock) {
        super(wifiInjector, wifiMonitor, carrierNetworkConfig, alarmManager, looper, clock);
    }

    public boolean blockClient(String ifaceName, String device, boolean isBlocked) {
        return true;
    }

    public boolean setMaxClient(String ifaceName, int maxNum) {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean blockSavedClients(String ifaceName, String[] devices) {
        return true;
    }
}
