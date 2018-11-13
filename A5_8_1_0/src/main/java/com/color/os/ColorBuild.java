package com.color.os;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.text.TextUtils;

public class ColorBuild {
    public static final int ColorOS_1_0 = 1;
    public static final int ColorOS_1_2 = 2;
    public static final int ColorOS_1_4 = 3;
    public static final int ColorOS_2_0 = 4;
    public static final int ColorOS_2_1 = 5;
    public static final int ColorOS_3_0 = 6;
    public static final int ColorOS_3_1 = 7;
    public static final int ColorOS_3_2 = 8;
    public static final int ColorOS_5_0 = 9;
    public static final int ColorOS_5_1 = 10;
    public static final String MARKET = getString("ro.oppo.market.name");
    private static final String SECURE_SETTINGS_BLUETOOTH_NAME = "bluetooth_name";
    private static final String SECURE_SETTINGS_DEVICE_NAME = "oppo_device_name";
    public static final int UNKNOWN = 0;
    private static final String[] VERSIONS = new String[]{"V1.0", "V1.2", "V1.4", "V2.0", "V2.1", "V3.0", "V3.1", "V3.2", "V5.0", "V5.1", null};

    public static class VERSION {
        public static final String RELEASE = ColorBuild.getString("ro.build.version.opporom");
    }

    public static int getColorOSVERSION() {
        int i = VERSIONS.length - 2;
        while (i >= 0) {
            if (!TextUtils.isEmpty(VERSION.RELEASE) && VERSION.RELEASE.startsWith(VERSIONS[i])) {
                return i + 1;
            }
            i--;
        }
        return 0;
    }

    public static boolean setDeviceName(String name) {
        return true;
    }

    public static String getDeviceName() {
        return null;
    }

    public static String getDeviceName(Context context) {
        String name = Secure.getString(context.getContentResolver(), SECURE_SETTINGS_DEVICE_NAME);
        if (name != null && name.length() != 0 && (name.trim().isEmpty() ^ 1) != 0) {
            return name;
        }
        name = SystemProperties.get("ro.oppo.market.name", "");
        if (TextUtils.isEmpty(name)) {
            return Build.MODEL;
        }
        putDeviceName(context, name);
        return name;
    }

    public static void putDeviceName(Context context, String deviceName) {
        if (deviceName != null) {
            Secure.putString(context.getContentResolver(), SECURE_SETTINGS_DEVICE_NAME, deviceName);
        }
    }

    public static void setDeviceName(Context context, String deviceName) {
        if (deviceName != null && (deviceName.trim().isEmpty() ^ 1) != 0) {
            putDeviceName(context, deviceName);
            if (((WifiManager) context.getSystemService("wifi")).isWifiEnabled()) {
                WifiP2pManager wifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
                if (wifiP2pManager != null) {
                    Channel channel = wifiP2pManager.initialize(context, context.getMainLooper(), null);
                    if (channel != null) {
                        wifiP2pManager.setDeviceName(channel, deviceName, null);
                    }
                }
            }
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                adapter.setName(deviceName);
            }
        }
    }

    private static String getString(String property) {
        return SystemProperties.get(property, "unknown");
    }
}
