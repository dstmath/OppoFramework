package com.android.server.wifi;

import android.os.Handler;
import android.os.INetworkManagementService;
import com.mediatek.server.wifi.MtkHostapdHal;
import java.util.Random;

public class OppoWifiNative extends WifiNative {
    private static final String TAG = "OPPOWifiNative";
    private boolean DBG = true;
    private final OppoWificondControl mOppoWificondControl;

    public OppoWifiNative(WifiVendorHal vendorHal, SupplicantStaIfaceHal staIfaceHal, HostapdHal hostapdHal, OppoWificondControl condControl, WifiMonitor wifiMonitor, INetworkManagementService nwService, PropertyService propertyService, WifiMetrics wifiMetrics, Handler handler, Random random) {
        super(vendorHal, staIfaceHal, hostapdHal, condControl, wifiMonitor, nwService, propertyService, wifiMetrics, handler, random);
        this.mOppoWificondControl = condControl;
    }

    public boolean blockClient(String ifaceName, String device, boolean isBlocked) {
        if (isBlocked) {
            return MtkHostapdHal.blockClient(device);
        }
        return MtkHostapdHal.unblockClient(device);
    }

    /* access modifiers changed from: package-private */
    public boolean setMaxClient(String ifaceName, int maxNum) {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean blockSavedClients(String ifaceName, String[] devices) {
        return true;
    }
}
