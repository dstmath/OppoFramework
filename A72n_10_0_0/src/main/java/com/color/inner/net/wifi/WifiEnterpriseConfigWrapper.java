package com.color.inner.net.wifi;

import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import oppo.net.wifi.OppoMirrorWifiEnterpriseConfig;

public class WifiEnterpriseConfigWrapper {
    private static final String TAG = "WifiEnterpriseConfigWrapper";

    private WifiEnterpriseConfigWrapper() {
    }

    public static void setSimNum(WifiEnterpriseConfig wifiEnterpriseConfig, int SIMNum) {
        try {
            OppoMirrorWifiEnterpriseConfig.setSimNum.call(wifiEnterpriseConfig, new Object[]{Integer.valueOf(SIMNum)});
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static String getSimNum(WifiEnterpriseConfig wifiEnterpriseConfig) {
        try {
            Object result = OppoMirrorWifiEnterpriseConfig.getSimNum.call(wifiEnterpriseConfig, new Object[0]);
            if (result instanceof Integer) {
                return String.valueOf(result);
            }
            if (result instanceof String) {
                return (String) result;
            }
            return "";
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }
}
