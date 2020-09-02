package android.net.ip;

import android.net.NetworkUtils;
import android.net.wifi.IWifiManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class OppoIpServerHelper {
    private static final String TAG = OppoIpServerHelper.class.getSimpleName();
    private static final String WIFI_HOST_IFACE_ADDR = "192.168.43.1";
    private static final String WIFI_HOST_IFACE_MASK = "192.168.43.";
    private static final String WIFI_SHARNG_HOST_IFACE_ADDR = "192.168.44.1";
    public static final int WIFI_SHARNG_HOST_IFACE_PREFIX_LENGTH = 24;

    public static String getWifiSharingIfaceAddr() {
        try {
            return intToStringGatewayForSharing(IWifiManager.Stub.asInterface(ServiceManager.getService("wifi")).getDhcpInfo().gateway);
        } catch (RemoteException e) {
            Log.e(TAG, "IWifiManager service died!", e);
            return WIFI_SHARNG_HOST_IFACE_ADDR;
        }
    }

    private static String intToStringGatewayForSharing(int hostAddress) {
        if (NetworkUtils.intToInetAddress(hostAddress).getHostAddress().indexOf(WIFI_HOST_IFACE_MASK) != -1) {
            return WIFI_SHARNG_HOST_IFACE_ADDR;
        }
        return WIFI_HOST_IFACE_ADDR;
    }
}
