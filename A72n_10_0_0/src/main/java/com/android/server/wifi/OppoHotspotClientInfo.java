package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import oppo.net.wifi.HotspotClient;

public class OppoHotspotClientInfo {
    private static final boolean DBG = true;
    private static final int DNSMASQ_POLLING_INTERVAL = 1000;
    private static final int DNSMASQ_POLLING_MAX_TIMES = 10;
    private static final String TAG = "OppoHotspotClientInfo";
    private static final String dhcpLocation = "/data/misc/dhcp/dnsmasq.leases";
    private static Context mContext;
    private static OppoHotspotClientInfo mOppoHotspotClientInfo;
    private List<HotspotClient> mCacheHotspotClientsList;
    private HashMap<String, WifiDevice> mConnectedDeviceMap = new HashMap<>();
    private HashMap<String, HotspotClient> mHtsptClientMap = new HashMap<>();
    private HashMap<String, WifiDevice> mL2ConnectedDeviceMap = new HashMap<>();

    public static OppoHotspotClientInfo getInstance(Context mContext2) {
        OppoHotspotClientInfo oppoHotspotClientInfo = mOppoHotspotClientInfo;
        if (oppoHotspotClientInfo != null) {
            return oppoHotspotClientInfo;
        }
        OppoHotspotClientInfo oppoHotspotClientInfo2 = new OppoHotspotClientInfo(mContext2);
        mOppoHotspotClientInfo = oppoHotspotClientInfo2;
        return oppoHotspotClientInfo2;
    }

    public void connectionStatusChange(Message message, boolean isConnected) {
        Log.d(TAG, "devices status=" + isConnected);
        interfaceMessageRecevied((String) message.obj, isConnected);
    }

    public void clearConnectedDevice() {
        this.mHtsptClientMap.clear();
        this.mConnectedDeviceMap.clear();
        this.mL2ConnectedDeviceMap.clear();
        List<HotspotClient> list = this.mCacheHotspotClientsList;
        if (list != null) {
            list.clear();
        }
    }

    public List<WifiDevice> getConnectedStations() {
        List<WifiDevice> getConnectedStationsList = new ArrayList<>();
        try {
            for (String key : this.mConnectedDeviceMap.keySet()) {
                WifiDevice device = this.mConnectedDeviceMap.get(key);
                if (device != null) {
                    Log.d(TAG, "getTetherConnectedSta: addr=" + key + " name=" + device.deviceName + " address=" + device.deviceAddress);
                    getConnectedStationsList.add(device);
                }
            }
        } catch (ConcurrentModificationException e) {
            Log.e(TAG, "ConcurrentModificationException occurs, return broken list.");
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return getConnectedStationsList;
    }

    public List<HotspotClient> getHotspotClients() {
        List<HotspotClient> getConnectedStationsList = new ArrayList<>();
        try {
            for (String key : this.mHtsptClientMap.keySet()) {
                HotspotClient device = this.mHtsptClientMap.get(key);
                Log.d(TAG, "getHotspotClients: addr=" + key + " name=" + device.name + " conTime=" + device.conTime);
                getConnectedStationsList.add(device);
            }
        } catch (ConcurrentModificationException e) {
            Log.e(TAG, "ConcurrentModificationException occurs, return cache list.");
            List<HotspotClient> list = this.mCacheHotspotClientsList;
            if (list != null) {
                return list;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        this.mCacheHotspotClientsList = getConnectedStationsList;
        return getConnectedStationsList;
    }

    private OppoHotspotClientInfo(Context context) {
        mContext = context;
    }

    /* access modifiers changed from: private */
    public static class DnsmasqThread extends Thread {
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
            while (true) {
                try {
                    if (this.mMaxTimes <= 0) {
                        break;
                    }
                    result = this.mOppoHotspotClientInformation.readDeviceInfoFromDnsmasq(this.mDevice);
                    Log.d(OppoHotspotClientInfo.TAG, "Thread Running");
                    if (result) {
                        Log.d(OppoHotspotClientInfo.TAG, "Successfully poll device info for " + this.mDevice.deviceAddress);
                        break;
                    }
                    this.mMaxTimes--;
                    Thread.sleep((long) this.mInterval);
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
                WifiDevice temp = (WifiDevice) this.mOppoHotspotClientInformation.mConnectedDeviceMap.get(this.mDevice.deviceAddress);
                if (temp != null && temp.deviceState == 1) {
                    this.mOppoHotspotClientInformation.mHtsptClientMap.remove(this.mDevice.deviceAddress);
                    this.mOppoHotspotClientInformation.mConnectedDeviceMap.remove(this.mDevice.deviceAddress);
                    Log.d(OppoHotspotClientInfo.TAG, "Exception: dnsmasq poll end, " + this.mDevice.deviceAddress + "is not a connected device, remove and send disconnect broadcast.");
                    this.mOppoHotspotClientInformation.sendConnectDevicesStateChangedBroadcast();
                }
                Log.d(OppoHotspotClientInfo.TAG, "Device " + this.mDevice.deviceAddress + "already disconnected, ignoring");
                return;
            }
            Date curDate = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            WifiDevice temp2 = (WifiDevice) this.mOppoHotspotClientInformation.mConnectedDeviceMap.get(this.mDevice.deviceAddress);
            if (temp2 == null || temp2.deviceState != 1) {
                Log.d(OppoHotspotClientInfo.TAG, "Exception: dnsmasq poll end, device not in mConnectedDeviceMap");
                this.mOppoHotspotClientInformation.mHtsptClientMap.put(this.mDevice.deviceAddress, new HotspotClient(this.mDevice.deviceAddress, false, this.mDevice.deviceName, formatter.format(curDate)));
                this.mOppoHotspotClientInformation.mConnectedDeviceMap.put(this.mDevice.deviceAddress, this.mDevice);
                this.mOppoHotspotClientInformation.sendConnectDevicesStateChangedBroadcast();
                return;
            }
            this.mOppoHotspotClientInformation.mHtsptClientMap.remove(this.mDevice.deviceAddress);
            this.mOppoHotspotClientInformation.mConnectedDeviceMap.remove(this.mDevice.deviceAddress);
            this.mOppoHotspotClientInformation.mHtsptClientMap.put(this.mDevice.deviceAddress, new HotspotClient(this.mDevice.deviceAddress, false, this.mDevice.deviceName, formatter.format(curDate)));
            this.mOppoHotspotClientInformation.mConnectedDeviceMap.put(this.mDevice.deviceAddress, this.mDevice);
        }
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) mContext.getSystemService("connectivity");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendConnectDevicesStateChangedBroadcast() {
        if (getConnectivityManager().isTetheringSupported()) {
            int clientNum = this.mConnectedDeviceMap.size();
            Log.d(TAG, "clientNum of L2ConnectedDevice is : " + clientNum);
            Intent numIntent = new Intent("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED");
            numIntent.addFlags(603979776);
            numIntent.putExtra("HotspotClientNum", clientNum);
            mContext.sendBroadcastAsUser(numIntent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r3.close();
     */
    private boolean readDeviceInfoFromDnsmasq(WifiDevice device) {
        boolean result = false;
        FileInputStream fstream = null;
        try {
            FileInputStream fstream2 = new FileInputStream(dhcpLocation);
            Log.e(TAG, "dhcpLocation path location/data/misc/dhcp/dnsmasq.leases");
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream2)));
            while (true) {
                String line = br.readLine();
                if (line != null && line.length() != 0) {
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
            }
        } catch (IOException ex) {
            Log.e(TAG, "readDeviceNameFromDnsmasq: " + ex);
            if (0 != 0) {
                try {
                    fstream.close();
                } catch (IOException e) {
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fstream.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
        return result;
    }

    private void interfaceMessageRecevied(String message, boolean isConnected) {
        Log.d(TAG, "interfaceMessageRecevied: message=" + message);
        WifiDevice device = new WifiDevice(message, isConnected);
        if (device.deviceState == 1) {
            this.mL2ConnectedDeviceMap.put(device.deviceAddress, device);
            Log.d(TAG, "device: connected");
            WifiInjector.getInstance().getOppoSoftapStatistics().informStaConnected(device);
            if (readDeviceInfoFromDnsmasq(device)) {
                Log.d(TAG, "readDeviceInfoFromDnsmasq");
                Date curDate = new Date(System.currentTimeMillis());
                this.mHtsptClientMap.put(device.deviceAddress, new HotspotClient(device.deviceAddress, false, device.deviceName, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curDate)));
                this.mConnectedDeviceMap.put(device.deviceAddress, device);
                sendConnectDevicesStateChangedBroadcast();
                return;
            }
            Date curDate2 = new Date(System.currentTimeMillis());
            this.mHtsptClientMap.put(device.deviceAddress, new HotspotClient(device.deviceAddress, false, device.deviceName, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curDate2)));
            this.mConnectedDeviceMap.put(device.deviceAddress, device);
            Log.d(TAG, "Device " + device.deviceAddress + "default handle with static ip");
            sendConnectDevicesStateChangedBroadcast();
            Log.d(TAG, "Starting poll device info for " + device.deviceAddress);
            new DnsmasqThread(this, device, 1000, 10).start();
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

    public class WifiDevice {
        public static final int CONNECTED = 1;
        public static final int DISCONNECTED = 0;
        public String deviceAddress = "";
        public String deviceName = "";
        public int deviceState = 0;

        public WifiDevice() {
        }

        public WifiDevice(String deviceAddress2, boolean isConnected) {
            if (isConnected) {
                this.deviceState = 1;
            } else {
                this.deviceState = 0;
            }
            this.deviceAddress = deviceAddress2;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof WifiDevice)) {
                return false;
            }
            WifiDevice other = (WifiDevice) obj;
            String str = this.deviceAddress;
            if (str != null) {
                return str.equals(other.deviceAddress);
            }
            if (other.deviceAddress == null) {
                return true;
            }
            return false;
        }

        public int describeContents() {
            return 0;
        }
    }
}
