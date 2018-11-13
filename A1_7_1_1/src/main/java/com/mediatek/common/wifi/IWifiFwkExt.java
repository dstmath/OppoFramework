package com.mediatek.common.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import java.util.List;

public interface IWifiFwkExt {
    public static final String ACTION_RESELECTION_AP = "android.net.wifi.WIFI_RESELECTION_AP";
    public static final String ACTION_WIFI_FAILOVER_GPRS_DIALOG = "android.intent.action_WIFI_FAILOVER_GPRS_DIALOG";
    public static final String AUTOCONNECT_ENABLE_ALL_NETWORKS = "com.mediatek.common.wifi.AUTOCONNECT_ENABLE_ALL_NETWORK";
    public static final String AUTOCONNECT_SETTINGS_CHANGE = "com.mediatek.common.wifi.AUTOCONNECT_SETTINGS_CHANGE";
    public static final int BEST_SIGNAL_THRESHOLD = -79;
    public static final int BSS_EXPIRE_AGE = 10;
    public static final int BSS_EXPIRE_COUNT = 1;
    public static final int DEFAULT_FRAMEWORK_SCAN_INTERVAL_MS = 15000;
    public static final String EXTRA_NOTIFICATION_NETWORKID = "network_id";
    public static final String EXTRA_NOTIFICATION_SSID = "ssid";
    public static final String EXTRA_SHOW_RESELECT_DIALOG_FLAG = "SHOW_RESELECT_DIALOG";
    public static final int MIN_INTERVAL_CHECK_WEAK_SIGNAL_MS = 60000;
    public static final int MIN_NETWORKS_NUM = 2;
    public static final int NOTIFY_TYPE_RESELECT = 1;
    public static final int NOTIFY_TYPE_SWITCH = 0;
    public static final int OP_01 = 1;
    public static final int OP_03 = 3;
    public static final int OP_NONE = 0;
    public static final String RESELECT_DIALOG_CLASSNAME = "com.mediatek.op01.plugin.WifiReselectApDialog";
    public static final long SUSPEND_NOTIFICATION_DURATION = 3600000;
    public static final int WEAK_SIGNAL_THRESHOLD = -85;
    public static final String WIFISETTINGS_CLASSNAME = "com.android.settings.Settings$WifiSettingsActivity";
    public static final int WIFI_CONNECT_REMINDER_ALWAYS = 0;
    public static final String WIFI_NOTIFICATION_ACTION = "android.net.wifi.WIFI_NOTIFICATION";

    int defaultFrameworkScanIntervalMs();

    String getApDefaultSsid();

    int getSecurity(ScanResult scanResult);

    int getSecurity(WifiConfiguration wifiConfiguration);

    boolean handleNetworkReselection();

    boolean hasConnectableAp();

    boolean hasCustomizedAutoConnect();

    int hasNetworkSelection();

    void init();

    boolean isPppoeSupported();

    boolean isWifiConnecting(int i, List<Integer> list);

    boolean needRandomSsid();

    void setCustomizedWifiSleepPolicy(Context context);

    void setNotificationVisible(boolean z);

    boolean shouldAutoConnect();

    void suspendNotification(int i);
}
