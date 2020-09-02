package com.mediatek.server.wifi;

import android.content.Context;
import android.hardware.wifi.hostapd.V1_0.IHostapd;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.util.ApConfigUtil;

public class MtkSoftApUtils {
    private static final String TAG = "MtkSoftApUtils";

    public static void stopP2p(Context context, Looper looper, WifiNative wifiNative) {
        if (context != null && wifiNative != null && wifiNative.getClientInterfaceName() != null) {
            WifiP2pManager wifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
            WifiP2pManager.Channel wifiP2pChannel = wifiP2pManager.initialize(context, looper, null);
            wifiP2pManager.cancelConnect(wifiP2pChannel, null);
            wifiP2pManager.removeGroup(wifiP2pChannel, null);
        }
    }

    public static void stopSoftAp(Context context) {
        if (context != null) {
            ((ConnectivityManager) context.getSystemService("connectivity")).stopTethering(0);
        }
    }

    public static boolean overwriteApChannelIfNeed(Context context, IHostapd.IfaceParams ifaceParams) {
        if (context == null || ifaceParams == null) {
            return false;
        }
        int fixChannel = SystemProperties.getInt("vendor.wifi.tethering.channel", 0);
        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        if (fixChannel > 0) {
            Log.d(TAG, "Disable ACS & overwrite hotspot op channel to " + fixChannel);
            ifaceParams.channelParams.enableAcs = false;
            ifaceParams.channelParams.channel = fixChannel;
            if (fixChannel <= 14) {
                ifaceParams.channelParams.band = 0;
            } else {
                ifaceParams.channelParams.band = 1;
            }
            return true;
        } else if (wifiManager.getCurrentNetwork() == null) {
            return false;
        } else {
            int staChannel = ApConfigUtil.convertFrequencyToChannel(wifiManager.getConnectionInfo().getFrequency());
            Log.d(TAG, "[STA+SAP] Need to config channel for STA+SAP case, getCurrentNetwork = " + wifiManager.getCurrentNetwork() + ", staChannel = " + staChannel);
            if (staChannel < 1 || staChannel > 14) {
                ifaceParams.channelParams.band = 1;
            } else {
                ifaceParams.channelParams.band = 0;
            }
            ifaceParams.channelParams.channel = staChannel;
            ifaceParams.channelParams.enableAcs = false;
            Log.d(TAG, "[STA+SAP] apBand = " + ifaceParams.channelParams.band + ", apChannel = " + ifaceParams.channelParams.channel);
            return true;
        }
    }
}
