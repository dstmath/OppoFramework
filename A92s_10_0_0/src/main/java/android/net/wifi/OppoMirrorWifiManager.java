package android.net.wifi;

import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;
import java.util.List;
import oppo.net.wifi.HotspotClient;

public class OppoMirrorWifiManager {
    public static Class<?> TYPE = RefClass.load(OppoMirrorWifiManager.class, WifiManager.class);
    @MethodParams({HotspotClient.class})
    public static RefMethod<Boolean> blockClient;
    public static RefMethod<String[]> getAllSlaAcceleratedApps;
    public static RefMethod<String[]> getAllSlaAppsAndStates;
    public static RefMethod<List<HotspotClient>> getBlockedHotspotClients;
    public static RefMethod<List<HotspotClient>> getHotspotClients;
    public static RefMethod<WifiConfiguration> getWifiSharingConfiguration;
    public static RefMethod<Boolean> isSlaSupported;
    @MethodParams({WifiConfiguration.class})
    public static RefMethod<Boolean> setWifiSharingConfiguration;
    @MethodParams({HotspotClient.class})
    public static RefMethod<Boolean> unblockClient;
}
