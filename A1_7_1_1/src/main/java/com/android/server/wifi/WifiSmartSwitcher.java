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
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Log;
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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WifiSmartSwitcher {
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
    private static final int DATA_CLEAN_EVT = 4;
    private static final int DATA_LOAD_EVT = 1;
    private static final int DATA_SAVE_EVT = 2;
    private static final int DATA_SHOW_EVT = 3;
    private static boolean DEBUG = false;
    private static final int HOTSPOT_ENTER = 1;
    private static final int HOTSPOT_ENTER_CHECK_EVT = 32;
    private static final int HOTSPOT_ENTER_EVT = 34;
    private static final int HOTSPOT_LEAVE = 2;
    private static final int HOTSPOT_LEAVE_CHECK_EVT = 33;
    private static final int HOTSPOT_LEAVE_EVT = 35;
    private static final int HOTSPOT_NONE = 0;
    public static final String IS_SMART_ENABLE = "is_smart_enable";
    private static final String PM_SCENARIO_FILE_PATH = "/data/data/com.coloros.oppoguardelf/shared_prefs/pmScenario.xml";
    private static final String SOFTAP_BACKUP_ENABLED_VALUE_STR = "value=\"true\"";
    private static final String SOFTAP_BACKUP_STATE_NAME_STR = "name=\"backup_wifi_hot_state\"";
    private static final String TAG = "WifiSmartSwitcher";
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
    private final String CONFIGFILE;
    private Integer mApCount;
    private boolean mApFoundCurr;
    private boolean mApFoundLast;
    private int mApLostCount;
    private long mApLostFirstScanTime;
    private boolean mBootCompleted;
    private final BroadcastReceiver mBroadcastReciever;
    private ConnectivityManager mConnManager;
    private HashMap<String, ConnectedApInfo> mConnectedApInfoHashMap;
    private final Context mContext;
    private List<String> mCurrScanAvailableBSSID;
    private boolean mEnabled;
    private boolean mFeatureStat;
    private WifiSmartSwitcherHandler mHandler;
    private HandlerThread mHandlerThread;
    private long mHotspotEntrOrLeaveToken;
    private int mHotspotStatus;
    private String mLastConnectedConfigKey;
    private List<String> mLastFoundScanAvailableBSSID;
    private List<String> mLastScanAvailableBSSID;
    private Object mLock;
    private boolean mLowPowerModeEnabled;
    private HashMap<Integer, String> mMsgStrHashMap;
    private List<ScanResult> mScanResult;
    private boolean mSwitchOn;
    private WifiConfigManager mWifiConfigManager;
    private boolean mWifiEnabled;
    private WifiManager mWifiManager;
    private WifiNetworkStateTraker mWifiNetworkStateTraker;
    private boolean mWifiScanAlwaysAvailabled;

    public class ConnectedApInfo {
        private List<String> mBSSIDList = new ArrayList();
        private String mConfigKey = "unknown";
        private Integer mConnectCount = Integer.valueOf(0);
        private String mSSID = "unknown";
    }

    private final class WifiSmartSwitcherHandler extends Handler {
        public WifiSmartSwitcherHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int message = msg.what;
            if (WifiSmartSwitcher.DEBUG) {
                Log.d(WifiSmartSwitcher.TAG, "handle messag event:" + ((String) WifiSmartSwitcher.this.mMsgStrHashMap.get(Integer.valueOf(message))));
            }
            switch (message) {
                case 1:
                    WifiSmartSwitcher.this.dataLoad();
                    return;
                case 2:
                    WifiSmartSwitcher.this.dataSave();
                    return;
                case 3:
                    WifiSmartSwitcher.this.dataShow();
                    return;
                case 4:
                    WifiSmartSwitcher.this.dataClean();
                    return;
                case 16:
                    WifiSmartSwitcher.this.handleWifiConnected();
                    return;
                case 17:
                    WifiSmartSwitcher.this.handleWifiDisconnected();
                    return;
                case 32:
                    WifiSmartSwitcher.this.checkHotspotEnter(msg.obj);
                    return;
                case 33:
                    WifiSmartSwitcher.this.checkHotspotLeave(msg.obj);
                    return;
                case 34:
                    WifiSmartSwitcher.this.handleHotspotEnter();
                    return;
                case 35:
                    WifiSmartSwitcher.this.handleHotspotLeave();
                    return;
                case 36:
                    WifiSmartSwitcher.this.handleWifiScanResultEvent();
                    return;
                case 48:
                    WifiSmartSwitcher.this.handleBootCompleted();
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiSmartSwitcher.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiSmartSwitcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiSmartSwitcher.<clinit>():void");
    }

    private void checkWifiScanAlwaysAvailabled() {
        if (DEBUG) {
            Log.d(TAG, "CheckWifiScanAlwaysEnabled");
        }
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1) {
            this.mWifiScanAlwaysAvailabled = true;
        } else {
            this.mWifiScanAlwaysAvailabled = false;
        }
        if (this.mWifiScanAlwaysAvailabled && this.mSwitchOn && this.mFeatureStat && !this.mLowPowerModeEnabled) {
            this.mEnabled = true;
        } else {
            this.mEnabled = false;
        }
        if (this.mEnabled) {
            handleEnabled();
        }
    }

    private boolean checkSoftApBackUpState() {
        if (DEBUG) {
            Log.d(TAG, "checkSoftApBackUpState");
        }
        try {
            FileInputStream fis = new FileInputStream(PM_SCENARIO_FILE_PATH);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    br.close();
                    isr.close();
                    fis.close();
                    break;
                }
                if (DEBUG) {
                    Log.d(TAG, line);
                }
                if (line.contains(SOFTAP_BACKUP_STATE_NAME_STR) && line.contains(SOFTAP_BACKUP_ENABLED_VALUE_STR)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "\texcption happed!");
            e.printStackTrace();
        }
        return false;
    }

    private void checkLowPowerModeChanged() {
        int value = System.getInt(this.mContext.getContentResolver(), IS_SMART_ENABLE, 0);
        if (DEBUG) {
            Log.d(TAG, "checkLowPowerModeChanged value:" + value);
        }
        if (value == 1) {
            this.mLowPowerModeEnabled = true;
        } else {
            this.mLowPowerModeEnabled = false;
        }
        if (!this.mLowPowerModeEnabled && this.mWifiScanAlwaysAvailabled && this.mSwitchOn && this.mFeatureStat) {
            this.mEnabled = true;
        } else {
            this.mEnabled = false;
        }
        if (this.mEnabled) {
            if (this.mWifiManager != null) {
                int softApState = this.mWifiManager.getWifiApState();
                if (DEBUG) {
                    Log.d(TAG, "softap state:" + softApState);
                }
                if (!(softApState == 11 || softApState == 10)) {
                    Log.d(TAG, "do not open wifi since softap is not in disabled state");
                    return;
                }
            }
            if (checkSoftApBackUpState()) {
                Log.d(TAG, "do not open wifi since softap needs opened!");
                return;
            }
            handleEnabled();
        }
    }

    private void messageStringInit() {
        if (DEBUG) {
            Log.d(TAG, "MessageStringInit");
        }
        this.mMsgStrHashMap.put(Integer.valueOf(1), "DATA_LOAD_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(2), "DATA_SAVE_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(3), "DATA_SHOW_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(4), "DATA_CLEAN_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(16), "WIFI_CONNECT_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(17), "WIFI_DISCONNECT_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(32), "HOTSPOT_ENTER_CHECK_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(33), "HOTSPOT_LEAVE_CHECK_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(34), "HOTSPOT_ENTER_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(35), "HOTSPOT_LEAVE_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(36), "WIFI_SCAN_RESULT_EVT");
        this.mMsgStrHashMap.put(Integer.valueOf(48), "BOOT_COMPLETED_EVT");
    }

    private void handleBootCompleted() {
        if (DEBUG) {
            Log.d(TAG, "handleBootCompleted");
        }
        this.mBootCompleted = true;
        this.mConnManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (this.mWifiManager.isWifiEnabled()) {
            this.mWifiEnabled = true;
            this.mApFoundLast = true;
            this.mApFoundCurr = true;
        }
    }

    public WifiSmartSwitcher(Context context, WifiNetworkStateTraker wifiNetworkStateTraker, WifiConfigManager wificonfigmanager) {
        this.mLock = new Object();
        this.mBootCompleted = false;
        this.mLastConnectedConfigKey = null;
        this.mWifiEnabled = false;
        this.mApCount = Integer.valueOf(0);
        this.CONFIGFILE = "/data/misc/wifi/connectedapinfo.conf";
        this.mLastScanAvailableBSSID = new ArrayList();
        this.mCurrScanAvailableBSSID = new ArrayList();
        this.mLastFoundScanAvailableBSSID = new ArrayList();
        this.mConnectedApInfoHashMap = new HashMap();
        this.mMsgStrHashMap = new HashMap();
        this.mHandlerThread = new HandlerThread("wifismartswitcher-handle-thread");
        this.mEnabled = false;
        this.mSwitchOn = true;
        this.mFeatureStat = true;
        this.mWifiScanAlwaysAvailabled = false;
        this.mLowPowerModeEnabled = false;
        this.mHotspotStatus = 0;
        this.mHotspotEntrOrLeaveToken = 0;
        this.mApFoundLast = false;
        this.mApFoundCurr = false;
        this.mApLostCount = 0;
        this.mApLostFirstScanTime = 0;
        this.mBroadcastReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiSmartSwitcher.DEBUG) {
                    Log.d(WifiSmartSwitcher.TAG, "mBroadcastReciever.onReceive!");
                }
                String action = intent.getAction();
                if (WifiSmartSwitcher.DEBUG) {
                    Log.d(WifiSmartSwitcher.TAG, "\taction:" + action);
                }
                if (action.equals(WifiSmartSwitcher.ACTION_BOOT_COMPLETED)) {
                    WifiSmartSwitcher.this.sendMessage(48);
                } else if (WifiSmartSwitcher.this.mBootCompleted) {
                    if (action.equals(WifiSmartSwitcher.ACTION_SWITCH)) {
                        WifiSmartSwitcher.this.switchOnOff(intent.getBooleanExtra("on", false));
                    } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        WifiSmartSwitcher.this.handleNetworkStateEvent(intent);
                    } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                        WifiSmartSwitcher.this.handlWifiStateEvent(intent);
                    } else if (action.equals(WifiSmartSwitcher.ACTION_DATA_SAVE)) {
                        WifiSmartSwitcher.this.sendMessage(2);
                    } else if (action.equals(WifiSmartSwitcher.ACTION_DATA_SHOW)) {
                        WifiSmartSwitcher.this.sendMessage(3);
                    } else if (action.equals(WifiSmartSwitcher.ACTION_DATA_CLEAN)) {
                        WifiSmartSwitcher.this.sendMessage(4);
                    } else if (action.equals(WifiSmartSwitcher.ACTION_DATA_LOAD)) {
                        WifiSmartSwitcher.this.sendMessage(1);
                    } else if (action.equals("android.net.wifi.SCAN_RESULTS")) {
                        WifiSmartSwitcher.this.sendMessage(36);
                    }
                } else {
                    Log.d(WifiSmartSwitcher.TAG, "\tboot not completed,ignore intent!!");
                }
            }
        };
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
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                if (WifiSmartSwitcher.DEBUG) {
                    Log.d(WifiSmartSwitcher.TAG, "WIFI_SCAN_ALWAYS_AVAILABLE value chagned!");
                }
                WifiSmartSwitcher.this.checkWifiScanAlwaysAvailabled();
            }
        });
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(IS_SMART_ENABLE), false, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                if (WifiSmartSwitcher.DEBUG) {
                    Log.d(WifiSmartSwitcher.TAG, "low power mode value chagned!");
                }
                WifiSmartSwitcher.this.checkLowPowerModeChanged();
            }
        });
    }

    public void dataInit() {
        if (DEBUG) {
            Log.d(TAG, "DataInit");
        }
        this.mHandler.sendEmptyMessage(1);
        checkWifiScanAlwaysAvailabled();
        checkLowPowerModeChanged();
    }

    public void switchOnOff(boolean on) {
        if (DEBUG) {
            Log.d(TAG, "switchOnOff");
        }
        this.mSwitchOn = on;
        if (this.mSwitchOn && this.mWifiScanAlwaysAvailabled && this.mFeatureStat && !this.mLowPowerModeEnabled) {
            this.mEnabled = true;
        } else {
            this.mEnabled = false;
        }
        if (this.mEnabled) {
            handleEnabled();
        }
    }

    public void featureState(boolean state) {
        this.mFeatureStat = state;
        if (!this.mFeatureStat) {
            this.mEnabled = false;
        } else if (this.mSwitchOn && this.mWifiScanAlwaysAvailabled && this.mFeatureStat && !this.mLowPowerModeEnabled) {
            this.mEnabled = true;
        }
        if (this.mEnabled) {
            handleEnabled();
        }
    }

    public void disable() {
        if (DEBUG) {
            Log.d(TAG, "Disable");
        }
        this.mEnabled = false;
    }

    private void handleEnabled() {
        if (DEBUG) {
            Log.d(TAG, "handleEnabled");
        }
        if (this.mApFoundLast) {
            sendMessage(34);
        }
    }

    private void sendMessage(int message) {
        this.mHandler.sendEmptyMessage(message);
    }

    private void sendMessage(int message, Object obj) {
        this.mHandler.obtainMessage(message, obj).sendToTarget();
    }

    private void sendDelayedMessage(int message, int delay) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(message), (long) delay);
    }

    private void sendDelayedMessage(int message, Object obj, int delay) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(message, obj), (long) delay);
    }

    private List<WifiNetworkRecord> getAvailableNetworks() {
        if (DEBUG) {
            Log.d(TAG, "getAvailableNetworks");
        }
        if (this.mWifiNetworkStateTraker == null) {
            return null;
        }
        return this.mWifiNetworkStateTraker.getWifiNetworkRecords();
    }

    private boolean checkAPFoundState(List<ScanResult> mScanResult) {
        if (DEBUG) {
            Log.d(TAG, "checkAPFoundState");
        }
        List<ScanResult> goodLevelScanResult = new ArrayList();
        List<ScanResult> candinateScanResultList = new ArrayList();
        List<WifiConfiguration> savedNetworkList = this.mWifiConfigManager.getSavedNetworksAll();
        if (savedNetworkList == null || savedNetworkList.size() <= 0) {
            if (DEBUG) {
                Log.d(TAG, "\tsavedNetworkList is null or empty!");
            }
            return false;
        }
        boolean found = false;
        for (ScanResult gsr : mScanResult) {
            if (gsr.level >= APFOUND_RSSI_MIN) {
                goodLevelScanResult.add(gsr);
                this.mCurrScanAvailableBSSID.add(gsr.BSSID);
                if (DEBUG) {
                    Log.d(TAG, "\tBSSID:" + gsr.BSSID + "\tlevel:" + gsr.level + "\tssid:" + gsr.SSID);
                }
            }
        }
        if (goodLevelScanResult.size() <= 0) {
            if (DEBUG) {
                Log.d(TAG, "\tall aps level is less than -78");
            }
            return false;
        }
        String configKey;
        for (ScanResult ssr : goodLevelScanResult) {
            configKey = WifiConfiguration.configKey(ssr);
            if (ssr != null && configKey != null) {
                boolean saved = false;
                for (WifiConfiguration wc : savedNetworkList) {
                    if (configKey.equals(wc.configKey())) {
                        saved = true;
                        break;
                    }
                }
                if (saved) {
                    ConnectedApInfo connectedApInfo = (ConnectedApInfo) this.mConnectedApInfoHashMap.get(configKey);
                    List -get0 = connectedApInfo != null ? connectedApInfo.mBSSIDList : null;
                    if (!(connectedApInfo == null || -get0 == null || -get0.size() <= 0)) {
                        if (connectedApInfo.mConnectCount.intValue() > 5) {
                            if (DEBUG) {
                                Log.d(TAG, "\tgot a possibled ap " + configKey + " connect count: " + connectedApInfo.mConnectCount + " BSSID:" + ssr.BSSID);
                            }
                            candinateScanResultList.add(ssr);
                        } else if (DEBUG) {
                            Log.d(TAG, "\t" + configKey + " connected count is less than " + 5);
                        }
                    }
                }
            } else if (DEBUG) {
                Log.d(TAG, "\t ssr or configKey is null!!");
            }
        }
        if (candinateScanResultList.size() <= 0) {
            if (DEBUG) {
                Log.d(TAG, "\tno ap found in mConnectedApInfoHashMap!");
            }
            return false;
        }
        List<WifiNetworkRecord> mAvailableNetworks = getAvailableNetworks();
        if (mAvailableNetworks == null) {
            if (DEBUG) {
                Log.d(TAG, "\tno available ap found!");
            }
            return false;
        }
        for (ScanResult csr : candinateScanResultList) {
            configKey = WifiConfiguration.configKey(csr);
            if (configKey != null) {
                for (WifiNetworkRecord record : mAvailableNetworks) {
                    if (configKey.equals(record.mConfigkey) && record.mNetworkValid) {
                        found = true;
                        if (DEBUG) {
                            Log.d(TAG, "\tfind a can access internet ap " + configKey + " BSSID:" + csr.BSSID);
                        }
                    }
                }
            }
        }
        if (!found && DEBUG) {
            Log.d(TAG, "\t find no can access internet ap!");
        }
        return found;
    }

    private void handleWifiScanResultEvent() {
        String str = null;
        if (DEBUG) {
            Log.d(TAG, "handleWifiScanResultEvent");
        }
        this.mScanResult = this.mWifiManager.getScanResults();
        this.mCurrScanAvailableBSSID.clear();
        this.mApFoundCurr = checkAPFoundState(this.mScanResult);
        if (this.mApFoundCurr) {
            this.mApLostCount = 0;
            this.mApLostFirstScanTime = 0;
            this.mLastFoundScanAvailableBSSID.clear();
            this.mLastFoundScanAvailableBSSID.addAll(this.mCurrScanAvailableBSSID);
        }
        if (this.mEnabled) {
            if (this.mConnManager.getNetworkInfo(1).isConnected()) {
                if (DEBUG) {
                    Log.d(TAG, "\twifi is in connected state update bssid list,and  do nothing!!!");
                    if (this.mLastConnectedConfigKey == null) {
                        if (DEBUG) {
                            Log.d(TAG, "\tmLastConnectedConfigKey should not be null,try update it! ");
                        }
                        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
                        WifiConfiguration wc = this.mWifiConfigManager.getWifiConfiguration(mWifiInfo != null ? mWifiInfo.getNetworkId() : -1);
                        if (wc != null) {
                            str = wc.configKey(false);
                        }
                        this.mLastConnectedConfigKey = str;
                    }
                    updateBSSIDList(this.mScanResult);
                }
                this.mApFoundCurr = true;
            } else {
                if (DEBUG) {
                    Log.d(TAG, "\tmApFoundCurr:" + this.mApFoundCurr + "\tmApFoundLast:" + this.mApFoundLast);
                }
                if (this.mApFoundCurr != this.mApFoundLast) {
                    if (this.mApFoundCurr) {
                        this.mHotspotStatus = 1;
                    } else {
                        this.mHotspotStatus = 2;
                    }
                    this.mHotspotEntrOrLeaveToken++;
                    if (this.mHotspotStatus == 1) {
                        sendDelayedMessage(32, Long.valueOf(this.mHotspotEntrOrLeaveToken), 0);
                    } else {
                        int apAlikePercent;
                        List<String> tmpList = new ArrayList();
                        tmpList.clear();
                        tmpList.addAll(this.mCurrScanAvailableBSSID);
                        tmpList.retainAll(this.mLastFoundScanAvailableBSSID);
                        if (this.mCurrScanAvailableBSSID.size() == 0 || this.mLastFoundScanAvailableBSSID.size() == 0) {
                            apAlikePercent = 0;
                        } else {
                            apAlikePercent = (tmpList.size() * 100) / this.mLastFoundScanAvailableBSSID.size();
                        }
                        if (apAlikePercent >= AP_AVAILABLE_INTERSECTION_PERCENT) {
                            if (DEBUG) {
                                Log.d(TAG, "ap not found but aps around is " + apAlikePercent + "% alike,treat it as in hotarea!");
                            }
                            this.mApFoundCurr = true;
                            this.mApLostCount = 0;
                            this.mApLostFirstScanTime = 0;
                        } else {
                            this.mApLostCount++;
                            if (DEBUG) {
                                Log.d(TAG, "\tmApLostCount=" + this.mApLostCount);
                            }
                            if (this.mApLostFirstScanTime > 0 && System.currentTimeMillis() - this.mApLostFirstScanTime > 8000) {
                                Log.d(TAG, "\t APFOUND_DOUBLE_CHECK_LOST_TIME timeout !");
                                this.mApLostCount = 4;
                            }
                            if (this.mApLostCount < 4) {
                                sendDelayedMessage(33, Long.valueOf(this.mHotspotEntrOrLeaveToken), APFOUND_DOUBLE_CHECK_LOST_DELAY);
                                if (this.mApLostCount == 1) {
                                    this.mApLostFirstScanTime = System.currentTimeMillis();
                                }
                                this.mApFoundCurr = true;
                            } else {
                                Log.d(TAG, "\tcontinues " + this.mApLostCount + " scan lost or double check timeout!");
                            }
                        }
                    }
                } else if (this.mApFoundCurr) {
                    if (DEBUG) {
                        Log.d(TAG, "\tin hot area,check if in the same hotspot area since last scan!");
                    }
                    this.mLastScanAvailableBSSID.retainAll(this.mCurrScanAvailableBSSID);
                    if (this.mLastScanAvailableBSSID.size() == 0) {
                        if (DEBUG) {
                            Log.d(TAG, "\tdifferent hotspot area,treat it as enter");
                        }
                        this.mHotspotEntrOrLeaveToken++;
                        sendDelayedMessage(32, Long.valueOf(this.mHotspotEntrOrLeaveToken), 0);
                    } else if (DEBUG) {
                        Log.d(TAG, "\tin the same hotspot area,we should do nothing!");
                    }
                }
            }
        } else if (DEBUG) {
            Log.d(TAG, "\tmEnabled set false,do nothing!!!");
        }
        this.mApFoundLast = this.mApFoundCurr;
        this.mLastScanAvailableBSSID.clear();
        this.mLastScanAvailableBSSID.addAll(this.mCurrScanAvailableBSSID);
    }

    private void handleNetworkStateEvent(Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "HandleNetworkBroadcast!");
        }
        String action = intent.getAction();
        NetworkInfo info = this.mConnManager.getNetworkInfo(((NetworkInfo) intent.getParcelableExtra("networkInfo")).getType());
        int dataType = info.getType();
        if (DEBUG) {
            Log.d(TAG, "\t>>>type:" + dataType + " wifistate:" + this.mConnManager.getNetworkInfo(1).getState());
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

    private void checkHotspotEnter(Object obj) {
        if (DEBUG) {
            Log.d(TAG, "checkHotspotEnter");
        }
        if (((Long) obj).longValue() != this.mHotspotEntrOrLeaveToken) {
            if (DEBUG) {
                Log.d(TAG, "\ttoken mismatch,do not send open wifi event!");
            }
            return;
        }
        sendMessage(34);
    }

    private void handleHotspotEnter() {
        if (DEBUG) {
            Log.d(TAG, "handleHotspotEnter");
        }
        openWifi();
    }

    private void checkHotspotLeave(Object obj) {
        if (DEBUG) {
            Log.d(TAG, "checkHotspotLeave");
        }
        this.mWifiManager.startScan();
    }

    private void handleHotspotLeave() {
        if (DEBUG) {
            Log.d(TAG, "handleHotspotLeave");
        }
        NetworkInfo info = this.mConnManager.getNetworkInfo(1);
        if (info != null && !info.isConnected()) {
            closeWifi();
        } else if (DEBUG) {
            Log.d(TAG, "\twifi is still in connected ,do nothing!");
        }
    }

    private void handlWifiStateEvent(Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "handlWifiStateEvent");
        }
        if (intent != null) {
            switch (intent.getIntExtra("wifi_state", 0)) {
                case 1:
                    if (DEBUG) {
                        Log.d(TAG, "\tWIFI_STATE_DISABLED!!!");
                    }
                    if (this.mWifiEnabled) {
                        this.mWifiEnabled = false;
                        if (DEBUG) {
                            Log.d(TAG, "\tsend message to clean&save data!");
                        }
                        sendMessage(4);
                        return;
                    } else if (DEBUG) {
                        Log.d(TAG, "\trepeat wifi disbale broadcast,ignore it");
                        return;
                    } else {
                        return;
                    }
                case 3:
                    if (DEBUG) {
                        Log.d(TAG, "\twifi is enabled!");
                    }
                    this.mWifiEnabled = true;
                    return;
                default:
                    return;
            }
        }
    }

    private void updateBSSIDList(List<ScanResult> srList) {
        if (DEBUG) {
            Log.d(TAG, "updateBSSIDList");
        }
        ConnectedApInfo connectedApInfo = (ConnectedApInfo) this.mConnectedApInfoHashMap.get(this.mLastConnectedConfigKey);
        if (connectedApInfo == null) {
            Log.d(TAG, "\tmLastConnectedConfigKey " + this.mLastConnectedConfigKey + " relatived ConnectedApInfo is null!");
            return;
        }
        List<String> bssidList = connectedApInfo.mBSSIDList;
        if (bssidList == null) {
            bssidList = new ArrayList();
        }
        if (srList != null && srList.size() > 0) {
            for (ScanResult sr : srList) {
                if (!bssidList.contains(sr.BSSID)) {
                    String mConfigKey = WifiConfiguration.configKey(sr);
                    if (mConfigKey != null && mConfigKey.equals(this.mLastConnectedConfigKey)) {
                        if (DEBUG) {
                            Log.d(TAG, "\t add new bssid " + sr.BSSID + " for ap " + mConfigKey);
                        }
                        bssidList.add(sr.BSSID);
                    }
                }
            }
        }
    }

    private void handleWifiConnected() {
        if (DEBUG) {
            Log.d(TAG, "handleWifiConnected");
        }
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            int mNetworkId = mWifiInfo.getNetworkId();
            WifiConfiguration wConf = this.mWifiConfigManager.getWifiConfiguration(mNetworkId);
            String mConfigKey = wConf != null ? wConf.configKey() : null;
            if (wConf == null || mConfigKey == null) {
                Log.d(TAG, "\twConf or mConfigKey is null,do nothing!");
                return;
            }
            boolean mIsContained;
            if (DEBUG) {
                Log.d(TAG, "\tConnected AP netId:" + mNetworkId + " configKey:" + mConfigKey);
            }
            ConnectedApInfo mConnectedApInfo = (ConnectedApInfo) this.mConnectedApInfoHashMap.get(mConfigKey);
            if (mConnectedApInfo != null) {
                mIsContained = true;
            } else {
                mConnectedApInfo = new ConnectedApInfo();
                mConnectedApInfo.mConfigKey = mConfigKey;
                mConnectedApInfo.mSSID = mWifiInfo.getSSID();
                mIsContained = false;
            }
            mConnectedApInfo.mConnectCount = Integer.valueOf(mConnectedApInfo.mConnectCount.intValue() + 1);
            this.mLastConnectedConfigKey = mConfigKey;
            updateBSSIDList(this.mWifiManager.getScanResults());
            synchronized (this.mLock) {
                this.mConnectedApInfoHashMap.put(this.mLastConnectedConfigKey, mConnectedApInfo);
                if (!mIsContained) {
                    this.mApCount = Integer.valueOf(this.mApCount.intValue() + 1);
                }
            }
            this.mApFoundLast = true;
            this.mApFoundCurr = true;
            sendMessage(2);
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "\tmWifiInfo is null,do nothing!");
        }
    }

    private void handleWifiDisconnected() {
        if (DEBUG) {
            Log.d(TAG, "handleWifiDisconnected");
        }
        if (this.mLastConnectedConfigKey == null) {
            if (DEBUG) {
                Log.d(TAG, "\tmLastConnectedConfigKey is null,repeat events,ignore!!");
            }
            return;
        }
        this.mLastConnectedConfigKey = null;
        sendMessage(4);
    }

    private void openWifi() {
        if (DEBUG) {
            Log.d(TAG, "openWifi");
        }
        if (!this.mWifiManager.isWifiEnabled()) {
            this.mWifiManager.setWifiEnabled(true);
        } else if (DEBUG) {
            Log.d(TAG, "\twifi has been enabled already!");
        }
    }

    private void closeWifi() {
        if (DEBUG) {
            Log.d(TAG, "closeWifi");
        }
        if (this.mWifiManager.isWifiEnabled()) {
            this.mWifiManager.setWifiEnabled(false);
        } else if (DEBUG) {
            Log.d(TAG, "\twifi has been disabled already!");
        }
    }

    private void dataClean() {
        if (DEBUG) {
            Log.d(TAG, "dataClean");
        }
        int count = 0;
        List<WifiConfiguration> mWifiConfigList = this.mWifiConfigManager.getSavedNetworks();
        if (mWifiConfigList == null || mWifiConfigList.size() <= 0) {
            if (DEBUG) {
                Log.d(TAG, "mWifiConfigList is null or 0 size");
            }
            return;
        }
        Iterator iterator = this.mConnectedApInfoHashMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            ConnectedApInfo mConnectedApInfo = (ConnectedApInfo) this.mConnectedApInfoHashMap.get(key);
            String -get1 = mConnectedApInfo != null ? mConnectedApInfo.mConfigKey : null;
            if (!(mConnectedApInfo == null || -get1 == null)) {
                boolean found = false;
                for (WifiConfiguration wc : mWifiConfigList) {
                    if (-get1.equals(wc.configKey())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (DEBUG) {
                        Log.d(TAG, "\tap configKey:" + -get1 + " not in configuredNetworks,clean it!");
                    }
                    iterator.remove();
                    this.mConnectedApInfoHashMap.remove(key);
                    this.mApCount = Integer.valueOf(this.mApCount.intValue() - 1);
                    count++;
                }
            }
        }
        if (count > 0) {
            sendMessage(2);
        }
    }

    private void dataLoad() {
        if (DEBUG) {
            Log.d(TAG, "dataLoad");
        }
        try {
            FileInputStream fis = new FileInputStream("/data/misc/wifi/connectedapinfo.conf");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            this.mConnectedApInfoHashMap.clear();
            this.mApCount = Integer.valueOf(0);
            String mBSSIDStr = null;
            String mSSID = null;
            String mConfigKey = null;
            Integer mConnectCount = Integer.valueOf(0);
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    br.close();
                    isr.close();
                    fis.close();
                    return;
                } else if (line.equals(TAG_SEPERATOR)) {
                    ConnectedApInfo connectedApInfo = new ConnectedApInfo();
                    connectedApInfo.mBSSIDList = new ArrayList();
                    if (mBSSIDStr != null) {
                        for (String str : mBSSIDStr.split(TAG_BSSID_SEPERATOR)) {
                            connectedApInfo.mBSSIDList.add(str);
                        }
                    }
                    connectedApInfo.mSSID = mSSID;
                    connectedApInfo.mConfigKey = mConfigKey;
                    connectedApInfo.mConnectCount = mConnectCount;
                    synchronized (this.mLock) {
                        this.mConnectedApInfoHashMap.put(connectedApInfo.mConfigKey, connectedApInfo);
                        this.mApCount = Integer.valueOf(this.mApCount.intValue() + 1);
                        if (DEBUG) {
                            Log.d(TAG, "\tadd a new item configKey:" + connectedApInfo.mConfigKey);
                        }
                    }
                } else {
                    int colon = line.indexOf(61);
                    String key = line.substring(0, colon).trim();
                    String value = line.substring(colon + 1).trim();
                    if (DEBUG) {
                        Log.d(TAG, "key=" + key + ",value=" + value);
                    }
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
            Log.d(TAG, "\texcption happed!");
            e.printStackTrace();
        }
    }

    private void dataSave() {
        if (DEBUG) {
            Log.d(TAG, "dataSave");
        }
        ConnectedApInfo connectedApInfo = new ConnectedApInfo();
        try {
            FileOutputStream fos = new FileOutputStream("/data/misc/wifi/connectedapinfo.conf");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            for (String key : this.mConnectedApInfoHashMap.keySet()) {
                connectedApInfo = (ConnectedApInfo) this.mConnectedApInfoHashMap.get(key);
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
                    line = (((line + "BSSID_LIST=" + bssidStr + TAG_NEWLINE) + "SSID=" + connectedApInfo.mSSID + TAG_NEWLINE) + "CONNECT_COUNT=" + connectedApInfo.mConnectCount + TAG_NEWLINE) + "****\n";
                    if (DEBUG) {
                        Log.d(TAG, line);
                    }
                    bw.write(line);
                }
            }
            bw.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dataShow() {
        if (DEBUG) {
            Log.d(TAG, "dataShow(),current ap count:" + this.mApCount);
        }
        if (DEBUG) {
            Log.d(TAG, "\tmConnectedApInfoHashMap in details!");
        }
        for (String key : this.mConnectedApInfoHashMap.keySet()) {
            ConnectedApInfo mConnectedApInfo = (ConnectedApInfo) this.mConnectedApInfoHashMap.get(key);
            if (mConnectedApInfo != null && DEBUG) {
                Log.d(TAG, "\n\tCONFIG_KEY=" + mConnectedApInfo.mConfigKey + "\n\t" + "SSID" + "=" + mConnectedApInfo.mSSID + "\n\t" + TAG_BSSID_LIST + "=" + mConnectedApInfo.mBSSIDList + "\n\t" + TAG_CONNECT_COUNT + " =" + mConnectedApInfo.mConnectCount + "\n\t" + TAG_SEPERATOR);
            }
        }
        if (DEBUG) {
            Log.d(TAG, "\nsorted mConnectedApInfoWeightHashMap!");
        }
        if (DEBUG) {
            Log.d(TAG, "\tother parameters:");
        }
        if (DEBUG) {
            Log.d(TAG, "\n\tmLastConnectedConfigKey:\t" + this.mLastConnectedConfigKey + "\n\tmWifiEnabled:\t" + this.mWifiEnabled + "\n\tmApCount:\t" + this.mApCount + "\n\tCONFIGFILE:\t" + "/data/misc/wifi/connectedapinfo.conf" + "\n\tmApFoundLast:\t" + this.mApFoundLast + "\n\tmApFoundCurr:\t" + this.mApFoundCurr + "\n\tmHotspotStatus:\t" + this.mHotspotStatus + "\n\tmWifiScanAlwaysAvailabled:\t" + this.mWifiScanAlwaysAvailabled + "\n\tmSwitchOn:\t" + this.mSwitchOn + "\n\tmEnabled:\t" + this.mEnabled);
        }
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DEBUG = true;
        } else {
            DEBUG = false;
        }
    }
}
