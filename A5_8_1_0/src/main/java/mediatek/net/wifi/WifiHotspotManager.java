package mediatek.net.wifi;

import android.net.wifi.IWifiManager;
import android.net.wifi.WpsInfo;
import android.os.RemoteException;
import java.util.List;

public class WifiHotspotManager {
    public static final String EXTRA_DEVICE_ADDRESS = "deviceAddress";
    public static final String EXTRA_DEVICE_NAME = "deviceName";
    public static final String EXTRA_IP_ADDRESS = "ipAddress";
    private static final String TAG = "WifiHotspotManager";
    public static final String WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION = "android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED";
    public static final String WIFI_HOTSPOT_OVERLAP_ACTION = "android.net.wifi.WIFI_HOTSPOT_OVERLAP";
    public static final String WIFI_WPS_CHECK_PIN_FAIL_ACTION = "android.net.wifi.WIFI_WPS_CHECK_PIN_FAIL";
    IWifiManager mService;

    public WifiHotspotManager(IWifiManager service) {
        this.mService = service;
    }

    public boolean startApWps(WpsInfo config) {
        try {
            this.mService.startApWps(config);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public List<HotspotClient> getHotspotClients() {
        try {
            return this.mService.getHotspotClients();
        } catch (RemoteException e) {
            return null;
        }
    }

    public List<HotspotClient> getBlockedHotspotClients() {
        try {
            return this.mService.getBlockedHotspotClients();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getClientIp(String deviceAddress) {
        try {
            return this.mService.getClientIp(deviceAddress);
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getClientDeviceName(String deviceAddress) {
        try {
            return this.mService.getClientDeviceName(deviceAddress);
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean blockClient(HotspotClient client) {
        try {
            return this.mService.blockClient(client);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean unblockClient(HotspotClient client) {
        try {
            return this.mService.unblockClient(client);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isAllDevicesAllowed() {
        try {
            return this.mService.isAllDevicesAllowed();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean setAllDevicesAllowed(boolean enabled, boolean allowAllConnectedDevices) {
        try {
            return this.mService.setAllDevicesAllowed(enabled, allowAllConnectedDevices);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean allowDevice(String deviceAddress, String name) {
        try {
            return this.mService.allowDevice(deviceAddress, name);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean disallowDevice(String deviceAddress) {
        try {
            return this.mService.disallowDevice(deviceAddress);
        } catch (RemoteException e) {
            return false;
        }
    }

    public List<HotspotClient> getAllowedDevices() {
        try {
            return this.mService.getAllowedDevices();
        } catch (RemoteException e) {
            return null;
        }
    }
}
