package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OppoWifiSmartSwitcher {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_DATA_CLEAN = "com.oppo.wifismartswitcher.intent.action.data.clean";
    private static final String ACTION_DATA_LOAD = "com.oppo.wifismartswitcher.intent.action.data.load";
    private static final String ACTION_DATA_SAVE = "com.oppo.wifismartswitcher.intent.action.data.save";
    private static final String ACTION_DATA_SHOW = "com.oppo.wifismartswitcher.intent.action.data.show";
    private static final String ACTION_STATUS_CHANGED = "com.oppo.wifismartswitcher.intent.action.status.changed";
    private static final String ACTION_SWITCH = "com.oppo.wifismartswitcher.intent.action.cmd.switch";
    private static final int APFOUND_DOUBLE_CHECK_FOUND_DELAY = 0;
    private static final int APFOUND_DOUBLE_CHECK_LOST_DELAY = 3000;
    private static final int APFOUND_DOUBLE_CHECK_LOST_TIME = 8000;
    private static final int APFOUND_RSSI_MIN = -78;
    private static final int AP_AVAILABLE_INTERSECTION_PERCENT = 70;
    private static final int AP_CONNECTED_COUNT_MIN = 5;
    private static final int AP_LOST_COUNT_MAX = 4;
    private static final int BOOT_COMPLETED_EVT = 48;
    private static final String CONFIGFILE = "/data/misc/wifi/connectedapinfo.conf";
    private static final int DATA_CLEAN_EVT = 4;
    private static final int DATA_LOAD_EVT = 1;
    private static final int DATA_SAVE_EVT = 2;
    private static final int DATA_SHOW_EVT = 3;
    private static final int HOTSPOT_ENTER = 1;
    private static final int HOTSPOT_ENTER_CHECK_EVT = 32;
    private static final int HOTSPOT_ENTER_EVT = 34;
    private static final int HOTSPOT_LEAVE = 2;
    private static final int HOTSPOT_LEAVE_CHECK_EVT = 33;
    private static final int HOTSPOT_LEAVE_EVT = 35;
    private static final int HOTSPOT_NONE = 0;
    public static final String IS_SMART_ENABLE = "is_smart_enable";
    private static final int PERCENT_BASE = 100;
    private static final String PM_SCENARIO_FILE_PATH = "/data/data/com.coloros.oppoguardelf/shared_prefs/pmScenario.xml";
    private static final String SOFTAP_BACKUP_ENABLED_VALUE_STR = "value=\"true\"";
    private static final String SOFTAP_BACKUP_STATE_NAME_STR = "name=\"backup_wifi_hot_state\"";
    private static final String TAG = "OppoWifiSmartSwitcher";
    private static final String TAG_BSSID_LIST = "BSSID_LIST";
    private static final String TAG_BSSID_SEPERATOR = ",";
    private static final String TAG_CONFIG_KEY = "CONFIG_KEY";
    private static final String TAG_CONNECT_COUNT = "CONNECT_COUNT";
    private static final String TAG_NEWLINE = "\n";
    private static final String TAG_SEPERATOR = "****";
    private static final String TAG_SSID = "SSID";
    private static final int WIFI_CONNECT_EVT = 16;
    private static final int WIFI_DISCONNECT_EVT = 17;
    private static final int WIFI_SCAN_RESULT_EVT = 36;
    private static boolean sDebug = false;
    private Integer mApCount = 0;
    private boolean mApFoundCurr = false;
    private boolean mApFoundLast = false;
    private int mApLostCount = 0;
    private long mApLostFirstScanTime = 0;
    /* access modifiers changed from: private */
    public boolean mBootCompleted = false;
    private final BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {
        /* class com.android.server.wifi.OppoWifiSmartSwitcher.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            OppoWifiSmartSwitcher.this.logd("mBroadcastReciever.onReceive!");
            String action = intent.getAction();
            OppoWifiSmartSwitcher oppoWifiSmartSwitcher = OppoWifiSmartSwitcher.this;
            oppoWifiSmartSwitcher.logd("\taction:" + action);
            if (action.equals(OppoWifiSmartSwitcher.ACTION_BOOT_COMPLETED)) {
                OppoWifiSmartSwitcher.this.sendMessage(48);
            } else if (!OppoWifiSmartSwitcher.this.mBootCompleted) {
                OppoWifiSmartSwitcher.this.logd("\tboot not completed,ignore intent!!");
            } else if (action.equals(OppoWifiSmartSwitcher.ACTION_SWITCH)) {
                OppoWifiSmartSwitcher.this.switchOnOff(intent.getBooleanExtra("on", false));
            } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                OppoWifiSmartSwitcher.this.handleNetworkStateEvent(intent);
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                OppoWifiSmartSwitcher.this.handlWifiStateEvent(intent);
            } else if (action.equals(OppoWifiSmartSwitcher.ACTION_DATA_SAVE)) {
                OppoWifiSmartSwitcher.this.sendMessage(2);
            } else if (action.equals(OppoWifiSmartSwitcher.ACTION_DATA_SHOW)) {
                OppoWifiSmartSwitcher.this.sendMessage(3);
            } else if (action.equals(OppoWifiSmartSwitcher.ACTION_DATA_CLEAN)) {
                OppoWifiSmartSwitcher.this.sendMessage(4);
            } else if (action.equals(OppoWifiSmartSwitcher.ACTION_DATA_LOAD)) {
                OppoWifiSmartSwitcher.this.sendMessage(1);
            } else if (action.equals("android.net.wifi.SCAN_RESULTS")) {
                OppoWifiSmartSwitcher.this.sendMessage(36);
            }
        }
    };
    private ConnectivityManager mConnManager;
    private HashMap<String, ConnectedApInfo> mConnectedApInfoHashMap = new HashMap<>();
    private final Context mContext;
    private List<String> mCurrScanAvailableBSSID = new ArrayList();
    private boolean mEnabled = false;
    private boolean mFeatureStat = true;
    private WifiSmartSwitcherHandler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread("wifismartswitcher-handle-thread");
    private long mHotspotEntrOrLeaveToken = 0;
    private int mHotspotStatus = 0;
    private String mLastConnectedConfigKey = null;
    private List<String> mLastFoundScanAvailableBSSID = new ArrayList();
    private List<String> mLastScanAvailableBSSID = new ArrayList();
    private Object mLock = new Object();
    private boolean mLowPowerModeEnabled = false;
    /* access modifiers changed from: private */
    public HashMap<Integer, String> mMsgStrHashMap = new HashMap<>();
    private List<ScanResult> mScanResult;
    private boolean mSwitchOn = true;
    private WifiConfigManager mWifiConfigManager;
    private boolean mWifiEnabled = false;
    private WifiManager mWifiManager;
    private OppoWifiAssistantStateTraker mWifiNetworkStateTraker;
    private boolean mWifiScanAlwaysAvailabled = false;

    /* access modifiers changed from: private */
    public void checkWifiScanAlwaysAvailabled() {
        logd("CheckWifiScanAlwaysEnabled");
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1) {
            this.mWifiScanAlwaysAvailabled = true;
        } else {
            this.mWifiScanAlwaysAvailabled = false;
        }
        if (!this.mWifiScanAlwaysAvailabled || !this.mSwitchOn || !this.mFeatureStat || this.mLowPowerModeEnabled) {
            this.mEnabled = false;
        } else {
            this.mEnabled = true;
        }
        if (this.mEnabled) {
            handleEnabled();
        }
    }

    private boolean checkSoftApBackUpState() {
        logd("checkSoftApBackUpState");
        try {
            FileInputStream fis = new FileInputStream(PM_SCENARIO_FILE_PATH);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    logd(line);
                    if (line.contains(SOFTAP_BACKUP_STATE_NAME_STR) && line.contains(SOFTAP_BACKUP_ENABLED_VALUE_STR)) {
                        return true;
                    }
                } else {
                    br.close();
                    isr.close();
                    fis.close();
                    return false;
                }
            }
        } catch (Exception e) {
            logd("\texcption happed!");
            e.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void checkLowPowerModeChanged() {
        int value = Settings.System.getInt(this.mContext.getContentResolver(), IS_SMART_ENABLE, 0);
        logd("checkLowPowerModeChanged value:" + value);
        if (value == 1) {
            this.mLowPowerModeEnabled = true;
        } else {
            this.mLowPowerModeEnabled = false;
        }
        if (this.mLowPowerModeEnabled || !this.mWifiScanAlwaysAvailabled || !this.mSwitchOn || !this.mFeatureStat) {
            this.mEnabled = false;
        } else {
            this.mEnabled = true;
        }
        if (this.mEnabled) {
            WifiManager wifiManager = this.mWifiManager;
            if (wifiManager != null) {
                int softApState = wifiManager.getWifiApState();
                logd("softap state:" + softApState);
                if (!(softApState == 11 || softApState == 10)) {
                    logd("do not open wifi since softap is not in disabled state");
                    return;
                }
            }
            if (checkSoftApBackUpState()) {
                logd("do not open wifi since softap needs opened!");
            } else {
                handleEnabled();
            }
        }
    }

    private void messageStringInit() {
        logd("MessageStringInit");
        this.mMsgStrHashMap.put(1, "DATA_LOAD_EVT");
        this.mMsgStrHashMap.put(2, "DATA_SAVE_EVT");
        this.mMsgStrHashMap.put(3, "DATA_SHOW_EVT");
        this.mMsgStrHashMap.put(4, "DATA_CLEAN_EVT");
        this.mMsgStrHashMap.put(16, "WIFI_CONNECT_EVT");
        this.mMsgStrHashMap.put(17, "WIFI_DISCONNECT_EVT");
        this.mMsgStrHashMap.put(32, "HOTSPOT_ENTER_CHECK_EVT");
        this.mMsgStrHashMap.put(33, "HOTSPOT_LEAVE_CHECK_EVT");
        this.mMsgStrHashMap.put(34, "HOTSPOT_ENTER_EVT");
        this.mMsgStrHashMap.put(35, "HOTSPOT_LEAVE_EVT");
        this.mMsgStrHashMap.put(36, "WIFI_SCAN_RESULT_EVT");
        this.mMsgStrHashMap.put(48, "BOOT_COMPLETED_EVT");
    }

    /* access modifiers changed from: private */
    public void handleBootCompleted() {
        logd("handleBootCompleted");
        this.mBootCompleted = true;
        this.mConnManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (this.mWifiManager.isWifiEnabled()) {
            this.mWifiEnabled = true;
            this.mApFoundLast = true;
            this.mApFoundCurr = true;
        }
    }

    public OppoWifiSmartSwitcher(Context context, OppoWifiAssistantStateTraker wifiNetworkStateTraker, WifiConfigManager wificonfigmanager) {
        this.mContext = context;
        this.mWifiNetworkStateTraker = wifiNetworkStateTraker;
        this.mWifiConfigManager = wificonfigmanager;
        this.mHandlerThread.start();
        this.mHandler = new WifiSmartSwitcherHandler(this.mHandlerThread.getLooper());
        listenForBroadcasts();
        messageStringInit();
    }

    private void listenForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SWITCH);
        intentFilter.addAction(ACTION_BOOT_COMPLETED);
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction(ACTION_DATA_SAVE);
        intentFilter.addAction(ACTION_DATA_LOAD);
        intentFilter.addAction(ACTION_DATA_SHOW);
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mContext.registerReceiver(this.mBroadcastReciever, intentFilter, null, this.mHandler);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(this.mHandler) {
            /* class com.android.server.wifi.OppoWifiSmartSwitcher.AnonymousClass2 */

            public void onChange(boolean selfChange) {
                OppoWifiSmartSwitcher.this.logd("WIFI_SCAN_ALWAYS_AVAILABLE value chagned!");
                OppoWifiSmartSwitcher.this.checkWifiScanAlwaysAvailabled();
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(IS_SMART_ENABLE), false, new ContentObserver(this.mHandler) {
            /* class com.android.server.wifi.OppoWifiSmartSwitcher.AnonymousClass3 */

            public void onChange(boolean selfChange) {
                OppoWifiSmartSwitcher.this.logd("low power mode value chagned!");
                OppoWifiSmartSwitcher.this.checkLowPowerModeChanged();
            }
        });
    }

    public void dataInit() {
        logd("DataInit");
        this.mHandler.sendEmptyMessage(1);
        checkWifiScanAlwaysAvailabled();
        checkLowPowerModeChanged();
    }

    public void switchOnOff(boolean on) {
        logd("switchOnOff");
        this.mSwitchOn = on;
        if (!this.mSwitchOn || !this.mWifiScanAlwaysAvailabled || !this.mFeatureStat || this.mLowPowerModeEnabled) {
            this.mEnabled = false;
        } else {
            this.mEnabled = true;
        }
        if (this.mEnabled) {
            handleEnabled();
        }
    }

    public void featureState(boolean state) {
        this.mFeatureStat = state;
        boolean z = this.mFeatureStat;
        if (!z) {
            this.mEnabled = false;
        } else if (this.mSwitchOn && this.mWifiScanAlwaysAvailabled && z && !this.mLowPowerModeEnabled) {
            this.mEnabled = true;
        }
        if (this.mEnabled) {
            handleEnabled();
        }
    }

    public void disable() {
        logd("Disable");
        this.mEnabled = false;
    }

    private void handleEnabled() {
        logd("handleEnabled");
        if (this.mApFoundLast) {
            sendMessage(34);
        }
    }

    /* access modifiers changed from: private */
    public void sendMessage(int message) {
        this.mHandler.sendEmptyMessage(message);
    }

    private void sendMessage(int message, Object obj) {
        this.mHandler.obtainMessage(message, obj).sendToTarget();
    }

    private void sendDelayedMessage(int message, int delay) {
        WifiSmartSwitcherHandler wifiSmartSwitcherHandler = this.mHandler;
        wifiSmartSwitcherHandler.sendMessageDelayed(wifiSmartSwitcherHandler.obtainMessage(message), (long) delay);
    }

    private void sendDelayedMessage(int message, Object obj, int delay) {
        WifiSmartSwitcherHandler wifiSmartSwitcherHandler = this.mHandler;
        wifiSmartSwitcherHandler.sendMessageDelayed(wifiSmartSwitcherHandler.obtainMessage(message, obj), (long) delay);
    }

    private final class WifiSmartSwitcherHandler extends Handler {
        public WifiSmartSwitcherHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int message = msg.what;
            OppoWifiSmartSwitcher oppoWifiSmartSwitcher = OppoWifiSmartSwitcher.this;
            oppoWifiSmartSwitcher.logd("handle messag event:" + ((String) OppoWifiSmartSwitcher.this.mMsgStrHashMap.get(Integer.valueOf(message))));
            if (message == 1) {
                OppoWifiSmartSwitcher.this.dataLoad();
            } else if (message == 2) {
                OppoWifiSmartSwitcher.this.dataSave();
            } else if (message == 3) {
                OppoWifiSmartSwitcher.this.dataShow();
            } else if (message == 4) {
                OppoWifiSmartSwitcher.this.dataClean();
            } else if (message == 16) {
                OppoWifiSmartSwitcher.this.handleWifiConnected();
            } else if (message == 17) {
                OppoWifiSmartSwitcher.this.handleWifiDisconnected();
            } else if (message != 48) {
                switch (message) {
                    case 32:
                        OppoWifiSmartSwitcher.this.checkHotspotEnter(msg.obj);
                        return;
                    case 33:
                        OppoWifiSmartSwitcher.this.checkHotspotLeave(msg.obj);
                        return;
                    case 34:
                        OppoWifiSmartSwitcher.this.handleHotspotEnter();
                        return;
                    case 35:
                        OppoWifiSmartSwitcher.this.handleHotspotLeave();
                        return;
                    case 36:
                        OppoWifiSmartSwitcher.this.handleWifiScanResultEvent();
                        return;
                    default:
                        return;
                }
            } else {
                OppoWifiSmartSwitcher.this.handleBootCompleted();
            }
        }
    }

    public class ConnectedApInfo {
        /* access modifiers changed from: private */
        public List<String> mBSSIDList = new ArrayList();
        /* access modifiers changed from: private */
        public String mConfigKey = "unknown";
        /* access modifiers changed from: private */
        public Integer mConnectCount = 0;
        /* access modifiers changed from: private */
        public String mSSID = "unknown";

        public ConnectedApInfo() {
        }
    }

    private List<OppoWifiAssistantRecord> getAvailableNetworks() {
        logd("getAvailableNetworks");
        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiNetworkStateTraker;
        if (oppoWifiAssistantStateTraker == null) {
            return null;
        }
        return oppoWifiAssistantStateTraker.getWifiNetworkRecords();
    }

    private boolean checkAPFoundState(List<ScanResult> mScanResult2) {
        logd("checkAPFoundState");
        List<ScanResult> goodLevelScanResult = new ArrayList<>();
        List<ScanResult> candinateScanResultList = new ArrayList<>();
        List<WifiConfiguration> savedNetworkList = this.mWifiConfigManager.getSavedNetworksAll();
        if (savedNetworkList == null || savedNetworkList.size() <= 0) {
            logd("\tsavedNetworkList is null or empty!");
            return false;
        }
        boolean found = false;
        for (ScanResult gsr : mScanResult2) {
            if (gsr.level >= APFOUND_RSSI_MIN) {
                goodLevelScanResult.add(gsr);
                this.mCurrScanAvailableBSSID.add(gsr.BSSID);
                logd("\tBSSID:" + gsr.BSSID + "\tlevel:" + gsr.level + "\tssid:" + gsr.SSID);
            }
        }
        if (goodLevelScanResult.size() <= 0) {
            logd("\tall aps level is less than -78");
            return false;
        }
        for (ScanResult ssr : goodLevelScanResult) {
            String configKey = WifiConfiguration.configKey(ssr);
            if (ssr == null || configKey == null) {
                logd("\t ssr or configKey is null!!");
            } else {
                boolean saved = false;
                Iterator<WifiConfiguration> it = savedNetworkList.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (configKey.equals(it.next().configKey())) {
                            saved = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (saved) {
                    ConnectedApInfo connectedApInfo = this.mConnectedApInfoHashMap.get(configKey);
                    List<String> bssidList = connectedApInfo != null ? connectedApInfo.mBSSIDList : null;
                    if (!(connectedApInfo == null || bssidList == null || bssidList.size() <= 0)) {
                        if (connectedApInfo.mConnectCount.intValue() > 5) {
                            logd("\tgot a possibled ap " + configKey + " connect count: " + connectedApInfo.mConnectCount + " BSSID:" + ssr.BSSID);
                            candinateScanResultList.add(ssr);
                        } else {
                            logd("\t" + configKey + " connected count is less than " + 5);
                        }
                    }
                }
            }
        }
        if (candinateScanResultList.size() <= 0) {
            logd("\tno ap found in mConnectedApInfoHashMap!");
            return false;
        }
        List<OppoWifiAssistantRecord> mAvailableNetworks = getAvailableNetworks();
        if (mAvailableNetworks == null) {
            logd("\tno available ap found!");
            return false;
        }
        for (ScanResult csr : candinateScanResultList) {
            String configKey2 = WifiConfiguration.configKey(csr);
            if (configKey2 != null) {
                for (OppoWifiAssistantRecord record : mAvailableNetworks) {
                    if (configKey2.equals(record.mConfigkey) && record.mNetworkValid) {
                        found = true;
                        logd("\tfind a can access internet ap " + configKey2 + " BSSID:" + csr.BSSID);
                    }
                }
            }
        }
        if (!found) {
            logd("\t find no can access internet ap!");
        }
        return found;
    }

    /* access modifiers changed from: private */
    public void handleWifiScanResultEvent() {
        int apAlikePercent;
        logd("handleWifiScanResultEvent");
        this.mScanResult = this.mWifiManager.getScanResults();
        this.mCurrScanAvailableBSSID.clear();
        this.mApFoundCurr = checkAPFoundState(this.mScanResult);
        if (this.mApFoundCurr) {
            this.mApLostCount = 0;
            this.mApLostFirstScanTime = 0;
            this.mLastFoundScanAvailableBSSID.clear();
            this.mLastFoundScanAvailableBSSID.addAll(this.mCurrScanAvailableBSSID);
        }
        if (!this.mEnabled) {
            logd("\tmEnabled set false,do nothing!!!");
        } else if (this.mConnManager.getNetworkInfo(1).isConnected()) {
            if (sDebug) {
                logd("\twifi is in connected state update bssid list,and  do nothing!!!");
                if (this.mLastConnectedConfigKey == null) {
                    logd("\tmLastConnectedConfigKey should not be null,try update it! ");
                    WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
                    WifiConfiguration wc = this.mWifiConfigManager.getConfiguredNetwork(mWifiInfo != null ? mWifiInfo.getNetworkId() : -1);
                    this.mLastConnectedConfigKey = wc != null ? wc.configKey(false) : null;
                }
                updateBSSIDList(this.mScanResult);
            }
            this.mApFoundCurr = true;
        } else {
            logd("\tmApFoundCurr:" + this.mApFoundCurr + "\tmApFoundLast:" + this.mApFoundLast);
            boolean z = this.mApFoundCurr;
            if (z != this.mApFoundLast) {
                if (z) {
                    this.mHotspotStatus = 1;
                } else {
                    this.mHotspotStatus = 2;
                }
                this.mHotspotEntrOrLeaveToken++;
                if (this.mHotspotStatus == 1) {
                    sendDelayedMessage(32, Long.valueOf(this.mHotspotEntrOrLeaveToken), 0);
                } else {
                    List<String> tmpList = new ArrayList<>();
                    tmpList.clear();
                    tmpList.addAll(this.mCurrScanAvailableBSSID);
                    tmpList.retainAll(this.mLastFoundScanAvailableBSSID);
                    if (this.mCurrScanAvailableBSSID.size() == 0 || this.mLastFoundScanAvailableBSSID.size() == 0) {
                        apAlikePercent = 0;
                    } else {
                        apAlikePercent = (tmpList.size() * 100) / this.mLastFoundScanAvailableBSSID.size();
                    }
                    if (apAlikePercent >= AP_AVAILABLE_INTERSECTION_PERCENT) {
                        logd("ap not found but aps around is " + apAlikePercent + "% alike,treat it as in hotarea!");
                        this.mApFoundCurr = true;
                        this.mApLostCount = 0;
                        this.mApLostFirstScanTime = 0;
                    } else {
                        this.mApLostCount++;
                        logd("\tmApLostCount=" + this.mApLostCount);
                        if (this.mApLostFirstScanTime > 0 && System.currentTimeMillis() - this.mApLostFirstScanTime > 8000) {
                            logd("\t APFOUND_DOUBLE_CHECK_LOST_TIME timeout !");
                            this.mApLostCount = 4;
                        }
                        if (this.mApLostCount < 4) {
                            sendDelayedMessage(33, Long.valueOf(this.mHotspotEntrOrLeaveToken), 3000);
                            if (this.mApLostCount == 1) {
                                this.mApLostFirstScanTime = System.currentTimeMillis();
                            }
                            this.mApFoundCurr = true;
                        } else {
                            logd("\tcontinues " + this.mApLostCount + " scan lost or double check timeout!");
                        }
                    }
                }
            } else if (z) {
                logd("\tin hot area,check if in the same hotspot area since last scan!");
                this.mLastScanAvailableBSSID.retainAll(this.mCurrScanAvailableBSSID);
                if (this.mLastScanAvailableBSSID.size() == 0) {
                    logd("\tdifferent hotspot area,treat it as enter");
                    this.mHotspotEntrOrLeaveToken++;
                    sendDelayedMessage(32, Long.valueOf(this.mHotspotEntrOrLeaveToken), 0);
                } else {
                    logd("\tin the same hotspot area,we should do nothing!");
                }
            }
        }
        this.mApFoundLast = this.mApFoundCurr;
        this.mLastScanAvailableBSSID.clear();
        this.mLastScanAvailableBSSID.addAll(this.mCurrScanAvailableBSSID);
    }

    /* access modifiers changed from: private */
    public void handleNetworkStateEvent(Intent intent) {
        logd("HandleNetworkBroadcast!");
        intent.getAction();
        NetworkInfo info = this.mConnManager.getNetworkInfo(((NetworkInfo) intent.getParcelableExtra("networkInfo")).getType());
        int dataType = info.getType();
        if (sDebug) {
            NetworkInfo.State wifiState = this.mConnManager.getNetworkInfo(1).getState();
            logd("\t>>>type:" + dataType + " wifistate:" + wifiState);
        }
        boolean dataConnected = info.isConnected();
        if (dataType != 1) {
            return;
        }
        if (dataConnected) {
            sendMessage(16);
        } else {
            sendMessage(17);
        }
    }

    /* access modifiers changed from: private */
    public void checkHotspotEnter(Object obj) {
        logd("checkHotspotEnter");
        if (((Long) obj).longValue() != this.mHotspotEntrOrLeaveToken) {
            logd("\ttoken mismatch,do not send open wifi event!");
        } else {
            sendMessage(34);
        }
    }

    /* access modifiers changed from: private */
    public void handleHotspotEnter() {
        logd("handleHotspotEnter");
        openWifi();
    }

    /* access modifiers changed from: private */
    public void checkHotspotLeave(Object obj) {
        logd("checkHotspotLeave");
        this.mWifiManager.startScan();
    }

    /* access modifiers changed from: private */
    public void handleHotspotLeave() {
        logd("handleHotspotLeave");
        NetworkInfo info = this.mConnManager.getNetworkInfo(1);
        if (info == null || info.isConnected()) {
            logd("\twifi is still in connected ,do nothing!");
        } else {
            closeWifi();
        }
    }

    /* access modifiers changed from: private */
    public void handlWifiStateEvent(Intent intent) {
        logd("handlWifiStateEvent");
        if (intent != null) {
            int wifiState = intent.getIntExtra("wifi_state", 0);
            if (wifiState == 1) {
                logd("\tWIFI_STATE_DISABLED!!!");
                if (!this.mWifiEnabled) {
                    logd("\trepeat wifi disbale broadcast,ignore it");
                    return;
                }
                this.mWifiEnabled = false;
                logd("\tsend message to clean&save data!");
                sendMessage(4);
            } else if (wifiState == 3) {
                logd("\twifi is enabled!");
                this.mWifiEnabled = true;
            }
        }
    }

    private void updateBSSIDList(List<ScanResult> srList) {
        String mConfigKey;
        logd("updateBSSIDList");
        ConnectedApInfo connectedApInfo = this.mConnectedApInfoHashMap.get(this.mLastConnectedConfigKey);
        if (connectedApInfo == null) {
            logd("\tmLastConnectedConfigKey " + this.mLastConnectedConfigKey + " relatived ConnectedApInfo is null!");
            return;
        }
        List<String> bssidList = connectedApInfo.mBSSIDList;
        if (bssidList == null) {
            bssidList = new ArrayList<>();
        }
        if (srList != null && srList.size() > 0) {
            for (ScanResult sr : srList) {
                if (!bssidList.contains(sr.BSSID) && (mConfigKey = WifiConfiguration.configKey(sr)) != null && mConfigKey.equals(this.mLastConnectedConfigKey)) {
                    logd("\t add new bssid " + sr.BSSID + " for ap " + mConfigKey);
                    bssidList.add(sr.BSSID);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleWifiConnected() {
        boolean mIsContained;
        logd("handleWifiConnected");
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            int mNetworkId = mWifiInfo.getNetworkId();
            WifiConfiguration wConf = this.mWifiConfigManager.getConfiguredNetwork(mNetworkId);
            String mConfigKey = wConf != null ? wConf.configKey() : null;
            if (wConf == null || mConfigKey == null) {
                logd("\twConf or mConfigKey is null,do nothing!");
                return;
            }
            logd("\tConnected AP netId:" + mNetworkId + " configKey:" + mConfigKey);
            ConnectedApInfo mConnectedApInfo = this.mConnectedApInfoHashMap.get(mConfigKey);
            if (mConnectedApInfo != null) {
                mIsContained = true;
            } else {
                mConnectedApInfo = new ConnectedApInfo();
                String unused = mConnectedApInfo.mConfigKey = mConfigKey;
                String unused2 = mConnectedApInfo.mSSID = mWifiInfo.getSSID();
                mIsContained = false;
            }
            Integer unused3 = mConnectedApInfo.mConnectCount;
            Integer unused4 = mConnectedApInfo.mConnectCount = Integer.valueOf(mConnectedApInfo.mConnectCount.intValue() + 1);
            this.mLastConnectedConfigKey = mConfigKey;
            updateBSSIDList(this.mWifiManager.getScanResults());
            synchronized (this.mLock) {
                this.mConnectedApInfoHashMap.put(this.mLastConnectedConfigKey, mConnectedApInfo);
                if (!mIsContained) {
                    Integer num = this.mApCount;
                    this.mApCount = Integer.valueOf(this.mApCount.intValue() + 1);
                }
            }
            this.mApFoundLast = true;
            this.mApFoundCurr = true;
            sendMessage(2);
            return;
        }
        logd("\tmWifiInfo is null,do nothing!");
    }

    /* access modifiers changed from: private */
    public void handleWifiDisconnected() {
        logd("handleWifiDisconnected");
        if (this.mLastConnectedConfigKey == null) {
            logd("\tmLastConnectedConfigKey is null,repeat events,ignore!!");
            return;
        }
        this.mLastConnectedConfigKey = null;
        sendMessage(4);
    }

    private void openWifi() {
        logd("openWifi");
        if (this.mWifiManager.isWifiEnabled()) {
            logd("\twifi has been enabled already!");
        } else {
            this.mWifiManager.setWifiEnabled(true);
        }
    }

    private void closeWifi() {
        logd("closeWifi");
        if (this.mWifiManager.isWifiEnabled()) {
            this.mWifiManager.setWifiEnabled(false);
        } else {
            logd("\twifi has been disabled already!");
        }
    }

    /* access modifiers changed from: private */
    public void dataClean() {
        logd("dataClean");
        int count = 0;
        List<WifiConfiguration> mWifiConfigList = this.mWifiConfigManager.getSavedNetworks(1010);
        if (mWifiConfigList == null || mWifiConfigList.size() <= 0) {
            logd("mWifiConfigList is null or 0 size");
            return;
        }
        Iterator iterator = this.mConnectedApInfoHashMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            ConnectedApInfo mConnectedApInfo = this.mConnectedApInfoHashMap.get(key);
            String configKey = mConnectedApInfo != null ? mConnectedApInfo.mConfigKey : null;
            if (!(mConnectedApInfo == null || configKey == null)) {
                boolean found = false;
                Iterator<WifiConfiguration> it = mWifiConfigList.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (configKey.equals(it.next().configKey())) {
                            found = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!found) {
                    logd("\tap configKey:" + configKey + " not in configuredNetworks,clean it!");
                    iterator.remove();
                    this.mConnectedApInfoHashMap.remove(key);
                    this.mApCount = Integer.valueOf(this.mApCount.intValue() + -1);
                    count++;
                }
            }
        }
        if (count > 0) {
            sendMessage(2);
        }
    }

    /* access modifiers changed from: private */
    public void dataLoad() {
        logd("dataLoad");
        try {
            FileInputStream fis = new FileInputStream(CONFIGFILE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            this.mConnectedApInfoHashMap.clear();
            int i = 0;
            this.mApCount = 0;
            String mBSSIDStr = null;
            String mSSID = null;
            String mConfigKey = null;
            int mConnectCount = 0;
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    br.close();
                    isr.close();
                    fis.close();
                    return;
                } else if (line.equals(TAG_SEPERATOR)) {
                    ConnectedApInfo connectedApInfo = new ConnectedApInfo();
                    List unused = connectedApInfo.mBSSIDList = new ArrayList();
                    if (mBSSIDStr != null) {
                        String[] strArray = mBSSIDStr.split(TAG_BSSID_SEPERATOR);
                        int length = strArray.length;
                        int i2 = i;
                        while (i2 < length) {
                            connectedApInfo.mBSSIDList.add(strArray[i2]);
                            i2++;
                            strArray = strArray;
                        }
                    }
                    String unused2 = connectedApInfo.mSSID = mSSID;
                    String unused3 = connectedApInfo.mConfigKey = mConfigKey;
                    Integer unused4 = connectedApInfo.mConnectCount = mConnectCount;
                    synchronized (this.mLock) {
                        this.mConnectedApInfoHashMap.put(connectedApInfo.mConfigKey, connectedApInfo);
                        Integer num = this.mApCount;
                        this.mApCount = Integer.valueOf(this.mApCount.intValue() + 1);
                        logd("\tadd a new item configKey:" + connectedApInfo.mConfigKey);
                    }
                    i = 0;
                } else {
                    int colon = line.indexOf(61);
                    i = 0;
                    String key = line.substring(0, colon).trim();
                    String value = line.substring(colon + 1).trim();
                    logd("key=" + key + ",value=" + value);
                    if (key.equals(TAG_BSSID_LIST)) {
                        mBSSIDStr = value;
                    } else if (key.equals("SSID")) {
                        mSSID = value;
                    } else if (key.equals(TAG_CONFIG_KEY)) {
                        mConfigKey = value;
                    } else if (key.equals(TAG_CONNECT_COUNT)) {
                        mConnectCount = Integer.valueOf(Integer.parseInt(value));
                    }
                }
            }
        } catch (Exception e) {
            logd("\texcption happed!");
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void dataSave() {
        logd("dataSave");
        new ConnectedApInfo();
        try {
            FileOutputStream fos = new FileOutputStream(CONFIGFILE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            for (String key : this.mConnectedApInfoHashMap.keySet()) {
                ConnectedApInfo connectedApInfo = this.mConnectedApInfoHashMap.get(key);
                if (connectedApInfo != null) {
                    String line = "CONFIG_KEY=" + connectedApInfo.mConfigKey + TAG_NEWLINE;
                    String bssidStr = null;
                    for (String str : connectedApInfo.mBSSIDList) {
                        if (bssidStr == null) {
                            bssidStr = str;
                        } else {
                            bssidStr = bssidStr + TAG_BSSID_SEPERATOR + str;
                        }
                    }
                    String line2 = (((line + "BSSID_LIST=" + bssidStr + TAG_NEWLINE) + "SSID=" + connectedApInfo.mSSID + TAG_NEWLINE) + "CONNECT_COUNT=" + connectedApInfo.mConnectCount + TAG_NEWLINE) + "****\n";
                    logd(line2);
                    bw.write(line2);
                }
            }
            bw.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void dataShow() {
        logd("dataShow(),current ap count:" + this.mApCount);
        logd("\tmConnectedApInfoHashMap in details!");
        for (String key : this.mConnectedApInfoHashMap.keySet()) {
            ConnectedApInfo mConnectedApInfo = this.mConnectedApInfoHashMap.get(key);
            if (mConnectedApInfo != null) {
                logd("\n\tCONFIG_KEY=" + mConnectedApInfo.mConfigKey + "\n\t" + "SSID" + "=" + mConnectedApInfo.mSSID + "\n\t" + TAG_BSSID_LIST + "=" + mConnectedApInfo.mBSSIDList + "\n\t" + TAG_CONNECT_COUNT + " =" + mConnectedApInfo.mConnectCount + "\n\t" + TAG_SEPERATOR);
            }
        }
        logd("\nsorted mConnectedApInfoWeightHashMap!");
        logd("\tother parameters:");
        logd("\n\tmLastConnectedConfigKey:\t" + this.mLastConnectedConfigKey + "\n\tmWifiEnabled:\t" + this.mWifiEnabled + "\n\tmApCount:\t" + this.mApCount + "\n\tCONFIGFILE:\t" + CONFIGFILE + "\n\tmApFoundLast:\t" + this.mApFoundLast + "\n\tmApFoundCurr:\t" + this.mApFoundCurr + "\n\tmHotspotStatus:\t" + this.mHotspotStatus + "\n\tmWifiScanAlwaysAvailabled:\t" + this.mWifiScanAlwaysAvailabled + "\n\tmSwitchOn:\t" + this.mSwitchOn + "\n\tmEnabled:\t" + this.mEnabled);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            sDebug = true;
        } else {
            sDebug = false;
        }
    }

    /* access modifiers changed from: private */
    public void logd(String msg) {
        if (sDebug) {
            logd(msg);
        }
    }
}
