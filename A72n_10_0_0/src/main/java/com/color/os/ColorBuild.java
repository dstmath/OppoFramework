package com.color.os;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
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
    public static final int ColorOS_5_2 = 11;
    public static final int ColorOS_6_0 = 12;
    public static final int ColorOS_6_1 = 13;
    public static final int ColorOS_6_2 = 14;
    public static final int ColorOS_6_7 = 15;
    public static final int ColorOS_7_0 = 16;
    public static final int ColorOS_7_1 = 17;
    public static final int ColorOS_7_2 = 18;
    public static final String MARKET = getString("ro.oppo.market.name");
    private static final String SECURE_SETTINGS_BLUETOOTH_NAME = "bluetooth_name";
    private static final String SECURE_SETTINGS_DEVICE_NAME = "oppo_device_name";
    public static final int UNKNOWN = 0;
    private static final String[] VERSIONS = {"V1.0", "V1.2", "V1.4", "V2.0", "V2.1", "V3.0", "V3.1", "V3.2", "V5.0", "V5.1", "V5.2", "V6.0", "V6.1", "V6.2", "V6.7", "V7", "V7.1", "V7.2", null};

    public static class VERSION {
        public static final String RELEASE = ColorBuild.getString("ro.build.version.opporom");
    }

    public static int getColorOSVERSION() {
        for (int i = VERSIONS.length - 2; i >= 0; i--) {
            if (!TextUtils.isEmpty(VERSION.RELEASE) && VERSION.RELEASE.startsWith(VERSIONS[i])) {
                return i + 1;
            }
        }
        return 16;
    }

    public static boolean setDeviceName(String name) {
        return true;
    }

    public static String getDeviceName() {
        return null;
    }

    public static String getDeviceName(Context context) {
        String name = Settings.Secure.getString(context.getContentResolver(), SECURE_SETTINGS_DEVICE_NAME);
        if (name != null && name.length() != 0 && !name.trim().isEmpty()) {
            return name;
        }
        String name2 = SystemProperties.get("ro.oppo.market.name", "");
        if (TextUtils.isEmpty(name2)) {
            return Build.MODEL;
        }
        putDeviceName(context, name2);
        return name2;
    }

    public static void putDeviceName(Context context, String deviceName) {
        if (deviceName != null) {
            Settings.Secure.putString(context.getContentResolver(), SECURE_SETTINGS_DEVICE_NAME, deviceName);
        }
    }

    public static void setDeviceName(Context context, String deviceName) {
        WifiP2pManager wifiP2pManager;
        WifiP2pManager.Channel channel;
        if (deviceName != null && !deviceName.trim().isEmpty()) {
            putDeviceName(context, deviceName);
            if (!(!((WifiManager) context.getSystemService("wifi")).isWifiEnabled() || (wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE)) == null || (channel = wifiP2pManager.initialize(context, context.getMainLooper(), null)) == null)) {
                wifiP2pManager.setDeviceName(channel, deviceName, null);
            }
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                adapter.setName(deviceName);
            }
        }
    }

    /* access modifiers changed from: private */
    public static String getString(String property) {
        return SystemProperties.get(property, "unknown");
    }
}
