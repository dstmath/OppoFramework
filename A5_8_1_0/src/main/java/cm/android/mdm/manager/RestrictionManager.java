package cm.android.mdm.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import cm.android.mdm.util.CustomizeServiceManager;

public class RestrictionManager extends RestrictionBaseManager {
    private static final boolean DEBUG = true;
    private static final String OPPO_CUSTOMIZE_SERVICE_NAME = "oppocustomize";
    public static final String SETTING_MTP_TRANSFER_ENABLED = "MTP_TRANSFER_ENABLED";
    public static final String SETTING_OTG_ENABLED = "OTG_ENABLED";
    public static final String SETTING_ZQ_ADB_ENABLED = "ZQ_ADB_ENABLED";
    private static final int START_NETWORK = 1;
    private static final int STOP_NETWORK = 2;
    private static final String TAG = "RestrictionManager";
    private static final String prefix = "persist.sys.oem_";
    private Context mContext;
    private ServiceHandler mServiceHandler;
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager = null;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                switch (msg.what) {
                    case RestrictionManager.START_NETWORK /*1*/:
                        CustomizeServiceManager.setDataEnabled(RestrictionManager.DEBUG);
                        break;
                    case RestrictionManager.STOP_NETWORK /*2*/:
                        CustomizeServiceManager.setDataEnabled(false);
                        break;
                    default:
                        Log.w(RestrictionManager.TAG, "what=" + msg.what);
                        return;
                }
            }
        }
    }

    public RestrictionManager(Context context) {
        this.mContext = context;
        HandlerThread thread = new HandlerThread("CustomizeControler");
        thread.start();
        this.mServiceHandler = new ServiceHandler(thread.getLooper());
        this.mTelephonyManager = TelephonyManager.getDefault();
    }

    public void setWifiDisabled(boolean disabled) {
        if (disabled) {
            Log.d(TAG, "lock wifi and lock UI...");
            try {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
                if (this.mWifiManager != null) {
                    Log.d(TAG, "close wifi...");
                    this.mWifiManager.setWifiEnabled(false);
                }
                Thread.sleep(1000);
                CustomizeServiceManager.setProp("persist.sys.wifi_disable", "1");
                CustomizeServiceManager.setProp("persist.sys.wifi_clickable", "0");
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        Log.d(TAG, "unlock wifi and unlock UI...");
        CustomizeServiceManager.setProp("persist.sys.wifi_disable", "0");
        CustomizeServiceManager.setProp("persist.sys.wifi_clickable", "1");
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (this.mWifiManager != null) {
            Log.d(TAG, "open wifi...");
            this.mWifiManager.setWifiEnabled(DEBUG);
        }
    }

    public boolean isWifiDisabled() {
        if (SystemProperties.get("persist.sys.wifi_disable", "0").equals("1")) {
            return DEBUG;
        }
        return false;
    }

    public void setBluetoothDisabled(boolean disabled) {
        BluetoothAdapter bluetoothAdapter;
        if (disabled) {
            Log.d(TAG, "lock bluetooth and lock UI...");
            try {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.disable();
                }
                Thread.sleep(1000);
                CustomizeServiceManager.setProp("persist.sys.bt_disable", "1");
                CustomizeServiceManager.setProp("persist.sys.bt_clickable", "0");
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        Log.d(TAG, "unlock bluetooth and unlock UI...");
        CustomizeServiceManager.setProp("persist.sys.bt_disable", "0");
        CustomizeServiceManager.setProp("persist.sys.bt_clickable", "1");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    public boolean isBluetoothDisabled() {
        if (SystemProperties.get("persist.sys.bt_disable", "0").equals("1")) {
            return DEBUG;
        }
        return false;
    }

    public void setWifiApDisabled(boolean disabled) {
        ConnectivityManager connectivityManager;
        if (disabled) {
            Log.d(TAG, "lock softap and lock UI...");
            try {
                connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                if (connectivityManager != null) {
                    Log.d(TAG, "close softap...");
                    connectivityManager.stopTethering(0);
                }
                Thread.sleep(1000);
                CustomizeServiceManager.setProp("persist.sys.ap_disable", "1");
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        Log.d(TAG, "unlock softap and unlock UI...");
        CustomizeServiceManager.setProp("persist.sys.ap_disable", "0");
        connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null) {
            Log.d(TAG, "open softap...");
            connectivityManager.startTethering(0, false, null);
        }
    }

    public boolean isWifiApDisabled() {
        if (SystemProperties.get("persist.sys.ap_disable", "0").equals("1")) {
            return DEBUG;
        }
        return false;
    }

    public void setUSBDataDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setDB(SETTING_MTP_TRANSFER_ENABLED, 0);
        } else {
            CustomizeServiceManager.setDB(SETTING_MTP_TRANSFER_ENABLED, START_NETWORK);
        }
    }

    public boolean isUSBDataDisabled() {
        if (Secure.getInt(this.mContext.getContentResolver(), SETTING_MTP_TRANSFER_ENABLED, START_NETWORK) == START_NETWORK) {
            return false;
        }
        return DEBUG;
    }

    public void setExternalStorageDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setProp("persist.sys.exStorage_support", "0");
        } else {
            CustomizeServiceManager.setProp("persist.sys.exStorage_support", "1");
        }
    }

    public boolean isExternalStorageDisabled() {
        if (SystemProperties.getInt("persist.sys.exStorage_support", START_NETWORK) == START_NETWORK) {
            return false;
        }
        return DEBUG;
    }

    private static void propSetEnable(String prop, boolean defval) {
        propSetEnable(prop, defval ? "1" : "0");
    }

    private static void propSetEnable(String prop, String defval) {
        try {
            Log.d(TAG, "propSetEnable " + prop + ": " + defval);
            CustomizeServiceManager.setProp(prop, defval);
        } catch (Exception ex) {
            Log.e(TAG, "setProp error :" + ex.getMessage());
        }
    }

    private static boolean propGetEnable(String prop, String defval) {
        boolean z = DEBUG;
        try {
            Log.d(TAG, "propGetEnable " + prop + ": " + defval);
            if (Integer.parseInt(SystemProperties.get(prop, defval)) == 0) {
                z = false;
            }
            return z;
        } catch (Exception ex) {
            Log.e(TAG, "getProp error :" + ex.getMessage());
            return DEBUG;
        }
    }

    public void setMobileDataDisabled(boolean disabled) {
        if (disabled) {
            propSetEnable("persist.sys.oem_db", "0");
        } else {
            propSetEnable("persist.sys.oem_db", "-1");
        }
        Message msg;
        if (disabled) {
            msg = Message.obtain();
            msg.what = STOP_NETWORK;
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg);
                return;
            }
            return;
        }
        msg = Message.obtain();
        msg.what = START_NETWORK;
        if (this.mServiceHandler != null) {
            this.mServiceHandler.sendMessage(msg);
        }
    }

    public boolean isMobileDataDisabled() {
        return propGetEnable("persist.sys.oem_db", "-1") ^ START_NETWORK;
    }

    public void setVoiceDisabled(boolean disabled) {
        propSetEnable("persist.sys.oem_vi", disabled ^ START_NETWORK);
        propSetEnable("persist.sys.oem_vo", disabled ^ START_NETWORK);
    }

    public boolean isVoiceDisabled() {
        return propGetEnable("persist.sys.oem_vo", "-1") ^ START_NETWORK;
    }

    public void setSMSDisabled(boolean disabled) {
        propSetEnable("persist.sys.oem_ss", disabled ^ START_NETWORK);
        propSetEnable("persist.sys.oem_sr", disabled ^ START_NETWORK);
    }

    public boolean isSMSDisabled() {
        return propGetEnable("persist.sys.oem_ss", "-1") ^ START_NETWORK;
    }

    public void setAdbDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setDB(SETTING_ZQ_ADB_ENABLED, 0);
            CustomizeServiceManager.setDB("adb_enabled", 0);
            return;
        }
        CustomizeServiceManager.setDB(SETTING_ZQ_ADB_ENABLED, START_NETWORK);
        CustomizeServiceManager.setDB("adb_enabled", START_NETWORK);
    }

    public boolean isAdbDisabled() {
        if (Secure.getInt(this.mContext.getContentResolver(), SETTING_ZQ_ADB_ENABLED, START_NETWORK) == START_NETWORK) {
            return false;
        }
        return DEBUG;
    }

    public void setUSBOtgDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setProp("persist.sys.oppo.otg_support", "false");
            CustomizeServiceManager.setDB(SETTING_OTG_ENABLED, 0);
            return;
        }
        CustomizeServiceManager.setProp("persist.sys.oppo.otg_support", "true");
        CustomizeServiceManager.setDB(SETTING_OTG_ENABLED, START_NETWORK);
    }

    public boolean isUSBOtgDisabled() {
        if (Secure.getInt(this.mContext.getContentResolver(), SETTING_OTG_ENABLED, START_NETWORK) == START_NETWORK) {
            return false;
        }
        return DEBUG;
    }

    public void setGPSDisabled(boolean disabled) {
        if (disabled) {
            Log.d(TAG, "lock gps and lock UI...");
            try {
                CustomizeServiceManager.openCloseGps(false);
                Thread.sleep(1000);
                CustomizeServiceManager.setProp("persist.sys.gps_disable", "1");
                CustomizeServiceManager.setProp("persist.sys.gps_clickable", "0");
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        Log.d(TAG, "unlock gps and unlock UI...");
        CustomizeServiceManager.setProp("persist.sys.gps_disable", "0");
        CustomizeServiceManager.setProp("persist.sys.gps_clickable", "1");
        CustomizeServiceManager.openCloseGps(DEBUG);
    }

    public boolean isGPSDisabled() {
        if (SystemProperties.get("persist.sys.gps_disable", "0").equals("1")) {
            return DEBUG;
        }
        return false;
    }

    public void setDeveloperOptionsDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setProp("persist.sys.developer_disable", "true");
        } else {
            CustomizeServiceManager.setProp("persist.sys.developer_disable", "false");
        }
    }

    public boolean isDeveloperOptionsDisabled() {
        return SystemProperties.getBoolean("persist.sys.developer_disable", false);
    }
}
