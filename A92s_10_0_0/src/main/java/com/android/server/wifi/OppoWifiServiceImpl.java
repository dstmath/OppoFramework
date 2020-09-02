package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WpsInfo;
import android.os.Binder;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.AsyncChannel;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oppo.net.wifi.HotspotClient;

public abstract class OppoWifiServiceImpl extends BaseWifiService {
    private static final String ACTION_LOAD_FROM_STORE = "android.intent.action.OPPO_ACTION_LOAD_FROM_STORE";
    private static final String TAG = "OPPOWifiService";
    private boolean DBG = true;
    private final Context mContext;
    private BroadcastReceiver mLoadFromStoreReceiver;
    private final HashMap<Integer, LocalOnlyHotspotRequestInfo> mLocalOnlyHotspotRequests;
    private WifiLog mLog;
    private int mPrevApBand = 0;
    private WifiApConfigStore mWifiApConfigStore;
    /* access modifiers changed from: private */
    public int mWifiApState = 11;
    private final WifiInjector mWifiInjector;

    public OppoWifiServiceImpl(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mLog = this.mWifiInjector.makeLog(TAG);
        this.mLocalOnlyHotspotRequests = new HashMap<>();
        this.mWifiApConfigStore = this.mWifiInjector.getWifiApConfigStore();
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoWifiServiceImpl.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                int unused = OppoWifiServiceImpl.this.mWifiApState = intent.getIntExtra("wifi_state", 11);
            }
        }, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
        if (this.mLoadFromStoreReceiver == null) {
            this.mLoadFromStoreReceiver = new BroadcastReceiver() {
                /* class com.android.server.wifi.OppoWifiServiceImpl.AnonymousClass2 */

                public void onReceive(Context context, Intent intent) {
                    if (OppoWifiServiceImpl.this.getSoftApManager() == null) {
                        Slog.e(OppoWifiServiceImpl.TAG, "update the OppoSoftApManager.mDeniedClients when hotspot off status");
                        OppoSoftApManager.loadDeniedDevice();
                    }
                }
            };
            this.mContext.registerReceiver(this.mLoadFromStoreReceiver, new IntentFilter(ACTION_LOAD_FROM_STORE));
        }
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", "WifiService");
    }

    public WifiConfiguration getWifiSharingConfiguration() {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (this.mWifiInjector.getWifiPermissionsUtil().checkConfigOverridePermission(uid)) {
            this.mLog.info("getWifiSharingConfiguration uid=%").c((long) uid).flush();
            return this.mWifiInjector.getOppoWifiSharingManager().syncGetWifiSharingConfiguration();
        }
        throw new SecurityException("App not allowed to read or update stored WiFi Sharing config (uid = " + uid + ")");
    }

    public boolean setWifiSharingConfiguration(WifiConfiguration wifiConfig) {
        int uid = Binder.getCallingUid();
        if (this.mWifiInjector.getWifiPermissionsUtil().checkConfigOverridePermission(uid)) {
            this.mLog.info("setWifiSharingConfiguration uid=%").c((long) uid).flush();
            if (wifiConfig == null) {
                return false;
            }
            if (WifiApConfigStore.validateApWifiConfiguration(wifiConfig)) {
                this.mWifiInjector.getOppoWifiSharingManager().setWifiSharingConfiguration(wifiConfig);
                return true;
            }
            Slog.e(TAG, "Invalid WifiConfiguration");
            return false;
        }
        throw new SecurityException("App not allowed to read or update stored WiFi Sharing config (uid = " + uid + ")");
    }

    public boolean startWifiSharing(WifiConfiguration wifiConfig) {
        OppoWifiSharingManager sharingManager = this.mWifiInjector.getOppoWifiSharingManager();
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", "WifiService");
        Slog.d(TAG, "startWifiSharing:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                stopSoftApInternal(1);
            }
            if (this.mWifiInjector.getClientModeImpl().isDisconnected()) {
                Log.e(TAG, "start wifisharing false, due to wifi disconnected.");
                return false;
            }
            this.mWifiInjector.getClientModeImpl().stopP2pConnectInternal();
            sharingManager.setWifiTetheringType(4);
            WifiConfiguration wifiConfig2 = this.mWifiInjector.getWifiApConfigStore().getSharingConfiguration();
            this.mWifiInjector.getWifiController().setStaSoftApConcurrencyForSharing(true);
            boolean startWifiSharingInternal = startWifiSharingInternal(wifiConfig2, 1);
            return startWifiSharingInternal;
        }
    }

    private boolean startWifiSharingInternal(WifiConfiguration wifiConfig, int mode) {
        this.mLog.trace("startSoftApInternal uid=% mode=%").c((long) Binder.getCallingUid()).c((long) mode).flush();
        if (wifiConfig == null || WifiApConfigStore.validateApWifiConfiguration(wifiConfig)) {
            this.mWifiInjector.getWifiController().sendMessage(155680, 1, 0, new SoftApModeConfiguration(mode, wifiConfig));
            return true;
        }
        Slog.e(TAG, "Invalid WifiConfiguration");
        return false;
    }

    public boolean stopWifiSharing() {
        boolean stopWifiSharingInternal;
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", "WifiService");
        Slog.d(TAG, "stopWifiSharing:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLog.trace("Call to stop Tethering while LOHS is active, Registered LOHS callers will be updated when softap stopped.").flush();
            }
            stopWifiSharingInternal = stopWifiSharingInternal();
        }
        return stopWifiSharingInternal;
    }

    private boolean stopWifiSharingInternal() {
        this.mLog.trace("stopWifiSharingInternal uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiInjector.getWifiController().sendMessage(155680, 0, 0);
        return true;
    }

    /* access modifiers changed from: protected */
    public void disableWifiSharing() {
        long token = Binder.clearCallingIdentity();
        try {
            if (this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingEnabledState()) {
                Log.d(TAG, "disableWifiSharing");
                stopWifiSharing();
            }
            Binder.restoreCallingIdentity(token);
        } catch (Exception e) {
            Log.e(TAG, "disableWifiSharing: exception -- " + e);
            throw e;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private boolean stopSoftApInternal(int mode) {
        this.mLog.trace("stopSoftApInternal uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiInjector.getWifiController().sendMessage(155658, 0, mode);
        return true;
    }

    public void startApWps(WpsInfo config) {
        enforceChangePermission();
        Slog.d(TAG, "startApWps config = " + config);
    }

    public List<HotspotClient> getHotspotClients() {
        enforceAccessPermission();
        Slog.d(TAG, "getHotspotClients ");
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
        Iterator<HotspotClient> it = list.iterator();
        while (it.hasNext()) {
            count++;
            Slog.d(TAG, count + "," + it.next());
        }
    }

    public List<HotspotClient> getBlockedHotspotClients() {
        enforceAccessPermission();
        if (getSoftApManager() != null) {
            return OppoSoftApManager.getBlockedHotspotClientsList();
        }
        Slog.e(TAG, "failed to get softApManager!");
        return OppoSoftApManager.getBlockedHotspotClientsList();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:?, code lost:
        r8.close();
     */
    private ArrayList<String> readClientList(String filename) {
        StringBuilder sb;
        FileInputStream fstream;
        DataInputStream in;
        FileInputStream fstream2 = null;
        DataInputStream in2 = null;
        BufferedReader br = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            fstream = new FileInputStream(filename);
            in = new DataInputStream(fstream);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String s = br2.readLine();
                if (s == null || s.length() == 0) {
                    try {
                        break;
                    } catch (IOException ex) {
                        Slog.e(TAG, "IOException1:" + ex);
                    }
                } else {
                    list.add(s);
                }
            }
        } catch (IOException ex2) {
            Slog.e(TAG, "IOException:" + ex2);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex3) {
                    Slog.e(TAG, "IOException1:" + ex3);
                }
            }
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException ex4) {
                    Slog.e(TAG, "IOException2:" + ex4);
                }
            }
            if (fstream2 != null) {
                try {
                    fstream2.close();
                } catch (IOException e) {
                    ex = e;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex5) {
                    Slog.e(TAG, "IOException1:" + ex5);
                }
            }
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException ex6) {
                    Slog.e(TAG, "IOException2:" + ex6);
                }
            }
            if (fstream2 != null) {
                try {
                    fstream2.close();
                } catch (IOException ex7) {
                    Slog.e(TAG, "IOException:" + ex7);
                }
            }
            throw th;
        }
        return list;
        try {
            in.close();
        } catch (IOException ex8) {
            Slog.e(TAG, "IOException2:" + ex8);
        }
        try {
            fstream.close();
            break;
        } catch (IOException e2) {
            ex = e2;
            sb = new StringBuilder();
        }
        return list;
        sb.append("IOException:");
        sb.append(ex);
        Slog.e(TAG, sb.toString());
        return list;
        fstream.close();
        return list;
    }

    public String getClientIp(String deviceAddress) {
        enforceAccessPermission();
        Slog.d(TAG, "getClientIp deviceAddress = " + deviceAddress);
        if (TextUtils.isEmpty(deviceAddress)) {
            return null;
        }
        Iterator<String> it = readClientList("/data/misc/dhcp/dnsmasq.leases").iterator();
        while (it.hasNext()) {
            String s = it.next();
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
        Iterator<String> it = readClientList("/data/misc/dhcp/dnsmasq.leases").iterator();
        while (it.hasNext()) {
            String s = it.next();
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
        if (client == null || client.deviceAddress == null) {
            Slog.e(TAG, "Client is null!");
            return false;
        }
        OppoSoftApManager softApManager = getSoftApManager();
        if (softApManager != null) {
            return softApManager.syncBlockClient(client);
        }
        return false;
    }

    public boolean unblockClient(HotspotClient client) {
        enforceChangePermission();
        Slog.d(TAG, "unblockClient client = " + client);
        if (client == null || client.deviceAddress == null) {
            Slog.e(TAG, "Client is null!");
            return false;
        }
        OppoSoftApManager softApManager = getSoftApManager();
        if (softApManager != null) {
            return softApManager.syncUnblockClient(client);
        }
        return OppoSoftApManager.rmDeniedClientFromListAndFile(client);
    }

    public boolean isAllDevicesAllowed() {
        enforceAccessPermission();
        Slog.d(TAG, "isAllDevicesAllowed");
        return false;
    }

    public boolean setAllDevicesAllowed(boolean enabled, boolean allowAllConnectedDevices) {
        enforceChangePermission();
        Slog.d(TAG, "setAllDevicesAllowed enabled = " + enabled + " allowAllConnectedDevices = " + allowAllConnectedDevices);
        return true;
    }

    public boolean allowDevice(String deviceAddress, String name) {
        enforceChangePermission();
        StringBuilder sb = new StringBuilder();
        sb.append("allowDevice address = ");
        sb.append(deviceAddress);
        sb.append(", name = ");
        sb.append(name);
        sb.append("is null?");
        sb.append(name == null);
        Slog.d(TAG, sb.toString());
        if (deviceAddress != null) {
            return true;
        }
        Slog.e(TAG, "deviceAddress is null!");
        return false;
    }

    public boolean disallowDevice(String deviceAddress) {
        enforceChangePermission();
        Slog.d(TAG, "disallowDevice address = " + deviceAddress);
        return true;
    }

    public List<HotspotClient> getAllowedDevices() {
        enforceAccessPermission();
        Slog.d(TAG, "getAllowedDevices");
        return Collections.emptyList();
    }

    /* access modifiers changed from: private */
    public OppoSoftApManager getSoftApManager() {
        ActiveModeWarden activeModeWarden = this.mWifiInjector.getActiveModeWarden();
        ArraySet<ActiveModeManager> activeModeManager = null;
        try {
            List<Field> fieldList = new ArrayList<>();
            for (Class tempClass = activeModeWarden.getClass(); tempClass != null; tempClass = tempClass.getSuperclass()) {
                fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            }
            Iterator<Field> it = fieldList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Field field = it.next();
                if ("mActiveModeManagers".equals(field.getName())) {
                    field.setAccessible(true);
                    activeModeManager = (ArraySet) field.get(activeModeWarden);
                    break;
                }
            }
            Iterator<ActiveModeManager> it2 = activeModeManager.iterator();
            while (it2.hasNext()) {
                ActiveModeManager manager = it2.next();
                if (manager instanceof OppoSoftApManager) {
                    return (OppoSoftApManager) manager;
                }
            }
            return null;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearWifiOCloudData(boolean hardDelete) {
        enforceAccessPermission();
        WifiConfigManager configManager = this.mWifiInjector.getWifiConfigManager();
        if (configManager != null) {
            configManager.clearWifiOCloudData(hardDelete);
        }
    }

    public List<String> getWifiOCloudData(boolean isDirtyOnly) {
        enforceAccessPermission();
        WifiConfigManager configManager = this.mWifiInjector.getWifiConfigManager();
        if (configManager != null) {
            return configManager.getWifiOCloudData(isDirtyOnly);
        }
        return null;
    }

    public void updateGlobalId(int networkId, String globalId) {
        enforceAccessPermission();
        WifiConfigManager configManager = this.mWifiInjector.getWifiConfigManager();
        if (configManager != null) {
            configManager.updateGlobalId(networkId, globalId);
        }
    }

    public void removeNetworkByGlobalId(String configKey, String globalId, boolean hardDelete) {
        enforceAccessPermission();
        WifiConfigManager configManager = this.mWifiInjector.getWifiConfigManager();
        if (configManager != null) {
            configManager.removeNetworkByGlobalId(configKey, globalId, hardDelete);
        }
    }

    public void setDirtyFlag(String globalId, boolean value) {
        enforceAccessPermission();
        WifiConfigManager configManager = this.mWifiInjector.getWifiConfigManager();
        if (configManager != null) {
            configManager.setDirtyFlag(globalId, value);
        }
    }

    public boolean hasOCloudDirtyData() {
        enforceAccessPermission();
        WifiConfigManager configManager = this.mWifiInjector.getWifiConfigManager();
        if (configManager != null) {
            return configManager.hasOCloudDirtyData();
        }
        return false;
    }

    public String getWifiPowerEventCode() {
        enforceAccessPermission();
        return this.mWifiInjector.getClientModeImpl().getWifiPowerEventCode();
    }

    public boolean isP2p5GSupported() {
        OppoSoftapP2pBandControl mOppoSoftapP2pBandControl = WifiInjector.getInstance().getOppoSoftapP2pBandControl();
        return !mOppoSoftapP2pBandControl.isP2pInOnly2GRegion() || !mOppoSoftapP2pBandControl.needP2pLimit2GOnlyBand();
    }

    public boolean isSoftap5GSupported() {
        OppoSoftapP2pBandControl mOppoSoftapP2pBandControl = WifiInjector.getInstance().getOppoSoftapP2pBandControl();
        return !mOppoSoftapP2pBandControl.isSoftapInOnly2GRegion() || !mOppoSoftapP2pBandControl.needSoftapUse2GOnlyCountry();
    }
}
