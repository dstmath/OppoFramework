package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.util.ApConfigUtil;
import com.mediatek.server.wifi.MtkHostapdHal;
import com.mediatek.server.wifi.MtkHostapdHalCallback;
import com.mediatek.server.wifi.MtkSoftApUtils;
import com.mediatek.server.wifi.MtkWifiApMonitor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import oppo.net.wifi.HotspotClient;

public class OppoSoftApManager extends SoftApManager {
    private static final String ACCEPT_MAC_UPDATE_FILE = (Environment.getDataDirectory() + "/misc/wifi/accept_mac_update.conf");
    private static final String ACTION_LOAD_FROM_STORE = "android.intent.action.OPPO_ACTION_LOAD_FROM_STORE";
    private static final String ALLOWED_LIST_FILE = (Environment.getDataDirectory() + "/misc/wifi/allowed_list.conf");
    private static final boolean DBG = true;
    private static final String DENIED_MAC_FILE = "/data/misc/wifi/hostapd.deny";
    private static final String HOSTAPD_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/hostapd.conf");
    private static final String MAC_PATTERN_STR = "([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}";
    private static final int MAX_CLIENT_NUM = 10;
    private static final int MAX_RECOVERY_COUNT = 2;
    private static final int MIN_SOFT_AP_TIMEOUT_DELAY_MS = 600000;
    @VisibleForTesting
    public static final String SOFT_AP_SEND_MESSAGE_TIMEOUT_TAG = "OppoSoftApManager Soft AP Send Message Timeout";
    private static final String TAG = "OppoSoftApManager";
    private static final String WIFI_HOTSPOT_FIX_BAND_VALUE = "oppo.wifi.hotspot.fix.band";
    private static final String WIFI_HOTSPOT_FIX_CHANNEL_FLAG_VALUE = "oppo.wifi.hotspot.fix.channel.flag";
    private static final String WIFI_HOTSPOT_FIX_CH_VALUE = "oppo.wifi.hotspot.fix.ch";
    private static final String WIFI_HOTSPOT_MAX_CLIENT_NUM = "oppo_wifi_ap_max_devices_connect";
    private static final String WIFI_PACKEG_NAME = "com.android.server.wifi";
    private static List<HotspotClient> mDeniedClients = new ArrayList();
    private static boolean mLoadFromStore = false;
    /* access modifiers changed from: private */
    public static int mRecoveryRetryCount = 0;
    private static final String nameTag = "#name-";
    private boolean expectedStop;
    private int inVaildValue;
    /* access modifiers changed from: private */
    public WifiConfiguration mApConfig;
    /* access modifiers changed from: private */
    public String mApInterfaceName;
    /* access modifiers changed from: private */
    public int mBlockSavedClientsRetryCount;
    /* access modifiers changed from: private */
    public final WifiManager.SoftApCallback mCallback;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final String mCountryCode;
    private int mFixBandValue;
    private int mFixChValue;
    private boolean mFixChannelFlag;
    /* access modifiers changed from: private */
    public final FrameworkFacade mFrameworkFacade;
    /* access modifiers changed from: private */
    public boolean mIfaceIsDestroyed;
    /* access modifiers changed from: private */
    public boolean mIfaceIsUp;
    private BroadcastReceiver mLoadFromStoreReceiver;
    /* access modifiers changed from: private */
    public int mMaxClientsRetryCount;
    /* access modifiers changed from: private */
    public int mMaxNumSta;
    /* access modifiers changed from: private */
    public final int mMode;
    /* access modifiers changed from: private */
    public int mNumAssociatedStations;
    /* access modifiers changed from: private */
    public OppoHotspotClientInfo mOppoHotspotClientInfo;
    /* access modifiers changed from: private */
    public int mQCNumAssociatedStations;
    /* access modifiers changed from: private */
    public int mReportedBandwidth;
    /* access modifiers changed from: private */
    public int mReportedFrequency;
    /* access modifiers changed from: private */
    public final SarManager mSarManager;
    private final WifiNative.SoftApListener mSoftApListener;
    private long mStartTimestamp;
    /* access modifiers changed from: private */
    public final SoftApStateMachine mStateMachine;
    /* access modifiers changed from: private */
    public boolean mTimeoutEnabled;
    private final WifiApConfigStore mWifiApConfigStore;
    /* access modifiers changed from: private */
    public WifiInjector mWifiInjector;
    /* access modifiers changed from: private */
    public final WifiMetrics mWifiMetrics;
    /* access modifiers changed from: private */
    public final OppoWifiNative mWifiNative;
    private String[] mdualApInterfaces;

    static /* synthetic */ int access$1008() {
        int i = mRecoveryRetryCount;
        mRecoveryRetryCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$2208(OppoSoftApManager x0) {
        int i = x0.mQCNumAssociatedStations;
        x0.mQCNumAssociatedStations = i + 1;
        return i;
    }

    static /* synthetic */ int access$2210(OppoSoftApManager x0) {
        int i = x0.mQCNumAssociatedStations;
        x0.mQCNumAssociatedStations = i - 1;
        return i;
    }

    static /* synthetic */ int access$2608(OppoSoftApManager x0) {
        int i = x0.mMaxClientsRetryCount;
        x0.mMaxClientsRetryCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$2708(OppoSoftApManager x0) {
        int i = x0.mBlockSavedClientsRetryCount;
        x0.mBlockSavedClientsRetryCount = i + 1;
        return i;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public OppoSoftApManager(Context context, Looper looper, FrameworkFacade framework, OppoWifiNative wifiNative, String countryCode, WifiManager.SoftApCallback callback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration apConfig, WifiInjector wifiInjector, WifiMetrics wifiMetrics, SarManager sarManager) {
        super(context, looper, framework, wifiNative, countryCode == null ? "CN" : countryCode, callback, wifiApConfigStore, apConfig, wifiMetrics, sarManager);
        this.mReportedFrequency = -1;
        this.mReportedBandwidth = -1;
        this.mNumAssociatedStations = 0;
        this.mQCNumAssociatedStations = 0;
        this.mTimeoutEnabled = true;
        this.expectedStop = false;
        this.mMaxNumSta = 10;
        this.mStartTimestamp = -1;
        this.mFixChannelFlag = false;
        this.mFixChValue = 6;
        this.mFixBandValue = 1;
        this.inVaildValue = 999;
        this.mSoftApListener = new WifiNative.SoftApListener() {
            /* class com.android.server.wifi.OppoSoftApManager.AnonymousClass1 */

            @Override // com.android.server.wifi.WifiNative.SoftApListener
            public void onFailure() {
                OppoSoftApManager.this.mStateMachine.sendMessage(2);
            }

            @Override // com.android.server.wifi.WifiNative.SoftApListener
            public void onNumAssociatedStationsChanged(int numStations) {
                OppoSoftApManager.this.mStateMachine.sendMessage(4, numStations);
            }

            @Override // com.android.server.wifi.WifiNative.SoftApListener
            public void onSoftApChannelSwitched(int frequency, int bandwidth) {
                OppoSoftApManager.this.mStateMachine.sendMessage(9, frequency, bandwidth);
            }
        };
        this.mContext = context;
        this.mFrameworkFacade = framework;
        this.mWifiNative = wifiNative;
        this.mWifiInjector = wifiInjector;
        this.mOppoHotspotClientInfo = OppoHotspotClientInfo.getInstance(context);
        if (countryCode == null) {
            this.mCountryCode = "CN";
        } else {
            this.mCountryCode = countryCode;
        }
        this.mCallback = callback;
        this.mWifiApConfigStore = wifiApConfigStore;
        this.mMode = apConfig.getTargetMode();
        WifiConfiguration config = apConfig.getWifiConfiguration();
        if (config == null) {
            this.mApConfig = this.mWifiApConfigStore.getApConfiguration();
        } else {
            this.mApConfig = config;
        }
        this.mWifiMetrics = wifiMetrics;
        this.mSarManager = sarManager;
        this.mStateMachine = new SoftApStateMachine(looper);
    }

    @Override // com.android.server.wifi.ActiveModeManager, com.android.server.wifi.SoftApManager
    public void start() {
        this.mStateMachine.sendMessage(0, this.mApConfig);
        if (this.mLoadFromStoreReceiver == null) {
            this.mLoadFromStoreReceiver = new BroadcastReceiver() {
                /* class com.android.server.wifi.OppoSoftApManager.AnonymousClass2 */

                public void onReceive(Context context, Intent intent) {
                    Log.d(OppoSoftApManager.TAG, "onReceive: LFS");
                    OppoSoftApManager.loadDeniedDevice();
                    boolean unused = OppoSoftApManager.this.blockSavedClients();
                }
            };
            this.mContext.registerReceiver(this.mLoadFromStoreReceiver, new IntentFilter(ACTION_LOAD_FROM_STORE));
        }
    }

    @Override // com.android.server.wifi.ActiveModeManager, com.android.server.wifi.SoftApManager
    public void stop() {
        Log.d(TAG, " currentstate: " + getCurrentStateName());
        this.expectedStop = true;
        if (this.mApInterfaceName != null) {
            if (this.mIfaceIsUp) {
                updateApState(10, 13, 0);
            } else {
                updateApState(10, 12, 0);
            }
        }
        BroadcastReceiver broadcastReceiver = this.mLoadFromStoreReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mLoadFromStoreReceiver = null;
        }
        this.mStateMachine.quitNow();
    }

    @Override // com.android.server.wifi.ActiveModeManager, com.android.server.wifi.SoftApManager
    public int getScanMode() {
        return 0;
    }

    @Override // com.android.server.wifi.SoftApManager
    public int getIpMode() {
        return this.mMode;
    }

    @Override // com.android.server.wifi.ActiveModeManager, com.android.server.wifi.SoftApManager
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("--Dump of SoftApManager--");
        pw.println("current StateMachine mode: " + getCurrentStateName());
        pw.println("mApInterfaceName: " + this.mApInterfaceName);
        pw.println("mIfaceIsUp: " + this.mIfaceIsUp);
        pw.println("mMode: " + this.mMode);
        pw.println("mCountryCode: " + this.mCountryCode);
        if (this.mApConfig != null) {
            pw.println("mApConfig.SSID: " + this.mApConfig.SSID);
            pw.println("mApConfig.apBand: " + this.mApConfig.apBand);
            pw.println("mApConfig.hiddenSSID: " + this.mApConfig.hiddenSSID);
        } else {
            pw.println("mApConfig: null");
        }
        pw.println("mNumAssociatedStations: " + this.mNumAssociatedStations);
        pw.println("mTimeoutEnabled: " + this.mTimeoutEnabled);
        pw.println("mReportedFrequency: " + this.mReportedFrequency);
        pw.println("mReportedBandwidth: " + this.mReportedBandwidth);
        pw.println("mStartTimestamp: " + this.mStartTimestamp);
    }

    @Override // com.android.server.wifi.SoftApManager
    private String getCurrentStateName() {
        IState currentState = this.mStateMachine.getCurrentState();
        if (currentState != null) {
            return currentState.getName();
        }
        return "StateMachine not active";
    }

    private boolean updateFixChInfo() {
        int flag = Settings.System.getInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_FIX_CHANNEL_FLAG_VALUE, this.inVaildValue);
        if (flag == 1) {
            this.mFixChannelFlag = true;
            this.mFixBandValue = Settings.System.getInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_FIX_BAND_VALUE, this.inVaildValue);
            if (this.mFixBandValue == this.inVaildValue) {
                this.mFixBandValue = 0;
                Settings.System.putInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_FIX_BAND_VALUE, this.mFixBandValue);
            }
            this.mFixChValue = Settings.System.getInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_FIX_CH_VALUE, this.inVaildValue);
            if (this.mFixChValue == this.inVaildValue) {
                if (this.mFixBandValue == 0) {
                    this.mFixChValue = 6;
                } else {
                    this.mFixChValue = 36;
                }
                Settings.System.putInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_FIX_CH_VALUE, this.mFixChValue);
            }
            Log.d(TAG, "updateFixChInfo mFixChValue: " + this.mFixChValue + " mFixBandValue: " + this.mFixBandValue);
            return true;
        }
        this.mFixChannelFlag = false;
        if (flag == this.inVaildValue) {
            this.mFixBandValue = 0;
            this.mFixChValue = 6;
            Settings.System.putInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_FIX_CHANNEL_FLAG_VALUE, 0);
            Settings.System.putInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_FIX_BAND_VALUE, this.mFixBandValue);
            Settings.System.putInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_FIX_CH_VALUE, this.mFixChValue);
        }
        Log.d(TAG, "updateFixChInfo mFixChannelFlag = false!");
        return false;
    }

    /* access modifiers changed from: private */
    @Override // com.android.server.wifi.SoftApManager
    public void updateApState(int newState, int currentState, int reason) {
        WifiManager manager;
        this.mWifiInjector.getOppoSoftapStatistics().informSoftApState(newState);
        OppoSapScanCoexistManager.getInstance(this.mContext).updateSapStatus(newState);
        OppoWifiSharingManager sharingManager = this.mWifiInjector.getOppoWifiSharingManager();
        if ((!sharingManager.isWifiSharingTethering() || sharingManager.isWifiApEnabledState()) && (!(newState == 10 || newState == 11) || !sharingManager.isWifiSharingEnabledState())) {
            if ((newState == 11 || newState == 14 || newState == 10) && (manager = (WifiManager) this.mContext.getSystemService("wifi")) != null) {
                manager.updateInterfaceIpState(null, -1);
            }
            this.mCallback.onStateChanged(newState, reason);
            Log.e(TAG, "SoftApStateMachine notificaiton for Ap " + newState);
            Intent intent = new Intent("android.net.wifi.WIFI_AP_STATE_CHANGED");
            intent.addFlags(67108864);
            intent.putExtra("wifi_state", newState);
            intent.putExtra("previous_wifi_state", currentState);
            if (newState == 14) {
                intent.putExtra("wifi_ap_error_code", reason);
            }
            if (this.mApInterfaceName == null) {
                Log.e(TAG, "Updating wifiApState with a null iface name");
            }
            intent.putExtra("wifi_ap_interface_name", this.mApInterfaceName);
            intent.putExtra("wifi_ap_mode", this.mMode);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            sharingManager.updateWifiSharingPersistState(111);
            return;
        }
        int wifiSharingState = sharingManager.transformApState2SharingState(newState);
        this.mCallback.onStateChanged(wifiSharingState, reason);
        Log.e(TAG, "SoftApStateMachine notificaiton for Sharing " + wifiSharingState);
        Intent intent2 = new Intent("oppo.intent.action.wifi.WIFI_SHARING_STATE_CHANGED");
        intent2.addFlags(67108864);
        intent2.putExtra("wifi_state", sharingManager.transformApState2SharingState(newState));
        intent2.putExtra("previous_wifi_state", sharingManager.transformApState2SharingState(currentState));
        if (newState == 14) {
            intent2.putExtra("wifi_ap_error_code", reason);
        }
        if (this.mApInterfaceName == null) {
            Log.e(TAG, "Updating wifiApState with a null iface name for wifi sharing");
        }
        intent2.putExtra("wifi_ap_interface_name", this.mApInterfaceName);
        intent2.putExtra("wifi_ap_mode", this.mMode);
        this.mContext.sendStickyBroadcastAsUser(intent2, UserHandle.ALL);
        sharingManager.updateWifiSharingPersistState(sharingManager.transformApState2SharingState(newState));
    }

    /* access modifiers changed from: private */
    @Override // com.android.server.wifi.SoftApManager
    public int startSoftAp(WifiConfiguration config) {
        if (config == null || config.SSID == null) {
            Log.e(TAG, "Unable to start soft AP without valid configuration");
            return 2;
        }
        if (!this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingTethering()) {
            this.mMaxNumSta = Settings.System.getInt(this.mContext.getContentResolver(), WIFI_HOTSPOT_MAX_CLIENT_NUM, 10);
        } else {
            this.mMaxNumSta = 10;
        }
        if (TextUtils.isEmpty(this.mCountryCode)) {
            if (config.apBand == 1) {
                Log.e(TAG, "Invalid country code, required for setting up soft ap in 5GHz");
                return 2;
            }
        } else if (!this.mWifiNative.setCountryCodeHal(this.mApInterfaceName, this.mCountryCode.toUpperCase(Locale.ROOT)) && config.apBand == 1) {
            Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
            return 2;
        }
        WifiConfiguration localConfig = new WifiConfiguration(config);
        MtkSoftApUtils.stopP2p(this.mContext, this.mStateMachine.getHandler().getLooper(), this.mWifiNative);
        int result = ApConfigUtil.updateApChannelConfig(this.mWifiNative, this.mCountryCode, this.mWifiApConfigStore.getAllowed2GChannel(), localConfig);
        if (result != 0) {
            Log.e(TAG, "Failed to update AP band and channel");
            return result;
        }
        if (localConfig.hiddenSSID) {
            Log.d(TAG, "SoftAP is a hidden network");
        }
        if (this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingTethering()) {
            WifiInfo wifiInfo = this.mWifiInjector.getClientModeImpl().syncRequestConnectionInfo();
            if (wifiInfo.is24GHz()) {
                localConfig.apChannel = ApConfigUtil.convertFrequencyToChannel(wifiInfo.getFrequency());
            }
            Log.d(TAG, "adapting channel when sharing " + localConfig.apChannel);
        }
        updateFixChInfo();
        if (this.mFixChannelFlag) {
            localConfig.apBand = this.mFixBandValue;
            localConfig.apChannel = this.mFixChValue;
        }
        ApConfigUtil.isConfigurationSupportBw40(this.mWifiNative, localConfig);
        if (!this.mWifiNative.startSoftAp(this.mApInterfaceName, localConfig, this.mSoftApListener)) {
            Log.e(TAG, "Soft AP start failed");
            return 2;
        } else if (!MtkHostapdHal.registerCallback(new MtkHostapdHalCallback())) {
            Log.d(TAG, "Failed to register MtkHostapdHalCallback");
            return 2;
        } else {
            this.mStartTimestamp = SystemClock.elapsedRealtime();
            Log.d(TAG, "Soft AP is started");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    @Override // com.android.server.wifi.SoftApManager
    public void stopSoftAp() {
        this.mWifiNative.teardownInterface(this.mApInterfaceName);
        Log.d(TAG, "Soft AP is stopped");
    }

    /* access modifiers changed from: private */
    public void sendClientsChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED");
        intent.addFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

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

    private boolean blockClient(String device, boolean isBlocked) {
        return this.mWifiNative.blockClient(this.mApInterfaceName, device, isBlocked);
    }

    public boolean syncBlockClient(HotspotClient client) {
        String deniedMac = client.deviceAddress;
        Log.d(TAG, "syncBlockClient " + deniedMac);
        if (deniedMac == null) {
            Log.e(TAG, "syncBlockClient while deniedMac is null!!");
            return false;
        }
        boolean deniedInFile = false;
        if (isHotspotListContains(mDeniedClients, deniedMac)) {
            Log.d(TAG, "The mac is already denied, no need to block again! ");
            deniedInFile = true;
        }
        String devName = null;
        for (HotspotClient cl : OppoHotspotClientInfo.getInstance(this.mContext).getHotspotClients()) {
            if (deniedMac.equals(cl.deviceAddress)) {
                devName = cl.name;
            }
        }
        boolean result = blockClient(deniedMac, true);
        if (result) {
            if (!deniedInFile) {
                writeDeniedDevicetoFile(deniedMac, devName);
            }
            synchronized (mDeniedClients) {
                mDeniedClients.add(new HotspotClient(deniedMac, true, devName));
            }
            sendClientsChangedBroadcast();
        } else {
            Log.e(TAG, "Failed to block " + deniedMac);
        }
        return result;
    }

    private void writeDeniedDevicetoFile(String deniedMac, String devName) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        StringBuffer configContent = new StringBuffer();
        createFileIfNotThere(DENIED_MAC_FILE);
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(DENIED_MAC_FILE));
            while (true) {
                String line = br2.readLine();
                if (line == null) {
                    break;
                }
                configContent.append(line + "\n");
            }
            if (devName == null) {
                configContent.append("#name-\n");
            } else {
                configContent.append(nameTag + devName + "\n");
            }
            configContent.append(deniedMac + "\n");
            Log.d(TAG, "DENIED_MAC_FILE: \n" + configContent.toString());
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(DENIED_MAC_FILE));
            bw2.write(configContent.toString());
            try {
                bw2.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e);
            }
            try {
                br2.close();
            } catch (IOException e2) {
                Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e2);
            }
        } catch (IOException e3) {
            Log.e(TAG, e3.toString(), new Throwable("Fail to write DENIED_MAC_FILE"));
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e4);
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e5) {
                    Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e5);
                }
            }
        } catch (Throwable th) {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e6) {
                    Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e6);
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e7) {
                    Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e7);
                }
            }
            throw th;
        }
    }

    private void createFileIfNotThere(String fileName) {
        StringBuilder sb;
        BufferedWriter bw = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                Log.d(TAG, fileName + " do not exists, create one.");
                file.createNewFile();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("chmod 604 " + fileName);
                bw = new BufferedWriter(new FileWriter(fileName));
                bw.write("# List of MAC addresses that are not allowed to authenticate (IEEE 802.11) with the AP.");
                bw.close();
            }
            if (bw != null) {
                try {
                    bw.close();
                    return;
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
            } else {
                return;
            }
            sb.append("Error closing BufferedWriter for hostapd.deny during write");
            sb.append(e);
            Log.e(TAG, sb.toString());
        } catch (IOException e2) {
            Log.e(TAG, e2.toString(), new Throwable("Fail to write DENIED_MAC_FILE"));
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e4);
                }
            }
            throw th;
        }
    }

    private static void rmDevFromDeniedFile(String devAddr) {
        StringBuilder sb;
        BufferedReader br = null;
        BufferedWriter bw = null;
        StringBuffer fileContent = new StringBuffer();
        String tmpName = "";
        if (devAddr == null || !devAddr.matches(MAC_PATTERN_STR)) {
            Log.i(TAG, "The devAddr param is null or not MAC patern!");
            return;
        }
        Log.i(TAG, "start rmDevFromDeniedFile..");
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(DENIED_MAC_FILE));
            while (true) {
                String oneLine = br2.readLine();
                if (oneLine == null) {
                    break;
                } else if (oneLine.startsWith("#")) {
                    if (oneLine.startsWith(nameTag)) {
                        tmpName = oneLine;
                    } else {
                        fileContent.append(oneLine + "\n");
                    }
                } else if (!oneLine.matches(MAC_PATTERN_STR)) {
                    Log.i(TAG, "Invalid dev mac: " + oneLine);
                } else if (!oneLine.equalsIgnoreCase(devAddr)) {
                    fileContent.append(tmpName + "\n");
                    fileContent.append(oneLine + "\n");
                } else {
                    Log.i(TAG, "Remove Device: " + tmpName);
                    Log.i(TAG, "Remove MAC: " + oneLine);
                }
            }
            Log.i(TAG, "Reach end of hostapd.deny!");
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(DENIED_MAC_FILE));
            bw2.write(fileContent.toString());
            try {
                bw2.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e);
            }
            try {
                br2.close();
                return;
            } catch (IOException e2) {
                e = e2;
                sb = new StringBuilder();
            }
        } catch (IOException e3) {
            Log.e(TAG, "Error reading hostapd.deny " + e3);
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e4);
                }
            }
            if (br != null) {
                try {
                    br.close();
                    return;
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            } else {
                return;
            }
        } catch (Throwable th) {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e6) {
                    Log.e(TAG, "Error closing BufferedWriter for hostapd.deny during write" + e6);
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e7) {
                    Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e7);
                }
            }
            throw th;
        }
        sb.append("Error closing BufferedReader for hostapd.deny during read");
        sb.append(e);
        Log.e(TAG, sb.toString());
    }

    public static List<HotspotClient> getBlockedHotspotClientsList() {
        List<HotspotClient> clients = new ArrayList<>();
        if (!mLoadFromStore) {
            loadDeniedDevice();
            mLoadFromStore = true;
        }
        synchronized (mDeniedClients) {
            for (HotspotClient client : mDeniedClients) {
                clients.add(new HotspotClient(client));
            }
        }
        return clients;
    }

    public static boolean rmDeniedClientFromListAndFile(HotspotClient client) {
        String deniedMac = client.deviceAddress;
        if (!isHotspotListContains(mDeniedClients, deniedMac)) {
            Log.d(TAG, "The mac is not denied, no need to unblock! ");
            return false;
        }
        rmDevFromDeniedFile(deniedMac);
        synchronized (mDeniedClients) {
            rmClientWhenContains(mDeniedClients, deniedMac);
        }
        return true;
    }

    public boolean syncUnblockClient(HotspotClient client) {
        boolean result;
        rmDeniedClientFromListAndFile(client);
        synchronized (mDeniedClients) {
            result = this.mWifiNative.blockClient(this.mApInterfaceName, client.deviceAddress, false);
            if (result) {
                mDeniedClients.remove(client.deviceAddress);
                sendClientsChangedBroadcast();
            } else {
                Log.e(TAG, "Failed to unblock " + client.deviceAddress);
            }
        }
        return result;
    }

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

    /* access modifiers changed from: private */
    public boolean blockSavedClients() {
        boolean result = true;
        if (this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingTethering()) {
            Log.d(TAG, "WNS on, dnt block");
            return true;
        }
        for (HotspotClient client : mDeniedClients) {
            if (!this.mWifiNative.blockClient(this.mApInterfaceName, client.deviceAddress, true)) {
                Log.d(TAG, "Initial set denied dev fail, devAddr = " + client.deviceAddress);
                result = false;
            }
        }
        return result;
    }

    public static void loadDeniedDevice() {
        StringBuilder sb;
        BufferedReader br = null;
        String tmpStr = "";
        Log.i(TAG, "loadDeniedDevice..");
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(DENIED_MAC_FILE));
            while (true) {
                String line = br2.readLine();
                if (line == null) {
                    Log.i(TAG, "Reach end of hostapd.deny!");
                    try {
                        br2.close();
                        return;
                    } catch (IOException e) {
                        e = e;
                        sb = new StringBuilder();
                    }
                } else if (line.startsWith("#")) {
                    if (line.startsWith(nameTag)) {
                        tmpStr = line.substring(6);
                        Log.i(TAG, "Device name: " + tmpStr);
                    } else {
                        Log.i(TAG, "Skip comment: " + line);
                    }
                } else if (!line.matches(MAC_PATTERN_STR)) {
                    Log.i(TAG, "Invalid dev mac: " + line);
                } else if (!isHotspotListContains(mDeniedClients, line)) {
                    HotspotClient client = new HotspotClient(line, true, tmpStr);
                    synchronized (mDeniedClients) {
                        mDeniedClients.add(client);
                    }
                } else {
                    Log.i(TAG, "Dup dev mac: " + line);
                }
            }
            sb.append("Error closing BufferedReader for hostapd.deny during read");
            sb.append(e);
            Log.e(TAG, sb.toString());
        } catch (IOException e2) {
            Log.e(TAG, "Error reading hostapd.deny " + e2);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Error closing BufferedReader for hostapd.deny during read" + e4);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public boolean setMaxClient(int max) {
        Log.d(TAG, "setMaxClient max = " + max);
        return this.mWifiNative.setMaxClient(this.mApInterfaceName, max);
    }

    /* access modifiers changed from: private */
    public class SoftApStateMachine extends StateMachine {
        public static final int CMD_BLOCK_SAVED_CLIENTS = 12;
        public static final int CMD_CONNECTED_STATIONS = 10;
        public static final int CMD_DISCONNECTED_STATIONS = 11;
        public static final int CMD_ENTER_SET_MAX_CLIENTS = 13;
        public static final int CMD_FAILURE = 2;
        public static final int CMD_INTERFACE_DESTROYED = 7;
        public static final int CMD_INTERFACE_DOWN = 8;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_NO_ASSOCIATED_STATIONS_TIMEOUT = 5;
        public static final int CMD_NUM_ASSOCIATED_STATIONS_CHANGED = 4;
        public static final int CMD_SOFT_AP_CHANNEL_SWITCHED = 9;
        public static final int CMD_START = 0;
        public static final int CMD_TIMEOUT_TOGGLE_CHANGED = 6;
        /* access modifiers changed from: private */
        public final State mIdleState = new IdleState();
        /* access modifiers changed from: private */
        public final State mStartedState = new StartedState();
        /* access modifiers changed from: private */
        public final WifiNative.InterfaceCallback mWifiNativeInterfaceCallback = new WifiNative.InterfaceCallback() {
            /* class com.android.server.wifi.OppoSoftApManager.SoftApStateMachine.AnonymousClass1 */

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDestroyed(String ifaceName) {
                if (OppoSoftApManager.this.mApInterfaceName != null && OppoSoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(7);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onUp(String ifaceName) {
                if (OppoSoftApManager.this.mApInterfaceName != null && OppoSoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(3, 1);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDown(String ifaceName) {
                if (OppoSoftApManager.this.mApInterfaceName != null && OppoSoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(3, 0);
                }
            }
        };

        SoftApStateMachine(Looper looper) {
            super(OppoSoftApManager.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }

        private class IdleState extends State {
            private IdleState() {
            }

            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: com.android.server.wifi.OppoSoftApManager.access$502(com.android.server.wifi.OppoSoftApManager, boolean):boolean
             arg types: [com.android.server.wifi.OppoSoftApManager, int]
             candidates:
              com.android.server.wifi.SoftApManager.access$502(com.android.server.wifi.SoftApManager, boolean):boolean
              com.android.server.wifi.OppoSoftApManager.access$502(com.android.server.wifi.OppoSoftApManager, boolean):boolean */
            public void enter() {
                String unused = OppoSoftApManager.this.mApInterfaceName = (String) null;
                boolean unused2 = OppoSoftApManager.this.mIfaceIsUp = false;
                boolean unused3 = OppoSoftApManager.this.mIfaceIsDestroyed = false;
            }

            public boolean processMessage(Message message) {
                if (message.what == 0) {
                    Settings.System.getInt(OppoSoftApManager.this.mContext.getContentResolver(), OppoSoftApManager.WIFI_HOTSPOT_MAX_CLIENT_NUM, 10);
                    WifiConfiguration wifiConfiguration = (WifiConfiguration) message.obj;
                    String unused = OppoSoftApManager.this.mApInterfaceName = OppoSoftApManager.this.mWifiNative.setupInterfaceForSoftApMode(SoftApStateMachine.this.mWifiNativeInterfaceCallback);
                    if (TextUtils.isEmpty(OppoSoftApManager.this.mApInterfaceName)) {
                        if (OppoSoftApManager.access$1008() < 2) {
                            Log.e(OppoSoftApManager.TAG, "Failed to create ApInterface, try recovery " + OppoSoftApManager.mRecoveryRetryCount);
                            OppoSoftApManager.this.mWifiInjector.getSelfRecovery().trigger(0);
                        }
                        Log.e(OppoSoftApManager.TAG, "setup failure when creating ap interface.");
                        OppoSoftApManager.this.updateApState(14, 11, 0);
                        OppoSoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, 0);
                    } else {
                        OppoSoftApManager.this.updateApState(12, 11, 0);
                        int result = OppoSoftApManager.this.startSoftAp((WifiConfiguration) message.obj);
                        if (result != 0) {
                            int failureReason = 0;
                            if (result == 1) {
                                failureReason = 1;
                            }
                            Log.e(OppoSoftApManager.TAG, "Failed to startSoftAp failureReason = " + failureReason);
                            OppoSoftApManager.this.updateApState(14, 12, failureReason);
                            OppoSoftApManager.this.stopSoftAp();
                            OppoSoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, failureReason);
                        } else {
                            MtkWifiApMonitor.registerHandler(OppoSoftApManager.this.mApInterfaceName, 147498, SoftApStateMachine.this.getHandler());
                            MtkWifiApMonitor.registerHandler(OppoSoftApManager.this.mApInterfaceName, 147497, SoftApStateMachine.this.getHandler());
                            MtkWifiApMonitor.startMonitoring(OppoSoftApManager.this.mApInterfaceName);
                            SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                            softApStateMachine.transitionTo(softApStateMachine.mStartedState);
                        }
                    }
                }
                return true;
            }
        }

        private class StartedState extends State {
            private SoftApTimeoutEnabledSettingObserver mSettingObserver;
            private WakeupMessage mSoftApTimeoutMessage;
            private int mTimeoutDelay;

            private StartedState() {
            }

            private class SoftApTimeoutEnabledSettingObserver extends ContentObserver {
                SoftApTimeoutEnabledSettingObserver(Handler handler) {
                    super(handler);
                }

                public void register() {
                    OppoSoftApManager.this.mFrameworkFacade.registerContentObserver(OppoSoftApManager.this.mContext, Settings.Global.getUriFor("soft_ap_timeout_enabled"), true, this);
                    boolean unused = OppoSoftApManager.this.mTimeoutEnabled = getValue();
                }

                public void unregister() {
                    OppoSoftApManager.this.mFrameworkFacade.unregisterContentObserver(OppoSoftApManager.this.mContext, this);
                }

                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    Log.d(OppoSoftApManager.TAG, "SoftApTimeoutEnabledSettingObserver: SOFT_AP_TIMEOUT_ENABLED is changed to " + selfChange);
                    OppoSoftApManager.this.mStateMachine.sendMessage(6, getValue() ? 1 : 0);
                }

                public boolean getValue() {
                    boolean enabled = true;
                    if (OppoSoftApManager.this.mFrameworkFacade.getIntegerSetting(OppoSoftApManager.this.mContext, "soft_ap_timeout_enabled", 1) != 1 && !OppoSoftApManager.this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingTethering()) {
                        enabled = false;
                    }
                    Log.d(OppoSoftApManager.TAG, "getValue, enabled: " + enabled);
                    return enabled;
                }
            }

            private int getConfigSoftApTimeoutDelay() {
                int delay = OppoSoftApManager.this.mContext.getResources().getInteger(17694946);
                if (delay < OppoSoftApManager.MIN_SOFT_AP_TIMEOUT_DELAY_MS) {
                    delay = OppoSoftApManager.MIN_SOFT_AP_TIMEOUT_DELAY_MS;
                    Log.w(OppoSoftApManager.TAG, "Overriding timeout delay with minimum limit value");
                }
                Log.d(OppoSoftApManager.TAG, "Timeout delay: " + delay);
                return delay;
            }

            private void scheduleTimeoutMessage() {
                if (OppoSoftApManager.this.mTimeoutEnabled || OppoSoftApManager.this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingTethering()) {
                    this.mSoftApTimeoutMessage.schedule(SystemClock.elapsedRealtime() + ((long) this.mTimeoutDelay));
                    Log.d(OppoSoftApManager.TAG, "Timeout message scheduled");
                }
            }

            private void cancelTimeoutMessage() {
                this.mSoftApTimeoutMessage.cancel();
                Log.d(OppoSoftApManager.TAG, "Timeout message canceled");
            }

            private void setNumAssociatedStations(int numStations) {
                if (OppoSoftApManager.this.mNumAssociatedStations != numStations) {
                    int unused = OppoSoftApManager.this.mNumAssociatedStations = numStations;
                    Log.d(OppoSoftApManager.TAG, "Number of associated stations changed: " + OppoSoftApManager.this.mNumAssociatedStations);
                    if (OppoSoftApManager.this.mCallback != null) {
                        OppoSoftApManager.this.mCallback.onNumClientsChanged(OppoSoftApManager.this.mNumAssociatedStations);
                    } else {
                        Log.e(OppoSoftApManager.TAG, "SoftApCallback is null. Dropping NumClientsChanged event.");
                    }
                    OppoSoftApManager.this.mWifiMetrics.addSoftApNumAssociatedStationsChangedEvent(OppoSoftApManager.this.mNumAssociatedStations, OppoSoftApManager.this.mMode);
                }
            }

            private void setConnectedStations(String Macaddr) {
                OppoSoftApManager.access$2208(OppoSoftApManager.this);
                if (OppoSoftApManager.this.mCallback == null) {
                    Log.e(OppoSoftApManager.TAG, "SoftApCallback is null. Dropping onStaConnected event.");
                }
                OppoSapScanCoexistManager.getInstance(OppoSoftApManager.this.mContext).updateSapAssociatedStations(OppoSoftApManager.this.mQCNumAssociatedStations);
                if (OppoSoftApManager.this.mQCNumAssociatedStations > 0) {
                    cancelTimeoutMessage();
                }
            }

            private void setDisConnectedStations(String Macaddr) {
                if (OppoSoftApManager.this.mQCNumAssociatedStations > 0) {
                    OppoSoftApManager.access$2210(OppoSoftApManager.this);
                }
                if (OppoSoftApManager.this.mCallback == null) {
                    Log.e(OppoSoftApManager.TAG, "SoftApCallback is null. Dropping onStaDisconnected event.");
                }
                OppoSapScanCoexistManager.getInstance(OppoSoftApManager.this.mContext).updateSapAssociatedStations(OppoSoftApManager.this.mQCNumAssociatedStations);
                if (OppoSoftApManager.this.mQCNumAssociatedStations == 0) {
                    scheduleTimeoutMessage();
                }
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != OppoSoftApManager.this.mIfaceIsUp) {
                    boolean unused = OppoSoftApManager.this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.d(OppoSoftApManager.TAG, "SoftAp is ready for use");
                        OppoSoftApManager.this.updateApState(13, 12, 0);
                        int unused2 = OppoSoftApManager.mRecoveryRetryCount = 0;
                        OppoSoftApManager.this.mWifiMetrics.incrementSoftApStartResult(true, 0);
                        if (OppoSoftApManager.this.mCallback != null) {
                            OppoSoftApManager.this.mCallback.onNumClientsChanged(OppoSoftApManager.this.mNumAssociatedStations);
                        }
                    } else {
                        if (OppoSoftApManager.access$1008() < 2) {
                            Log.e(OppoSoftApManager.TAG, "Failed to create ApInterface, try recovery " + OppoSoftApManager.mRecoveryRetryCount);
                            OppoSoftApManager.this.mWifiInjector.getSelfRecovery().trigger(0);
                        }
                        SoftApStateMachine.this.sendMessage(8);
                    }
                    OppoSoftApManager.this.mWifiMetrics.addSoftApUpChangedEvent(isUp, OppoSoftApManager.this.mMode);
                }
            }

            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: com.android.server.wifi.OppoSoftApManager.access$502(com.android.server.wifi.OppoSoftApManager, boolean):boolean
             arg types: [com.android.server.wifi.OppoSoftApManager, int]
             candidates:
              com.android.server.wifi.SoftApManager.access$502(com.android.server.wifi.SoftApManager, boolean):boolean
              com.android.server.wifi.OppoSoftApManager.access$502(com.android.server.wifi.OppoSoftApManager, boolean):boolean */
            public void enter() {
                boolean unused = OppoSoftApManager.this.mIfaceIsUp = false;
                boolean unused2 = OppoSoftApManager.this.mIfaceIsDestroyed = false;
                onUpChanged(OppoSoftApManager.this.mWifiNative.isInterfaceUp(OppoSoftApManager.this.mApInterfaceName));
                this.mTimeoutDelay = getConfigSoftApTimeoutDelay();
                Handler handler = OppoSoftApManager.this.mStateMachine.getHandler();
                this.mSoftApTimeoutMessage = new WakeupMessage(OppoSoftApManager.this.mContext, handler, OppoSoftApManager.SOFT_AP_SEND_MESSAGE_TIMEOUT_TAG, 5);
                this.mSettingObserver = new SoftApTimeoutEnabledSettingObserver(handler);
                SoftApTimeoutEnabledSettingObserver softApTimeoutEnabledSettingObserver = this.mSettingObserver;
                if (softApTimeoutEnabledSettingObserver != null) {
                    softApTimeoutEnabledSettingObserver.register();
                }
                OppoSoftApManager.this.mSarManager.setSapWifiState(13);
                Log.d(OppoSoftApManager.TAG, "Resetting num stations on start");
                int unused3 = OppoSoftApManager.this.mNumAssociatedStations = 0;
                int unused4 = OppoSoftApManager.this.mQCNumAssociatedStations = 0;
                if (this.mSettingObserver.getValue() || OppoSoftApManager.this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingTethering()) {
                    Log.d(OppoSoftApManager.TAG, "SOFT_AP_TIMEOUT_ENABLED is enabled, schedule timeout alarm");
                    scheduleTimeoutMessage();
                }
                OppoSoftApManager.loadDeniedDevice();
                if (!OppoSoftApManager.this.setMaxClient(OppoSoftApManager.this.mMaxNumSta)) {
                    int unused5 = OppoSoftApManager.this.mMaxClientsRetryCount = 1;
                    SoftApStateMachine.this.sendMessageDelayed(13, 0, 0, null, 100);
                }
                if (!OppoSoftApManager.this.blockSavedClients()) {
                    int unused6 = OppoSoftApManager.this.mBlockSavedClientsRetryCount = 1;
                    SoftApStateMachine.this.sendMessageDelayed(12, 0, 0, null, 400);
                }
            }

            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: com.android.server.wifi.OppoSoftApManager.access$502(com.android.server.wifi.OppoSoftApManager, boolean):boolean
             arg types: [com.android.server.wifi.OppoSoftApManager, int]
             candidates:
              com.android.server.wifi.SoftApManager.access$502(com.android.server.wifi.SoftApManager, boolean):boolean
              com.android.server.wifi.OppoSoftApManager.access$502(com.android.server.wifi.OppoSoftApManager, boolean):boolean */
            public void exit() {
                if (!OppoSoftApManager.this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingTethering()) {
                    int latestMaxClient = Settings.System.getInt(OppoSoftApManager.this.mContext.getContentResolver(), OppoSoftApManager.WIFI_HOTSPOT_MAX_CLIENT_NUM, 10);
                    Log.d(OppoSoftApManager.TAG, "exit setMaxClient mMaxNumSta=" + OppoSoftApManager.this.mMaxNumSta + " latestMaxClient=" + latestMaxClient);
                    if (OppoSoftApManager.this.mMaxNumSta != latestMaxClient) {
                        boolean unused = OppoSoftApManager.this.setMaxClient(latestMaxClient);
                    }
                }
                OppoSoftApManager.this.mOppoHotspotClientInfo.clearConnectedDevice();
                OppoSoftApManager.this.sendClientsChangedBroadcast();
                if (OppoSoftApManager.this.mApInterfaceName != null) {
                    OppoSoftApManager.this.stopSoftAp();
                }
                SoftApTimeoutEnabledSettingObserver softApTimeoutEnabledSettingObserver = this.mSettingObserver;
                if (softApTimeoutEnabledSettingObserver != null) {
                    softApTimeoutEnabledSettingObserver.unregister();
                }
                Log.d(OppoSoftApManager.TAG, "Resetting num stations on stop");
                int unused2 = OppoSoftApManager.this.mQCNumAssociatedStations = 0;
                setNumAssociatedStations(0);
                cancelTimeoutMessage();
                OppoSapScanCoexistManager.getInstance(OppoSoftApManager.this.mContext).updateSapAssociatedStations(OppoSoftApManager.this.mQCNumAssociatedStations);
                OppoSoftApManager.this.mWifiMetrics.addSoftApUpChangedEvent(false, OppoSoftApManager.this.mMode);
                OppoSoftApManager.this.updateApState(11, 10, 0);
                MtkWifiApMonitor.deregisterAllHandler();
                MtkWifiApMonitor.stopMonitoring(OppoSoftApManager.this.mApInterfaceName);
                OppoSoftApManager.this.mSarManager.setSapWifiState(11);
                String unused3 = OppoSoftApManager.this.mApInterfaceName = (String) null;
                boolean unused4 = OppoSoftApManager.this.mIfaceIsUp = false;
                boolean unused5 = OppoSoftApManager.this.mIfaceIsDestroyed = false;
                OppoSoftApManager.this.mStateMachine.quitNow();
            }

            private void updateUserBandPreferenceViolationMetricsIfNeeded() {
                boolean bandPreferenceViolated = false;
                if (OppoSoftApManager.this.mApConfig.apBand == 0 && ScanResult.is5GHz(OppoSoftApManager.this.mReportedFrequency)) {
                    bandPreferenceViolated = true;
                } else if (OppoSoftApManager.this.mApConfig.apBand == 1 && ScanResult.is24GHz(OppoSoftApManager.this.mReportedFrequency)) {
                    bandPreferenceViolated = true;
                }
                if (bandPreferenceViolated) {
                    Log.e(OppoSoftApManager.TAG, "Channel does not satisfy user band preference: " + OppoSoftApManager.this.mReportedFrequency);
                    OppoSoftApManager.this.mWifiMetrics.incrementNumSoftApUserBandPreferenceUnsatisfied();
                }
            }

            /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
            public boolean processMessage(Message message) {
                int i = message.what;
                if (i != 0) {
                    boolean isEnabled = false;
                    switch (i) {
                        case 2:
                            Log.w(OppoSoftApManager.TAG, "hostapd failure, stop and report failure");
                            Log.w(OppoSoftApManager.TAG, "interface error, stop and report failure");
                            OppoSoftApManager.this.updateApState(14, 13, 0);
                            OppoSoftApManager.this.updateApState(10, 14, 0);
                            SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                            softApStateMachine.transitionTo(softApStateMachine.mIdleState);
                            break;
                        case 3:
                            if (message.arg1 == 1) {
                                isEnabled = true;
                            }
                            onUpChanged(isEnabled);
                            break;
                        case 4:
                            if (message.arg1 >= 0) {
                                Log.d(OppoSoftApManager.TAG, "Setting num stations on CMD_NUM_ASSOCIATED_STATIONS_CHANGED");
                                setNumAssociatedStations(message.arg1);
                                break;
                            } else {
                                Log.e(OppoSoftApManager.TAG, "Invalid number of associated stations: " + message.arg1);
                                break;
                            }
                        case 5:
                            if (OppoSoftApManager.this.mTimeoutEnabled) {
                                if (OppoSoftApManager.this.mQCNumAssociatedStations == 0) {
                                    Log.i(OppoSoftApManager.TAG, "Timeout message received. Stopping soft AP.");
                                    OppoSoftApManager.this.updateApState(10, 13, 0);
                                    OppoWifiSharingManager sharingManager = WifiInjector.getInstance().getOppoWifiSharingManager();
                                    if (sharingManager.isWifiSharingSupported()) {
                                        WifiController wifiController = WifiInjector.getInstance().getWifiController();
                                        sharingManager.setWifiClosedByUser(true);
                                        wifiController.setStaSoftApConcurrencyForSharing(false);
                                    }
                                    SoftApStateMachine softApStateMachine2 = SoftApStateMachine.this;
                                    softApStateMachine2.transitionTo(softApStateMachine2.mIdleState);
                                    break;
                                } else {
                                    Log.wtf(OppoSoftApManager.TAG, "Timeout message received but has clients. Dropping.");
                                    break;
                                }
                            } else {
                                Log.wtf(OppoSoftApManager.TAG, "Timeout message received while timeout is disabled. Dropping.");
                                break;
                            }
                        case 6:
                            if (message.arg1 == 1) {
                                isEnabled = true;
                            }
                            if (OppoSoftApManager.this.mTimeoutEnabled != isEnabled) {
                                boolean unused = OppoSoftApManager.this.mTimeoutEnabled = isEnabled;
                                if (!OppoSoftApManager.this.mTimeoutEnabled) {
                                    cancelTimeoutMessage();
                                }
                                if (OppoSoftApManager.this.mTimeoutEnabled && OppoSoftApManager.this.mQCNumAssociatedStations == 0) {
                                    scheduleTimeoutMessage();
                                    break;
                                }
                            }
                            break;
                        case 7:
                            Log.d(OppoSoftApManager.TAG, "Interface was cleanly destroyed.");
                            OppoSoftApManager.this.updateApState(10, 13, 0);
                            boolean unused2 = OppoSoftApManager.this.mIfaceIsDestroyed = true;
                            SoftApStateMachine softApStateMachine3 = SoftApStateMachine.this;
                            softApStateMachine3.transitionTo(softApStateMachine3.mIdleState);
                            break;
                        case 8:
                            Log.w(OppoSoftApManager.TAG, "interface error, stop and report failure");
                            OppoSoftApManager.this.updateApState(14, 13, 0);
                            OppoSoftApManager.this.updateApState(10, 14, 0);
                            SoftApStateMachine softApStateMachine4 = SoftApStateMachine.this;
                            softApStateMachine4.transitionTo(softApStateMachine4.mIdleState);
                            break;
                        case 9:
                            int unused3 = OppoSoftApManager.this.mReportedFrequency = message.arg1;
                            int unused4 = OppoSoftApManager.this.mReportedBandwidth = message.arg2;
                            Log.d(OppoSoftApManager.TAG, "Channel switched. Frequency: " + OppoSoftApManager.this.mReportedFrequency + " Bandwidth: " + OppoSoftApManager.this.mReportedBandwidth);
                            OppoSoftApManager.this.mWifiMetrics.addSoftApChannelSwitchedEvent(OppoSoftApManager.this.mReportedFrequency, OppoSoftApManager.this.mReportedBandwidth, OppoSoftApManager.this.mMode);
                            updateUserBandPreferenceViolationMetricsIfNeeded();
                            break;
                        case 10:
                            if (message.obj != null) {
                                Log.d(OppoSoftApManager.TAG, "Setting Macaddr of stations on CMD_CONNECTED_STATIONS");
                                setConnectedStations((String) message.obj);
                                OppoSoftApManager.this.mOppoHotspotClientInfo.connectionStatusChange(message, true);
                                break;
                            } else {
                                Log.e(OppoSoftApManager.TAG, "Invalid Macaddr of connected station: " + message.obj);
                                break;
                            }
                        case 11:
                            if (message.obj != null) {
                                Log.d(OppoSoftApManager.TAG, "Setting Macaddr of stations on CMD_DISCONNECTED_STATIONS");
                                setDisConnectedStations((String) message.obj);
                                OppoSoftApManager.this.mOppoHotspotClientInfo.connectionStatusChange(message, false);
                                break;
                            } else {
                                Log.e(OppoSoftApManager.TAG, "Invalid Macaddr of disconnected station: " + message.obj);
                                break;
                            }
                        case 12:
                            if (!OppoSoftApManager.this.blockSavedClients()) {
                                if (OppoSoftApManager.this.mBlockSavedClientsRetryCount > 3) {
                                    Log.e(OppoSoftApManager.TAG, "retry " + OppoSoftApManager.this.mBlockSavedClientsRetryCount + " times bsc failed! give up.");
                                    int unused5 = OppoSoftApManager.this.mBlockSavedClientsRetryCount = 0;
                                    break;
                                } else {
                                    Log.d(OppoSoftApManager.TAG, "block saved clients failed, retry " + OppoSoftApManager.this.mBlockSavedClientsRetryCount);
                                    OppoSoftApManager.access$2708(OppoSoftApManager.this);
                                    SoftApStateMachine.this.sendMessageDelayed(12, 0, 0, null, 200);
                                    break;
                                }
                            } else {
                                int unused6 = OppoSoftApManager.this.mBlockSavedClientsRetryCount = 0;
                                break;
                            }
                        case 13:
                            if (!OppoSoftApManager.this.setMaxClient(OppoSoftApManager.this.mMaxNumSta)) {
                                if (OppoSoftApManager.this.mMaxClientsRetryCount > 3) {
                                    Log.e(OppoSoftApManager.TAG, "retry " + OppoSoftApManager.this.mMaxClientsRetryCount + " times sma failed! give up.");
                                    int unused7 = OppoSoftApManager.this.mMaxClientsRetryCount = 0;
                                    break;
                                } else {
                                    Log.d(OppoSoftApManager.TAG, "set max clients failed, retry " + OppoSoftApManager.this.mMaxClientsRetryCount);
                                    OppoSoftApManager.access$2608(OppoSoftApManager.this);
                                    SoftApStateMachine.this.sendMessageDelayed(13, 0, 0, null, 200);
                                    break;
                                }
                            } else {
                                int unused8 = OppoSoftApManager.this.mMaxClientsRetryCount = 0;
                                break;
                            }
                        default:
                            switch (i) {
                                case 147497:
                                    Log.d(OppoSoftApManager.TAG, "AP STA DISCONNECTED:" + message.obj);
                                    if (message.obj != null) {
                                        Log.d(OppoSoftApManager.TAG, "Setting Macaddr of stations on CMD_DISCONNECTED_STATIONS");
                                        setDisConnectedStations((String) message.obj);
                                        OppoSoftApManager.this.mOppoHotspotClientInfo.connectionStatusChange(message, false);
                                        break;
                                    } else {
                                        Log.e(OppoSoftApManager.TAG, "Invalid Macaddr of disconnected station: " + message.obj);
                                        break;
                                    }
                                case 147498:
                                    Log.d(OppoSoftApManager.TAG, "AP STA CONNECTED:" + message.obj);
                                    if (message.obj != null) {
                                        Log.d(OppoSoftApManager.TAG, "Setting Macaddr of stations on CMD_CONNECTED_STATIONS");
                                        setConnectedStations((String) message.obj);
                                        OppoSoftApManager.this.mOppoHotspotClientInfo.connectionStatusChange(message, true);
                                        break;
                                    } else {
                                        Log.e(OppoSoftApManager.TAG, "Invalid Macaddr of connected station: " + message.obj);
                                        break;
                                    }
                                default:
                                    return false;
                            }
                    }
                }
                return true;
            }
        }
    }
}
