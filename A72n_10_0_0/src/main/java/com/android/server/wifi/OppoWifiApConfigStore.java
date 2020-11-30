package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;

public class OppoWifiApConfigStore extends WifiApConfigStore {
    private static final String DEFAULT_AP_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/softap.conf");
    private static final String DEFAULT_WIFI_SHARING_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/wifi_sharing.conf");
    private static final String TAG = OppoWifiApConfigStore.class.getSimpleName();
    private ArrayList<Integer> mAllowed2GChannel;
    private final String mApConfigFile;
    private final BackupManagerProxy mBackupManagerProxy;
    private final String mSharingConfigFile;
    private WifiConfiguration mWifiApConfig;
    private WifiConfiguration mWifiSharingConfig;

    OppoWifiApConfigStore(Context context, Looper looper, BackupManagerProxy backupManagerProxy, FrameworkFacade frameworkFacade) {
        this(context, looper, backupManagerProxy, frameworkFacade, DEFAULT_AP_CONFIG_FILE);
    }

    OppoWifiApConfigStore(Context context, Looper looper, BackupManagerProxy backupManagerProxy, FrameworkFacade frameworkFacade, String apConfigFile) {
        super(context, looper, backupManagerProxy, frameworkFacade, apConfigFile);
        this.mWifiApConfig = null;
        this.mAllowed2GChannel = null;
        this.mWifiSharingConfig = null;
        this.mSharingConfigFile = DEFAULT_WIFI_SHARING_CONFIG_FILE;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mApConfigFile = apConfigFile;
        String ap2GChannelListStr = context.getResources().getString(17039775);
        Log.d(TAG, "2G band allowed channels are:" + ap2GChannelListStr);
        if (ap2GChannelListStr != null) {
            this.mAllowed2GChannel = new ArrayList<>();
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
        this.mWifiSharingConfig = loadApConfiguration(this.mSharingConfigFile);
        if (this.mWifiSharingConfig == null) {
            Log.d(TAG, "Fallback to use default wifi sharing configuration");
            this.mWifiSharingConfig = getDefaultSharingConfiguration();
            writeApConfiguration(this.mSharingConfigFile, this.mWifiSharingConfig);
        }
    }

    public synchronized WifiConfiguration getSharingConfiguration() {
        return this.mWifiSharingConfig;
    }

    public synchronized void setSharingConfiguration(WifiConfiguration config) {
        if (config == null) {
            this.mWifiSharingConfig = getDefaultSharingConfiguration();
        } else {
            this.mWifiSharingConfig = config;
        }
        writeApConfiguration(this.mSharingConfigFile, this.mWifiSharingConfig);
        this.mBackupManagerProxy.notifyDataChanged();
    }

    private WifiConfiguration getDefaultSharingConfiguration() {
        return getDefaultApConfiguration();
    }
}
