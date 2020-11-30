package com.android.server.wifi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.wifi.V1_3.IWifiChip;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class WifiApConfigStore {
    public static final String ACTION_HOTSPOT_CONFIG_USER_TAPPED_CONTENT = "com.android.server.wifi.WifiApConfigStoreUtil.HOTSPOT_CONFIG_USER_TAPPED_CONTENT";
    @VisibleForTesting
    static final int AP_CHANNEL_DEFAULT = 0;
    private static final int AP_CONFIG_FILE_VERSION = 3;
    private static final String DEFAULT_AP_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/softap.conf");
    @VisibleForTesting
    static final int PSK_MAX_LEN = 63;
    @VisibleForTesting
    static final int PSK_MIN_LEN = 8;
    private static final int RAND_SSID_INT_MAX = 9999;
    private static final int RAND_SSID_INT_MIN = 1000;
    @VisibleForTesting
    static final int SSID_MAX_LEN = 32;
    @VisibleForTesting
    static final int SSID_MIN_LEN = 1;
    private static final String TAG = "WifiApConfigStore";
    private ArrayList<Integer> mAllowed2GChannel;
    private final String mApConfigFile;
    private final BackupManagerProxy mBackupManagerProxy;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private final FrameworkFacade mFrameworkFacade;
    private final Handler mHandler;
    private boolean mRequiresApBandConversion;
    private WifiConfiguration mWifiApConfig;

    WifiApConfigStore(Context context, Looper looper, BackupManagerProxy backupManagerProxy, FrameworkFacade frameworkFacade) {
        this(context, looper, backupManagerProxy, frameworkFacade, DEFAULT_AP_CONFIG_FILE);
    }

    WifiApConfigStore(Context context, Looper looper, BackupManagerProxy backupManagerProxy, FrameworkFacade frameworkFacade, String apConfigFile) {
        this.mWifiApConfig = null;
        this.mAllowed2GChannel = null;
        this.mRequiresApBandConversion = false;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiApConfigStore.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (((action.hashCode() == 765958520 && action.equals(WifiApConfigStore.ACTION_HOTSPOT_CONFIG_USER_TAPPED_CONTENT)) ? (char) 0 : 65535) != 0) {
                    Log.e(WifiApConfigStore.TAG, "Unknown action " + intent.getAction());
                    return;
                }
                WifiApConfigStore.this.handleUserHotspotConfigTappedContent();
            }
        };
        this.mContext = context;
        this.mHandler = new Handler(looper);
        this.mBackupManagerProxy = backupManagerProxy;
        this.mFrameworkFacade = frameworkFacade;
        this.mApConfigFile = apConfigFile;
        String ap2GChannelListStr = this.mContext.getResources().getString(17039775);
        Log.d(TAG, "2G band allowed channels are:" + ap2GChannelListStr);
        if (ap2GChannelListStr != null) {
            this.mAllowed2GChannel = new ArrayList<>();
            for (String tmp : ap2GChannelListStr.split(",")) {
                this.mAllowed2GChannel.add(Integer.valueOf(Integer.parseInt(tmp)));
            }
        }
        this.mRequiresApBandConversion = this.mContext.getResources().getBoolean(17891577);
        this.mWifiApConfig = loadApConfiguration(this.mApConfigFile);
        if (this.mWifiApConfig == null) {
            Log.d(TAG, "Fallback to use default AP configuration");
            this.mWifiApConfig = getDefaultApConfiguration();
            writeApConfiguration(this.mApConfigFile, this.mWifiApConfig);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HOTSPOT_CONFIG_USER_TAPPED_CONTENT);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, this.mHandler);
    }

    public synchronized WifiConfiguration getApConfiguration() {
        WifiConfiguration config = apBandCheckConvert(this.mWifiApConfig);
        if (this.mWifiApConfig != config) {
            Log.d(TAG, "persisted config was converted, need to resave it");
            this.mWifiApConfig = config;
            persistConfigAndTriggerBackupManagerProxy(this.mWifiApConfig);
        }
        return this.mWifiApConfig;
    }

    public synchronized void setApConfiguration(WifiConfiguration config) {
        if (config == null) {
            this.mWifiApConfig = getDefaultApConfiguration();
        } else {
            this.mWifiApConfig = apBandCheckConvert(config);
        }
        persistConfigAndTriggerBackupManagerProxy(this.mWifiApConfig);
    }

    public ArrayList<Integer> getAllowed2GChannel() {
        return this.mAllowed2GChannel;
    }

    public void notifyUserOfApBandConversion(String packageName) {
        Log.w(TAG, "ready to post notification - triggered by " + packageName);
        ((NotificationManager) this.mContext.getSystemService("notification")).notify(50, createConversionNotification());
    }

    private Notification createConversionNotification() {
        CharSequence title = this.mContext.getResources().getText(17041299);
        CharSequence contentSummary = this.mContext.getResources().getText(17041301);
        CharSequence content = this.mContext.getResources().getText(17041300);
        return new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS).setSmallIcon(17302845).setPriority(1).setCategory("sys").setContentTitle(title).setContentText(contentSummary).setContentIntent(getPrivateBroadcast(ACTION_HOTSPOT_CONFIG_USER_TAPPED_CONTENT)).setTicker(title).setShowWhen(false).setLocalOnly(true).setColor(this.mContext.getResources().getColor(17170460, this.mContext.getTheme())).setStyle(new Notification.BigTextStyle().bigText(content).setBigContentTitle(title).setSummaryText(contentSummary)).build();
    }

    private WifiConfiguration apBandCheckConvert(WifiConfiguration config) {
        if (this.mRequiresApBandConversion) {
            if (config.apBand == 1) {
                Log.w(TAG, "Supplied ap config band was 5GHz only, converting to ANY");
                WifiConfiguration convertedConfig = new WifiConfiguration(config);
                convertedConfig.apBand = -1;
                convertedConfig.apChannel = 0;
                return convertedConfig;
            }
        } else if (config.apBand == -1) {
            Log.w(TAG, "Supplied ap config band was ANY, converting to 5GHz");
            WifiConfiguration convertedConfig2 = new WifiConfiguration(config);
            convertedConfig2.apBand = 1;
            convertedConfig2.apChannel = 0;
            return convertedConfig2;
        }
        return config;
    }

    private void persistConfigAndTriggerBackupManagerProxy(WifiConfiguration config) {
        writeApConfiguration(this.mApConfigFile, this.mWifiApConfig);
        this.mBackupManagerProxy.notifyDataChanged();
    }

    protected static WifiConfiguration loadApConfiguration(String filename) {
        WifiConfiguration config;
        StringBuilder sb;
        DataInputStream in = null;
        try {
            config = new WifiConfiguration();
            DataInputStream in2 = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
            int version = in2.readInt();
            if (version >= 1) {
                if (version <= 3) {
                    config.SSID = in2.readUTF();
                    if (version >= 2) {
                        config.apBand = in2.readInt();
                        config.apChannel = in2.readInt();
                    }
                    int authType = in2.readInt();
                    config.allowedKeyManagement.set(authType);
                    if (authType != 0) {
                        config.preSharedKey = in2.readUTF();
                        if (!(config.preSharedKey == null || config.preSharedKey.length() <= 0 || authType == 1 || authType == 4)) {
                            config.allowedKeyManagement.clear();
                            config.allowedKeyManagement.set(4);
                            Log.d(TAG, "Wrong key mgmt set default to WPA2_PSK!");
                        }
                    }
                    try {
                        config.hiddenSSID = in2.readBoolean();
                    } catch (EOFException e) {
                        Log.e(TAG, "hiddenSSID error " + e);
                        config.hiddenSSID = false;
                    }
                    try {
                        in2.close();
                    } catch (IOException e2) {
                        e = e2;
                        sb = new StringBuilder();
                    }
                    return config;
                }
            }
            Log.e(TAG, "Bad version on hotspot configuration file");
            try {
                in2.close();
            } catch (IOException e3) {
                Log.e(TAG, "Error closing hotspot configuration during read" + e3);
            }
            return null;
            sb.append("Error closing hotspot configuration during read");
            sb.append(e);
            Log.e(TAG, sb.toString());
            return config;
        } catch (IOException e4) {
            Log.e(TAG, "Error reading hotspot configuration " + e4);
            config = null;
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e6) {
                    Log.e(TAG, "Error closing hotspot configuration during read" + e6);
                }
            }
            throw th;
        }
    }

    protected static void writeApConfiguration(String filename, WifiConfiguration config) {
        StringBuilder sb;
        DataOutputStream out = null;
        try {
            DataOutputStream out2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            Log.d(TAG, "do writeApConfiguration " + config.SSID);
            out2.writeInt(3);
            if (config.SSID != null) {
                out2.writeUTF(config.SSID);
            } else {
                out2.writeUTF(" ");
            }
            out2.writeInt(config.apBand);
            out2.writeInt(config.apChannel);
            int authType = config.getAuthType();
            out2.writeInt(authType);
            if (!(authType == 0 || config.preSharedKey == null)) {
                out2.writeUTF(config.preSharedKey);
            }
            out2.writeBoolean(config.hiddenSSID);
            try {
                out2.close();
                return;
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
            sb.append("Error closing hotspot configuration during write");
            sb.append(e);
            Log.e(TAG, sb.toString());
        } catch (IOException e2) {
            Log.e(TAG, "Error writing hotspot configuration" + e2);
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Error closing hotspot configuration during write" + e4);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public WifiConfiguration getDefaultApConfiguration() {
        WifiConfiguration config = new WifiConfiguration();
        if (needRandomSsid()) {
            String randomSSID = UUID.randomUUID().toString();
            config.SSID = "OPPO " + randomSSID.substring(0, 6);
        } else {
            config.SSID = SystemProperties.get("ro.oppo.market.enname");
            if (config.SSID == null || config.SSID.equals("")) {
                config.SSID = SystemProperties.get("ro.oppo.market.name", "OPPO");
            }
        }
        config.allowedKeyManagement.set(4);
        if (isUniquePasswdNeed()) {
            String randomUUID = UUID.randomUUID().toString();
            config.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
        } else {
            config.preSharedKey = "12345678";
        }
        return config;
    }

    private boolean isUniquePasswdNeed() {
        return !SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("CN");
    }

    private boolean needRandomSsid() {
        if ("EE".equalsIgnoreCase(SystemProperties.get("ro.oppo.operator", ""))) {
            return true;
        }
        return false;
    }

    private static int getRandomIntForDefaultSsid() {
        return new Random().nextInt(9000) + 1000;
    }

    public static WifiConfiguration generateLocalOnlyHotspotConfig(Context context, int apBand) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = context.getResources().getString(17041284) + "_" + getRandomIntForDefaultSsid();
        config.apBand = apBand;
        config.allowedKeyManagement.set(4);
        config.networkId = -2;
        String randomUUID = UUID.randomUUID().toString();
        config.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
        return config;
    }

    private static boolean validateApConfigSsid(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "SSID for softap configuration must be set.");
            return false;
        }
        try {
            byte[] ssid_bytes = ssid.getBytes(StandardCharsets.UTF_8);
            if (ssid_bytes.length >= 1) {
                if (ssid_bytes.length <= 32) {
                    return true;
                }
            }
            Log.d(TAG, "softap SSID is defined as UTF-8 and it must be at least 1 byte and not more than 32 bytes");
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "softap config SSID verification failed: malformed string " + ssid);
            return false;
        }
    }

    private static boolean validateApConfigPreSharedKey(String preSharedKey) {
        if (preSharedKey.length() < 8 || preSharedKey.length() > 63) {
            Log.d(TAG, "softap network password string size must be at least 8 and no more than 63");
            return false;
        }
        try {
            preSharedKey.getBytes(StandardCharsets.UTF_8);
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "softap network password verification failed: malformed string");
            return false;
        }
    }

    static boolean validateApWifiConfiguration(WifiConfiguration apConfig) {
        if (!validateApConfigSsid(apConfig.SSID)) {
            return false;
        }
        if (apConfig.allowedKeyManagement == null) {
            Log.d(TAG, "softap config key management bitset was null");
            return false;
        }
        String preSharedKey = apConfig.preSharedKey;
        boolean hasPreSharedKey = !TextUtils.isEmpty(preSharedKey);
        try {
            int authType = apConfig.getAuthType();
            if (authType == 0) {
                if (hasPreSharedKey) {
                    Log.d(TAG, "open softap network should not have a password");
                    return false;
                }
            } else if (authType != 4) {
                Log.d(TAG, "softap configs must either be open or WPA2 PSK networks");
                return false;
            } else if (!hasPreSharedKey) {
                Log.d(TAG, "softap network password must be set");
                return false;
            } else if (!validateApConfigPreSharedKey(preSharedKey)) {
                return false;
            }
            return true;
        } catch (IllegalStateException e) {
            Log.d(TAG, "Unable to get AuthType for softap config: " + e.getMessage());
            return false;
        }
    }

    private void startSoftApSettings() {
        this.mContext.startActivity(new Intent("com.android.settings.WIFI_TETHER_SETTINGS").addFlags(IWifiChip.ChipCapabilityMask.DUAL_BAND_DUAL_CHANNEL));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserHotspotConfigTappedContent() {
        startSoftApSettings();
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(50);
    }

    private PendingIntent getPrivateBroadcast(String action) {
        return this.mFrameworkFacade.getBroadcast(this.mContext, 0, new Intent(action).setPackage("android"), 134217728);
    }
}
