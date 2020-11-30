package com.mediatek.server.wifi;

import android.net.wifi.WifiConfiguration;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.WifiNetworkSelector;
import java.util.List;

public class MtkNetworkEvaluator implements WifiNetworkSelector.NetworkEvaluator {
    private static final String NAME = "MtkNetworkEvaluator";

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public int getId() {
        return 0;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public String getName() {
        return NAME;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public void update(List<ScanDetail> list) {
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public WifiConfiguration evaluateNetworks(List<ScanDetail> list, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, WifiNetworkSelector.NetworkEvaluator.OnConnectableListener onConnectableListener) {
        MtkWifiServiceAdapter.triggerNetworkEvaluatorCallBack();
        return null;
    }
}
