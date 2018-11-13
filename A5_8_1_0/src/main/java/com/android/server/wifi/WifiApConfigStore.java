package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class WifiApConfigStore {
    private static final int AP_CONFIG_FILE_VERSION = 2;
    private static final String DEFAULT_AP_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/softap.conf");
    private static final String ENABLE_STA_SAP = "ENABLE_STA_SAP_CONCURRENCY:";
    static final int PSK_MAX_LEN = 63;
    static final int PSK_MIN_LEN = 8;
    private static final int RAND_SSID_INT_MAX = 9999;
    private static final int RAND_SSID_INT_MIN = 1000;
    private static final String SAP_CHANNEL = "SAP_CHANNEL:";
    private static final String SEPARATOR_KEY = "\n";
    static final int SSID_MAX_LEN = 32;
    static final int SSID_MIN_LEN = 1;
    private static final String TAG = "WifiApConfigStore";
    private static final String mConcurrencyCfgTemplateFile = "/vendor/etc/wifi/wifi_concurrency_cfg.txt";
    private ArrayList<Integer> mAllowed2GChannel;
    private final String mApConfigFile;
    private final BackupManagerProxy mBackupManagerProxy;
    private String mBridgeInterfaceName;
    private final Context mContext;
    private boolean mDualSapBuildEnabled;
    private boolean mDualSapStatus;
    private int mSoftApChannel;
    private boolean mSoftApCreateIntf;
    private String mSoftApInterfaceName;
    private boolean mStaSapConcurrentMode;
    private WifiConfiguration mWifiApConfig;
    private String[] mdualApInterfaces;

    WifiApConfigStore(Context context, BackupManagerProxy backupManagerProxy) {
        this(context, backupManagerProxy, DEFAULT_AP_CONFIG_FILE);
    }

    WifiApConfigStore(Context context, BackupManagerProxy backupManagerProxy, String apConfigFile) {
        int i = 0;
        this.mWifiApConfig = null;
        this.mAllowed2GChannel = null;
        this.mSoftApCreateIntf = false;
        this.mSoftApChannel = 0;
        this.mBridgeInterfaceName = null;
        this.mSoftApInterfaceName = null;
        this.mStaSapConcurrentMode = false;
        this.mDualSapBuildEnabled = false;
        this.mDualSapStatus = false;
        this.mdualApInterfaces = null;
        this.mContext = context;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mApConfigFile = apConfigFile;
        String ap2GChannelListStr = this.mContext.getResources().getString(17039715);
        Log.d(TAG, "2G band allowed channels are:" + ap2GChannelListStr);
        if (ap2GChannelListStr != null) {
            this.mAllowed2GChannel = new ArrayList();
            for (String tmp : ap2GChannelListStr.split(",")) {
                this.mAllowed2GChannel.add(Integer.valueOf(Integer.parseInt(tmp)));
            }
        }
        this.mWifiApConfig = loadApConfiguration(this.mApConfigFile);
        if (this.mWifiApConfig == null) {
            Log.d(TAG, "Fallback to use default AP configuration");
            this.mWifiApConfig = getDefaultApConfiguration();
            writeApConfiguration(this.mApConfigFile, this.mWifiApConfig);
        }
        String[] softApInterfaces = this.mContext.getResources().getStringArray(17236047);
        int length = softApInterfaces.length;
        while (i < length) {
            String intf = softApInterfaces[i];
            if (intf.equals("wlan0")) {
                this.mSoftApInterfaceName = intf;
            } else if (intf.equals("softap0")) {
                this.mSoftApInterfaceName = intf;
                this.mSoftApCreateIntf = true;
            } else if (intf.equals("wifi_br0")) {
                this.mBridgeInterfaceName = intf;
            }
            i++;
        }
        if (this.mSoftApInterfaceName != null) {
            SystemProperties.set("persist.vendor.fst.wifi.sap.interface", this.mSoftApInterfaceName);
        }
        if (ensureConcurrencyFileExist()) {
            readConcurrencyConfig();
        }
        this.mDualSapBuildEnabled = this.mContext.getResources().getBoolean(17957065);
        this.mdualApInterfaces = this.mContext.getResources().getStringArray(17236053);
    }

    public synchronized boolean getStaSapConcurrency() {
        return this.mStaSapConcurrentMode;
    }

    public synchronized String getSapInterface() {
        return this.mSoftApInterfaceName;
    }

    public synchronized String getBridgeInterface() {
        return this.mBridgeInterfaceName;
    }

    public synchronized int getConfigFileChannel() {
        return this.mSoftApChannel;
    }

    public synchronized boolean isSapNewIntfRequired() {
        return this.mSoftApCreateIntf;
    }

    public synchronized boolean isDualSapSupported() {
        return this.mDualSapBuildEnabled;
    }

    public synchronized String[] getDualSapInterfaces() {
        return this.mdualApInterfaces;
    }

    public synchronized boolean getDualSapStatus() {
        return this.mDualSapStatus;
    }

    public synchronized void setDualSapStatus(boolean enable) {
        this.mDualSapStatus = enable;
    }

    public synchronized WifiConfiguration getApConfiguration() {
        return this.mWifiApConfig;
    }

    public synchronized void setApConfiguration(WifiConfiguration config) {
        if (config == null) {
            this.mWifiApConfig = getDefaultApConfiguration();
        } else {
            this.mWifiApConfig = config;
        }
        writeApConfiguration(this.mApConfigFile, this.mWifiApConfig);
        this.mBackupManagerProxy.notifyDataChanged();
    }

    public ArrayList<Integer> getAllowed2GChannel() {
        return this.mAllowed2GChannel;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00d8 A:{SYNTHETIC, Splitter: B:41:0x00d8} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00d8 A:{SYNTHETIC, Splitter: B:41:0x00d8} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00fb A:{SYNTHETIC, Splitter: B:47:0x00fb} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00fb A:{SYNTHETIC, Splitter: B:47:0x00fb} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static WifiConfiguration loadApConfiguration(String filename) {
        IOException e;
        WifiConfiguration wifiConfiguration;
        Throwable th;
        DataInputStream in = null;
        try {
            DataInputStream in2;
            WifiConfiguration config = new WifiConfiguration();
            try {
                in2 = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
            } catch (IOException e2) {
                e = e2;
                wifiConfiguration = config;
                try {
                    Log.e(TAG, "Error reading hotspot configuration " + e);
                    wifiConfiguration = null;
                    if (in != null) {
                    }
                    return wifiConfiguration;
                } catch (Throwable th2) {
                    th = th2;
                    if (in != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Error closing hotspot configuration during read" + e3);
                    }
                }
                throw th;
            }
            try {
                int version = in2.readInt();
                if (version == 1 || version == 2) {
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
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e32) {
                            Log.e(TAG, "Error closing hotspot configuration during read" + e32);
                        }
                    }
                    wifiConfiguration = config;
                    return wifiConfiguration;
                }
                Log.e(TAG, "Bad version on hotspot configuration file");
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e322) {
                        Log.e(TAG, "Error closing hotspot configuration during read" + e322);
                    }
                }
                return null;
            } catch (IOException e4) {
                e322 = e4;
                in = in2;
                wifiConfiguration = config;
                Log.e(TAG, "Error reading hotspot configuration " + e322);
                wifiConfiguration = null;
                if (in != null) {
                }
                return wifiConfiguration;
            } catch (Throwable th4) {
                th = th4;
                in = in2;
                wifiConfiguration = config;
                if (in != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e322 = e5;
            Log.e(TAG, "Error reading hotspot configuration " + e322);
            wifiConfiguration = null;
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3222) {
                    Log.e(TAG, "Error closing hotspot configuration during read" + e3222);
                }
            }
            return wifiConfiguration;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00c0 A:{SYNTHETIC, Splitter: B:31:0x00c0} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void writeApConfiguration(String filename, WifiConfiguration config) {
        IOException e;
        Throwable th;
        DataOutputStream out = null;
        try {
            DataOutputStream out2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            try {
                Log.d(TAG, "do writeApConfiguration " + config.SSID);
                out2.writeInt(2);
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
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "Error closing hotspot configuration during write" + e2);
                    }
                }
                out = out2;
            } catch (IOException e3) {
                e2 = e3;
                out = out2;
            } catch (Throwable th2) {
                th = th2;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e2 = e4;
            try {
                Log.e(TAG, "Error writing hotspot configuration" + e2);
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e22) {
                        Log.e(TAG, "Error closing hotspot configuration during write" + e22);
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e222) {
                        Log.e(TAG, "Error closing hotspot configuration during write" + e222);
                    }
                }
                throw th;
            }
        }
    }

    private WifiConfiguration getDefaultApConfiguration() {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = SystemProperties.get("ro.oppo.market.enname");
        if (config.SSID == null || config.SSID.equals("")) {
            config.SSID = SystemProperties.get("ro.oppo.market.name", "OPPO");
        }
        config.allowedKeyManagement.set(4);
        if (isVodafoneOperator()) {
            String randomUUID = UUID.randomUUID().toString();
            config.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
        } else {
            config.preSharedKey = "12345678";
        }
        return config;
    }

    private static boolean isVodafoneOperator() {
        String operator = SystemProperties.get("ro.oppo.operator", "null");
        if ("AU".equalsIgnoreCase(SystemProperties.get("ro.oppo.regionmark", "null")) && ("VODAFONE".equalsIgnoreCase(operator) || "VODAFONE_PREPAID".equalsIgnoreCase(operator) || "VODAFONE_POSTPAID".equalsIgnoreCase(operator))) {
            return true;
        }
        return false;
    }

    private static int getRandomIntForDefaultSsid() {
        return new Random().nextInt(9000) + 1000;
    }

    public static WifiConfiguration generateLocalOnlyHotspotConfig(Context context) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = context.getResources().getString(17041117) + "_" + getRandomIntForDefaultSsid();
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
        } else if (ssid.length() < 1 || ssid.length() > 32) {
            Log.d(TAG, "SSID for softap configuration string size must be at least 1 and not more than 32");
            return false;
        } else {
            try {
                ssid.getBytes(StandardCharsets.UTF_8);
                return true;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "softap config SSID verification failed: malformed string " + ssid);
                return false;
            }
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
        boolean hasPreSharedKey = TextUtils.isEmpty(preSharedKey) ^ 1;
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

    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ee A:{SYNTHETIC, Splitter: B:37:0x00ee} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ee A:{SYNTHETIC, Splitter: B:37:0x00ee} */
    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConcurrencyConfig() {
        IOException e;
        BufferedReader reader = null;
        try {
            if (mConcurrencyCfgTemplateFile != null) {
                Log.d(TAG, "mConcurrencyCfgTemplateFile : /vendor/etc/wifi/wifi_concurrency_cfg.txt");
            }
            BufferedReader reader2 = new BufferedReader(new FileReader(mConcurrencyCfgTemplateFile));
            try {
                for (String key = reader2.readLine(); key != null; key = reader2.readLine()) {
                    if (key != null) {
                        Log.d(TAG, "mConcurrencyCfgTemplateFile line: " + key);
                    }
                    if (key.startsWith(ENABLE_STA_SAP)) {
                        try {
                            this.mStaSapConcurrentMode = Integer.parseInt(key.replace(ENABLE_STA_SAP, "").replace(SEPARATOR_KEY, "")) == 1;
                            Log.d(TAG, "mConcurrencyCfgTemplateFile EnableConcurrency = " + this.mStaSapConcurrentMode);
                        } catch (NumberFormatException e2) {
                            Log.e(TAG, "mConcurrencyCfgTemplateFile: incorrect format :" + key);
                        }
                    }
                    if (key.startsWith(SAP_CHANNEL)) {
                        try {
                            this.mSoftApChannel = Integer.parseInt(key.replace(SAP_CHANNEL, "").replace(SEPARATOR_KEY, ""));
                            Log.d(TAG, "mConcurrencyCfgTemplateFile SAPChannel = " + this.mSoftApChannel);
                        } catch (NumberFormatException e3) {
                            Log.e(TAG, "mConcurrencyCfgTemplateFile: incorrect format :" + key);
                        }
                    }
                }
                reader = reader2;
            } catch (EOFException e4) {
                reader = reader2;
            } catch (IOException e5) {
                e = e5;
                reader = reader2;
            }
        } catch (EOFException e6) {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (Exception e7) {
                    Log.e(TAG, "mConcurrencyCfgTemplateFile: Error closing file" + e7);
                }
            }
            if (reader != null) {
            }
        } catch (IOException e8) {
            e = e8;
            Log.e(TAG, "mConcurrencyCfgTemplateFile: Error parsing configuration" + e);
            if (reader != null) {
            }
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e72) {
                Log.e(TAG, "mConcurrencyCfgTemplateFile: Error closing file" + e72);
            }
        }
    }

    private boolean ensureConcurrencyFileExist() {
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(mConcurrencyCfgTemplateFile)));
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            return true;
        } catch (Exception e2) {
            Log.e(TAG, "ensureConcurrencyFile template file doesnt exist" + e2);
            return false;
        }
    }
}
