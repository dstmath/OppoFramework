package android.net.wifi;

import java.util.List;
import oppo.net.wifi.HotspotClient;

public interface IOppoWifiManagerEx {
    boolean blockClient(HotspotClient hotspotClient);

    String[] getAllSlaAcceleratedApps();

    String[] getAllSlaAppsAndStates();

    List<HotspotClient> getBlockedHotspotClients();

    List<HotspotClient> getHotspotClients();

    boolean getSlaAppState(String str);

    WifiConfiguration getWifiSharingConfiguration();

    boolean isSlaSupported();

    boolean setSlaAppState(String str, boolean z);

    boolean setWifiSharingConfiguration(WifiConfiguration wifiConfiguration);

    boolean unblockClient(HotspotClient hotspotClient);
}
