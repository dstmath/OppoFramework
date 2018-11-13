package com.mediatek.server.wifi;

import android.content.Context;
import android.net.wifi.IWifiManager.Stub;
import android.net.wifi.WpsInfo;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.OppoHotspotClientInfo;
import com.android.server.wifi.WifiInjector;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mediatek.net.wifi.HotspotClient;

public abstract class OppoWifiServiceImpl extends Stub {
    private static final String TAG = "OPPOWifiService";
    private boolean DBG = false;
    private final Context mContext;
    private final WifiApStateMachine mWifiApStateMachine;
    private final WifiInjector mWifiInjector;
    private AsyncChannel mWifiStateMachineChannel;

    public OppoWifiServiceImpl(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mWifiStateMachineChannel = asyncChannel;
        this.mWifiApStateMachine = new WifiApStateMachine(wifiInjector.getWifiStateMachine(), context);
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", "WifiService");
    }

    public void startApWps(WpsInfo config) {
        enforceChangePermission();
        Slog.d(TAG, "startApWps config = " + config);
        this.mWifiApStateMachine.startApWpsCommand(config);
    }

    public List<HotspotClient> getHotspotClients() {
        enforceAccessPermission();
        if (!this.mContext.getResources().getBoolean(17957022)) {
            return Collections.emptyList();
        }
        List<HotspotClient> clientList = OppoHotspotClientInfo.getInstance(this.mContext).getHotspotClients();
        if (this.DBG) {
            dumpHotspotClients(clientList);
        }
        return clientList;
    }

    public void enableOppoWifiServiceLogging(int verbose) {
        if (verbose > 0) {
            this.DBG = true;
        } else {
            this.DBG = false;
        }
    }

    private void dumpHotspotClients(List<HotspotClient> list) {
        Slog.d(TAG, "dumpHotspotClients: \n");
        int count = 0;
        for (HotspotClient client : list) {
            count++;
            Slog.d(TAG, count + "," + client);
        }
    }

    public List<HotspotClient> getBlockedHotspotClients() {
        enforceAccessPermission();
        return this.mWifiApStateMachine.syncGetBlockedHotspotClientsList(this.mWifiStateMachineChannel);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x004e A:{SYNTHETIC, Splitter: B:20:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x008e A:{SYNTHETIC, Splitter: B:29:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0048 A:{SYNTHETIC, Splitter: B:16:0x0048} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> readClientList(String filename) {
        IOException ex;
        Throwable th;
        FileInputStream fstream = null;
        ArrayList<String> list = new ArrayList();
        try {
            FileInputStream fstream2 = new FileInputStream(filename);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream2)));
                while (true) {
                    String s = br.readLine();
                    if (s != null && s.length() != 0) {
                        list.add(s);
                    } else if (fstream2 != null) {
                        try {
                            fstream2.close();
                        } catch (IOException ex2) {
                            Slog.e(TAG, "IOException:" + ex2);
                        }
                    }
                }
                if (fstream2 != null) {
                }
            } catch (IOException e) {
                ex2 = e;
                fstream = fstream2;
                try {
                    Slog.e(TAG, "IOException:" + ex2);
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException ex22) {
                            Slog.e(TAG, "IOException:" + ex22);
                        }
                    }
                    return list;
                } catch (Throwable th2) {
                    th = th2;
                    if (fstream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fstream = fstream2;
                if (fstream != null) {
                    try {
                        fstream.close();
                    } catch (IOException ex222) {
                        Slog.e(TAG, "IOException:" + ex222);
                    }
                }
                throw th;
            }
        } catch (IOException e2) {
            ex222 = e2;
            Slog.e(TAG, "IOException:" + ex222);
            if (fstream != null) {
            }
            return list;
        }
        return list;
    }

    public String getClientIp(String deviceAddress) {
        enforceAccessPermission();
        Slog.d(TAG, "getClientIp deviceAddress = " + deviceAddress);
        if (TextUtils.isEmpty(deviceAddress)) {
            return null;
        }
        String LEASES_FILE = "/data/misc/dhcp/dnsmasq.leases";
        for (String s : readClientList("/data/misc/dhcp/dnsmasq.leases")) {
            if (s.indexOf(deviceAddress) != -1) {
                String[] fields = s.split(" ");
                if (fields.length > 3) {
                    return fields[2];
                }
            }
        }
        return null;
    }

    public String getClientDeviceName(String deviceAddress) {
        enforceAccessPermission();
        Slog.d(TAG, "getClientDeviceName deviceAddress = " + deviceAddress);
        if (TextUtils.isEmpty(deviceAddress)) {
            return null;
        }
        String LEASES_FILE = "/data/misc/dhcp/dnsmasq.leases";
        for (String s : readClientList("/data/misc/dhcp/dnsmasq.leases")) {
            if (s.indexOf(deviceAddress) != -1) {
                String[] fields = s.split(" ");
                if (fields.length > 4) {
                    return fields[3];
                }
            }
        }
        return null;
    }

    public boolean blockClient(HotspotClient client) {
        enforceChangePermission();
        Slog.d(TAG, "blockClient client = " + client);
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiApStateMachine.syncBlockClient(this.mWifiStateMachineChannel, client);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean unblockClient(HotspotClient client) {
        enforceChangePermission();
        Slog.d(TAG, "unblockClient client = " + client);
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiApStateMachine.syncUnblockClient(this.mWifiStateMachineChannel, client);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean isAllDevicesAllowed() {
        enforceAccessPermission();
        Slog.d(TAG, "isAllDevicesAllowed");
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiApStateMachine.syncIsAllDevicesAllowed(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean setAllDevicesAllowed(boolean enabled, boolean allowAllConnectedDevices) {
        enforceChangePermission();
        Slog.d(TAG, "setAllDevicesAllowed enabled = " + enabled + " allowAllConnectedDevices = " + allowAllConnectedDevices);
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiApStateMachine.syncSetAllDevicesAllowed(this.mWifiStateMachineChannel, enabled, allowAllConnectedDevices);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean allowDevice(String deviceAddress, String name) {
        boolean z;
        enforceChangePermission();
        String str = TAG;
        StringBuilder append = new StringBuilder().append("allowDevice address = ").append(deviceAddress).append(", name = ").append(name).append("is null?");
        if (name == null) {
            z = true;
        } else {
            z = false;
        }
        Slog.d(str, append.append(z).toString());
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiApStateMachine.syncAllowDevice(this.mWifiStateMachineChannel, deviceAddress, name);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean disallowDevice(String deviceAddress) {
        enforceChangePermission();
        Slog.d(TAG, "disallowDevice address = " + deviceAddress);
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiApStateMachine.syncDisallowDevice(this.mWifiStateMachineChannel, deviceAddress);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public List<HotspotClient> getAllowedDevices() {
        enforceAccessPermission();
        Slog.d(TAG, "getAllowedDevices");
        return this.mWifiApStateMachine.syncGetAllowedDevices(this.mWifiStateMachineChannel);
    }
}
