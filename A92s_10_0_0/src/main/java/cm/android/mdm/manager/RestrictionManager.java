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
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import cm.android.mdm.util.CustomizeServiceManager;
import java.util.List;

public class RestrictionManager extends RestrictionBaseManager {
    private static final int BLACK_LIST = 1;
    private static final String BLUETOOTH_POLICY = "persist.sys.bluetooth_policy";
    private static final String CLICKABLE = "1";
    private static final boolean DEBUG = true;
    private static final String GREY = "1";
    private static final int MODE_BT_DISABLED = 0;
    private static final int MODE_BT_ENABLED = 3;
    private static final int MODE_BT_NORMAL = 2;
    private static final int MODE_BT_WHITELIST = 1;
    private static final String NORMAL = "0";
    private static final int NORMAL_INSTALL = 0;
    private static final String OPPO_CUSTOMIZE_SERVICE_NAME = "oppocustomize";
    private static final String PERSIST_SYS_WIFI_CLICKABLE = "persist.sys.wifi_clickable";
    private static final String PERSIST_SYS_WIFI_GREY = "persist.sys.wifi_grey";
    public static final String SETTING_MTP_TRANSFER_ENABLED = "MTP_TRANSFER_ENABLED";
    public static final String SETTING_OTG_ENABLED = "OTG_ENABLED";
    public static final String SETTING_ZQ_ADB_ENABLED = "ZQ_ADB_ENABLED";
    private static final int START_NETWORK = 1;
    private static final int STOP_NETWORK = 2;
    private static final String TAG = "RestrictionManager";
    private static final String UNCLICKABLE = "0";
    private static final int WHITE_LIST = 2;
    private static final String WLAN_POLICY = "persist.sys.wifi_policy";
    private static final String prefix = "persist.sys.oem_";
    private Context mContext;
    private ServiceHandler mServiceHandler;
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager = null;

    public RestrictionManager(Context context) {
        this.mContext = context;
        HandlerThread thread = new HandlerThread("CustomizeControler");
        thread.start();
        this.mServiceHandler = new ServiceHandler(thread.getLooper());
        this.mTelephonyManager = TelephonyManager.getDefault();
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
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
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_CLICKABLE, "0");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "unlock wifi and unlock UI...");
            CustomizeServiceManager.setProp("persist.sys.wifi_disable", "0");
            CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_CLICKABLE, "1");
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            if (this.mWifiManager != null) {
                Log.d(TAG, "open wifi...");
                this.mWifiManager.setWifiEnabled(DEBUG);
            }
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isWifiDisabled() {
        if (SystemProperties.get("persist.sys.wifi_disable", "0").equals("1")) {
            return DEBUG;
        }
        return false;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean setWlanPolicies(int mode) {
        int prePolicy = getWlanPolicies();
        try {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            if (this.mWifiManager == null) {
                return false;
            }
            CustomizeServiceManager.setProp(WLAN_POLICY, "2");
            if (mode == 0) {
                Log.d(TAG, "lock wifi and lock UI...");
                this.mWifiManager.setWifiEnabled(false);
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_CLICKABLE, "0");
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_GREY, "1");
            } else if (mode == 1) {
                Log.d(TAG, "close wifi with scan always mode and lock UI ...");
                Settings.Secure.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 1);
                this.mWifiManager.setWifiEnabled(false);
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_CLICKABLE, "0");
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_GREY, "1");
            } else if (mode == 2) {
                Log.d(TAG, "unlock wifi and unlock UI...");
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_CLICKABLE, "1");
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_GREY, "0");
            } else if (mode == MODE_BT_ENABLED) {
                this.mWifiManager.setWifiEnabled(false);
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_CLICKABLE, "1");
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_GREY, "0");
            } else if (mode == 4) {
                this.mWifiManager.setWifiEnabled(DEBUG);
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_CLICKABLE, "1");
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_GREY, "0");
            } else if (mode != 5) {
                CustomizeServiceManager.setProp(WLAN_POLICY, String.valueOf(prePolicy));
                return false;
            } else {
                this.mWifiManager.setWifiEnabled(DEBUG);
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_CLICKABLE, "0");
                CustomizeServiceManager.setProp(PERSIST_SYS_WIFI_GREY, "0");
            }
            CustomizeServiceManager.setProp(WLAN_POLICY, String.valueOf(mode));
            return DEBUG;
        } catch (Exception ex) {
            CustomizeServiceManager.setProp(WLAN_POLICY, String.valueOf(prePolicy));
            Log.e(TAG, "setWlanPolicies exception!", ex);
            return false;
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public int getWlanPolicies() {
        return Integer.valueOf(SystemProperties.get(WLAN_POLICY, "2")).intValue();
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setBluetoothDisabled(boolean disabled) {
        if (disabled) {
            Log.d(TAG, "lock bluetooth and lock UI...");
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.disable();
                }
                Thread.sleep(1000);
                CustomizeServiceManager.setProp("persist.sys.bt_disable", "1");
                CustomizeServiceManager.setProp("persist.sys.bt_clickable", "0");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "unlock bluetooth and unlock UI...");
            CustomizeServiceManager.setProp("persist.sys.bt_disable", "0");
            CustomizeServiceManager.setProp("persist.sys.bt_clickable", "1");
            BluetoothAdapter bluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter2 != null) {
                bluetoothAdapter2.enable();
            }
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setBluetoothEnabled(boolean enabled) {
        if (enabled) {
            Log.d(TAG, "setBluetoothEnabled lock bluetooth and lock UI...");
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.enable();
                }
                Thread.sleep(1000);
                CustomizeServiceManager.setProp(BLUETOOTH_POLICY, String.valueOf((int) MODE_BT_ENABLED));
                CustomizeServiceManager.setProp("persist.sys.bt_clickable", "0");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "setBluetoothEnabled unlock bluetooth and unlock UI...");
            CustomizeServiceManager.setProp(BLUETOOTH_POLICY, String.valueOf(2));
            CustomizeServiceManager.setProp("persist.sys.bt_clickable", "1");
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isBluetoothDisabled() {
        if (SystemProperties.get("persist.sys.bt_disable", "0").equals("1")) {
            return DEBUG;
        }
        return false;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isBluetoothEnabled() {
        int value = SystemProperties.getInt(BLUETOOTH_POLICY, 0);
        Log.d(TAG, "isBluetoothEnabled, value: " + value);
        if (value == MODE_BT_ENABLED) {
            return DEBUG;
        }
        return false;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setWifiApDisabled(boolean disabled) {
        if (disabled) {
            Log.d(TAG, "lock softap and lock UI...");
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                if (connectivityManager != null) {
                    Log.d(TAG, "close softap...");
                    connectivityManager.stopTethering(0);
                }
                Thread.sleep(1000);
                CustomizeServiceManager.setProp("persist.sys.ap_disable", "1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "unlock softap and unlock UI...");
            CustomizeServiceManager.setProp("persist.sys.ap_disable", "0");
            ConnectivityManager connectivityManager2 = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (connectivityManager2 != null) {
                Log.d(TAG, "open softap...");
                connectivityManager2.startTethering(0, false, null);
            }
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isWifiApDisabled() {
        if (SystemProperties.get("persist.sys.ap_disable", "0").equals("1")) {
            return DEBUG;
        }
        return false;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setUSBDataDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setDB(SETTING_MTP_TRANSFER_ENABLED, 0);
        } else {
            CustomizeServiceManager.setDB(SETTING_MTP_TRANSFER_ENABLED, 1);
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isUSBDataDisabled() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), SETTING_MTP_TRANSFER_ENABLED, 1) == 1) {
            return false;
        }
        return DEBUG;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setExternalStorageDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setProp("persist.sys.exStorage_support", "0");
        } else {
            CustomizeServiceManager.setProp("persist.sys.exStorage_support", "1");
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isExternalStorageDisabled() {
        if (SystemProperties.getInt("persist.sys.exStorage_support", 1) == 1) {
            return false;
        }
        return DEBUG;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                int i = msg.what;
                if (i == 1) {
                    CustomizeServiceManager.setDataEnabled(RestrictionManager.DEBUG);
                } else if (i != 2) {
                    Log.w(RestrictionManager.TAG, "what=" + msg.what);
                } else {
                    CustomizeServiceManager.setDataEnabled(false);
                }
            }
        }
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
        try {
            Log.d(TAG, "propGetEnable " + prop + ": " + defval);
            if (Integer.parseInt(SystemProperties.get(prop, defval)) != 0) {
                return DEBUG;
            }
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "getProp error :" + ex.getMessage());
            return DEBUG;
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setMobileDataDisabled(boolean disabled) {
        if (disabled) {
            propSetEnable("persist.sys.oem_db", "0");
        } else {
            propSetEnable("persist.sys.oem_db", "-1");
        }
        if (disabled) {
            Message msg = Message.obtain();
            msg.what = 2;
            ServiceHandler serviceHandler = this.mServiceHandler;
            if (serviceHandler != null) {
                serviceHandler.sendMessage(msg);
                return;
            }
            return;
        }
        Message msg2 = Message.obtain();
        msg2.what = 1;
        ServiceHandler serviceHandler2 = this.mServiceHandler;
        if (serviceHandler2 != null) {
            serviceHandler2.sendMessage(msg2);
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isMobileDataDisabled() {
        return propGetEnable("persist.sys.oem_db", "-1") ^ DEBUG;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setVoiceDisabled(boolean disabled) {
        propSetEnable("persist.sys.oem_vi", disabled ^ DEBUG);
        propSetEnable("persist.sys.oem_vo", disabled ^ DEBUG);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isVoiceDisabled() {
        return propGetEnable("persist.sys.oem_vo", "-1") ^ DEBUG;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setSMSDisabled(boolean disabled) {
        propSetEnable("persist.sys.oem_ss", disabled ^ DEBUG);
        propSetEnable("persist.sys.oem_sr", disabled ^ DEBUG);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isSMSDisabled() {
        return propGetEnable("persist.sys.oem_ss", "-1") ^ DEBUG;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setAdbDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setDB(SETTING_ZQ_ADB_ENABLED, 0);
            CustomizeServiceManager.setDB("adb_enabled", 0);
            return;
        }
        CustomizeServiceManager.setDB(SETTING_ZQ_ADB_ENABLED, 1);
        CustomizeServiceManager.setDB("adb_enabled", 1);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isAdbDisabled() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), SETTING_ZQ_ADB_ENABLED, 1) == 1) {
            return false;
        }
        return DEBUG;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setUSBOtgDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setProp("persist.sys.oppo.otg_support", "false");
            CustomizeServiceManager.setProp("persist.vendor.otg.switch", "false");
            CustomizeServiceManager.setDB(SETTING_OTG_ENABLED, 0);
            return;
        }
        CustomizeServiceManager.setProp("persist.sys.oppo.otg_support", "true");
        CustomizeServiceManager.setProp("persist.vendor.otg.switch", "true");
        CustomizeServiceManager.setDB(SETTING_OTG_ENABLED, 1);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isUSBOtgDisabled() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), SETTING_OTG_ENABLED, 1) == 1) {
            return false;
        }
        return DEBUG;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setGPSDisabled(boolean disabled) {
        if (disabled) {
            Log.d(TAG, "lock gps and lock UI...");
            try {
                CustomizeServiceManager.openCloseGps(false);
                Thread.sleep(1000);
                CustomizeServiceManager.setProp("persist.sys.gps_disable", "1");
                CustomizeServiceManager.setProp("persist.sys.gps_clickable", "0");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "unlock gps and unlock UI...");
            CustomizeServiceManager.setProp("persist.sys.gps_disable", "0");
            CustomizeServiceManager.setProp("persist.sys.gps_clickable", "1");
            CustomizeServiceManager.openCloseGps(DEBUG);
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isGPSDisabled() {
        if (SystemProperties.get("persist.sys.gps_disable", "0").equals("1")) {
            return DEBUG;
        }
        return false;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setDeveloperOptionsDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setProp("persist.sys.developer_disable", "true");
            CustomizeServiceManager.setDevelopmentEnabled(false);
            return;
        }
        CustomizeServiceManager.setProp("persist.sys.developer_disable", "false");
        CustomizeServiceManager.setDevelopmentEnabled(DEBUG);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isDeveloperOptionsDisabled() {
        return SystemProperties.getBoolean("persist.sys.developer_disable", false);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setNFCDisabled(boolean disabled) {
        if (disabled) {
            CustomizeServiceManager.setProp("persist.sys.nfc_disable", "1");
            CustomizeServiceManager.openCloseNFC(false);
            return;
        }
        CustomizeServiceManager.setProp("persist.sys.nfc_disable", "0");
        CustomizeServiceManager.openCloseNFC(DEBUG);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isNFCDisabled() {
        return SystemProperties.getBoolean("persist.sys.nfc_disable", false);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setTaskButtonDisabled(boolean disabled) {
        if (disabled) {
            try {
                CustomizeServiceManager.setProp("persist.sys.custom_task_disable", "true");
            } catch (Exception e) {
                Log.e(TAG, "setTaskButtonDisabled error", e);
            }
        } else {
            CustomizeServiceManager.setProp("persist.sys.custom_task_disable", "false");
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isTaskButtonDisabled() {
        try {
            return SystemProperties.getBoolean("persist.sys.custom_task_disable", false);
        } catch (Exception e) {
            Log.e(TAG, "isTaskButtonDisabled error", e);
            return false;
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setHomeButtonDisabled(boolean disabled) {
        if (disabled) {
            try {
                CustomizeServiceManager.setProp("persist.sys.custom_home_disable", "true");
            } catch (Exception e) {
                Log.e(TAG, "setHomeButtonDisabled error", e);
            }
        } else {
            CustomizeServiceManager.setProp("persist.sys.custom_home_disable", "false");
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isHomeButtonDisabled() {
        try {
            return SystemProperties.getBoolean("persist.sys.custom_home_disable", false);
        } catch (Exception e) {
            Log.e(TAG, "isHomeButtonDisabled error", e);
            return false;
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setBackButtonDisabled(boolean disabled) {
        if (disabled) {
            try {
                CustomizeServiceManager.setProp("persist.sys.custom_back_disable", "true");
            } catch (Exception e) {
                Log.e(TAG, "setBackButtonDisabled error", e);
            }
        } else {
            CustomizeServiceManager.setProp("persist.sys.custom_back_disable", "false");
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isBackButtonDisabled() {
        try {
            return SystemProperties.getBoolean("persist.sys.custom_back_disable", false);
        } catch (Exception e) {
            Log.e(TAG, "isBackButtonDisabled error", e);
            return false;
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setSendNotificationDisabled(boolean disabled) {
        CustomizeServiceManager.setSendNotificationDisabled(disabled);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isSendNotificationDisabled() {
        return CustomizeServiceManager.isSendNotificationDisabled(this.mContext);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setStatusBarExpandPanelDisabled(boolean disable) {
        CustomizeServiceManager.setStatusBarExpandPanelDisabled(disable);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isStatusBarExpandPanelDisabled() {
        return CustomizeServiceManager.isStatusBarExpandPanelDisabled();
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setSafeModeDisabled(boolean disabled) {
        if (disabled) {
            try {
                CustomizeServiceManager.setProp("persist.sys.disable.safemode", "1");
            } catch (Exception e) {
                Log.e(TAG, "setSafeModeDisabled error", e);
            }
        } else {
            CustomizeServiceManager.setProp("persist.sys.disable.safemode", "0");
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean isSafeModeDisabled() {
        Log.d(TAG, "isSafeModeDisabled start");
        int temp = SystemProperties.getInt("persist.sys.disable.safemode", 0);
        Log.d(TAG, "isSafeModeDisabled finish");
        if (temp == 0 || temp != 1) {
            return false;
        }
        return DEBUG;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setNetworkRestriction(int pattern) {
        CustomizeServiceManager.setNetworkRestriction(pattern);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void addNetworkRestriction(int pattern, List<String> list) {
        CustomizeServiceManager.addNetworkRestriction(pattern, list);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void removeNetworkRestriction(int pattern, List<String> list) {
        CustomizeServiceManager.removeNetworkRestriction(pattern, list);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void removeNetworkRestrictionAll(int pattern) {
        CustomizeServiceManager.removeNetworkRestrictionAll(pattern);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public boolean setCameraPolicies(int mode) {
        if (mode == 0) {
            CustomizeServiceManager.setProp("persist.sys.camera_available", "0");
        } else if (mode != 1) {
            return false;
        } else {
            CustomizeServiceManager.setProp("persist.sys.camera_available", "1");
        }
        return DEBUG;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public int getCameraPolicies() {
        return SystemProperties.getInt("persist.sys.camera_available", 1);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void setAppRestrictionPolicies(int pattern) {
        if (pattern == 1) {
            SystemProperties.set("persist.sys.enable_black_list", "true");
            SystemProperties.set("persist.sys.enable_white_list", "false");
        } else if (pattern == 2) {
            SystemProperties.set("persist.sys.enable_black_list", "false");
            SystemProperties.set("persist.sys.enable_white_list", "true");
        } else if (pattern == 0) {
            SystemProperties.set("persist.sys.enable_black_list", "false");
            SystemProperties.set("persist.sys.enable_white_list", "false");
        }
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public int getAppRestrictionPolicies() {
        boolean blacklist = SystemProperties.getBoolean("persist.sys.enable_black_list", false);
        boolean whitelist = SystemProperties.getBoolean("persist.sys.enable_white_list", false);
        if (blacklist && !whitelist) {
            return 1;
        }
        if (!whitelist || blacklist) {
            return 0;
        }
        return 2;
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public List<String> getAppRestriction(int pattern) {
        return CustomizeServiceManager.getAppRestriction(pattern);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void addAppRestriction(int pattern, List<String> pkgs) {
        CustomizeServiceManager.addAppRestriction(pattern, pkgs);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void removeAppRestriction(int pattern, List<String> pkgs) {
        CustomizeServiceManager.removeAppRestriction(pattern, pkgs);
    }

    @Override // cm.android.mdm.interfaces.IRestrictionManager, cm.android.mdm.manager.RestrictionBaseManager
    public void removeAllAppRestriction(int pattern) {
        CustomizeServiceManager.removeAllAppRestriction(pattern);
    }
}
