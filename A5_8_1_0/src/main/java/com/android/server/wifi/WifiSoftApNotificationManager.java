package com.android.server.wifi;

import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiDevice;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WifiSoftApNotificationManager {
    private static final boolean DBG = false;
    private static final int DNSMASQ_POLLING_INTERVAL = 1000;
    private static final int DNSMASQ_POLLING_MAX_TIMES = 10;
    private static String HOTSPOT_NOTIFICATION = "HOTSPOT_NOTIFICATION";
    private static final String TAG = "WifiSoftApNotificationManager";
    private static final String dhcpLocation = "/data/misc/dhcp/dnsmasq.leases";
    private static Context mContext;
    private static WifiSoftApNotificationManager mWifiSoftApNotificationManager;
    private String mChannelName;
    private HashMap<String, WifiDevice> mConnectedDeviceMap = new HashMap();
    private HashMap<String, WifiDevice> mL2ConnectedDeviceMap = new HashMap();
    private int mLastSoftApNotificationId = 0;
    private Builder softApNotificationBuilder;

    private static class DnsmasqThread extends Thread {
        private WifiDevice mDevice;
        private int mInterval;
        private int mMaxTimes;
        private final WifiSoftApNotificationManager mWifiSoftApNotificationmgr;

        public DnsmasqThread(WifiSoftApNotificationManager softap, WifiDevice device, int interval, int maxTimes) {
            super("SoftAp");
            this.mWifiSoftApNotificationmgr = softap;
            this.mInterval = interval;
            this.mMaxTimes = maxTimes;
            this.mDevice = device;
        }

        public void run() {
            while (this.mMaxTimes > 0 && !this.mWifiSoftApNotificationmgr.readDeviceInfoFromDnsmasq(this.mDevice)) {
                try {
                    this.mMaxTimes--;
                    Thread.sleep((long) this.mInterval);
                } catch (Exception ex) {
                    Log.e(WifiSoftApNotificationManager.TAG, "Polling " + this.mDevice.deviceAddress + "error" + ex);
                }
            }
            WifiDevice other = (WifiDevice) this.mWifiSoftApNotificationmgr.mL2ConnectedDeviceMap.get(this.mDevice.deviceAddress);
            if (other != null && other.deviceState == 1) {
                this.mWifiSoftApNotificationmgr.mConnectedDeviceMap.put(this.mDevice.deviceAddress, this.mDevice);
                this.mWifiSoftApNotificationmgr.sendConnectDevicesStateChangedBroadcast();
            }
        }
    }

    private WifiSoftApNotificationManager(Context context) {
        mContext = context;
    }

    public static WifiSoftApNotificationManager getInstance(Context mContext) {
        if (mWifiSoftApNotificationManager != null) {
            return mWifiSoftApNotificationManager;
        }
        WifiSoftApNotificationManager wifiSoftApNotificationManager = new WifiSoftApNotificationManager(mContext);
        mWifiSoftApNotificationManager = wifiSoftApNotificationManager;
        return wifiSoftApNotificationManager;
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) mContext.getSystemService("connectivity");
    }

    private void sendConnectDevicesStateChangedBroadcast() {
        if (getConnectivityManager().isTetheringSupported()) {
            Intent broadcast = new Intent("codeaurora.net.conn.TETHER_CONNECT_STATE_CHANGED");
            broadcast.addFlags(603979776);
            mContext.sendStickyBroadcastAsUser(broadcast, UserHandle.ALL);
            showSoftApClientsNotification(17303494);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0067 A:{SYNTHETIC, Splitter: B:25:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0070 A:{SYNTHETIC, Splitter: B:30:0x0070} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readDeviceInfoFromDnsmasq(WifiDevice device) {
        IOException ex;
        Throwable th;
        boolean result = false;
        FileInputStream fstream = null;
        try {
            FileInputStream fstream2 = new FileInputStream(dhcpLocation);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream2)));
                while (true) {
                    String line = br.readLine();
                    if (line == null || line.length() == 0) {
                        break;
                    }
                    String[] fields = line.split(" ");
                    if (fields.length > 3) {
                        String addr = fields[1];
                        String name = fields[3];
                        if (addr.equals(device.deviceAddress)) {
                            device.deviceName = name;
                            result = true;
                            break;
                        }
                    }
                }
                if (fstream2 != null) {
                    try {
                        fstream2.close();
                    } catch (IOException e) {
                    }
                }
                fstream = fstream2;
            } catch (IOException e2) {
                ex = e2;
                fstream = fstream2;
                try {
                    Log.e(TAG, "readDeviceNameFromDnsmasq: " + ex);
                    if (fstream != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fstream = fstream2;
                if (fstream != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            ex = e4;
            Log.e(TAG, "readDeviceNameFromDnsmasq: " + ex);
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e5) {
                }
            }
            return result;
        }
        return result;
    }

    private void interfaceMessageRecevied(String message, boolean isConnected) {
        if (mContext.getResources().getBoolean(17957022)) {
            WifiDevice device = new WifiDevice(message, isConnected);
            if (device.deviceState == 1) {
                this.mL2ConnectedDeviceMap.put(device.deviceAddress, device);
                if (readDeviceInfoFromDnsmasq(device)) {
                    this.mConnectedDeviceMap.put(device.deviceAddress, device);
                    sendConnectDevicesStateChangedBroadcast();
                } else {
                    new DnsmasqThread(this, device, 1000, 10).start();
                }
            } else if (device.deviceState == 0) {
                this.mConnectedDeviceMap.remove(device.deviceAddress);
                if (this.mL2ConnectedDeviceMap.remove(device.deviceAddress) != null) {
                    sendConnectDevicesStateChangedBroadcast();
                }
            }
        }
    }

    private void showSoftApClientsNotification(int icon) {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService("notification");
        if (notificationManager != null) {
            CharSequence message;
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
            intent.setFlags(1073741824);
            PendingIntent pi = PendingIntent.getActivityAsUser(mContext, 0, intent, 0, null, UserHandle.CURRENT);
            Resources r = Resources.getSystem();
            CharSequence title = r.getText(17040959);
            this.mChannelName = r.getText(17040372).toString();
            int size = this.mConnectedDeviceMap.size();
            if (size == 0) {
                message = r.getText(17040957);
            } else if (size == 1) {
                message = String.format(r.getText(17040958).toString(), new Object[]{Integer.valueOf(size)});
            } else {
                message = String.format(r.getText(17040956).toString(), new Object[]{Integer.valueOf(size)});
            }
            if (this.softApNotificationBuilder == null) {
                notificationManager.createNotificationChannel(new NotificationChannel(HOTSPOT_NOTIFICATION, this.mChannelName, 1));
                this.softApNotificationBuilder = new Builder(mContext, HOTSPOT_NOTIFICATION);
                this.softApNotificationBuilder.setWhen(0).setOngoing(true).setColor(mContext.getColor(17170763)).setVisibility(1).setCategory("status");
            }
            this.softApNotificationBuilder.setSmallIcon(icon).setContentTitle(title).setContentText(message).setContentIntent(pi).setPriority(-2);
            this.softApNotificationBuilder.setContentText(message);
            this.mLastSoftApNotificationId = icon + 10;
            notificationManager.notify(this.mChannelName, this.mLastSoftApNotificationId, this.softApNotificationBuilder.build());
        }
    }

    public void clearSoftApClientsNotification() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService("notification");
        if (notificationManager != null && this.mLastSoftApNotificationId != 0) {
            notificationManager.cancel(this.mChannelName, this.mLastSoftApNotificationId);
            this.mLastSoftApNotificationId = 0;
        }
    }

    public void connectionStatusChange(Message message) {
        boolean isConnected = message.arg1 == 1;
        Log.d(TAG, "devices status=" + isConnected);
        interfaceMessageRecevied((String) message.obj, isConnected);
    }

    public void clearConnectedDevice() {
        this.mConnectedDeviceMap.clear();
        this.mL2ConnectedDeviceMap.clear();
    }

    public List<WifiDevice> getConnectedStations() {
        List<WifiDevice> getConnectedStationsList = new ArrayList();
        if (mContext.getResources().getBoolean(17957022)) {
            for (String key : this.mConnectedDeviceMap.keySet()) {
                getConnectedStationsList.add((WifiDevice) this.mConnectedDeviceMap.get(key));
            }
        }
        return getConnectedStationsList;
    }
}
