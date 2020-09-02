package com.color.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ColorNetworkUtil {
    public static final int AIRPLANE_MODE_ON_STR = 0;
    private static final String DEFAULT_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final String DEFAULT_HTTP_URL_IN_CHINA = "http://www.baidu.com";
    private static final String KEY_NETWORK_MONITOR_AVAILABLE = "oppo.comm.network.monitor.available";
    private static final String KEY_NETWORK_MONITOR_PORTAL = "oppo.comm.network.monitor.portal";
    private static final String KEY_NETWORK_MONITOR_SSID = "oppo.comm.network.monitor.ssid";
    public static final int MOBILE_AND_WLAN_NETWORK_NOT_CONNECT_STR = 1;
    public static final int NETWORK_CONNECT_OK_STR = -1;
    public static final int NO_NETWORK_CONNECT_STR = 3;
    public static final String TAG = "ColorNetworkUtil";
    public static final int WLAN_NEED_LOGIN_STR = 2;
    private static String mCurrSSID;
    /* access modifiers changed from: private */
    public static boolean mIsBluetoothTetherConnected = false;
    private static BluetoothProfile.ServiceListener mProfileServiceListener = new ProfileServiceListener();
    /* access modifiers changed from: private */
    public static BluetoothPan mService;

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connect = (ConnectivityManager) context.getSystemService("connectivity");
        if (connect.getNetworkInfo(1).getState() != NetworkInfo.State.CONNECTED) {
            return false;
        }
        mCurrSSID = connect.getNetworkInfo(1).getExtraInfo();
        return true;
    }

    public static boolean isMobileDataConnected(Context context) {
        if (((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }
        return false;
    }

    public static boolean isSimInserted(Context context, int slotId) {
        return ((TelephonyManager) context.getSystemService("phone")).hasIccCard(slotId);
    }

    private static boolean hasSimCard(Context context) {
        boolean hasSimCard = false;
        try {
            if (isSimInserted(context, 0) || isSimInserted(context, 1)) {
                hasSimCard = true;
            }
            return hasSimCard;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    private static boolean isNotChineseOperator(Context context) {
        return false;
    }

    public static void onClickLoginBtn(Context context) {
        String url;
        if (isNotChineseOperator(context)) {
            url = DEFAULT_HTTP_URL;
        } else {
            url = DEFAULT_HTTP_URL_IN_CHINA;
        }
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        intent.setFlags(272629760);
        context.startActivity(intent);
    }

    public static Boolean isAirplaneMode(Context context) {
        boolean z = false;
        if (Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
            z = true;
        }
        return Boolean.valueOf(z);
    }

    private static int getCaptivePortalStr(Context context, String url) {
        String ssid = Settings.Global.getString(context.getContentResolver(), KEY_NETWORK_MONITOR_SSID);
        Log.d(TAG, "mCurrSSID=" + mCurrSSID + ", ssid=" + ssid);
        String str = mCurrSSID;
        if (str == null || ssid == null || !str.equals(ssid)) {
            return 3;
        }
        boolean isPortal = false;
        if (Settings.Global.getInt(context.getContentResolver(), KEY_NETWORK_MONITOR_AVAILABLE, 0) == 1) {
            return -1;
        }
        if (Settings.Global.getInt(context.getContentResolver(), KEY_NETWORK_MONITOR_PORTAL, 0) == 1) {
            isPortal = true;
        }
        if (isPortal) {
            return 2;
        }
        return 3;
    }

    public static int getErrorString(Context context, String url) {
        getBluetoothTether(context);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        closeProxy();
        if (isAirplaneMode(context).booleanValue() && !isWifiConnected(context) && !mIsBluetoothTetherConnected) {
            return 0;
        }
        if (isWifiConnected(context) || mIsBluetoothTetherConnected) {
            return getCaptivePortalStr(context, url);
        }
        if (!hasSimCard(context)) {
            return 3;
        }
        if (isMobileDataConnected(context)) {
            return -1;
        }
        return 1;
    }

    public static void getBluetoothTether(Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getState() == 12) {
            adapter.getProfileProxy(context.getApplicationContext(), mProfileServiceListener, 5);
        }
    }

    private static class ProfileServiceListener implements BluetoothProfile.ServiceListener {
        private ProfileServiceListener() {
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            List<BluetoothDevice> connectedDevicesList;
            AtomicReference<BluetoothPan> sBluetoothPan = new AtomicReference<>();
            sBluetoothPan.set((BluetoothPan) proxy);
            BluetoothPan unused = ColorNetworkUtil.mService = (BluetoothPan) proxy;
            BluetoothPan bluetoothPan = sBluetoothPan.get();
            if (bluetoothPan != null && (connectedDevicesList = bluetoothPan.getConnectedDevices()) != null && connectedDevicesList.size() > 0) {
                boolean unused2 = ColorNetworkUtil.mIsBluetoothTetherConnected = true;
            }
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int profile) {
            boolean unused = ColorNetworkUtil.mIsBluetoothTetherConnected = false;
        }
    }

    private static void closeProxy() {
        if (mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(5, mService);
                mService = null;
            } catch (Throwable t) {
                Log.e(TAG, "Error cleaning up PAN proxy", t);
            }
        }
    }
}
