package com.color.inner.net.wifi;

import android.net.wifi.OppoMirrorWifiConfiguration;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import java.lang.reflect.Method;

public class WifiConfigurationWrapper {
    private static final String TAG = "WifiConfigWrapper";

    private WifiConfigurationWrapper() {
    }

    public static int getApChannel(WifiConfiguration wifiConfiguration) {
        try {
            return wifiConfiguration.apChannel;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setApChannel(WifiConfiguration wifiConfiguration, int apChannel) {
        try {
            wifiConfiguration.apChannel = apChannel;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static int getApBand(WifiConfiguration wifiConfiguration) {
        try {
            return wifiConfiguration.apBand;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setApBand(WifiConfiguration wifiConfiguration, int apBand) {
        try {
            wifiConfiguration.apBand = apBand;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void setWapiPsk(WifiConfiguration wifiConfiguration, String wapiPsk) {
        try {
            OppoMirrorWifiConfiguration.wapiPsk.set(wifiConfiguration, wapiPsk);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void setWapiPskType(WifiConfiguration wifiConfiguration, int wapiPskType) {
        try {
            OppoMirrorWifiConfiguration.wapiPskType.set(wifiConfiguration, wapiPskType);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void setWapiCertSel(WifiConfiguration wifiConfiguration, String wapiCertSel) {
        try {
            OppoMirrorWifiConfiguration.wapiCertSel.set(wifiConfiguration, wapiCertSel);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void setWapiCertSelMode(WifiConfiguration wifiConfiguration, int wapiCertSelMode) {
        try {
            OppoMirrorWifiConfiguration.wapiCertSelMode.set(wifiConfiguration, wapiCertSelMode);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static String getWapiPsk(WifiConfiguration wifiConfiguration) {
        try {
            return (String) OppoMirrorWifiConfiguration.wapiPsk.get(wifiConfiguration);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    public static int getWapiPskType(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration == null) {
            return -1;
        }
        try {
            return OppoMirrorWifiConfiguration.wapiPskType.get(wifiConfiguration);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static String getWapiCertSel(WifiConfiguration wifiConfiguration) {
        try {
            return (String) OppoMirrorWifiConfiguration.wapiCertSel.get(wifiConfiguration);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    public static int getWapiCertSelMode(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration == null) {
            return -1;
        }
        try {
            return OppoMirrorWifiConfiguration.wapiCertSelMode.get(wifiConfiguration);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static class KeyMgmtWrapper {
        public static int getWapiPSK() {
            return ((Integer) WifiConfigurationWrapper.callMethodByReflect(null, WifiConfiguration.KeyMgmt.class, "getWapiPSK")).intValue();
        }

        public static int getWapiCERT() {
            return ((Integer) WifiConfigurationWrapper.callMethodByReflect(null, WifiConfiguration.KeyMgmt.class, "getWapiCERT")).intValue();
        }
    }

    /* access modifiers changed from: private */
    public static Object callMethodByReflect(Object object, Class<?> clazz, String methodName) {
        if (object != null) {
            try {
                clazz = object.getClass();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return null;
            }
        }
        Method method = clazz.getDeclaredMethod(methodName, new Class[0]);
        method.setAccessible(true);
        return method.invoke(object, new Object[0]);
    }
}
