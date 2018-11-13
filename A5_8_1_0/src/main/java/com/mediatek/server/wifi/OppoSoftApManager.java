package com.mediatek.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.InterfaceConfiguration;
import android.net.wifi.IApInterface;
import android.net.wifi.IClientInterface;
import android.net.wifi.IInterfaceEventCallback.Stub;
import android.net.wifi.IWificond;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.wifi.OppoHotspotClientInfo;
import com.android.server.wifi.SoftApManager;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.StateMachineDeathRecipient;
import com.android.server.wifi.WifiApConfigStore;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiMetrics;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.util.ApConfigUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import mediatek.net.wifi.HotspotClient;

public class OppoSoftApManager extends SoftApManager {
    private static final String ACCEPT_MAC_UPDATE_FILE = (Environment.getDataDirectory() + "/misc/wifi/accept_mac_update.conf");
    private static final String ALLOWED_LIST_FILE = (Environment.getDataDirectory() + "/misc/wifi/allowed_list.conf");
    static final int BASE = 131072;
    private static final boolean DBG = true;
    private static final String DENIED_MAC_FILE = "/data/misc/wifi/hostapd.deny";
    private static final String HOSTAPD_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/hostapd.conf");
    private static final String MAC_PATTERN_STR = "([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}";
    private static final int MAX_CLIENT_NUM = 10;
    public static final int M_CMD_ALLOW_DEVICE = 131378;
    public static final int M_CMD_BLOCK_CLIENT = 131372;
    public static final int M_CMD_BLOCK_CLIENT_ONLY = 131473;
    public static final int M_CMD_BLOCK_SAVED_CLIENTS = 131472;
    public static final int M_CMD_DISALLOW_DEVICE = 131379;
    public static final int M_CMD_ENTER_SET_MAX_CLIENTS = 131474;
    public static final int M_CMD_GET_ALLOWED_DEVICES = 131380;
    public static final int M_CMD_GET_BLOCKED_CLIENTS_LIST = 131381;
    public static final int M_CMD_GET_CLIENTS_LIST = 131374;
    public static final int M_CMD_IS_ALL_DEVICES_ALLOWED = 131376;
    public static final int M_CMD_SET_ALL_DEVICES_ALLOWED = 131377;
    public static final int M_CMD_START_AP_WPS = 131375;
    public static final int M_CMD_UNBLOCK_CLIENT = 131373;
    private static final String TAG = "OppoSoftApManager";
    private static final String WIFI_HOTSPOT_MAX_CLIENT_NUM = "oppo_wifi_ap_max_devices_connect";
    private static List<HotspotClient> mDeniedClients = new ArrayList();
    private static SoftApStateMachine mStateMachine = null;
    private static final String nameTag = "#name-";
    private static LinkedHashMap<String, HotspotClient> sAllowedDevices;
    private WifiConfiguration mApConfig;
    private final IApInterface mApInterface;
    private int mBlockSavedClientsRetryCount;
    private final Context mContext;
    private final String mCountryCode;
    private HashMap<String, HotspotClient> mHotspotClients = new HashMap();
    private final Listener mListener;
    private int mMaxClientsRetryCount;
    private int mMaxNumSta = 10;
    private final INetworkManagementService mNwService;
    private OppoHotspotClientInfo mOppoHotspotClientInfo;
    private final WifiApConfigStore mWifiApConfigStore;
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;

    private static class InterfaceEventHandler extends Stub {
        private SoftApStateMachine mSoftApStateMachine;

        InterfaceEventHandler(SoftApStateMachine stateMachine) {
            this.mSoftApStateMachine = stateMachine;
        }

        public void OnClientTorndownEvent(IClientInterface networkInterface) {
        }

        public void OnClientInterfaceReady(IClientInterface networkInterface) {
        }

        public void OnApTorndownEvent(IApInterface networkInterface) {
        }

        public void OnApInterfaceReady(IApInterface networkInterface) {
        }

        public void OnSoftApClientEvent(byte[] mac_address, boolean connect_status) {
            int i = 1;
            StringBuilder sb = new StringBuilder(18);
            for (byte b : mac_address) {
                if (sb.length() > 0) {
                    sb.append(':');
                }
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(b)}));
            }
            Log.d(OppoSoftApManager.TAG, "Client mac_addr = " + sb.toString() + " status = " + connect_status);
            Message msg = Message.obtain();
            msg.obj = sb.toString();
            SoftApStateMachine softApStateMachine = this.mSoftApStateMachine;
            if (!connect_status) {
                i = 0;
            }
            softApStateMachine.sendMessage(5, i, 0, msg.obj);
        }
    }

    private class SoftApStateMachine extends StateMachine {
        public static final int CMD_AP_INTERFACE_BINDER_DEATH = 2;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_POLL_IP_ADDRESS = 4;
        public static final int CMD_SOFTAP_CLIENT_CONNECT_STATUS_CHANGED = 5;
        public static final int CMD_START = 0;
        public static final int CMD_STOP = 1;
        private static final int POLL_IP_ADDRESS_INTERVAL_MSECS = 2000;
        private static final int POLL_IP_TIMES = 15;
        private static final int Wifi_FAILURE = -1;
        private static final int Wifi_SUCCESS = 1;
        private final StateMachineDeathRecipient mDeathRecipient = new StateMachineDeathRecipient(this, 2);
        private final State mIdleState = new IdleState(this, null);
        private InterfaceEventHandler mInterfaceEventHandler;
        private String mMonitorInterfaceName;
        private NetworkObserver mNetworkObserver;
        private AsyncChannel mReplyChannel = new AsyncChannel();
        private final State mStartedState = new StartedState(this, null);
        private WifiInjector mWifiInjector;
        private final WifiManager mWifiManager;
        private IWificond mWificond;

        private class IdleState extends State {
            /* synthetic */ IdleState(SoftApStateMachine this$1, IdleState -this1) {
                this();
            }

            private IdleState() {
            }

            public void enter() {
                Log.d(OppoSoftApManager.TAG, getName());
                SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                unregisterObserver();
                SoftApStateMachine.this.mWificond = SoftApStateMachine.this.mWifiInjector.makeWificond();
                if (SoftApStateMachine.this.mWificond == null) {
                    Log.w(OppoSoftApManager.TAG, "Failed to get wificond binder handler");
                }
                try {
                    SoftApStateMachine.this.mWificond.RegisterCallback(SoftApStateMachine.this.mInterfaceEventHandler);
                } catch (RemoteException e) {
                }
            }

            public boolean processMessage(Message message) {
                switch (message.what) {
                    case 0:
                        OppoSoftApManager.this.updateApState(12, 0);
                        if (!SoftApStateMachine.this.mDeathRecipient.linkToDeath(OppoSoftApManager.this.mApInterface.asBinder())) {
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            OppoSoftApManager.this.updateApState(14, 0);
                            OppoSoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, 0);
                            break;
                        }
                        try {
                            SoftApStateMachine.this.mNetworkObserver = new NetworkObserver(OppoSoftApManager.this.mApInterface.getInterfaceName());
                            OppoSoftApManager.this.mNwService.registerObserver(SoftApStateMachine.this.mNetworkObserver);
                            int result = OppoSoftApManager.this.startSoftAp((WifiConfiguration) message.obj);
                            if (result == 0) {
                                SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                                break;
                            }
                            int failureReason = 0;
                            if (result == 1) {
                                failureReason = 1;
                            }
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            unregisterObserver();
                            OppoSoftApManager.this.updateApState(14, failureReason);
                            OppoSoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, failureReason);
                            break;
                        } catch (RemoteException e) {
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            unregisterObserver();
                            OppoSoftApManager.this.updateApState(14, 0);
                            OppoSoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, 0);
                            break;
                        }
                }
                return true;
            }

            private void unregisterObserver() {
                if (SoftApStateMachine.this.mNetworkObserver != null) {
                    try {
                        OppoSoftApManager.this.mNwService.unregisterObserver(SoftApStateMachine.this.mNetworkObserver);
                    } catch (RemoteException e) {
                    }
                    SoftApStateMachine.this.mNetworkObserver = null;
                }
            }
        }

        private class NetworkObserver extends BaseNetworkObserver {
            private final String mIfaceName;

            NetworkObserver(String ifaceName) {
                this.mIfaceName = ifaceName;
            }

            public void interfaceLinkStateChanged(String iface, boolean up) {
                if (this.mIfaceName.equals(iface)) {
                    int i;
                    SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                    if (up) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    softApStateMachine.sendMessage(3, i, 0, this);
                }
            }
        }

        private class StartedState extends State {
            private boolean mIfaceIsUp;

            /* synthetic */ StartedState(SoftApStateMachine this$1, StartedState -this1) {
                this();
            }

            private StartedState() {
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != this.mIfaceIsUp) {
                    this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.d(OppoSoftApManager.TAG, "SoftAp is ready for use");
                        OppoSoftApManager.this.updateApState(13, 0);
                        OppoSoftApManager.this.mWifiMetrics.incrementSoftApStartResult(true, 0);
                    }
                }
            }

            private void sendClientsChangedBroadcast() {
                Intent intent = new Intent("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED");
                intent.addFlags(67108864);
                OppoSoftApManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }

            private void sendClientsIpReadyBroadcast(String mac, String ip, String deviceName) {
                Intent intent = new Intent("android.net.wifi.WIFI_HOTSPOT_CLIENTS_IP_READY");
                intent.addFlags(67108864);
                intent.putExtra("deviceAddress", mac);
                intent.putExtra("ipAddress", ip);
                intent.putExtra("deviceName", deviceName);
                OppoSoftApManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }

            private boolean blockSavedClients() {
                String[] devices = new String[OppoSoftApManager.mDeniedClients.size()];
                int i = 0;
                for (HotspotClient client : OppoSoftApManager.mDeniedClients) {
                    if (i < OppoSoftApManager.mDeniedClients.size()) {
                        int i2 = i + 1;
                        devices[i] = client.deviceAddress;
                        i = i2;
                    }
                }
                try {
                    return OppoSoftApManager.this.mApInterface.blockSavedClients(devices);
                } catch (RemoteException e) {
                    Log.e(OppoSoftApManager.TAG, "remote exception: " + e);
                    return false;
                }
            }

            public void enter() {
                Log.d(OppoSoftApManager.TAG, getName());
                this.mIfaceIsUp = false;
                InterfaceConfiguration config = null;
                try {
                    config = OppoSoftApManager.this.mNwService.getInterfaceConfig(OppoSoftApManager.this.mApInterface.getInterfaceName());
                } catch (RemoteException e) {
                }
                if (config != null) {
                    onUpChanged(config.isUp());
                }
                if (!OppoSoftApManager.this.setMaxClient(OppoSoftApManager.this.mMaxNumSta)) {
                    OppoSoftApManager.this.mMaxClientsRetryCount = 1;
                    SoftApStateMachine.this.sendMessageDelayed(OppoSoftApManager.M_CMD_ENTER_SET_MAX_CLIENTS, 0, 0, null, 100);
                }
                if (!blockSavedClients()) {
                    OppoSoftApManager.this.mBlockSavedClientsRetryCount = 1;
                    SoftApStateMachine.this.sendMessageDelayed(OppoSoftApManager.M_CMD_BLOCK_SAVED_CLIENTS, 0, 0, null, 100);
                }
            }

            public void exit() {
                synchronized (OppoSoftApManager.this.mHotspotClients) {
                    OppoSoftApManager.this.mHotspotClients.clear();
                }
                sendClientsChangedBroadcast();
            }

            public boolean processMessage(Message message) {
                String deniedMac;
                boolean result;
                OppoSoftApManager oppoSoftApManager;
                switch (message.what) {
                    case 0:
                        break;
                    case 1:
                    case 2:
                        int latestMaxClient = System.getInt(OppoSoftApManager.this.mContext.getContentResolver(), OppoSoftApManager.WIFI_HOTSPOT_MAX_CLIENT_NUM, 10);
                        if (OppoSoftApManager.this.mMaxNumSta != latestMaxClient) {
                            OppoSoftApManager.this.setMaxClient(latestMaxClient);
                        }
                        OppoSoftApManager.this.updateApState(10, 0);
                        OppoSoftApManager.this.stopSoftAp();
                        if (message.what == 2) {
                            OppoSoftApManager.this.updateApState(14, 0);
                        } else {
                            OppoSoftApManager.this.updateApState(11, 0);
                        }
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mIdleState);
                        OppoSoftApManager.this.mOppoHotspotClientInfo.clearConnectedDevice();
                        try {
                            SoftApStateMachine.this.mWificond.UnregisterCallback(SoftApStateMachine.this.mInterfaceEventHandler);
                        } catch (RemoteException e) {
                        }
                        SoftApStateMachine.this.mInterfaceEventHandler = null;
                        break;
                    case 3:
                        if (message.obj == SoftApStateMachine.this.mNetworkObserver) {
                            onUpChanged(message.arg1 == 1);
                            break;
                        }
                        break;
                    case 4:
                        String deviceAddress = message.obj;
                        int count = message.arg1;
                        String ipAddress = SoftApStateMachine.this.mWifiManager.getWifiHotspotManager().getClientIp(deviceAddress);
                        String deviceName = SoftApStateMachine.this.mWifiManager.getWifiHotspotManager().getClientDeviceName(deviceAddress);
                        Log.d(OppoSoftApManager.TAG, "CMD_POLL_IP_ADDRESS ,deviceAddress = " + message.obj + " ipAddress = " + ipAddress + ", count = " + count);
                        if (ipAddress != null || count >= 15) {
                            if (ipAddress != null) {
                                sendClientsIpReadyBroadcast(deviceAddress, ipAddress, deviceName);
                                break;
                            }
                        }
                        SoftApStateMachine.this.sendMessageDelayed(4, count + 1, 0, deviceAddress, 2000);
                        break;
                        break;
                    case 5:
                        OppoSoftApManager.this.mOppoHotspotClientInfo.connectionStatusChange(message);
                        break;
                    case 131372:
                        deniedMac = ((HotspotClient) message.obj).deviceAddress;
                        Log.d(OppoSoftApManager.TAG, "M_CMD_BLOCK_CLIENT " + deniedMac);
                        if (deniedMac != null) {
                            HotspotClient client;
                            boolean deniedInFile = false;
                            if (OppoSoftApManager.isHotspotListContains(OppoSoftApManager.mDeniedClients, deniedMac)) {
                                Log.d(OppoSoftApManager.TAG, "The mac is already denied, no need to block again! ");
                                deniedInFile = true;
                            }
                            String devName = null;
                            if (OppoSoftApManager.this.mContext.getResources().getBoolean(17957022)) {
                                for (HotspotClient client2 : OppoHotspotClientInfo.getInstance(OppoSoftApManager.this.mContext).getHotspotClients()) {
                                    if (deniedMac.equals(client2.deviceAddress)) {
                                        devName = client2.name;
                                    }
                                }
                            }
                            result = OppoSoftApManager.this.blockClient(deniedMac, true);
                            if (result) {
                                if (!deniedInFile) {
                                    OppoSoftApManager.this.writeDeniedDevicetoFile(deniedMac, devName);
                                }
                                synchronized (OppoSoftApManager.this.mHotspotClients) {
                                    client2 = (HotspotClient) OppoSoftApManager.this.mHotspotClients.get(deniedMac);
                                    if (client2 != null) {
                                        client2.isBlocked = true;
                                        Log.d(OppoSoftApManager.TAG, "blocked device can be found in list ");
                                    } else {
                                        Log.e(OppoSoftApManager.TAG, "blocked device can not be found in list " + deniedMac);
                                    }
                                }
                                synchronized (OppoSoftApManager.mDeniedClients) {
                                    OppoSoftApManager.mDeniedClients.add(new HotspotClient(deniedMac, true, devName));
                                }
                                sendClientsChangedBroadcast();
                            } else {
                                Log.e(OppoSoftApManager.TAG, "Failed to block " + deniedMac);
                            }
                            SoftApStateMachine.this.mReplyChannel.replyToMessage(message, message.what, result ? 1 : -1);
                            break;
                        }
                        break;
                    case 131373:
                        deniedMac = ((HotspotClient) message.obj).deviceAddress;
                        Log.d(OppoSoftApManager.TAG, "M_CMD_UNBLOCK_CLIENT " + deniedMac);
                        if (!OppoSoftApManager.isHotspotListContains(OppoSoftApManager.mDeniedClients, deniedMac)) {
                            Log.d(OppoSoftApManager.TAG, "The mac is not denied, no need to unblock! ");
                            break;
                        }
                        OppoSoftApManager.rmDevFromDeniedFile(deniedMac);
                        synchronized (OppoSoftApManager.mDeniedClients) {
                            OppoSoftApManager.rmClientWhenContains(OppoSoftApManager.mDeniedClients, deniedMac);
                        }
                        result = OppoSoftApManager.this.blockClient(deniedMac, false);
                        if (result) {
                            synchronized (OppoSoftApManager.this.mHotspotClients) {
                                OppoSoftApManager.this.mHotspotClients.remove(deniedMac);
                            }
                            sendClientsChangedBroadcast();
                        } else {
                            Log.e(OppoSoftApManager.TAG, "Failed to unblock " + deniedMac);
                        }
                        SoftApStateMachine.this.mReplyChannel.replyToMessage(message, message.what, result ? 1 : -1);
                        break;
                    case OppoSoftApManager.M_CMD_BLOCK_SAVED_CLIENTS /*131472*/:
                        if (!blockSavedClients()) {
                            if (OppoSoftApManager.this.mBlockSavedClientsRetryCount > 3) {
                                Log.e(OppoSoftApManager.TAG, "retry " + OppoSoftApManager.this.mBlockSavedClientsRetryCount + " times bsc failed! give up.");
                                OppoSoftApManager.this.mBlockSavedClientsRetryCount = 0;
                                break;
                            }
                            Log.d(OppoSoftApManager.TAG, "block saved clients failed, retry " + OppoSoftApManager.this.mBlockSavedClientsRetryCount);
                            oppoSoftApManager = OppoSoftApManager.this;
                            oppoSoftApManager.mBlockSavedClientsRetryCount = oppoSoftApManager.mBlockSavedClientsRetryCount + 1;
                            SoftApStateMachine.this.sendMessageDelayed(OppoSoftApManager.M_CMD_BLOCK_SAVED_CLIENTS, 0, 0, null, 200);
                            break;
                        }
                        OppoSoftApManager.this.mBlockSavedClientsRetryCount = 0;
                        break;
                    case OppoSoftApManager.M_CMD_BLOCK_CLIENT_ONLY /*131473*/:
                        deniedMac = ((HotspotClient) message.obj).deviceAddress;
                        if (!OppoSoftApManager.this.blockClient(deniedMac, true)) {
                            Log.e(OppoSoftApManager.TAG, "Failed to block " + deniedMac);
                            break;
                        }
                        sendClientsChangedBroadcast();
                        break;
                    case OppoSoftApManager.M_CMD_ENTER_SET_MAX_CLIENTS /*131474*/:
                        if (!OppoSoftApManager.this.setMaxClient(OppoSoftApManager.this.mMaxNumSta)) {
                            if (OppoSoftApManager.this.mMaxClientsRetryCount > 3) {
                                Log.e(OppoSoftApManager.TAG, "retry " + OppoSoftApManager.this.mMaxClientsRetryCount + " times smc failed! give up.");
                                OppoSoftApManager.this.mMaxClientsRetryCount = 0;
                                break;
                            }
                            Log.d(OppoSoftApManager.TAG, "set max clients failed, retry " + OppoSoftApManager.this.mMaxClientsRetryCount);
                            oppoSoftApManager = OppoSoftApManager.this;
                            oppoSoftApManager.mMaxClientsRetryCount = oppoSoftApManager.mMaxClientsRetryCount + 1;
                            SoftApStateMachine.this.sendMessageDelayed(OppoSoftApManager.M_CMD_ENTER_SET_MAX_CLIENTS, 0, 0, null, 200);
                            break;
                        }
                        OppoSoftApManager.this.mMaxClientsRetryCount = 0;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        SoftApStateMachine(Looper looper, WifiInjector wifiInjector) {
            super(OppoSoftApManager.TAG, looper);
            this.mWifiInjector = wifiInjector;
            this.mInterfaceEventHandler = new InterfaceEventHandler(this);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
            this.mWifiManager = (WifiManager) OppoSoftApManager.this.mContext.getSystemService("wifi");
            try {
                this.mMonitorInterfaceName = OppoSoftApManager.this.mApInterface.getInterfaceName();
            } catch (RemoteException e) {
                Log.e(OppoSoftApManager.TAG, "Exception in SoftApStateMachine constructor: " + e);
            }
            Log.d(OppoSoftApManager.TAG, "mMonitorInterfaceName = " + this.mMonitorInterfaceName);
        }
    }

    public OppoSoftApManager(Context context, Looper looper, WifiNative wifiNative, String countryCode, Listener listener, IApInterface apInterface, INetworkManagementService nms, WifiApConfigStore wifiApConfigStore, WifiConfiguration config, WifiMetrics wifiMetrics, WifiInjector wifiInjector, Context injectContext) {
        super(looper, wifiNative, countryCode, listener, apInterface, nms, wifiApConfigStore, config, wifiMetrics, wifiInjector, injectContext);
        this.mContext = context;
        this.mApInterface = apInterface;
        this.mOppoHotspotClientInfo = OppoHotspotClientInfo.getInstance(context);
        mStateMachine = new SoftApStateMachine(looper, wifiInjector);
        this.mWifiNative = wifiNative;
        if (countryCode == null) {
            this.mCountryCode = "CN";
        } else {
            this.mCountryCode = countryCode;
        }
        this.mListener = listener;
        this.mNwService = nms;
        this.mWifiApConfigStore = wifiApConfigStore;
        if (config == null) {
            this.mApConfig = this.mWifiApConfigStore.getApConfiguration();
        } else {
            this.mApConfig = config;
        }
        this.mWifiMetrics = wifiMetrics;
    }

    public void start() {
        mStateMachine.sendMessage(0, this.mApConfig);
    }

    public void stop() {
        if (mStateMachine != null) {
            mStateMachine.sendMessage(1);
            mStateMachine = null;
        }
    }

    public void startApWpsCommand(Message message) {
        mStateMachine.sendMessage(message);
    }

    public List<HotspotClient> getHotspotClientsList() {
        List<HotspotClient> clients = new ArrayList();
        synchronized (this.mHotspotClients) {
            for (HotspotClient client : this.mHotspotClients.values()) {
                clients.add(new HotspotClient(client));
            }
        }
        return clients;
    }

    public static List<HotspotClient> getBlockedHotspotClientsList() {
        List<HotspotClient> clients = new ArrayList();
        synchronized (mDeniedClients) {
            for (HotspotClient client : mDeniedClients) {
                clients.add(new HotspotClient(client));
            }
        }
        return clients;
    }

    public void syncBlockClient(Message message) {
        mStateMachine.sendMessage(message);
    }

    public static void syncUnblockClient(Message message) {
        Log.d(TAG, "syncUnblockClient");
        if (mStateMachine != null) {
            mStateMachine.sendMessage(message);
            return;
        }
        String deniedMac = ((HotspotClient) message.obj).deviceAddress;
        Log.d(TAG, "In StopedState M_CMD_UNBLOCK_CLIENT " + deniedMac);
        if (isHotspotListContains(mDeniedClients, deniedMac)) {
            rmDevFromDeniedFile(deniedMac);
            synchronized (mDeniedClients) {
                rmClientWhenContains(mDeniedClients, deniedMac);
            }
            return;
        }
        Log.d(TAG, "The mac is not denied, no need to unblock! ");
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean rmClientWhenContains(List<HotspotClient> list, String devMac) {
        if (list == null || devMac == null || !devMac.matches(MAC_PATTERN_STR)) {
            return false;
        }
        int index = 0;
        for (HotspotClient client : list) {
            if (devMac.equalsIgnoreCase(client.deviceAddress)) {
                list.remove(index);
                return true;
            }
            index++;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isHotspotListContains(List<HotspotClient> list, String devMac) {
        if (list == null || devMac == null || !devMac.matches(MAC_PATTERN_STR)) {
            return false;
        }
        for (HotspotClient client : list) {
            if (devMac.equalsIgnoreCase(client.deviceAddress)) {
                return true;
            }
        }
        return false;
    }

    private static void initAllowedListIfNecessary() {
        if (sAllowedDevices == null) {
            sAllowedDevices = new LinkedHashMap();
            try {
                BufferedReader br = new BufferedReader(new FileReader(ALLOWED_LIST_FILE));
                String line = br.readLine();
                while (line != null) {
                    String[] result = line.split("\t");
                    if (result != null) {
                        String address = result[0];
                        sAllowedDevices.put(address, new HotspotClient(address, result[1].equals("1"), result.length == 3 ? result[2] : ""));
                        line = br.readLine();
                    }
                }
                br.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString(), new Throwable("initAllowedListIfNecessary"));
            }
        }
    }

    private static void writeAllowedList() {
        String content = "";
        for (HotspotClient device : sAllowedDevices.values()) {
            String blocked = device.isBlocked ? "1" : "0";
            if (device.name != null) {
                content = content + device.deviceAddress + "\t" + blocked + "\t" + device.name + "\n";
            } else {
                content = content + device.deviceAddress + "\t" + blocked + "\n";
            }
        }
        Log.d(TAG, "writeAllowedLis content = " + content);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ALLOWED_LIST_FILE));
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString(), new Throwable("writeAllowedList"));
        }
    }

    public static boolean isAllDevicesAllowed(Context context) {
        return false;
    }

    public static void writeAllDevicesAllowed(Context context, boolean enabled) {
    }

    public static void addDeviceToAllowedList(HotspotClient device) {
        Log.d(TAG, "addDeviceToAllowedList device = " + device + ", is name null?" + (device.name == null));
        initAllowedListIfNecessary();
        if (!sAllowedDevices.containsKey(device.deviceAddress)) {
            sAllowedDevices.put(device.deviceAddress, device);
        }
        writeAllowedList();
    }

    public static void removeDeviceFromAllowedList(String address) {
        Log.d(TAG, "removeDeviceFromAllowedList address = " + address);
        initAllowedListIfNecessary();
        sAllowedDevices.remove(address);
        writeAllowedList();
    }

    private void initAcceptMacFile() {
        initAllowedListIfNecessary();
        String content = "";
        for (HotspotClient device : sAllowedDevices.values()) {
            content = content + (device.isBlocked ? "-" : "") + device.deviceAddress + "\n";
        }
        try {
            Runtime.getRuntime().exec("chmod 604 " + ACCEPT_MAC_UPDATE_FILE);
            BufferedWriter bw = new BufferedWriter(new FileWriter(ACCEPT_MAC_UPDATE_FILE));
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString(), new Throwable("writeAllowedList"));
        }
    }

    private void updateAcceptMacFile(String content) {
        Log.d(TAG, "updateAllowedList content = " + content);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ACCEPT_MAC_UPDATE_FILE));
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString(), new Throwable("updateAcceptMacFile"));
        }
    }

    public static List<HotspotClient> getAllowedDevices() {
        Log.d(TAG, "getAllowedDevices");
        initAllowedListIfNecessary();
        List<HotspotClient> devices = new ArrayList();
        for (HotspotClient device : sAllowedDevices.values()) {
            devices.add(new HotspotClient(device));
            Log.d(TAG, "device = " + device);
        }
        return devices;
    }

    public void syncAllowDevice(String address) {
        updateAcceptMacFile(address);
    }

    public void syncDisallowDevice(String address) {
        updateAcceptMacFile("-" + address);
    }

    public void syncSetAllDevicesAllowed(boolean enabled, boolean allowAllConnectedDevices) {
        if (!enabled) {
            initAllowedListIfNecessary();
            if (allowAllConnectedDevices && this.mHotspotClients.size() > 0) {
                String content = "";
                for (HotspotClient client : this.mHotspotClients.values()) {
                    if (!(client.isBlocked || (sAllowedDevices.containsKey(client.deviceAddress) ^ 1) == 0)) {
                        sAllowedDevices.put(client.deviceAddress, new HotspotClient(client));
                        content = content + client.deviceAddress + "\n";
                    }
                }
                if (!content.equals("")) {
                    writeAllowedList();
                    updateAcceptMacFile(content);
                }
            }
        }
    }

    private void updateApState(int state, int reason) {
        if (this.mListener != null) {
            this.mListener.onStateChanged(state, reason);
        }
    }

    private int startSoftAp(WifiConfiguration config) {
        if (config == null || config.SSID == null) {
            Log.e(TAG, "Unable to start soft AP without valid configuration");
            return 2;
        }
        WifiConfiguration localConfig = new WifiConfiguration(config);
        int result = ApConfigUtil.updateApChannelConfig(this.mWifiNative, this.mCountryCode, this.mWifiApConfigStore.getAllowed2GChannel(), localConfig);
        if (result != 0) {
            Log.e(TAG, "Failed to update AP band and channel");
            return result;
        } else if (this.mCountryCode == null || this.mWifiNative.setCountryCodeHal(this.mCountryCode.toUpperCase(Locale.ROOT)) || config.apBand != 1) {
            int encryptionType = getIApInterfaceEncryptionType(localConfig);
            if (localConfig.hiddenSSID) {
                Log.d(TAG, "SoftAP is a hidden network");
            }
            try {
                byte[] bytes;
                String fixChannelString = SystemProperties.get("wifi.tethering.channel");
                if (fixChannelString != null && fixChannelString.length() > 0) {
                    int fixChannel = Integer.parseInt(fixChannelString);
                    if (fixChannel >= 0) {
                        localConfig.apChannel = fixChannel;
                    }
                }
                IApInterface iApInterface = this.mApInterface;
                byte[] bytes2 = localConfig.SSID.getBytes(StandardCharsets.UTF_8);
                boolean z = localConfig.hiddenSSID;
                int i = localConfig.apChannel;
                if (localConfig.preSharedKey != null) {
                    bytes = localConfig.preSharedKey.getBytes(StandardCharsets.UTF_8);
                } else {
                    bytes = new byte[0];
                }
                if (iApInterface.writeHostapdConfig(bytes2, z, i, encryptionType, bytes)) {
                    this.mMaxNumSta = System.getInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_MAX_CLIENT_NUM, 10);
                    if (!this.mApInterface.startHostapd(false)) {
                        Log.e(TAG, "Failed to start hostapd.");
                        return 2;
                    }
                    Log.d(TAG, "Soft AP is started");
                    return 0;
                }
                Log.e(TAG, "Failed to write hostapd configuration");
                return 2;
            } catch (RemoteException e) {
                Log.e(TAG, "Exception in starting soft AP: " + e);
            }
        } else {
            Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
            return 2;
        }
    }

    private static int getIApInterfaceEncryptionType(WifiConfiguration localConfig) {
        switch (localConfig.getAuthType()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 4:
                return 2;
            default:
                return 0;
        }
    }

    private void stopSoftAp() {
        try {
            this.mApInterface.stopHostapd(false);
            Log.d(TAG, "Soft AP is stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in stopping soft AP: " + e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x009e A:{SYNTHETIC, Splitter: B:25:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00e7 A:{SYNTHETIC, Splitter: B:36:0x00e7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void loadDeniedDevice() {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        String tmpStr = "";
        Log.i(TAG, "loadDeniedDevice..");
        try {
            BufferedReader br = new BufferedReader(new FileReader(DENIED_MAC_FILE));
            while (true) {
                try {
                    String line = br.readLine();
                    if (line != null) {
                        if (line.startsWith("#")) {
                            if (line.startsWith(nameTag)) {
                                tmpStr = line.substring(6);
                                Log.i(TAG, "Device name: " + tmpStr);
                            } else {
                                Log.i(TAG, "Skip comment: " + line);
                            }
                        } else if (!line.matches(MAC_PATTERN_STR)) {
                            Log.i(TAG, "Invalid dev mac: " + line);
                        } else if (isHotspotListContains(mDeniedClients, line)) {
                            Log.i(TAG, "Dup dev mac: " + line);
                        } else {
                            HotspotClient client = new HotspotClient(line, true, tmpStr);
                            synchronized (mDeniedClients) {
                                mDeniedClients.add(client);
                            }
                            if (mStateMachine != null) {
                                mStateMachine.sendMessage(M_CMD_BLOCK_CLIENT_ONLY, client);
                            }
                        }
                        if (line == null) {
                            break;
                        }
                    } else {
                        Log.i(TAG, "Reach end of hostapd.deny!");
                        break;
                    }
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = br;
                    try {
                        Log.e(TAG, "Error reading hostapd.deny " + e);
                        if (bufferedReader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = br;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e3);
                        }
                    }
                    throw th;
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e32) {
                    Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e32);
                }
            }
            bufferedReader = br;
        } catch (IOException e4) {
            e32 = e4;
            Log.e(TAG, "Error reading hostapd.deny " + e32);
            if (bufferedReader == null) {
                try {
                    bufferedReader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e322);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d2 A:{SYNTHETIC, Splitter: B:36:0x00d2} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d7 A:{SYNTHETIC, Splitter: B:39:0x00d7} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d2 A:{SYNTHETIC, Splitter: B:36:0x00d2} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d7 A:{SYNTHETIC, Splitter: B:39:0x00d7} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0048 A:{SYNTHETIC, Splitter: B:13:0x0048} */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004d A:{SYNTHETIC, Splitter: B:16:0x004d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeDeniedDevicetoFile(String deniedMac, String devName) {
        IOException e;
        Throwable th;
        BufferedReader br = null;
        BufferedWriter bw = null;
        StringBuffer configContent = new StringBuffer();
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(DENIED_MAC_FILE));
            while (true) {
                try {
                    String line = br2.readLine();
                    if (line == null) {
                        break;
                    }
                    configContent.append(line + "\n");
                } catch (IOException e2) {
                    e = e2;
                    br = br2;
                } catch (Throwable th2) {
                    th = th2;
                    br = br2;
                    if (bw != null) {
                    }
                    if (br != null) {
                    }
                    throw th;
                }
            }
            if (devName == null) {
                configContent.append("#name-\n");
            } else {
                configContent.append(nameTag + devName + "\n");
            }
            configContent.append(deniedMac + "\n");
            Log.d(TAG, "DENIED_MAC_FILE: \n" + configContent.toString());
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(DENIED_MAC_FILE));
            try {
                bw2.write(configContent.toString());
                if (bw2 != null) {
                    try {
                        bw2.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e3);
                    }
                }
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (IOException e32) {
                        Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e32);
                    }
                }
            } catch (IOException e4) {
                e32 = e4;
                bw = bw2;
                br = br2;
                try {
                    Log.e(TAG, e32.toString(), new Throwable("Fail to write DENIED_MAC_FILE"));
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e322) {
                            Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e322);
                        }
                    }
                    if (br == null) {
                        try {
                            br.close();
                        } catch (IOException e3222) {
                            Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e3222);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (bw != null) {
                    }
                    if (br != null) {
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                bw = bw2;
                br = br2;
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e32222) {
                        Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e32222);
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e322222) {
                        Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e322222);
                    }
                }
                throw th;
            }
        } catch (IOException e5) {
            e322222 = e5;
            Log.e(TAG, e322222.toString(), new Throwable("Fail to write DENIED_MAC_FILE"));
            if (bw != null) {
            }
            if (br == null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00b4 A:{SYNTHETIC, Splitter: B:38:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00b9 A:{SYNTHETIC, Splitter: B:41:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0102 A:{SYNTHETIC, Splitter: B:52:0x0102} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0107 A:{SYNTHETIC, Splitter: B:55:0x0107} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0102 A:{SYNTHETIC, Splitter: B:52:0x0102} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0107 A:{SYNTHETIC, Splitter: B:55:0x0107} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00b4 A:{SYNTHETIC, Splitter: B:38:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00b9 A:{SYNTHETIC, Splitter: B:41:0x00b9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void rmDevFromDeniedFile(String devAddr) {
        IOException e;
        Throwable th;
        BufferedReader br = null;
        BufferedWriter bw = null;
        StringBuffer fileContent = new StringBuffer();
        String tmpName = "";
        if (devAddr == null || (devAddr.matches(MAC_PATTERN_STR) ^ 1) != 0) {
            Log.i(TAG, "The devAddr param is null or not MAC patern!");
            return;
        }
        Log.i(TAG, "start rmDevFromDeniedFile..");
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(DENIED_MAC_FILE));
            while (true) {
                try {
                    String oneLine = br2.readLine();
                    if (oneLine != null) {
                        if (oneLine.startsWith("#")) {
                            if (oneLine.startsWith(nameTag)) {
                                tmpName = oneLine;
                            } else {
                                fileContent.append(oneLine + "\n");
                            }
                        } else if (!oneLine.matches(MAC_PATTERN_STR)) {
                            Log.i(TAG, "Invalid dev mac: " + oneLine);
                        } else if (oneLine.equalsIgnoreCase(devAddr)) {
                            Log.i(TAG, "Remove Device: " + tmpName);
                            Log.i(TAG, "Remove MAC: " + oneLine);
                        } else {
                            fileContent.append(tmpName + "\n");
                            fileContent.append(oneLine + "\n");
                        }
                        if (oneLine == null) {
                            break;
                        }
                    } else {
                        Log.i(TAG, "Reach end of hostapd.deny!");
                        break;
                    }
                } catch (IOException e2) {
                    e = e2;
                    br = br2;
                    try {
                        Log.e(TAG, "Error reading hostapd.deny " + e);
                        if (bw != null) {
                            try {
                                bw.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e3);
                            }
                        }
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e32) {
                                Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e32);
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bw != null) {
                        }
                        if (br != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    br = br2;
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e322) {
                            Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e322);
                        }
                    }
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e3222) {
                            Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e3222);
                        }
                    }
                    throw th;
                }
            }
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(DENIED_MAC_FILE));
            try {
                bw2.write(fileContent.toString());
                if (bw2 != null) {
                    try {
                        bw2.close();
                    } catch (IOException e32222) {
                        Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e32222);
                    }
                }
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (IOException e322222) {
                        Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e322222);
                    }
                }
                br = br2;
            } catch (IOException e4) {
                e322222 = e4;
                bw = bw2;
                br = br2;
                Log.e(TAG, "Error reading hostapd.deny " + e322222);
                if (bw != null) {
                }
                if (br != null) {
                }
            } catch (Throwable th4) {
                th = th4;
                bw = bw2;
                br = br2;
                if (bw != null) {
                }
                if (br != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e322222 = e5;
            Log.e(TAG, "Error reading hostapd.deny " + e322222);
            if (bw != null) {
            }
            if (br != null) {
            }
        }
    }

    private boolean blockClient(String device, boolean isBlocked) {
        try {
            return this.mApInterface.blockClient(device, isBlocked);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception: " + e);
            return false;
        }
    }

    private boolean setMaxClient(int max) {
        try {
            return this.mApInterface.setMaxClient(max);
        } catch (RemoteException e) {
            Log.e(TAG, "remote exception: " + e);
            return false;
        }
    }
}
