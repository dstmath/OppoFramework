package com.android.server.wifi;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.rtt.RttServiceImpl;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/* access modifiers changed from: package-private */
public class OppoWifiNetworkSwitchEnhance {
    private static final String ACTION_WIFI_NETWORK_AVAILABLE = "android.net.wifi.OPPO_WIFI_VALID";
    private static final String ACTION_WIFI_NETWORK_CONNECT = "android.net.wifi.OPPO_WIFI_CONNECT";
    private static final String ACTION_WIFI_NETWORK_NOT_AVAILABLE = "android.net.wifi.OPPO_WIFI_INVALID";
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STATE";
    private static final int CMD_RSSI_FETCH = 3003;
    private static final int DECT_TIME = 2;
    private static final int EVENT_ADD_UPDATE_NETWORK = 2000;
    private static final int EVENT_AUTO_CONNECT_AP = 2001;
    private static final int EVENT_DETECT_RSSI = 2004;
    private static final int EVENT_DETECT_SCAN_RESULT = 2005;
    private static final int EVENT_NETWORK_NOT_AVAILABLE = 2002;
    private static final int EVENT_NETWORK_STATE_CHANGE = 3000;
    private static final int EVENT_REOVE_UPDATE_NETWORK = 2003;
    private static final int EVENT_SCREEN_OFF = 3002;
    private static final int EVENT_SCREEN_ON = 3001;
    private static final String EXTRA_NETWORK_STATE = "netState";
    private static final String EXTRA_WIFI_LINK = "linkProperties";
    private static final String EXTRA_WIFI_MANUAL = "manualConnect";
    private static final String EXTRA_WIFI_NETWORK = "network";
    private static final String EXTRA_WIFI_SSID = "ssid";
    private static final int GOOD_RSSI_SWITCH_VALUE = -70;
    private static final int INVALID_RSSI = -127;
    private static final int LOSS_PKT = 2;
    private static final int LOW_RSSI = -78;
    private static final int RSSI_DELTA = 5;
    private static final long SCAN_RESULT_AGE = 15000;
    private static final String SECURITY_EAP = "WPA_EAP";
    private static final String SECURITY_NONE = "NONE";
    private static final String SECURITY_PSK = "WPA_PSK";
    private static final String SECURITY_WAPI_CERT = "WAPI_CERT";
    private static final String SECURITY_WAPI_PSK = "WAPI_PSK";
    private static final String SECURITY_WEP = "WEP";
    private static final String SECURITY_WPA2_PSK = "WPA2_PSK";
    private static final int SWITCH_CONNECT_DELAY = 5000;
    private static final String TAG = "OppoWifiNetworkSwitchEnhance";
    private static final int TYPE_AUTO_AVAILABLE_RSSI = 1;
    private static final int TYPE_AUTO_UNAVAILABLE_CAPTIVE = 0;
    private static final int TYPE_AUTO_UNAVAILABLE_SCAN = 2;
    private static final String WIFI_NETWORK_AVAILABLE = (Environment.getDataDirectory() + "/misc/wifi/network_available");
    /* access modifiers changed from: private */
    public AlertDialog mAlertDialog = null;
    private boolean mAutoSwitch = true;
    private CharSequence mAvailableAP;
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public boolean mCaptivePortal = false;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public boolean mDebug = true;
    private int mDetectTime;
    /* access modifiers changed from: private */
    public boolean mFeature = true;
    private CharSequence mGoodAvailableAP;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mInitAutoConnect = true;
    private String mInterfaceName = null;
    /* access modifiers changed from: private */
    public boolean mIsSoftAP = false;
    /* access modifiers changed from: private */
    public String mLastSSID = null;
    private long mLastScanTime = 0;
    /* access modifiers changed from: private */
    public boolean mLossPktDetect = false;
    private int mLossPktTime;
    private boolean mManualConnect = false;
    private List<NetworkAvailableConfig> mNetworkAvailables = new ArrayList();
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo = null;
    /* access modifiers changed from: private */
    public NetworkLinkMonitor mNetworkLinkMonitor;
    private String mNewBssid = " ";
    private String mNewSsid = " ";
    private String mOldBssid = " ";
    private String mOldSsid = " ";
    private ScanRequestProxy mScanResultsProxy;
    private List<WifiConfiguration> mSortWifiConfig = new ArrayList();
    private SupplicantStateTracker mSupplicantTracker;
    /* access modifiers changed from: private */
    public String mUnavailableSsid = " ";
    private WifiConfigManager mWifiConfigManager;
    private WifiNative mWifiNative;
    /* access modifiers changed from: private */
    public ClientModeImpl mWifiStateMachine;

    public OppoWifiNetworkSwitchEnhance(Context c, ClientModeImpl wsm, WifiConfigManager wcs, WifiNative wnt, SupplicantStateTracker wst, ScanRequestProxy mSrp) {
        this.mContext = c;
        this.mWifiStateMachine = wsm;
        this.mWifiConfigManager = wcs;
        this.mWifiNative = wnt;
        this.mSupplicantTracker = wst;
        this.mNetworkLinkMonitor = new NetworkLinkMonitor(this.mContext, wsm.getMessenger());
        this.mNetworkLinkMonitor.start();
        this.mWifiConfigManager.setWifiNetwork(this);
        this.mAvailableAP = this.mContext.getText(201653535);
        this.mGoodAvailableAP = this.mContext.getText(201653540);
        setupNetworkReceiver();
        this.mHandler = new H(wsm.getHandler().getLooper());
        this.mScanResultsProxy = mSrp;
    }

    private void setupNetworkReceiver() {
        IntentFilter netWorkFilter = new IntentFilter();
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_CONNECT);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_STATE);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_AVAILABLE);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_NOT_AVAILABLE);
        netWorkFilter.addAction("android.net.wifi.STATE_CHANGE");
        netWorkFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        netWorkFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        netWorkFilter.addAction("android.intent.action.SCREEN_ON");
        netWorkFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoWifiNetworkSwitchEnhance.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (!OppoWifiNetworkSwitchEnhance.this.mFeature) {
                    OppoWifiNetworkSwitchEnhance.this.logD("mf dis");
                    return;
                }
                OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = OppoWifiNetworkSwitchEnhance.this;
                oppoWifiNetworkSwitchEnhance.logD("get----action: " + action);
                if (action.equals(OppoWifiNetworkSwitchEnhance.ACTION_WIFI_NETWORK_CONNECT)) {
                    WifiInfo actionConnectInfo = OppoWifiNetworkSwitchEnhance.this.mWifiStateMachine.syncRequestConnectionInfo();
                    String curSsid = intent.getStringExtra("ssid");
                    if (actionConnectInfo != null && curSsid != null && curSsid.equals(actionConnectInfo.getSSID())) {
                        OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance2 = OppoWifiNetworkSwitchEnhance.this;
                        oppoWifiNetworkSwitchEnhance2.logD("conn ssid" + curSsid + ", current ssid: " + actionConnectInfo.getSSID());
                        OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance3 = OppoWifiNetworkSwitchEnhance.this;
                        boolean unused = oppoWifiNetworkSwitchEnhance3.mIsSoftAP = oppoWifiNetworkSwitchEnhance3.isSoftAp((LinkProperties) intent.getExtra(OppoWifiNetworkSwitchEnhance.EXTRA_WIFI_LINK));
                        String unused2 = OppoWifiNetworkSwitchEnhance.this.mLastSSID = curSsid;
                    }
                } else if (action.equals(OppoWifiNetworkSwitchEnhance.ACTION_WIFI_NETWORK_STATE)) {
                    OppoWifiNetworkSwitchEnhance.this.mHandler.sendMessage(OppoWifiNetworkSwitchEnhance.this.mHandler.obtainMessage(2000, intent));
                } else if (action.equals("EVENT_NETWORK_AVAILABLE") || action.equals("EVENT_NETWORK_NOT_AVAILABLE")) {
                } else {
                    if (action.equals("android.net.wifi.STATE_CHANGE")) {
                        NetworkInfo unused3 = OppoWifiNetworkSwitchEnhance.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if ((OppoWifiNetworkSwitchEnhance.this.mNetworkInfo == null || OppoWifiNetworkSwitchEnhance.this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) && OppoWifiNetworkSwitchEnhance.this.mCaptivePortal) {
                            boolean unused4 = OppoWifiNetworkSwitchEnhance.this.mCaptivePortal = false;
                        }
                        OppoWifiNetworkSwitchEnhance.this.mNetworkLinkMonitor.sendMessage(3000, intent);
                    } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                        if (intent.getIntExtra("wifi_state", 1) == 1) {
                            boolean unused5 = OppoWifiNetworkSwitchEnhance.this.mInitAutoConnect = true;
                            boolean unused6 = OppoWifiNetworkSwitchEnhance.this.mIsSoftAP = false;
                        }
                    } else if (action.equals("android.intent.action.SCREEN_ON")) {
                        OppoWifiNetworkSwitchEnhance.this.mNetworkLinkMonitor.sendMessage(OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_ON);
                    } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                        OppoWifiNetworkSwitchEnhance.this.mNetworkLinkMonitor.sendMessage(OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_OFF);
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, netWorkFilter);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mDebug = true;
        } else {
            this.mDebug = false;
        }
    }

    public void reportRssi() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(EVENT_DETECT_RSSI, 0, 0));
    }

    public void removeNetwork(int netId) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(EVENT_REOVE_UPDATE_NETWORK, Integer.valueOf(netId)));
    }

    public void setManualConnect(boolean isManualConnect) {
        this.mManualConnect = isManualConnect;
    }

    private boolean getIsOppoManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnect();
        }
        return false;
    }

    public void setAutoSwitch(boolean isAutoSwitch) {
        logD("setAutoSwitch: " + isAutoSwitch);
        this.mWifiConfigManager.setWifiAutoSwitch(isAutoSwitch);
        this.mAutoSwitch = isAutoSwitch;
    }

    public void setFeature(boolean enable) {
        logD("sf=" + enable);
        this.mFeature = enable;
        if (!this.mFeature) {
            BroadcastReceiver broadcastReceiver = this.mBroadcastReceiver;
            if (broadcastReceiver != null) {
                this.mContext.unregisterReceiver(broadcastReceiver);
            }
            this.mWifiConfigManager.setWifiNetwork(null);
            return;
        }
        this.mWifiConfigManager.setWifiNetwork(this);
    }

    public void setInterfaceName(String interfaceName) {
        this.mInterfaceName = interfaceName;
    }

    public void detectScanResult(long time) {
        updateSortConfigByRssi();
        this.mLastScanTime = time;
        WifiInfo scanCurrentInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
        String currentSsid = scanCurrentInfo != null ? scanCurrentInfo.getSSID() : " ";
        ClientModeImpl clientModeImpl = this.mWifiStateMachine;
        if (clientModeImpl == null || clientModeImpl.syncGetWifiState() != 3) {
            logE("wifi is not enable.");
        } else if (!this.mIsSoftAP) {
            NetworkInfo networkInfo = this.mNetworkInfo;
            if (networkInfo == null || networkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED || scanCurrentInfo == null || !detectSSID(currentSsid, scanCurrentInfo.getNetworkId())) {
                StringBuilder sb = new StringBuilder();
                sb.append("detectScanResult currentSsid: ");
                sb.append(currentSsid);
                sb.append(",DetailedState: ");
                NetworkInfo networkInfo2 = this.mNetworkInfo;
                sb.append(networkInfo2 != null ? networkInfo2.getDetailedState() : null);
                logD(sb.toString());
                autoConnectAP(2, -100, currentSsid);
                return;
            }
            logD("current ssid: " + scanCurrentInfo.getSSID());
        }
    }

    public List<WifiConfiguration> getValidSortConfigByRssi() {
        List<WifiConfiguration> mSortConfig = new ArrayList<>();
        synchronized (this.mSortWifiConfig) {
            for (WifiConfiguration config : this.mSortWifiConfig) {
                mSortConfig.add(new WifiConfiguration(config));
            }
        }
        return mSortConfig;
    }

    private void updateSortConfigByRssi() {
        List<ScanResult> currentScan = this.mScanResultsProxy.syncGetScanResultsList();
        List<NetworkAvailableConfig> mSortValidConfig = new ArrayList<>();
        synchronized (this.mNetworkAvailables) {
            for (NetworkAvailableConfig nc : this.mNetworkAvailables) {
                int referRssi = WifiConfiguration.INVALID_RSSI;
                for (ScanResult result : currentScan) {
                    String ssid = "\"" + result.SSID + "\"";
                    String str = result.BSSID;
                    String capabilitie = result.capabilities;
                    if (ssid.equals(nc.mSsid) && matchKeymgmt(nc.mKeymgmt, capabilitie) && result.level > referRssi) {
                        referRssi = result.level;
                    }
                }
                int unused = nc.mRssi = referRssi;
                if (referRssi != WifiConfiguration.INVALID_RSSI) {
                    mSortValidConfig.add(nc);
                }
            }
        }
        Collections.sort(mSortValidConfig, new Comparator<NetworkAvailableConfig>() {
            /* class com.android.server.wifi.OppoWifiNetworkSwitchEnhance.AnonymousClass2 */

            public int compare(NetworkAvailableConfig b1, NetworkAvailableConfig b2) {
                return b2.mRssi - b1.mRssi;
            }
        });
        synchronized (this.mSortWifiConfig) {
            this.mSortWifiConfig.clear();
            for (NetworkAvailableConfig ms1 : mSortValidConfig) {
                String configKey = getConfigKey(ms1);
                WifiConfiguration temp = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                if (temp != null) {
                    logD("getSortNetwokConfByRssi temp = " + configKey + ", id=" + temp.networkId + ", state= " + temp.status);
                    this.mSortWifiConfig.add(temp);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void detectRssi(boolean hasLossPkt) {
        String str;
        String str2;
        WifiInfo rssiCurrentInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
        long nowMs = System.currentTimeMillis();
        if (rssiCurrentInfo != null) {
            int currentId = rssiCurrentInfo.getNetworkId();
            int currentRssi = rssiCurrentInfo.getRssi();
            this.mNewSsid = rssiCurrentInfo.getSSID();
            this.mNewBssid = rssiCurrentInfo.getBSSID();
            String str3 = this.mNewSsid;
            if (str3 != null && detectSSID(str3, currentId)) {
                logD("detectRssi: currentRssi= " + currentRssi + ",mNewSsid= " + this.mNewSsid + ", mNewBssid= " + this.mNewBssid + ",mOldSsid= " + this.mOldSsid + ", mOldBssid= " + this.mOldBssid);
                String str4 = this.mOldSsid;
                if (str4 == null || (str2 = this.mNewSsid) == null || str2.equals(str4)) {
                    String str5 = this.mOldBssid;
                    if (!(str5 == null || (str = this.mNewBssid) == null || str.equals(str5))) {
                        this.mOldBssid = this.mNewBssid;
                        this.mDetectTime = 0;
                        this.mLossPktTime = 0;
                        this.mHandler.removeMessages(EVENT_AUTO_CONNECT_AP);
                    }
                } else {
                    this.mOldSsid = this.mNewSsid;
                    this.mOldBssid = this.mNewBssid;
                    this.mDetectTime = 0;
                    this.mLossPktTime = 0;
                    this.mHandler.removeMessages(EVENT_AUTO_CONNECT_AP);
                }
                if (currentRssi >= LOW_RSSI || currentRssi <= -127) {
                    this.mHandler.removeMessages(EVENT_AUTO_CONNECT_AP);
                    this.mDetectTime = 0;
                } else {
                    this.mDetectTime++;
                }
                if (hasLossPkt) {
                    this.mLossPktTime++;
                } else {
                    this.mLossPktTime = 0;
                }
                if ((this.mDetectTime > 1 || this.mLossPktTime > 1) && nowMs - this.mLastScanTime > SCAN_RESULT_AGE) {
                    this.mScanResultsProxy.startScan(1000, ClientModeImpl.WIFI_PACKEG_NAME);
                }
                if (this.mDetectTime <= 2 && this.mLossPktTime <= 2) {
                    return;
                }
                if (detectInEss(this.mNewSsid, this.mNewBssid, currentId, currentRssi)) {
                    Handler handler = this.mHandler;
                    handler.sendMessageDelayed(handler.obtainMessage(EVENT_AUTO_CONNECT_AP, 1, currentRssi, this.mNewSsid), RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
                    return;
                }
                autoConnectAP(1, currentRssi, this.mNewSsid);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r6 = com.android.server.wifi.OppoWifiNetworkSwitchEnhance.NetworkAvailableConfig.access$1200(r13);
     */
    private boolean detectInEss(String ssid, String bssid, int netId, int rssi) {
        boolean willRoam = false;
        List<ScanResult> scanList = this.mScanResultsProxy.syncGetScanResultsList();
        if (ssid != null) {
            if (scanList != null) {
                Iterator<ScanResult> it = scanList.iterator();
                String key = " ";
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ScanResult result = it.next();
                    String scanSsid = "\"" + result.SSID + "\"";
                    String scanBssid = result.BSSID;
                    int delta = result.level - rssi;
                    if (scanSsid.equals(ssid) && delta > 5) {
                        if (!scanBssid.equals(bssid)) {
                            if (this.mNetworkAvailables.size() > 0) {
                                synchronized (this.mNetworkAvailables) {
                                    try {
                                        Iterator<NetworkAvailableConfig> it2 = this.mNetworkAvailables.iterator();
                                        while (true) {
                                            if (!it2.hasNext()) {
                                                break;
                                            }
                                            NetworkAvailableConfig network = it2.next();
                                            if (network.mSsid.equals(ssid)) {
                                                if (network.mNetid == netId) {
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        throw th;
                                    }
                                }
                            }
                            if (matchKeymgmt(key, result.capabilities)) {
                                willRoam = true;
                                break;
                            }
                        }
                    }
                }
                logD("detectInEss: willRoam " + willRoam);
                return willRoam;
            }
        }
        return false;
    }

    private boolean detectSSID(String ssid, int netId) {
        boolean contain = false;
        if (ssid == null) {
            return false;
        }
        WifiConfiguration detectconfig = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (detectconfig == null) {
            logE("get [id:" + netId + "] config is null");
            return false;
        }
        synchronized (this.mNetworkAvailables) {
            if (this.mNetworkAvailables.size() > 0) {
                Iterator<NetworkAvailableConfig> it = this.mNetworkAvailables.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    NetworkAvailableConfig network = it.next();
                    if (network.mSsid.equals(ssid) && detectconfig.SSID != null && network.mSsid.equals(detectconfig.SSID) && network.mKeymgmt.equals(parseKeymgmt(detectconfig))) {
                        contain = true;
                        break;
                    }
                }
            }
        }
        logD("detectSSID: return = " + contain);
        return contain;
    }

    /* access modifiers changed from: private */
    public WifiConfiguration getWifiConfig(String ssid, String bssid) {
        if (ssid == null || bssid == null) {
            return null;
        }
        WifiConfiguration currentconfig = null;
        Iterator<ScanResult> it = this.mScanResultsProxy.syncGetScanResultsList().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ScanResult result = it.next();
            String scanSsid = "\"" + result.SSID + "\"";
            String scanBssid = result.BSSID;
            String str = result.capabilities;
            if (scanSsid.equals(ssid) && scanBssid.equals(bssid)) {
                String configKey = WifiConfiguration.configKey(result);
                logE("getWifiConfig configKey= " + configKey);
                currentconfig = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                break;
            }
        }
        if (currentconfig != null) {
            logD("getWifiConfig currentconfig: " + currentconfig.networkId + ",SSID:" + currentconfig.SSID + ",BSSID:" + currentconfig.BSSID);
            if (currentconfig.BSSID == null || currentconfig.BSSID.equals("any")) {
                currentconfig.BSSID = bssid;
            }
        }
        return currentconfig;
    }

    /* access modifiers changed from: private */
    public void updateNetworkAvailables(WifiConfiguration wfg, int rssi, boolean add) {
        List<NetworkAvailableConfig> updateNetworks = new ArrayList<>();
        if (wfg == null) {
            logE("wfg is null, do nothing");
        } else if (wfg.SSID == null || wfg.BSSID == null) {
            logE("wfg SSID: " + wfg.SSID + " BSSID: " + wfg.BSSID);
        } else {
            synchronized (this.mNetworkAvailables) {
                try {
                    for (NetworkAvailableConfig network : this.mNetworkAvailables) {
                        if (wfg.SSID.equals(network.mSsid) && parseKeymgmt(wfg).equals(network.mKeymgmt)) {
                            updateNetworks.add(network);
                        }
                    }
                    boolean remove = false;
                    for (NetworkAvailableConfig mUpdateNetworks : updateNetworks) {
                        try {
                            this.mNetworkAvailables.remove(mUpdateNetworks);
                            remove = true;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                    updateNetworks.clear();
                    logD("remove networks= " + remove + ", add networks = " + add);
                    if (remove || add) {
                        if (add) {
                            WifiConfiguration networks = this.mWifiConfigManager.getConfiguredNetwork(wfg.configKey());
                            if (networks != null) {
                                this.mNetworkAvailables.add(new NetworkAvailableConfig(networks.networkId, rssi, networks.SSID, networks.BSSID, parseKeymgmt(networks)));
                            } else {
                                logE("getConfig is null");
                            }
                        }
                        updateAndWriteConfig();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }
    }

    public void removeNetworkAvailable(int netId) {
        List<NetworkAvailableConfig> removeNetworks = new ArrayList<>();
        WifiConfiguration rmconfig = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (rmconfig != null) {
            synchronized (this.mNetworkAvailables) {
                if (this.mNetworkAvailables.size() > 0) {
                    for (NetworkAvailableConfig network : this.mNetworkAvailables) {
                        if (rmconfig.SSID != null && network.mSsid.equals(rmconfig.SSID) && network.mKeymgmt.equals(parseKeymgmt(rmconfig))) {
                            removeNetworks.add(network);
                        }
                    }
                    for (NetworkAvailableConfig mRemoveNetworks : removeNetworks) {
                        logD("removeNetworkAvailable, network.mSsid= " + mRemoveNetworks.mSsid);
                        this.mNetworkAvailables.remove(mRemoveNetworks);
                    }
                    removeNetworks.clear();
                    updateAndWriteConfig();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isSoftAp(LinkProperties lp) {
        if (lp == null) {
            logE("LinkProperties is null, return");
            return false;
        }
        InetAddress mCurrentGateway = null;
        for (RouteInfo route : lp.getRoutes()) {
            if (route.hasGateway()) {
                mCurrentGateway = route.getGateway();
            }
        }
        if (mCurrentGateway == null) {
            logE("InetAddress getGateway is null, return");
            return false;
        }
        logD("mCurrentGateway : " + mCurrentGateway.toString());
        if (mCurrentGateway.toString().equals("/192.168.43.1") || mCurrentGateway.toString().equals("/172.20.10.1")) {
            return true;
        }
        return false;
    }

    private String inStream2String(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
            int len = is.read(buf);
            if (len == -1) {
                return new String(baos.toByteArray());
            }
            baos.write(buf, 0, len);
        }
    }

    /* access modifiers changed from: private */
    public void autoConnectAP(int autoType, int rssi, String currentssid) {
        int currentNetid;
        String ssid;
        String capabilitie;
        NetworkAvailableConfig nc;
        int bestRssi;
        int connectNetid;
        int bestRssi2;
        String str;
        String ssid2;
        String capabilitie2;
        int connectNetid2;
        int bestRssi3;
        StringBuilder sb;
        String aConfigKey;
        if (this.mWifiStateMachine.isDupDhcp()) {
            logD("[bug#1131400] waiting for net diags.");
            return;
        }
        int mNetworkConfigLen = this.mNetworkAvailables.size();
        int bestRssi4 = -100;
        int connectNetid3 = -1;
        boolean available = autoType == 1;
        List<ScanResult> scanResultsList = this.mScanResultsProxy.syncGetScanResultsList();
        if (this.mAutoSwitch) {
            if (this.mFeature) {
                if (scanResultsList != null) {
                    ClientModeImpl clientModeImpl = this.mWifiStateMachine;
                    if (clientModeImpl == null) {
                        return;
                    }
                    if (clientModeImpl.syncGetWifiState() == 3) {
                        WifiInfo autoConnectCurrentInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
                        if (autoConnectCurrentInfo != null) {
                            currentNetid = autoConnectCurrentInfo.getNetworkId();
                        } else {
                            currentNetid = -1;
                        }
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("autoConnectAP: ");
                        sb2.append(available ? "switch GOOD AP" : "switch VALID AP");
                        sb2.append(",currentNetid = ");
                        sb2.append(currentNetid);
                        logE(sb2.toString());
                        if (this.mAutoSwitch) {
                            synchronized (this.mNetworkAvailables) {
                                if (available) {
                                    try {
                                        Iterator<NetworkAvailableConfig> it = this.mNetworkAvailables.iterator();
                                        while (it.hasNext()) {
                                            NetworkAvailableConfig nc2 = it.next();
                                            if (currentssid != null) {
                                                try {
                                                    if (currentssid.equals(nc2.mSsid) && currentNetid == nc2.mNetid) {
                                                    }
                                                } catch (Throwable th) {
                                                    th = th;
                                                    throw th;
                                                }
                                            }
                                            for (ScanResult result : scanResultsList) {
                                                try {
                                                    ssid2 = "\"" + result.SSID + "\"";
                                                    String str2 = result.BSSID;
                                                    capabilitie2 = result.capabilities;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    throw th;
                                                }
                                                try {
                                                    if (!ssid2.equals(nc2.mSsid) || !matchKeymgmt(nc2.mKeymgmt, capabilitie2)) {
                                                        bestRssi3 = bestRssi4;
                                                        connectNetid2 = connectNetid3;
                                                    } else if (result.level <= bestRssi4) {
                                                        bestRssi3 = bestRssi4;
                                                        connectNetid2 = connectNetid3;
                                                    } else if (result.level - rssi > 5) {
                                                        String aConfigKey2 = WifiConfiguration.configKey(result);
                                                        WifiConfiguration aConfig = this.mWifiConfigManager.getConfiguredNetwork(aConfigKey2);
                                                        bestRssi3 = bestRssi4;
                                                        try {
                                                            sb = new StringBuilder();
                                                            connectNetid2 = connectNetid3;
                                                        } catch (Throwable th3) {
                                                            th = th3;
                                                            throw th;
                                                        }
                                                        try {
                                                            sb.append("a compare config key:");
                                                            sb.append(aConfigKey2);
                                                            sb.append(",aConfig: ");
                                                            if (aConfig != null) {
                                                                aConfigKey = aConfig.SSID + ",status = " + aConfig.status + ",id= " + aConfig.networkId;
                                                            } else {
                                                                aConfigKey = "null";
                                                            }
                                                            sb.append(aConfigKey);
                                                            logD(sb.toString());
                                                            if (!(aConfig == null || aConfig.SSID == null || !ssid2.equals(aConfig.SSID) || aConfig.status == 1)) {
                                                                bestRssi4 = result.level;
                                                                try {
                                                                    connectNetid3 = aConfig.networkId;
                                                                    it = it;
                                                                    mNetworkConfigLen = mNetworkConfigLen;
                                                                } catch (Throwable th4) {
                                                                    th = th4;
                                                                    throw th;
                                                                }
                                                            }
                                                        } catch (Throwable th5) {
                                                            th = th5;
                                                            throw th;
                                                        }
                                                    } else {
                                                        bestRssi3 = bestRssi4;
                                                        connectNetid2 = connectNetid3;
                                                    }
                                                    bestRssi4 = bestRssi3;
                                                    connectNetid3 = connectNetid2;
                                                    it = it;
                                                    mNetworkConfigLen = mNetworkConfigLen;
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                    throw th;
                                                }
                                            }
                                        }
                                    } catch (Throwable th7) {
                                        th = th7;
                                        throw th;
                                    }
                                } else {
                                    try {
                                        Iterator<NetworkAvailableConfig> it2 = this.mNetworkAvailables.iterator();
                                        while (it2.hasNext()) {
                                            NetworkAvailableConfig nc3 = it2.next();
                                            for (ScanResult result2 : scanResultsList) {
                                                try {
                                                    ssid = "\"" + result2.SSID + "\"";
                                                    String str3 = result2.BSSID;
                                                    capabilitie = result2.capabilities;
                                                } catch (Throwable th8) {
                                                    th = th8;
                                                    throw th;
                                                }
                                                try {
                                                    if (!ssid.equals(nc3.mSsid) || !matchKeymgmt(nc3.mKeymgmt, capabilitie)) {
                                                        nc = nc3;
                                                        bestRssi = bestRssi4;
                                                    } else if (result2.level > bestRssi4) {
                                                        String uConfigKey = WifiConfiguration.configKey(result2);
                                                        WifiConfiguration uConfig = this.mWifiConfigManager.getConfiguredNetwork(uConfigKey);
                                                        nc = nc3;
                                                        StringBuilder sb3 = new StringBuilder();
                                                        bestRssi = bestRssi4;
                                                        try {
                                                            sb3.append("u compare config key:");
                                                            sb3.append(uConfigKey);
                                                            sb3.append(",uConfig: ");
                                                            if (uConfig != null) {
                                                                str = uConfig.SSID + ",status = " + uConfig.status + ",id= " + uConfig.networkId;
                                                            } else {
                                                                str = "null";
                                                            }
                                                            sb3.append(str);
                                                            logD(sb3.toString());
                                                            if (!(uConfig == null || uConfig.SSID == null || !ssid.equals(uConfig.SSID) || uConfig.status == 1)) {
                                                                bestRssi2 = result2.level;
                                                                try {
                                                                    connectNetid = uConfig.networkId;
                                                                    it2 = it2;
                                                                    nc3 = nc;
                                                                } catch (Throwable th9) {
                                                                    th = th9;
                                                                    throw th;
                                                                }
                                                            }
                                                        } catch (Throwable th10) {
                                                            th = th10;
                                                            throw th;
                                                        }
                                                    } else {
                                                        nc = nc3;
                                                        bestRssi = bestRssi4;
                                                    }
                                                    connectNetid = connectNetid3;
                                                    bestRssi2 = bestRssi;
                                                    it2 = it2;
                                                    nc3 = nc;
                                                } catch (Throwable th11) {
                                                    th = th11;
                                                    throw th;
                                                }
                                            }
                                        }
                                        if (connectNetid3 == -1 && autoType == 0) {
                                            logE("ALERT! this is a ap cannot acess internet");
                                            showDialog(currentssid);
                                        }
                                    } catch (Throwable th12) {
                                        th = th12;
                                        throw th;
                                    }
                                }
                            }
                        } else {
                            logE("mAutoSwitch is off!");
                        }
                        if (connectNetid3 == -1 && (this.mInitAutoConnect || currentNetid == -1)) {
                            List<WifiConfiguration> mWificonfiguration = this.mWifiConfigManager.getSavedNetworks(1010);
                            List<WifiConfiguration> mToBeEnabledConfiguration = new ArrayList<>();
                            boolean mAllDisabled = true;
                            if (!getIsOppoManuConnect() && mWificonfiguration != null && mWificonfiguration.size() > 0) {
                                for (ScanResult sr : scanResultsList) {
                                    Iterator<WifiConfiguration> it3 = mWificonfiguration.iterator();
                                    while (true) {
                                        if (!it3.hasNext()) {
                                            break;
                                        }
                                        WifiConfiguration wc = it3.next();
                                        if (WifiConfiguration.configKey(sr).equals(wc.configKey())) {
                                            if (wc.status != 1) {
                                                mAllDisabled = false;
                                                break;
                                            }
                                            int level = this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_GOOD_RSSI_SWITCH_VALUE", Integer.valueOf((int) GOOD_RSSI_SWITCH_VALUE)).intValue();
                                            logD("switch value=" + level + ",sr.level=" + sr.level);
                                            if (!(wc.disableReason == 3 || wc.disableReason == 13 || sr.level < level)) {
                                                mToBeEnabledConfiguration.add(wc);
                                            }
                                            mWificonfiguration = mWificonfiguration;
                                        }
                                    }
                                    mWificonfiguration = mWificonfiguration;
                                }
                                if (mAllDisabled) {
                                    logD("all configured network are disabled!");
                                    if (mToBeEnabledConfiguration.size() > 0) {
                                        logD("enabled networks that not disabled by wrongkey or auth failure!");
                                        for (WifiConfiguration wc2 : mToBeEnabledConfiguration) {
                                            logD("enable netId:" + wc2.networkId + " SSID:" + wc2.SSID);
                                            this.mWifiConfigManager.enableNetwork(wc2.networkId, false, 1000);
                                        }
                                    }
                                }
                            }
                            this.mWifiNative.reconnect(this.mInterfaceName);
                            this.mInitAutoConnect = false;
                        }
                        if (connectNetid3 != -1 && currentNetid != connectNetid3) {
                            if (getIsOppoManuConnect()) {
                                logE("manual connect, do not auto connect");
                                return;
                            }
                            logE("auto conntect id:" + connectNetid3);
                            WifiConfiguration selectConf = this.mWifiConfigManager.getConfiguredNetwork(connectNetid3);
                            if (selectConf == null) {
                                logE("select config is null");
                                return;
                            } else if (this.mWifiStateMachine.isNetworkAutoConnectingOrConnected(selectConf.networkId)) {
                                logD("network: " + selectConf.SSID + " is connecting or connected, do nothing!");
                                return;
                            } else {
                                this.mWifiConfigManager.enableNetwork(selectConf.networkId, false, 1000);
                                this.mWifiStateMachine.prepareForForcedConnection(selectConf.networkId);
                                this.mWifiStateMachine.startConnectToNetwork(selectConf.networkId, 1000, "any");
                                this.mSupplicantTracker.sendMessage(131372);
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
        logE("mAutoSwitch is off!");
    }

    private void showDialog(String ssid) {
        logD("showDialog mUnavailableSsid: " + this.mUnavailableSsid);
        String str = this.mUnavailableSsid;
        if (str == null || !str.equals(ssid)) {
            NetworkInfo networkInfo = this.mNetworkInfo;
            if (networkInfo == null || networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                this.mUnavailableSsid = ssid;
                AlertDialog alertDialog = this.mAlertDialog;
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
                this.mAlertDialog = builder.setTitle(ssid + ((Object) this.mContext.getText(201653544))).setMessage(this.mContext.getText(201653540)).setPositiveButton(201653545, new DialogInterface.OnClickListener() {
                    /* class com.android.server.wifi.OppoWifiNetworkSwitchEnhance.AnonymousClass3 */

                    public void onClick(DialogInterface d, int w) {
                        if (OppoWifiNetworkSwitchEnhance.this.mAlertDialog != null) {
                            OppoWifiNetworkSwitchEnhance.this.mAlertDialog.dismiss();
                        }
                    }
                }).create();
                this.mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    /* class com.android.server.wifi.OppoWifiNetworkSwitchEnhance.AnonymousClass4 */

                    public void onDismiss(DialogInterface dialog) {
                        AlertDialog unused = OppoWifiNetworkSwitchEnhance.this.mAlertDialog = null;
                        OppoWifiCommonUtil.disableStatusBar(OppoWifiNetworkSwitchEnhance.this.mContext, false);
                    }
                });
                OppoWifiCommonUtil.disableStatusBar(this.mContext, true);
                this.mAlertDialog.getWindow().setType(EVENT_REOVE_UPDATE_NETWORK);
                this.mAlertDialog.getWindow().addFlags(2);
                WindowManager.LayoutParams p = this.mAlertDialog.getWindow().getAttributes();
                p.privateFlags = 16;
                this.mAlertDialog.getWindow().setAttributes(p);
                this.mAlertDialog.show();
                TextView msg = (TextView) this.mAlertDialog.findViewById(16908299);
                if (msg != null) {
                    msg.setGravity(17);
                } else {
                    logE("textview is null");
                }
            } else {
                logD("not CONNECTED,so shouldn't showDialog for the ap can't go internet");
            }
        } else {
            logE("name[" + ssid + "] is same");
        }
    }

    private boolean matchKeymgmt(String validKey, String scanKey) {
        if (validKey == null || scanKey == null) {
            return false;
        }
        char c = 65535;
        switch (validKey.hashCode()) {
            case -2038298883:
                if (validKey.equals(SECURITY_EAP)) {
                    c = 2;
                    break;
                }
                break;
            case -2038287759:
                if (validKey.equals(SECURITY_PSK)) {
                    c = 0;
                    break;
                }
                break;
            case -850615648:
                if (validKey.equals(SECURITY_WAPI_CERT)) {
                    c = 5;
                    break;
                }
                break;
            case 85826:
                if (validKey.equals(SECURITY_WEP)) {
                    c = 6;
                    break;
                }
                break;
            case 2402104:
                if (validKey.equals(SECURITY_NONE)) {
                    c = 7;
                    break;
                }
                break;
            case 36491973:
                if (validKey.equals("IEEE8021X")) {
                    c = 3;
                    break;
                }
                break;
            case 1196474771:
                if (validKey.equals(SECURITY_WPA2_PSK)) {
                    c = 1;
                    break;
                }
                break;
            case 1219499692:
                if (validKey.equals(SECURITY_WAPI_PSK)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
                if (scanKey.contains("WPA-PSK") || scanKey.contains("WPA2-PSK")) {
                    return true;
                }
                return false;
            case 2:
                if (scanKey.contains("EAP")) {
                    return true;
                }
                return false;
            case 3:
                if (scanKey.contains("IEEE8021X")) {
                    return true;
                }
                return false;
            case 4:
                if (scanKey.contains("WAPI-KEY") || scanKey.contains("WAPI-PSK")) {
                    return true;
                }
                return false;
            case 5:
                if (scanKey.contains("WAPI-CERT")) {
                    return true;
                }
                return false;
            case 6:
                if (scanKey.contains(SECURITY_WEP)) {
                    return true;
                }
                return false;
            case 7:
                if (scanKey.contains("PSK") || scanKey.contains("EAP") || scanKey.contains(SECURITY_WEP) || scanKey.contains("WAPI") || scanKey.contains("IEEE8021X")) {
                    return false;
                }
                return true;
            default:
                logE("matchKeymgmt default");
                return false;
        }
    }

    private String parseKeymgmt(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return SECURITY_EAP;
        }
        if (config.allowedKeyManagement.get(13)) {
            return SECURITY_WAPI_PSK;
        }
        if (config.allowedKeyManagement.get(14)) {
            return SECURITY_WAPI_CERT;
        }
        if (config.wepTxKeyIndex >= 0 && config.wepTxKeyIndex < config.wepKeys.length && config.wepKeys[config.wepTxKeyIndex] != null) {
            return SECURITY_WEP;
        }
        if (config.allowedKeyManagement.get(4)) {
            return SECURITY_WPA2_PSK;
        }
        return SECURITY_NONE;
    }

    private String getConfigKey(NetworkAvailableConfig nc) {
        if (nc == null) {
            return null;
        }
        String key = nc.mSsid;
        String access$1200 = nc.mKeymgmt;
        char c = 65535;
        switch (access$1200.hashCode()) {
            case -2038298883:
                if (access$1200.equals(SECURITY_EAP)) {
                    c = 2;
                    break;
                }
                break;
            case -2038287759:
                if (access$1200.equals(SECURITY_PSK)) {
                    c = 0;
                    break;
                }
                break;
            case -850615648:
                if (access$1200.equals(SECURITY_WAPI_CERT)) {
                    c = 5;
                    break;
                }
                break;
            case 85826:
                if (access$1200.equals(SECURITY_WEP)) {
                    c = 6;
                    break;
                }
                break;
            case 2402104:
                if (access$1200.equals(SECURITY_NONE)) {
                    c = 7;
                    break;
                }
                break;
            case 36491973:
                if (access$1200.equals("IEEE8021X")) {
                    c = 3;
                    break;
                }
                break;
            case 1196474771:
                if (access$1200.equals(SECURITY_WPA2_PSK)) {
                    c = 1;
                    break;
                }
                break;
            case 1219499692:
                if (access$1200.equals(SECURITY_WAPI_PSK)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
                return nc.mSsid + "-" + SECURITY_PSK;
            case 2:
            case 3:
                return nc.mSsid + "-" + SECURITY_EAP;
            case 4:
                return nc.mSsid + "-" + SECURITY_WAPI_PSK;
            case 5:
                return nc.mSsid + "-" + SECURITY_WAPI_CERT;
            case 6:
                return nc.mSsid + "-" + SECURITY_WEP;
            case 7:
                return nc.mSsid + "-" + SECURITY_NONE;
            default:
                return key;
        }
    }

    private void updateAndWriteConfig() {
        FileWriter config = null;
        BufferedWriter out = null;
        try {
            FileWriter config2 = new FileWriter(WIFI_NETWORK_AVAILABLE);
            BufferedWriter out2 = new BufferedWriter(config2);
            for (NetworkAvailableConfig na : this.mNetworkAvailables) {
                out2.write((na.mNetid + "\t" + na.mBssid + "\t" + na.mKeymgmt + "\t" + na.mSsid) + "\n");
                out2.flush();
            }
            out2.close();
            try {
                out2.close();
            } catch (IOException e) {
            }
            try {
                config2.close();
            } catch (IOException e2) {
            }
        } catch (FileNotFoundException e3) {
            logE("FileNotFoundException: " + e3);
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                }
            }
            if (config != null) {
                config.close();
            }
        } catch (IOException e5) {
            logE("IOException: " + e5);
            e5.printStackTrace();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e6) {
                }
            }
            if (config != null) {
                config.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e7) {
                }
            }
            if (config != null) {
                try {
                    config.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x013f A[SYNTHETIC, Splitter:B:49:0x013f] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x015d  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0165 A[SYNTHETIC, Splitter:B:59:0x0165] */
    public void readConfigAndUpdate() {
        Throwable th;
        int id;
        BufferedReader reader = null;
        if (this.mNetworkAvailables == null) {
            logE("networkAvailables exception, re-init it");
            this.mNetworkAvailables = new ArrayList();
        }
        synchronized (this.mNetworkAvailables) {
            try {
                this.mNetworkAvailables.clear();
                try {
                    BufferedReader reader2 = new BufferedReader(new FileReader(WIFI_NETWORK_AVAILABLE));
                    while (true) {
                        try {
                            String line = reader2.readLine();
                            if (line == null) {
                                try {
                                    break;
                                } catch (IOException e) {
                                }
                            } else if (line.split("\t").length == 4) {
                                int idIndex = line.indexOf(9);
                                if (idIndex >= 0) {
                                    try {
                                        id = Integer.parseInt(line.substring(0, idIndex));
                                    } catch (NumberFormatException e2) {
                                        logD("NumberFormatException e:" + e2);
                                        id = -1;
                                    }
                                    String bssidString = line.substring(idIndex + 1, line.length());
                                    int bssidIndex = bssidString.indexOf(9);
                                    if (bssidIndex >= 0) {
                                        String bssid = bssidString.substring(0, bssidIndex);
                                        String keymgmtString = bssidString.substring(bssidIndex + 1, bssidString.length());
                                        int keymgmtIndex = keymgmtString.indexOf(9);
                                        if (keymgmtIndex >= 0) {
                                            if (!keymgmtString.startsWith("\"")) {
                                                String keyMgmt = keymgmtString.substring(0, keymgmtIndex);
                                                String ssid = keymgmtString.substring(keymgmtIndex + 1, keymgmtString.length());
                                                logD("readConfigAndUpdate: id= " + id + ",bssid= " + bssid + ",keyMgmt= " + keyMgmt + ",ssid= " + ssid);
                                                this.mNetworkAvailables.add(new NetworkAvailableConfig(id, WifiConfiguration.INVALID_RSSI, ssid, bssid, keyMgmt));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (FileNotFoundException e3) {
                            e = e3;
                            reader = reader2;
                            logE("readConfigAndUpdate: FileNotFoundException: " + e);
                            if (reader != null) {
                            }
                        } catch (IOException e4) {
                            e = e4;
                            reader = reader2;
                            try {
                                logE("readConfigAndUpdate: IOException: " + e);
                                if (reader != null) {
                                }
                            } catch (Throwable th2) {
                                reader2 = reader;
                                th = th2;
                                if (reader2 != null) {
                                    try {
                                        reader2.close();
                                    } catch (IOException e5) {
                                    }
                                }
                                try {
                                    throw th;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            if (reader2 != null) {
                            }
                            throw th;
                        }
                    }
                    reader2.close();
                } catch (FileNotFoundException e6) {
                    e = e6;
                    logE("readConfigAndUpdate: FileNotFoundException: " + e);
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e7) {
                    e = e7;
                    logE("readConfigAndUpdate: IOException: " + e);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e8) {
                        }
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                throw th;
            }
        }
    }

    private final class H extends Handler {
        public H(Looper lp) {
            super(lp);
        }

        public void handleMessage(Message msg) {
            WifiInfo handlerCurrentInfo = OppoWifiNetworkSwitchEnhance.this.mWifiStateMachine.syncRequestConnectionInfo();
            OppoWifiNetworkSwitchEnhance.this.logD("handleMessage handlerCurrentInfo: " + handlerCurrentInfo);
            boolean hasLossPkt = true;
            switch (msg.what) {
                case 2000:
                    Intent available = (Intent) msg.obj;
                    if (!OppoWifiNetworkSwitchEnhance.this.mIsSoftAP && available != null && handlerCurrentInfo != null && !"<unknown ssid>".equals(handlerCurrentInfo.getSSID())) {
                        String avaSsid = available.getStringExtra("ssid");
                        OppoWifiNetworkSwitchEnhance.this.logD("EVENT_ADD_UPDATE_NETWORK avaSsid" + avaSsid + ", getcurrent ssid: " + handlerCurrentInfo.getSSID());
                        if (avaSsid != null && avaSsid.equals(handlerCurrentInfo.getSSID())) {
                            boolean netValid = available.getBooleanExtra(OppoWifiNetworkSwitchEnhance.EXTRA_NETWORK_STATE, false);
                            if (netValid || !OppoWifiNetworkSwitchEnhance.this.mWifiStateMachine.isDupDhcp()) {
                                OppoWifiNetworkSwitchEnhance.this.updateNetworkAvailables(OppoWifiNetworkSwitchEnhance.this.getWifiConfig(avaSsid, handlerCurrentInfo.getBSSID()), handlerCurrentInfo.getRssi(), netValid);
                                if (!netValid) {
                                    OppoWifiNetworkSwitchEnhance.this.autoConnectAP(0, -100, avaSsid);
                                }
                                boolean unused = OppoWifiNetworkSwitchEnhance.this.mCaptivePortal = true;
                                String unused2 = OppoWifiNetworkSwitchEnhance.this.mLastSSID = avaSsid;
                                return;
                            }
                            OppoWifiNetworkSwitchEnhance.this.logD("[bug#1131400] wait for net diags.");
                            return;
                        }
                        return;
                    }
                    return;
                case OppoWifiNetworkSwitchEnhance.EVENT_AUTO_CONNECT_AP /*{ENCODED_INT: 2001}*/:
                    OppoWifiNetworkSwitchEnhance.this.autoConnectAP(msg.arg1, msg.arg2, (String) msg.obj);
                    return;
                case OppoWifiNetworkSwitchEnhance.EVENT_NETWORK_NOT_AVAILABLE /*{ENCODED_INT: 2002}*/:
                    Intent unavailable = (Intent) msg.obj;
                    if (unavailable != null && handlerCurrentInfo != null && !"<unknown ssid>".equals(handlerCurrentInfo.getSSID())) {
                        String unavaSsid = unavailable.getStringExtra("ssid");
                        OppoWifiNetworkSwitchEnhance.this.logD("EVENT_NETWORK_NOT_AVAILABLE removeSsid = " + unavaSsid + ", getcurrent ssid: " + handlerCurrentInfo.getSSID());
                        if (unavaSsid != null && unavaSsid.equals(handlerCurrentInfo.getSSID())) {
                            OppoWifiNetworkSwitchEnhance.this.updateNetworkAvailables(OppoWifiNetworkSwitchEnhance.this.getWifiConfig(unavaSsid, handlerCurrentInfo.getBSSID()), WifiConfiguration.INVALID_RSSI, false);
                            boolean unused3 = OppoWifiNetworkSwitchEnhance.this.mCaptivePortal = true;
                            String unused4 = OppoWifiNetworkSwitchEnhance.this.mLastSSID = unavaSsid;
                            if (!OppoWifiNetworkSwitchEnhance.this.mIsSoftAP) {
                                OppoWifiNetworkSwitchEnhance.this.autoConnectAP(0, -100, unavaSsid);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                case OppoWifiNetworkSwitchEnhance.EVENT_REOVE_UPDATE_NETWORK /*{ENCODED_INT: 2003}*/:
                    if (msg.obj != null) {
                        int netd = ((Integer) msg.obj).intValue();
                        OppoWifiNetworkSwitchEnhance.this.logD("EVENT_REOVE_UPDATE_NETWORK: netid= " + netd);
                        OppoWifiNetworkSwitchEnhance.this.removeNetworkAvailable(netd);
                        return;
                    }
                    return;
                case OppoWifiNetworkSwitchEnhance.EVENT_DETECT_RSSI /*{ENCODED_INT: 2004}*/:
                    if (msg.arg1 != 1) {
                        hasLossPkt = false;
                    }
                    OppoWifiNetworkSwitchEnhance.this.detectRssi(hasLossPkt);
                    return;
                default:
                    return;
            }
        }
    }

    private class NetworkAvailableConfig {
        /* access modifiers changed from: private */
        public String mBssid;
        /* access modifiers changed from: private */
        public String mKeymgmt;
        /* access modifiers changed from: private */
        public int mNetid;
        /* access modifiers changed from: private */
        public int mRssi;
        /* access modifiers changed from: private */
        public String mSsid;

        public NetworkAvailableConfig(int netid, int rssi, String ssid, String bssid, String keymgmt) {
            this.mNetid = netid;
            this.mRssi = rssi;
            this.mSsid = ssid;
            this.mBssid = bssid;
            this.mKeymgmt = keymgmt;
        }
    }

    /* access modifiers changed from: private */
    public class NetworkLinkMonitor extends StateMachine {
        private static final double EXP_COEFFICIENT_MONITOR = 0.5d;
        private static final int LINK_SAMPLING_INTERVAL_MS = 1000;
        private static final double POOR_LINK_LOSS_THRESHOLD = 0.5d;
        private static final double POOR_LINK_MIN_VOLUME = 2.0d;
        private static final int POOR_LINK_SAMPLE_COUNT = 2;
        /* access modifiers changed from: private */
        public ConnectedState mConnectedState = new ConnectedState();
        /* access modifiers changed from: private */
        public VolumeWeightedEMA mCurrentLoss;
        private DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public DisConnectedState mDisConnectedState = new DisConnectedState();
        /* access modifiers changed from: private */
        public boolean mIsScreenOn = true;
        /* access modifiers changed from: private */
        public LinkMonitoringState mLinkMonitoringState = new LinkMonitoringState();
        /* access modifiers changed from: private */
        public int mRssiFetchToken = 0;
        /* access modifiers changed from: private */
        public AsyncChannel mWsmChannel = new AsyncChannel();

        static /* synthetic */ int access$2704(NetworkLinkMonitor x0) {
            int i = x0.mRssiFetchToken + 1;
            x0.mRssiFetchToken = i;
            return i;
        }

        public NetworkLinkMonitor(Context context, Messenger dstMessenger) {
            super("NetworkLinkMonitor");
            this.mWsmChannel.connectSync(OppoWifiNetworkSwitchEnhance.this.mContext, getHandler(), dstMessenger);
            addState(this.mDefaultState);
            addState(this.mDisConnectedState, this.mDefaultState);
            addState(this.mConnectedState, this.mDefaultState);
            addState(this.mLinkMonitoringState, this.mConnectedState);
            setInitialState(this.mDefaultState);
        }

        class DefaultState extends State {
            DefaultState() {
            }

            public void enter() {
                OppoWifiNetworkSwitchEnhance.this.logD(getName());
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 3000:
                        if (OppoWifiNetworkSwitchEnhance.this.mNetworkInfo != null) {
                            OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = OppoWifiNetworkSwitchEnhance.this;
                            oppoWifiNetworkSwitchEnhance.logD("Network state change " + OppoWifiNetworkSwitchEnhance.this.mNetworkInfo.getDetailedState());
                            int i = AnonymousClass5.$SwitchMap$android$net$NetworkInfo$DetailedState[OppoWifiNetworkSwitchEnhance.this.mNetworkInfo.getDetailedState().ordinal()];
                            if (i != 1) {
                                if (i == 2) {
                                    String unused = OppoWifiNetworkSwitchEnhance.this.mUnavailableSsid = " ";
                                    if (OppoWifiNetworkSwitchEnhance.this.mIsSoftAP) {
                                        boolean unused2 = OppoWifiNetworkSwitchEnhance.this.mIsSoftAP = false;
                                    }
                                    NetworkLinkMonitor networkLinkMonitor = NetworkLinkMonitor.this;
                                    networkLinkMonitor.transitionTo(networkLinkMonitor.mDisConnectedState);
                                    break;
                                } else {
                                    NetworkLinkMonitor networkLinkMonitor2 = NetworkLinkMonitor.this;
                                    networkLinkMonitor2.transitionTo(networkLinkMonitor2.mDisConnectedState);
                                    break;
                                }
                            } else {
                                if (OppoWifiNetworkSwitchEnhance.this.mHandler != null) {
                                    OppoWifiNetworkSwitchEnhance.this.mHandler.removeMessages(OppoWifiNetworkSwitchEnhance.EVENT_AUTO_CONNECT_AP);
                                }
                                NetworkLinkMonitor networkLinkMonitor3 = NetworkLinkMonitor.this;
                                networkLinkMonitor3.transitionTo(networkLinkMonitor3.mLinkMonitoringState);
                                break;
                            }
                        }
                        break;
                    case OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_ON /*{ENCODED_INT: 3001}*/:
                        boolean unused3 = NetworkLinkMonitor.this.mIsScreenOn = true;
                        break;
                    case OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_OFF /*{ENCODED_INT: 3002}*/:
                        boolean unused4 = NetworkLinkMonitor.this.mIsScreenOn = false;
                        break;
                }
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public class DisConnectedState extends State {
            DisConnectedState() {
            }

            public void enter() {
                OppoWifiNetworkSwitchEnhance.this.logD(getName());
            }
        }

        /* access modifiers changed from: package-private */
        public class ConnectedState extends State {
            ConnectedState() {
            }

            public void enter() {
                OppoWifiNetworkSwitchEnhance.this.logD(getName());
            }

            public boolean processMessage(Message msg) {
                if (msg.what != OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_ON) {
                    return false;
                }
                boolean unused = NetworkLinkMonitor.this.mIsScreenOn = true;
                NetworkLinkMonitor networkLinkMonitor = NetworkLinkMonitor.this;
                networkLinkMonitor.transitionTo(networkLinkMonitor.mLinkMonitoringState);
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public class LinkMonitoringState extends State {
            private int mLastRssi;
            private long mLastTimeSample;
            private int mLastTxBad;
            private int mLastTxGood;
            private int mSampleCount;

            LinkMonitoringState() {
            }

            public void enter() {
                OppoWifiNetworkSwitchEnhance.this.logD(getName());
                this.mSampleCount = 0;
                NetworkLinkMonitor networkLinkMonitor = NetworkLinkMonitor.this;
                VolumeWeightedEMA unused = networkLinkMonitor.mCurrentLoss = new VolumeWeightedEMA(0.5d);
                NetworkLinkMonitor networkLinkMonitor2 = NetworkLinkMonitor.this;
                networkLinkMonitor2.sendMessage(networkLinkMonitor2.obtainMessage(OppoWifiNetworkSwitchEnhance.CMD_RSSI_FETCH, NetworkLinkMonitor.access$2704(networkLinkMonitor2), 0));
            }

            /* JADX WARN: Type inference failed for: r2v19, types: [boolean, int] */
            /* JADX WARN: Type inference failed for: r2v22 */
            /* JADX WARN: Type inference failed for: r2v26 */
            public boolean processMessage(Message msg) {
                int txbad;
                ? r2;
                int i = msg.what;
                if (i != OppoWifiNetworkSwitchEnhance.CMD_RSSI_FETCH) {
                    switch (i) {
                        case 151573:
                            RssiPacketCountInfo info = (RssiPacketCountInfo) msg.obj;
                            if (info == null) {
                                return true;
                            }
                            int rssi = info.rssi;
                            int txbad2 = info.txbad;
                            int txgood = info.txgood;
                            OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = OppoWifiNetworkSwitchEnhance.this;
                            oppoWifiNetworkSwitchEnhance.logD("Fetch RSSI succeed, rssi=" + rssi + " mrssi=" + ((this.mLastRssi + rssi) / 2) + " txbad=" + txbad2 + " txgood=" + txgood);
                            long now = SystemClock.elapsedRealtime();
                            if (now - this.mLastTimeSample < 2000) {
                                int dbad = txbad2 - this.mLastTxBad;
                                int dtotal = dbad + (txgood - this.mLastTxGood);
                                OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance2 = OppoWifiNetworkSwitchEnhance.this;
                                oppoWifiNetworkSwitchEnhance2.logD("RSSI_PKTCNT_FETCH_SUCCEEDED--dtotal= " + dtotal);
                                if (dtotal > 0) {
                                    txbad = txbad2;
                                    NetworkLinkMonitor.this.mCurrentLoss.update(((double) dbad) / ((double) dtotal), dtotal);
                                    if (OppoWifiNetworkSwitchEnhance.this.mDebug) {
                                        DecimalFormat df = new DecimalFormat("#.####");
                                        OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance3 = OppoWifiNetworkSwitchEnhance.this;
                                        oppoWifiNetworkSwitchEnhance3.logD("Incremental loss=" + dbad + "/" + dtotal + " Current loss=" + df.format(NetworkLinkMonitor.this.mCurrentLoss.mValue) + " volume=" + df.format(NetworkLinkMonitor.this.mCurrentLoss.mVolume));
                                    }
                                    if (NetworkLinkMonitor.this.mCurrentLoss.mValue <= 0.5d) {
                                        r2 = 0;
                                    } else if (NetworkLinkMonitor.this.mCurrentLoss.mVolume > NetworkLinkMonitor.POOR_LINK_MIN_VOLUME) {
                                        int i2 = this.mSampleCount + 1;
                                        this.mSampleCount = i2;
                                        if (i2 >= 2) {
                                            boolean unused = OppoWifiNetworkSwitchEnhance.this.mLossPktDetect = true;
                                            OppoWifiNetworkSwitchEnhance.this.detectRssi(OppoWifiNetworkSwitchEnhance.this.mLossPktDetect);
                                            this.mSampleCount = 0;
                                        }
                                    } else {
                                        r2 = 0;
                                    }
                                    this.mSampleCount = r2;
                                    if (OppoWifiNetworkSwitchEnhance.this.mLossPktDetect) {
                                        boolean unused2 = OppoWifiNetworkSwitchEnhance.this.mLossPktDetect = r2;
                                        OppoWifiNetworkSwitchEnhance.this.detectRssi(OppoWifiNetworkSwitchEnhance.this.mLossPktDetect);
                                    }
                                } else {
                                    txbad = txbad2;
                                }
                            } else {
                                txbad = txbad2;
                            }
                            this.mLastTimeSample = now;
                            this.mLastTxBad = txbad;
                            this.mLastTxGood = txgood;
                            this.mLastRssi = rssi;
                            return true;
                        case 151574:
                            OppoWifiNetworkSwitchEnhance.this.logD("RSSI_FETCH_FAILED");
                            return true;
                        default:
                            return false;
                    }
                } else if (!NetworkLinkMonitor.this.mIsScreenOn) {
                    NetworkLinkMonitor networkLinkMonitor = NetworkLinkMonitor.this;
                    networkLinkMonitor.transitionTo(networkLinkMonitor.mConnectedState);
                    return true;
                } else if (msg.arg1 != NetworkLinkMonitor.this.mRssiFetchToken) {
                    return true;
                } else {
                    NetworkLinkMonitor.this.mWsmChannel.sendMessage(151572);
                    NetworkLinkMonitor networkLinkMonitor2 = NetworkLinkMonitor.this;
                    networkLinkMonitor2.sendMessageDelayed(networkLinkMonitor2.obtainMessage(OppoWifiNetworkSwitchEnhance.CMD_RSSI_FETCH, NetworkLinkMonitor.access$2704(networkLinkMonitor2), 0), 1000);
                    return true;
                }
            }
        }

        /* access modifiers changed from: private */
        public class VolumeWeightedEMA {
            private double mAlpha;
            private double mProduct = 0.0d;
            /* access modifiers changed from: private */
            public double mValue = 0.0d;
            /* access modifiers changed from: private */
            public double mVolume = 0.0d;

            public VolumeWeightedEMA(double coefficient) {
                this.mAlpha = coefficient;
            }

            public void update(double newValue, int newVolume) {
                if (newVolume > 0) {
                    double d = this.mAlpha;
                    this.mProduct = (d * ((double) newVolume) * newValue) + ((1.0d - d) * this.mProduct);
                    this.mVolume = (((double) newVolume) * d) + ((1.0d - d) * this.mVolume);
                    this.mValue = this.mProduct / this.mVolume;
                }
            }
        }
    }

    /* renamed from: com.android.server.wifi.OppoWifiNetworkSwitchEnhance$5  reason: invalid class name */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$DetailedState = new int[NetworkInfo.DetailedState.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void logD(String log) {
        if (this.mDebug) {
            Log.d(TAG, "" + log);
        }
    }

    private void logE(String log) {
        Log.e(TAG, "" + log);
    }
}
