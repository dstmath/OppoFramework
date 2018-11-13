package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import mediatek.net.wifi.HotspotClient;

public class OppoHotspotClientInfo {
    private static final boolean DBG = true;
    private static final int DNSMASQ_POLLING_INTERVAL = 1000;
    private static final int DNSMASQ_POLLING_MAX_TIMES = 10;
    private static final String TAG = "OppoHotspotClientInfo";
    private static final String dhcpLocation = "/data/misc/dhcp/dnsmasq.leases";
    private static Context mContext;
    private static OppoHotspotClientInfo mOppoHotspotClientInfo;
    private HashMap<String, WifiDevice> mConnectedDeviceMap = new HashMap();
    private HashMap<String, HotspotClient> mHtsptClientMap = new HashMap();
    private HashMap<String, WifiDevice> mL2ConnectedDeviceMap = new HashMap();

    private static class DnsmasqThread extends Thread {
        private WifiDevice mDevice;
        private int mInterval;
        private int mMaxTimes;
        private final OppoHotspotClientInfo mOppoHotspotClientInformation;

        public DnsmasqThread(OppoHotspotClientInfo clientInfo, WifiDevice device, int interval, int maxTimes) {
            super("SoftAp");
            this.mOppoHotspotClientInformation = clientInfo;
            this.mInterval = interval;
            this.mMaxTimes = maxTimes;
            this.mDevice = device;
        }

        public void run() {
            boolean result = false;
            while (this.mMaxTimes > 0) {
                try {
                    result = this.mOppoHotspotClientInformation.readDeviceInfoFromDnsmasq(this.mDevice);
                    Log.d(OppoHotspotClientInfo.TAG, "Thread Running");
                    if (result) {
                        Log.d(OppoHotspotClientInfo.TAG, "Successfully poll device info for " + this.mDevice.deviceAddress);
                        break;
                    } else {
                        this.mMaxTimes--;
                        Thread.sleep((long) this.mInterval);
                    }
                } catch (Exception ex) {
                    result = false;
                    Log.e(OppoHotspotClientInfo.TAG, "Polling " + this.mDevice.deviceAddress + "error" + ex);
                }
            }
            if (!result) {
                Log.d(OppoHotspotClientInfo.TAG, "Polling timeout, suppose STA uses static ip " + this.mDevice.deviceAddress);
            }
            WifiDevice other = (WifiDevice) this.mOppoHotspotClientInformation.mL2ConnectedDeviceMap.get(this.mDevice.deviceAddress);
            if (other == null || other.deviceState != 1) {
                Log.d(OppoHotspotClientInfo.TAG, "Device " + this.mDevice.deviceAddress + "already disconnected, ignoring");
                return;
            }
            Date curDate = new Date(System.currentTimeMillis());
            this.mOppoHotspotClientInformation.mHtsptClientMap.put(this.mDevice.deviceAddress, new HotspotClient(this.mDevice.deviceAddress, false, this.mDevice.deviceName, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curDate)));
            this.mOppoHotspotClientInformation.mConnectedDeviceMap.put(this.mDevice.deviceAddress, this.mDevice);
            this.mOppoHotspotClientInformation.sendConnectDevicesStateChangedBroadcast();
        }
    }

    public static OppoHotspotClientInfo getInstance(Context mContext) {
        if (mOppoHotspotClientInfo != null) {
            return mOppoHotspotClientInfo;
        }
        OppoHotspotClientInfo oppoHotspotClientInfo = new OppoHotspotClientInfo(mContext);
        mOppoHotspotClientInfo = oppoHotspotClientInfo;
        return oppoHotspotClientInfo;
    }

    public void connectionStatusChange(Message message) {
        boolean isConnected = message.arg1 == 1;
        Log.d(TAG, "devices status=" + isConnected);
        interfaceMessageRecevied((String) message.obj, isConnected);
    }

    public void clearConnectedDevice() {
        this.mHtsptClientMap.clear();
        this.mConnectedDeviceMap.clear();
        this.mL2ConnectedDeviceMap.clear();
    }

    public List<WifiDevice> getConnectedStations() {
        List<WifiDevice> getConnectedStationsList = new ArrayList();
        if (mContext.getResources().getBoolean(17957022)) {
            for (String key : this.mConnectedDeviceMap.keySet()) {
                WifiDevice device = (WifiDevice) this.mConnectedDeviceMap.get(key);
                if (device != null) {
                    Log.d(TAG, "getTetherConnectedSta: addr=" + key + " name=" + device.deviceName + " address=" + device.deviceAddress);
                    getConnectedStationsList.add(device);
                }
            }
        }
        return getConnectedStationsList;
    }

    public List<HotspotClient> getHotspotClients() {
        List<HotspotClient> getConnectedStationsList = new ArrayList();
        if (mContext.getResources().getBoolean(17957022)) {
            for (String key : this.mHtsptClientMap.keySet()) {
                HotspotClient device = (HotspotClient) this.mHtsptClientMap.get(key);
                Log.d(TAG, "getHotspotClients: addr=" + key + " name=" + device.name + " conTime=" + device.conTime);
                getConnectedStationsList.add(device);
            }
        }
        return getConnectedStationsList;
    }

    private OppoHotspotClientInfo(Context context) {
        mContext = context;
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) mContext.getSystemService("connectivity");
    }

    private void sendConnectDevicesStateChangedBroadcast() {
        if (getConnectivityManager().isTetheringSupported()) {
            Intent broadcast = new Intent("codeaurora.net.conn.TETHER_CONNECT_STATE_CHANGED");
            broadcast.addFlags(603979776);
            mContext.sendStickyBroadcastAsUser(broadcast, UserHandle.ALL);
            int clientNum = this.mConnectedDeviceMap.size();
            Log.d(TAG, "clientNum of L2ConnectedDevice is : " + clientNum);
            Intent numIntent = new Intent("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED");
            numIntent.addFlags(603979776);
            numIntent.putExtra("HotspotClientNum", clientNum);
            mContext.sendStickyBroadcastAsUser(numIntent, UserHandle.ALL);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x00a6 A:{SYNTHETIC, Splitter: B:25:0x00a6} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00af A:{SYNTHETIC, Splitter: B:30:0x00af} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readDeviceInfoFromDnsmasq(WifiDevice device) {
        IOException ex;
        Throwable th;
        boolean result = false;
        FileInputStream fstream = null;
        try {
            FileInputStream fstream2 = new FileInputStream(dhcpLocation);
            try {
                Log.e(TAG, "dhcpLocation path location/data/misc/dhcp/dnsmasq.leases");
                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream2)));
                while (true) {
                    String line = br.readLine();
                    if (line == null || line.length() == 0) {
                        break;
                    }
                    String[] fields = line.split(" ");
                    Log.e(TAG, "lease file data" + line);
                    if (fields.length > 3) {
                        String addr = fields[1];
                        String name = fields[3];
                        if (addr.equals(device.deviceAddress)) {
                            Log.d(TAG, "Successfully poll device info for " + device.deviceAddress);
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
            Log.d(TAG, "interfaceMessageRecevied: message=" + message);
            WifiDevice device = new WifiDevice(message, isConnected);
            if (device.deviceState == 1) {
                this.mL2ConnectedDeviceMap.put(device.deviceAddress, device);
                Log.d(TAG, "device: connected");
                if (readDeviceInfoFromDnsmasq(device)) {
                    Log.d(TAG, "readDeviceInfoFromDnsmasq");
                    Date curDate = new Date(System.currentTimeMillis());
                    this.mHtsptClientMap.put(device.deviceAddress, new HotspotClient(device.deviceAddress, false, device.deviceName, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curDate)));
                    this.mConnectedDeviceMap.put(device.deviceAddress, device);
                    sendConnectDevicesStateChangedBroadcast();
                } else {
                    Log.d(TAG, "Starting poll device info for " + device.deviceAddress);
                    new DnsmasqThread(this, device, 1000, 10).start();
                }
            } else if (device.deviceState == 0) {
                Log.d(TAG, "device: disconnected");
                this.mHtsptClientMap.remove(device.deviceAddress);
                this.mL2ConnectedDeviceMap.remove(device.deviceAddress);
                if (this.mConnectedDeviceMap.remove(device.deviceAddress) == null) {
                    Log.d(TAG, " " + device.deviceAddress + " is not a connected device, don't send disconnect broadcast.");
                    return;
                }
                sendConnectDevicesStateChangedBroadcast();
            }
        }
    }
}
