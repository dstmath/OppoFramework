package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothMap;
import android.bluetooth.BluetoothPbap;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioSystem;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IOppoCustomizeService;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceConnectivityManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import com.oppo.enterprise.mdmcoreservice.utils.HarmonyNetUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DeviceRestrictionManagerImpl extends IDeviceRestrictionManager.Stub {
    private static boolean DEBUG = false;
    private static String PERSIST_SYS_OEM_P_SST = "persist.sys.oem_p_sst";
    private static String PERSIST_SYS_OEM_P_ST = "persist.sys.oem_p_st";
    private static String PERSIST_SYS_OEM_S_SST = "persist.sys.oem_s_sst";
    private static String PERSIST_SYS_OEM_S_ST = "persist.sys.oem_s_st";
    private static final List<String> mBrowser = Arrays.asList("com.android.browser", "com.coloros.browser", "com.heytap.browser");
    private static final TextUtils.SimpleStringSplitter sBannedStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
    private final String DISABLE_SPLITSCREEN_ACTION = "enterprise.intent.action.DISABLE_SPLITSCREEN";
    private final String DISABLE_UNKNOWN_SOURCE_KEY = "oppo_settings_manager_unknownsource";
    private final String OPPO_SETTINGS_FORBID_GLOBALACTION = "forbid_globalaction_by_power";
    private final String OPPO_SETTINGS_FORBID_SPLITSCREEN = "forbid_splitscreen_by_ep";
    private final String SHORTCUT_ALLOWED_FILE_NAME = "shortcut_allowed_app.xml";
    private final String SHORTCUT_ALLOWED_FILE_PATH = "/data/oppo/common/";
    private Context mContext;
    private IOppoCustomizeService mCustService;
    private DevicePolicyManager mDpm;
    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        /* class com.oppo.enterprise.mdmcoreservice.service.managerimpl.DeviceRestrictionManagerImpl.AnonymousClass2 */

        public void onServiceDisconnected(int profile) {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            DeviceRestrictionManagerImpl.this.disconnect(profile, proxy);
        }
    };
    private PackageManager mPackageManager = null;
    private ServiceHandler mServiceHandler;
    private WifiManager mWifiManager = null;

    public Object getDeclaredField(Object target, String clsName, String fieldName) {
        Log.i("DeviceRestrictionManagerImpl", target + " getDeclaredField : " + clsName + "." + fieldName);
        if (target == null || clsName == null || fieldName == null) {
            return null;
        }
        try {
            Field field = Class.forName(clsName).getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            Log.i("DeviceRestrictionManagerImpl", "getDeclaredField exception caught : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public DeviceRestrictionManagerImpl(Context context) {
        this.mContext = context;
        HandlerThread thread = new HandlerThread("CustomizeControler");
        thread.start();
        if (thread.getLooper() != null) {
            this.mServiceHandler = new ServiceHandler(thread.getLooper());
        }
        this.mPackageManager = context.getPackageManager();
        this.mCustService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        this.mDpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setAppRestrictionPolicies(ComponentName admin, int pattern) {
        PermissionManager.getInstance().checkPermission();
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

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getAppRestrictionPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
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

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public List<String> getAppRestriction(ComponentName admin, int pattern) {
        PermissionManager.getInstance().checkPermission();
        if (this.mCustService == null) {
            return null;
        }
        try {
            return this.mCustService.getAppInstallationPolicies(pattern);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void addAppRestriction(ComponentName admin, int pattern, List<String> pkgs) {
        PermissionManager.getInstance().checkPermission();
        if (this.mCustService == null) {
            return;
        }
        if (pattern == 1) {
            try {
                this.mCustService.addInstallPackageBlacklist(1, pkgs);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (pattern == 2) {
            this.mCustService.addInstallPackageWhitelist(1, pkgs);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void removeAppRestriction(ComponentName admin, int pattern, List<String> pkgs) {
        PermissionManager.getInstance().checkPermission();
        if (this.mCustService == null) {
            return;
        }
        if (pattern == 1) {
            try {
                this.mCustService.addInstallPackageBlacklist(2, pkgs);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (pattern == 2) {
            this.mCustService.addInstallPackageWhitelist(2, pkgs);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void removeAllAppRestriction(ComponentName admin, int pattern) {
        PermissionManager.getInstance().checkPermission();
        if (this.mCustService == null) {
            return;
        }
        if (pattern == 1) {
            try {
                this.mCustService.addInstallPackageBlacklist(2, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (pattern == 2) {
            this.mCustService.addInstallPackageWhitelist(2, null);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setMmsDisabled(ComponentName componentName, boolean disabled) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceRestrictionManagerImpl", "call setMmsDisabled: " + disabled);
        setSmsReceiveDisabled(disabled);
        setSmsSendDisabled(disabled);
        setMmsSendReceiveDisabled(disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isMmsDisabled(ComponentName componentName) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceRestrictionManagerImpl", "call isMmsDisabled.");
        return isSmsSendDisabled() || isSmsReceiveDisabled() || isMmsSendReceiveDisabled();
    }

    private void setSmsReceiveDisabled(boolean disabled) {
        SystemProperties.set("persist.sys.oem_sr", disabled ? "0" : "1");
    }

    private boolean isSmsReceiveDisabled() {
        return SystemProperties.get("persist.sys.oem_sr", "1").equals("0");
    }

    private boolean isSmsSendDisabled() {
        return SystemProperties.get("persist.sys.oem_ss", "1").equals("0");
    }

    private void setSmsSendDisabled(boolean disabled) {
        SystemProperties.set("persist.sys.oem_ss", disabled ? "0" : "1");
    }

    private void setMmsSendReceiveDisabled(boolean disabled) {
        SystemProperties.set("persist.sys.oem_mb", disabled ? "0" : "1");
    }

    private boolean isMmsSendReceiveDisabled() {
        return SystemProperties.get("persist.sys.oem_mb", "1").equals("0");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setStatusBarExpandPanelDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        Settings.Secure.putInt(this.mContext.getContentResolver(), "statusbar_expand_disable", disabled ? 1 : 0);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isStatusBarExpandPanelDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "statusbar_expand_disable", 0) == 1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setWifiDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        String preVal = propGetEnable("persist.sys.wifi_policy", "2", "");
        propSetEnable("persist.sys.wifi_policy", "2");
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock wifi and lock UI...");
            try {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
                if (this.mWifiManager != null) {
                    Log.d("DeviceRestrictionManagerImpl", "close wifi...");
                    this.mWifiManager.setWifiEnabled(false);
                }
                Thread.sleep(1000);
                propSetEnable("persist.sys.wifi_policy", "0");
                propSetEnable("persist.sys.wifi_clickable", "0");
                propSetEnable("persist.sys.wifi_grey", "1");
            } catch (InterruptedException e) {
                propSetEnable("persist.sys.wifi_policy", preVal);
                Log.d("DeviceRestrictionManagerImpl", "setWifiDisabled error");
            }
        } else {
            Log.d("DeviceRestrictionManagerImpl", "unlock wifi and unlock UI...");
            propSetEnable("persist.sys.wifi_clickable", "1");
            propSetEnable("persist.sys.wifi_grey", "0");
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            if (this.mWifiManager != null) {
                Log.d("DeviceRestrictionManagerImpl", "open wifi...");
                this.mWifiManager.setWifiEnabled(true);
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isWifiDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.wifi_policy", "2");
        Log.d("DeviceRestrictionManagerImpl", "isWifiDisabled, value: " + value);
        if (value.equals("0")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setWifiP2pDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock wifi p2p and lock UI...");
            propSetEnable("persist.sys.p2p_forbidden", "1");
            propSetEnable("persist.sys.p2p_clickable", "0");
            propSetEnable("persist.sys.p2p_grey", "1");
            return;
        }
        Log.d("DeviceRestrictionManagerImpl", "unlock wifi p2p and unlock UI...");
        propSetEnable("persist.sys.p2p_forbidden", "0");
        propSetEnable("persist.sys.p2p_clickable", "1");
        propSetEnable("persist.sys.p2p_grey", "0");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isWifiP2pDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.p2p_forbidden", "0");
        Log.d("DeviceRestrictionManagerImpl", "isWifiDisabled, value: " + value);
        if (value.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setWifiApDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        String prePolicy = SystemProperties.get("persist.sys.wifi_ap_policy", String.valueOf(0));
        SystemProperties.set("persist.sys.wifi_ap_policy", String.valueOf(0));
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock softap and lock UI...");
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                if (connectivityManager != null) {
                    Log.d("DeviceRestrictionManagerImpl", "close softap...");
                    connectivityManager.stopTethering(0);
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e("DeviceRestrictionManagerImpl", "setWifiApDisabled stopTethering exception");
                SystemProperties.set("persist.sys.wifi_ap_policy", prePolicy);
            }
            SystemProperties.set("persist.sys.wifi_ap_policy", String.valueOf(1));
            propSetEnable("persist.sys.ap_clickable", "0");
            propSetEnable("persist.sys.ap_grey", "1");
            return;
        }
        Log.d("DeviceRestrictionManagerImpl", "unlock softap and unlock UI...");
        SystemProperties.set("persist.sys.wifi_ap_policy", String.valueOf(0));
        propSetEnable("persist.sys.ap_clickable", "1");
        propSetEnable("persist.sys.ap_grey", "0");
        ConnectivityManager connectivityManager2 = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager2 != null) {
            Log.d("DeviceRestrictionManagerImpl", "open softap...");
            connectivityManager2.startTethering(0, false, new ConnectivityManager.OnStartTetheringCallback() {
                /* class com.oppo.enterprise.mdmcoreservice.service.managerimpl.DeviceRestrictionManagerImpl.AnonymousClass1 */

                public void onTetheringStarted() {
                    Log.d("DeviceRestrictionManagerImpl", "onTetheringStarted ");
                }

                public void onTetheringFailed() {
                    Log.d("DeviceRestrictionManagerImpl", "onTetheringFailed ");
                }
            });
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isWifiApDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.wifi_ap_policy", String.valueOf(0));
        Log.d("DeviceRestrictionManagerImpl", "isWifiApDisabled, value: " + value);
        if (value.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setWifiSharingDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        int sharingState = 0;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        Object obj = getDeclaredField(WifiManager.class, "android.net.wifi.WifiManager", "WIFI_SHARING_STATE_ENABLED");
        int WIFI_SHARING_STATE_ENABLED = obj != null ? ((Integer) obj).intValue() : 113;
        Object obj2 = getDeclaredField(ConnectivityManager.class, "android.net.ConnectivityManager", "TETHERING_WIFI_SHARING");
        int TETHERING_WIFI_SHARING = obj2 != null ? ((Integer) obj2).intValue() : 4;
        if (this.mWifiManager != null) {
            sharingState = this.mWifiManager.getWifiApState();
        }
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock wifi sharing and lock UI...");
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                if (connectivityManager != null && sharingState == WIFI_SHARING_STATE_ENABLED) {
                    Log.d("DeviceRestrictionManagerImpl", "close wifi sharing...");
                    connectivityManager.stopTethering(TETHERING_WIFI_SHARING);
                }
                Thread.sleep(1000);
                SystemProperties.set("persist.sys.wifi_sharing_disable", "1");
                SystemProperties.set("persist.sys.wifi_sharing_clickable", "0");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("DeviceRestrictionManagerImpl", "unlock wifi sharing and unlock UI...");
            SystemProperties.set("persist.sys.wifi_sharing_disable", "0");
            SystemProperties.set("persist.sys.wifi_sharing_clickable", "1");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isWifiSharingDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.wifi_sharing_disable", "0");
        Log.d("DeviceRestrictionManagerImpl", "isWifiSharingDisabled, value: " + value);
        if (value.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setWifiInBackground(ComponentName admin, boolean enable) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceRestrictionManagerImpl", "setWifiInBackground, enable: " + enable);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (this.mWifiManager == null) {
            return false;
        }
        if (enable) {
            Log.d("DeviceRestrictionManagerImpl", "open wifi...");
            return this.mWifiManager.setWifiEnabled(true);
        }
        Log.d("DeviceRestrictionManagerImpl", "close wifi...");
        return this.mWifiManager.setWifiEnabled(false);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isWifiOpen(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceRestrictionManagerImpl", "isWifiOpen");
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (this.mWifiManager != null) {
            return this.mWifiManager.isWifiEnabled();
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setBluetoothDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock bluetooth and lock UI...");
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.disable();
                }
                Thread.sleep(1000);
                SystemProperties.set("persist.sys.bluetooth_policy", String.valueOf(0));
                SystemProperties.set("persist.sys.bt_clickable", "0");
                SystemProperties.set("persist.sys.bt_grey", "1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            SystemProperties.set("persist.sys.bluetooth_policy", String.valueOf(2));
            SystemProperties.set("persist.sys.bt_clickable", "1");
            SystemProperties.set("persist.sys.bt_grey", "0");
            BluetoothAdapter bluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter2 != null) {
                bluetoothAdapter2.enable();
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isBluetoothDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        int value = SystemProperties.getInt("persist.sys.bluetooth_policy", 2);
        Log.d("DeviceRestrictionManagerImpl", "isBluetoothDisabled, value: " + value);
        if (value == 0) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setBluetoothEnabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock bluetooth and lock UI...");
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.enable();
                }
                Thread.sleep(1000);
                SystemProperties.set("persist.sys.bluetooth_policy", String.valueOf(3));
                SystemProperties.set("persist.sys.bt_clickable", "0");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("DeviceRestrictionManagerImpl", "unlock bluetooth and unlock UI...");
            SystemProperties.set("persist.sys.bluetooth_policy", String.valueOf(2));
            SystemProperties.set("persist.sys.bt_clickable", "1");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isBluetoothEnabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        int value = SystemProperties.getInt("persist.sys.bluetooth_policy", 2);
        Log.d("DeviceRestrictionManagerImpl", "isBluetoothEnabled, value: " + value);
        if (value == 3) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setBluetoothTetheringDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock Bluetooth PAN and lock UI...");
            try {
                disableBtTethering();
                Thread.sleep(1000);
                SystemProperties.set("persist.sys.bt_tethering_disable", "1");
                SystemProperties.set("persist.sys.bt_tethering_clickable", "0");
            } catch (InterruptedException e) {
                Log.e("DeviceRestrictionManagerImpl", "setBluetoothTetheringDisabled error", e);
            }
        } else {
            Log.d("DeviceRestrictionManagerImpl", "unlock Bluetooth PAN and unlock UI...");
            SystemProperties.set("persist.sys.bt_tethering_disable", "0");
            SystemProperties.set("persist.sys.bt_tethering_clickable", "1");
        }
    }

    private void disableBtTethering() {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).stopTethering(2);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isBluetoothTetheringDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.bt_tethering_disable", "0");
        Log.d("DeviceRestrictionManagerImpl", "isBluetoothTetheringDisabled, value: " + value);
        if (value.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setBluetoothDataTransferDisabled(boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock Bluetooth DataTransfer and lock UI...");
            try {
                if (BluetoothAdapter.getDefaultAdapter().getState() == 12) {
                    Intent mIntent = new Intent();
                    mIntent.setAction("android.btopp.intent.action.CLOSE_URI_STREAM");
                    mIntent.setComponent(new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppReceiver"));
                    this.mContext.sendBroadcast(mIntent);
                }
                Thread.sleep(1000);
                SystemProperties.set("persist.sys.bt_opp_policy", "0");
                return true;
            } catch (InterruptedException e) {
                Log.e("DeviceRestrictionManagerImpl", "isBluetoothDataTransferDisabled error", e);
                return false;
            }
        } else {
            Log.d("DeviceRestrictionManagerImpl", "unlock Bluetooth DataTransfer and unlock UI...");
            SystemProperties.set("persist.sys.bt_opp_policy", "1");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isBluetoothDataTransferDisabled() {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.bt_opp_policy", "1");
        Log.d("DeviceRestrictionManagerImpl", "isBluetoothDataTransferDisabled, value: " + value);
        if (value.equals("0")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setBluetoothPairingDisabled(boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock Bluetooth Pairing and lock UI...");
            SystemProperties.set("persist.sys.bt_pair_policy", "0");
            return true;
        }
        Log.d("DeviceRestrictionManagerImpl", "unlock Bluetooth Pairing and unlock UI...");
        SystemProperties.set("persist.sys.bt_pair_policy", "1");
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isBluetoothPairingDisabled() {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.bt_pair_policy", "1");
        Log.d("DeviceRestrictionManagerImpl", "isBluetoothPairingDisabled, value: " + value);
        if (value.equals("0")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setBluetoothOutGoingCallDisabled(boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock Bluetooth OutGoingCall and lock UI...");
            try {
                if (BluetoothAdapter.getDefaultAdapter().getState() == 12) {
                    disconnectAllDevice();
                }
                SystemProperties.set("persist.sys.bt_call_policy", "0");
                return true;
            } catch (Exception e) {
                Log.e("DeviceRestrictionManagerImpl", "setBluetoothOutGoingCallDisabled error", e);
                return false;
            }
        } else {
            Log.d("DeviceRestrictionManagerImpl", "unlock Bluetooth OutGoingCall and unlock UI...");
            SystemProperties.set("persist.sys.bt_call_policy", "1");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isBluetoothOutGoingCallDisabled() {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.bt_call_policy", "1");
        Log.d("DeviceRestrictionManagerImpl", "isBluetoothOutGoingCallDisabled, value: " + value);
        if (value.equals("0")) {
            return true;
        }
        return false;
    }

    private void getProfileProxy(int profile) {
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(this.mContext, this.mListener, profile);
    }

    private void disconnectAllDevice() {
        getProfileProxy(1);
        getProfileProxy(6);
        getProfileProxy(9);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnect(int profile, BluetoothProfile mProfile) {
        List<BluetoothDevice> mBluetoothDevices = mProfile.getConnectedDevices();
        if (mBluetoothDevices != null && !mBluetoothDevices.isEmpty()) {
            try {
                for (BluetoothDevice device : mBluetoothDevices) {
                    if (profile == 1) {
                        Object[] objArr = {device};
                        Class.forName("android.bluetooth.BluetoothHeadset").getMethod("disconnect", BluetoothDevice.class).invoke((BluetoothHeadset) mProfile, objArr);
                    } else if (profile == 6) {
                        Object[] objArr2 = {device};
                        Class.forName("android.bluetooth.BluetoothPbap").getMethod("disconnect", BluetoothDevice.class).invoke((BluetoothPbap) mProfile, objArr2);
                    } else if (profile == 9) {
                        Object[] objArr3 = {device};
                        Class.forName("android.bluetooth.BluetoothMap").getMethod("disconnect", BluetoothDevice.class).invoke((BluetoothMap) mProfile, objArr3);
                    }
                }
                Thread.sleep(1000);
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, mProfile);
            } catch (Exception e) {
                Log.e("DeviceRestrictionManagerImpl", "disconnect error", e);
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setNFCDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        if (disabled) {
            SystemProperties.set("persist.sys.nfc_disable", "1");
            propSetEnable("persist.sys.nfc_clickable", "0");
            propSetEnable("persist.sys.nfc_grey", "1");
            if (nfcAdapter != null) {
                Log.d("DeviceRestrictionManagerImpl", "setNFCDisabled, close nfc");
                nfcAdapter.disable();
                return;
            }
            return;
        }
        SystemProperties.set("persist.sys.nfc_disable", "0");
        propSetEnable("persist.sys.nfc_clickable", "1");
        propSetEnable("persist.sys.nfc_grey", "0");
        if (nfcAdapter != null) {
            Log.d("DeviceRestrictionManagerImpl", "setNFCDisabled, open nfc");
            nfcAdapter.enable();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isNFCDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.nfc_disable", "0");
        Log.d("DeviceRestrictionManagerImpl", "isNFCDisabled, value: " + value);
        if (value.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void openCloseNFC(ComponentName admin, boolean enable) {
        PermissionManager.getInstance().checkPermission();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        if (nfcAdapter == null) {
            Log.e("DeviceRestrictionManagerImpl", "NfcAdapter is null");
        } else if (enable) {
            Log.d("DeviceRestrictionManagerImpl", "openCloseNFC, open nfc");
            nfcAdapter.enable();
        } else {
            Log.d("DeviceRestrictionManagerImpl", "openCloseNFC, close nfc");
            nfcAdapter.disable();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isNFCTurnOn(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setAndroidBeamDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        if (disabled) {
            SystemProperties.set("persist.sys.beam_disable", "1");
            propSetEnable("persist.sys.androidbeam_clickable", "0");
            propSetEnable("persist.sys.androidbeam_grey", "1");
            if (nfcAdapter == null) {
                return false;
            }
            Log.d("DeviceRestrictionManagerImpl", "setAndroidBeamDisabled, close Android Beam");
            return nfcAdapter.disableNdefPush();
        }
        SystemProperties.set("persist.sys.beam_disable", "0");
        propSetEnable("persist.sys.androidbeam_clickable", "1");
        propSetEnable("persist.sys.androidbeam_grey", "0");
        if (nfcAdapter == null) {
            return false;
        }
        Log.d("DeviceRestrictionManagerImpl", "setAndroidBeamDisabled, open Android Beam");
        return nfcAdapter.enableNdefPush();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isAndroidBeamDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String disable = SystemProperties.get("persist.sys.beam_disable", "0");
        Log.d("DeviceRestrictionManagerImpl", "isAndroidBeamDisabled, disable: " + disable);
        if (!disable.equals("0") && disable.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setRecordDisabled(ComponentName componentName, boolean enable) {
        PermissionManager.getInstance().checkPermission();
        Log.i("DeviceRestrictionManagerImpl", "checkedPermission");
        if (enable) {
            SystemProperties.set("persist.sys.record.forbid", "1");
            return true;
        }
        SystemProperties.set("persist.sys.record.forbid", "0");
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isRecordDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        Log.i("DeviceRestrictionManagerImpl", "checkedPermission");
        if (SystemProperties.get("persist.sys.record.forbid", "0").equals("1")) {
            return true;
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setChangeWallpaperDisable(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        Settings.Secure.putInt(this.mContext.getContentResolver(), "changeWallpaperDisabledState", disabled ? 1 : 0);
        Log.d("DeviceRestrictionManagerImpl", "call setChangeWallpaperDisable finish !!! newState = " + ((int) disabled) + "disabled = " + disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isChangeWallpaperDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        int state = Settings.Secure.getInt(this.mContext.getContentResolver(), "changeWallpaperDisabledState", 0);
        boolean isChangeWallpaperDisabled = true;
        if (state != 1) {
            isChangeWallpaperDisabled = false;
        }
        Log.d("DeviceRestrictionManagerImpl", "call isChangeWallpaperDisabled finish!!! state = " + state + " isChangeWallpaperDisabled =" + isChangeWallpaperDisabled);
        return isChangeWallpaperDisabled;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setExternalStorageDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            SystemProperties.set("persist.sys.exStorage_support", "0");
        } else {
            SystemProperties.set("persist.sys.exStorage_support", "1");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isExternalStorageDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.get("persist.sys.exStorage_support", "0").equals("0");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setUSBDataDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        long identify = Binder.clearCallingIdentity();
        if (disabled) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "ZQ_ADB_ENABLED", 0);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "MTP_TRANSFER_ENABLED", 0);
            propSetEnable("persist.sys.usb_debugging_clickable", false);
            try {
                ((UserManager) this.mContext.getSystemService("user")).setUserRestriction("no_usb_file_transfer", true);
            } finally {
                Binder.restoreCallingIdentity(identify);
            }
        } else {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "ZQ_ADB_ENABLED", 1);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "adb_enabled", 1);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "MTP_TRANSFER_ENABLED", 1);
            propSetEnable("persist.sys.usb_debugging_clickable", true);
            try {
                ((UserManager) this.mContext.getSystemService("user")).setUserRestriction("no_usb_file_transfer", false);
            } finally {
                Binder.restoreCallingIdentity(identify);
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isUSBDataDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "MTP_TRANSFER_ENABLED", 1) == 0;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setUSBOtgDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            SystemProperties.set("persist.sys.oppo.otg_support", "false");
            Settings.Secure.putInt(this.mContext.getContentResolver(), "OTG_ENABLED", 0);
            return;
        }
        SystemProperties.set("persist.sys.oppo.otg_support", "true");
        Settings.Secure.putInt(this.mContext.getContentResolver(), "OTG_ENABLED", 1);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isUSBOtgDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        try {
            return !SystemProperties.getBoolean("persist.sys.oppo.otg_support", false);
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "isUSBOtgDisabled error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public Bundle setDefaultVoiceCard(ComponentName componentName, int slotId) {
        PermissionManager.getInstance().checkPermission();
        Bundle bundle = new Bundle();
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
            Log.d("DeviceRestrictionManagerImpl", "AIR_PLANE_MODE_ON");
            bundle.putBoolean("RESULT", false);
            bundle.putString("EXCEPTION", "AIR_PLANE_MODE_ON");
            return bundle;
        }
        int subId = getSubId(slotId);
        if (!SubscriptionManager.from(this.mContext).isActiveSubId(subId)) {
            Log.d("DeviceRestrictionManagerImpl", "SUBSCRIPTION_INACTIVE");
            bundle.putBoolean("RESULT", false);
            bundle.putString("EXCEPTION", "SUBSCRIPTION_INACTIVE");
            return bundle;
        }
        SubscriptionManager.from(this.mContext).setDefaultVoiceSubId(subId);
        Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_prompt", slotId);
        bundle.putBoolean("RESULT", true);
        bundle.putString("EXCEPTION", null);
        Log.d("DeviceRestrictionManagerImpl", "setDefaultVoiceSubId():  " + subId);
        return bundle;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getDefaultVoiceCard(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        SubscriptionManager.from(this.mContext);
        return SubscriptionManager.getDefaultVoicePhoneId();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isVoiceDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        return !propGetEnable("persist.sys.oem_vo", "-1") || !propGetEnable("persist.sys.oem_vi", "-1");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setVoiceDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        propSetEnable("persist.sys.oem_vi", !disabled);
        propSetEnable("persist.sys.oem_vo", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getMobileDataMode(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        try {
            return Integer.parseInt(SystemProperties.get("persist.sys.oem_pl_db", "-1"));
        } catch (Exception ex) {
            Log.e("DeviceRestrictionManagerImpl", "setProp error :" + ex.getMessage());
            return -1;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setMobileDataMode(ComponentName componentName, int mode) {
        PermissionManager.getInstance().checkPermission();
        propSetEnable("persist.sys.oem_pl_db", mode + "");
        int phoneId = SubscriptionManager.from(this.mContext).getDefaultDataPhoneId();
        if (phoneId == 0) {
            propSetEnable("persist.sys.oem_slot1_db", "" + mode);
        } else if (1 == phoneId) {
            propSetEnable("persist.sys.oem_slot2_db", "" + mode);
        }
        if (mode == 0) {
            propSetEnable("persist.sys.cellular_clickable", "0");
            propSetEnable("persist.sys.cellular_grey", "1");
            Message msg = Message.obtain();
            msg.what = 2;
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg);
            }
        } else if (mode == 1) {
            propSetEnable("persist.sys.cellular_clickable", "0");
            propSetEnable("persist.sys.cellular_grey", "0");
            Message msg2 = Message.obtain();
            msg2.what = 1;
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg2);
            }
        } else if (mode == 2) {
            propSetEnable("persist.sys.cellular_clickable", "1");
            propSetEnable("persist.sys.cellular_grey", "0");
        } else if (mode == 3) {
            Message msg3 = Message.obtain();
            msg3.what = 2;
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg3);
            }
        } else if (mode == 4) {
            Message msg4 = Message.obtain();
            msg4.what = 1;
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendMessage(msg4);
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setScreenCaptureDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.customize.forbcap", String.valueOf(disabled));
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isScreenCaptureDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return propGetEnable("persist.sys.customize.forbcap", "false");
    }

    private static boolean propSetEnable(String prop, boolean defval) {
        return propSetEnable(prop, defval ? "1" : "0");
    }

    private static boolean propSetEnable(String prop, String defval) {
        try {
            Log.d("DeviceRestrictionManagerImpl", "propSetEnable " + prop + ": " + defval);
            SystemProperties.set(prop, defval);
            return true;
        } catch (Exception ex) {
            Log.e("DeviceRestrictionManagerImpl", "setProp error :" + ex.getMessage());
            return false;
        }
    }

    private static boolean propGetEnable(String prop, String defval) {
        boolean ret = false;
        try {
            Log.d("DeviceRestrictionManagerImpl", "propGetEnable " + prop + ": " + defval);
            String val = SystemProperties.get(prop, defval);
            if (val == null) {
                return false;
            }
            if (!val.equals("true")) {
                if (!val.equals("false")) {
                    if (Integer.parseInt(val) != 0) {
                        ret = true;
                    }
                    return ret;
                }
            }
            return Boolean.parseBoolean(val);
        } catch (Exception ex) {
            Log.e("DeviceRestrictionManagerImpl", "getProp error :" + ex.getMessage());
            return false;
        }
    }

    private static String propGetEnable(String prop, String defval, String type) {
        try {
            if (DEBUG) {
                Log.d("DeviceRestrictionManagerImpl", "propGetEnable " + prop + ": " + defval);
            }
            return SystemProperties.get(prop, defval);
        } catch (Exception ex) {
            Log.e("DeviceRestrictionManagerImpl", "getProp error :" + ex.getMessage());
            return defval;
        }
    }

    /* access modifiers changed from: private */
    public final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                switch (msg.what) {
                    case 1:
                        TelephonyManager.getDefault().setDataEnabled(true);
                        return;
                    case 2:
                        TelephonyManager.getDefault().setDataEnabled(false);
                        return;
                    case 3:
                        try {
                            DeviceRestrictionManagerImpl.this.mCustService.activateSubId(DeviceRestrictionManagerImpl.getSubId(1));
                            return;
                        } catch (Exception e) {
                            Log.d("DeviceRestrictionManagerImpl", "activateSubId:err!");
                            return;
                        }
                    case 4:
                        try {
                            DeviceRestrictionManagerImpl.this.mCustService.deactivateSubId(DeviceRestrictionManagerImpl.getSubId(1));
                            return;
                        } catch (Exception e2) {
                            Log.d("DeviceRestrictionManagerImpl", "deactivateSubId:err!");
                            return;
                        }
                    default:
                        Log.w("DeviceRestrictionManagerImpl", "what=" + msg.what);
                        return;
                }
            }
        }
    }

    public static int getSubId(int slotId) {
        int vRetSubId;
        int[] subId = SubscriptionManager.getSubId(slotId);
        if (subId == null || subId.length <= 0) {
            vRetSubId = -1000;
        } else {
            vRetSubId = subId[0];
        }
        Log.d("DeviceRestrictionManagerImpl", "SubId=" + vRetSubId);
        return vRetSubId;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setGpsPolicies(ComponentName admin, int mode) {
        PermissionManager.getInstance().checkPermission();
        boolean result = true;
        DeviceConnectivityManager manager = DeviceConnectivityManager.getInstance(this.mContext);
        if (mode == 0) {
            try {
                Log.d("DeviceRestrictionManagerImpl", "lock gps and lock UI...");
                Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps.always_enable", 0);
                Thread.sleep(100);
                manager.turnOnGPS(admin, false);
                Thread.sleep(300);
                SystemProperties.set("persist.sys.gps_disable", "1");
                SystemProperties.set("persist.sys.gps.always_enable", "0");
                Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps_disable", 1);
                propSetEnable("persist.sys.gps_clickable", "0");
                propSetEnable("persist.sys.gps_grey", "1");
            } catch (Exception e) {
                Log.d("DeviceRestrictionManagerImpl", "setGpsPolicies " + mode + " error");
                result = false;
            }
        } else if (mode == 1) {
            Log.d("DeviceRestrictionManagerImpl", "lock gps and lock UI...");
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps_disable", 0);
            Thread.sleep(100);
            manager.turnOnGPS(admin, true);
            Thread.sleep(300);
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps.always_enable", 1);
            SystemProperties.set("persist.sys.gps_disable", "0");
            SystemProperties.set("persist.sys.gps.always_enable", "1");
            propSetEnable("persist.sys.gps_clickable", "0");
            propSetEnable("persist.sys.gps_grey", "0");
        } else if (mode == 2) {
            Log.d("DeviceRestrictionManagerImpl", "unlock gps and unlock UI...");
            SystemProperties.set("persist.sys.gps_disable", "0");
            SystemProperties.set("persist.sys.gps.always_enable", "0");
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps_disable", 0);
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps.always_enable", 0);
            propSetEnable("persist.sys.gps_clickable", "1");
            propSetEnable("persist.sys.gps_grey", "0");
        } else if (mode == 3) {
            Log.d("DeviceRestrictionManagerImpl", "close gps...");
            SystemProperties.set("persist.sys.gps_disable", "0");
            SystemProperties.set("persist.sys.gps.always_enable", "0");
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps_disable", 0);
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps.always_enable", 0);
            Thread.sleep(100);
            manager.turnOnGPS(admin, false);
            propSetEnable("persist.sys.gps_clickable", "1");
            propSetEnable("persist.sys.gps_grey", "0");
        } else if (mode == 4) {
            SystemProperties.set("persist.sys.gps_disable", "0");
            SystemProperties.set("persist.sys.gps.always_enable", "0");
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps_disable", 0);
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps.always_enable", 0);
            Thread.sleep(100);
            manager.turnOnGPS(admin, true);
            propSetEnable("persist.sys.gps_clickable", "1");
            propSetEnable("persist.sys.gps_grey", "0");
        } else {
            result = false;
        }
        if (result) {
            SystemProperties.set("persist.sys.mdm_gps_mode", String.valueOf(mode));
        }
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getGpsPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.mdm_gps_mode", 2);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setPowerDisable(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        try {
            if ((Settings.Secure.getInt(this.mContext.getContentResolver(), "forbid_globalaction_by_power", 0) != 0) != disabled) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), "forbid_globalaction_by_power", disabled ? 1 : 0);
            }
            return true;
        } catch (Exception e) {
            Log.d("DeviceRestrictionManagerImpl", "setPowerDisable fail", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean getPowerDisable(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), "forbid_globalaction_by_power", 0) != 0;
        } catch (Exception e) {
            Log.d("DeviceRestrictionManagerImpl", "getPowerDisable fail", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setSplitScreenDisable(ComponentName admin, boolean disable) {
        PermissionManager.getInstance().checkPermission();
        try {
            if (disable != (Settings.Secure.getInt(this.mContext.getContentResolver(), "forbid_splitscreen_by_ep", 0) == 1)) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), "forbid_splitscreen_by_ep", disable ? 1 : 0);
                Intent intent = new Intent();
                intent.setAction("enterprise.intent.action.DISABLE_SPLITSCREEN");
                intent.putExtra("disableStatus", disable);
                this.mContext.sendBroadcast(intent);
            }
            return true;
        } catch (Exception e) {
            Log.d("DeviceRestrictionManagerImpl", "setSplitScreenDisable fail", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean getSplitScreenDisable(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), "forbid_splitscreen_by_ep", 0) != 0;
        } catch (Exception e) {
            Log.d("DeviceRestrictionManagerImpl", "getSplitScreenDisable fail", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setUnknownSourceAppInstallDisabled(ComponentName admin, boolean disabled) {
        String[] packages;
        PermissionManager.getInstance().checkPermission();
        try {
            boolean currentStatus = SystemProperties.getBoolean("persist.sys.unknownsourceapp", false);
            AppOpsManager appOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
            if (currentStatus == disabled) {
                return true;
            }
            SystemProperties.set("persist.sys.unknownsourceapp", disabled ? "true" : "false");
            Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_unknownsource", disabled ? 1 : 0);
            if (!disabled || appOpsManager == null || (packages = AppGlobals.getPackageManager().getAppOpPermissionPackages("android.permission.REQUEST_INSTALL_PACKAGES")) == null || packages.length <= 0) {
                return true;
            }
            for (String packageName : packages) {
                appOpsManager.setMode(66, this.mPackageManager.getPackageUid(packageName, 0), packageName, 2);
            }
            return true;
        } catch (Exception e) {
            Log.d("DeviceRestrictionManagerImpl", "setUnknownSourceAppInstallDisabled fail", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isUnknownSourceAppInstallDisabled(ComponentName admin) {
        try {
            PermissionManager.getInstance().checkPermission();
            return SystemProperties.getBoolean("persist.sys.unknownsourceapp", false);
        } catch (Exception e) {
            Log.d("DeviceRestrictionManagerImpl", "isUnknownSourceAppInstallDisabled fail", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setMicrophonePolicies(ComponentName admin, int mode) {
        PermissionManager.getInstance().checkPermission();
        AudioSystem.setParameters("custom_enable=enable");
        switch (mode) {
            case 0:
                AudioSystem.muteMicrophone(true);
                SystemProperties.set("persist.sys.mic.forbid", "1");
                AudioSystem.setParameters("mute_mic_enable=on");
                break;
            case 1:
                AudioSystem.muteMicrophone(false);
                SystemProperties.set("persist.sys.mic.forbid", "0");
                AudioSystem.setParameters("mute_mic_enable=off");
                break;
            default:
                return false;
        }
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getMicrophonePolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        if (AudioSystem.isMicrophoneMuted()) {
            return 0;
        }
        return 1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setSpeakerPolicies(ComponentName admin, int mode) {
        int res;
        PermissionManager.getInstance().checkPermission();
        switch (mode) {
            case 0:
                res = AudioSystem.setParameters("speaker_mute=On");
                break;
            case 1:
                res = AudioSystem.setParameters("speaker_mute=Off");
                break;
            default:
                return false;
        }
        if (res == 0) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getSpeakerPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return !AudioSystem.getParameters("speaker_mute").contains("=On");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setSafeModeDisabled(boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        try {
            SystemProperties.set("persist.sys.disable.safemode", disabled ? "1" : "0");
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isSafeModeDisabled() {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        try {
            return SystemProperties.getBoolean("persist.sys.disable.safemode", false);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setCameraPolicies(int mode) {
        PermissionManager.getInstance().checkPermission();
        switch (mode) {
            case 0:
                SystemProperties.set("persist.sys.camera_available", "0");
                return true;
            case 1:
                SystemProperties.set("persist.sys.camera_available", "1");
                return true;
            default:
                return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getCameraPolicies() {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.camera_available", 1);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setAppUninstallationPolicies(int mode, List<String> appPackageNames) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        if (mode < 0 || mode > 1) {
            Log.d("DeviceRestrictionManagerImpl", "setAppUninstallationPolicies:invalid mode!");
            return false;
        } else if (this.mPackageManager == null) {
            return false;
        } else {
            if (mode == 0) {
                SystemProperties.set("persist.sys.forbiduninstall.package.policy.whitelist", "false");
                SystemProperties.set("persist.sys.forbiduninstall.package.policy.blacklist", "true");
                Log.d("DeviceRestrictionManagerImpl", "set mode:" + mode);
                if (appPackageNames != null && appPackageNames.size() > 0) {
                    for (String pname : appPackageNames) {
                        try {
                            this.mPackageManager.deletePackage(pname, null, 0);
                        } catch (Exception e) {
                        }
                    }
                }
            } else {
                SystemProperties.set("persist.sys.forbiduninstall.package.policy.whitelist", "true");
                SystemProperties.set("persist.sys.forbiduninstall.package.policy.blacklist", "false");
                Log.d("DeviceRestrictionManagerImpl", "set mode:" + mode);
            }
            this.mCustService.setAppUninstallationPolicies(mode, null);
            this.mCustService.setAppUninstallationPolicies(mode, appPackageNames);
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public List<String> getAppUninstallationPolicies() throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        List<String> packagelist = new ArrayList<>();
        int mode = -1;
        boolean black = SystemProperties.getBoolean("persist.sys.forbiduninstall.package.policy.blacklist", false);
        boolean white = SystemProperties.getBoolean("persist.sys.forbiduninstall.package.policy.whitelist", false);
        if (black || white) {
            mode = !black ? 1 : 0;
            try {
                packagelist = this.mCustService.getAppUninstallationPolicies(mode);
            } catch (Exception e) {
                Log.d("DeviceRestrictionManagerImpl", "getAppUninstallationPolicies:err!");
            }
        }
        packagelist.add(0, String.valueOf(mode));
        return packagelist;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setLanguageChangeDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            SystemProperties.set("persist.sys.local_picker_dis", "true");
            try {
                IActivityManager am = ActivityManagerNative.getDefault();
                if (am != null) {
                    Configuration config = am.getConfiguration();
                    if (config.locale != Locale.SIMPLIFIED_CHINESE) {
                        config.locale = Locale.SIMPLIFIED_CHINESE;
                        config.userSetLocale = true;
                        long identity = Binder.clearCallingIdentity();
                        try {
                            ActivityManagerNative.getDefault().updateConfiguration(config);
                        } catch (Exception ex) {
                            Log.d("DeviceRestrictionManagerImpl", "update Configuration failed!", ex);
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                }
            } catch (RemoteException e) {
                Log.d("DeviceRestrictionManagerImpl", "set Language Change Disabled failed," + e);
            }
        } else {
            SystemProperties.set("persist.sys.local_picker_dis", "false");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isLanguageChangeDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return "true".equals(SystemProperties.get("persist.sys.local_picker_dis", "false"));
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setAdbDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "ZQ_ADB_ENABLED", 0);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
            propSetEnable("persist.sys.usb_debugging_clickable", false);
            return;
        }
        Settings.Secure.putInt(this.mContext.getContentResolver(), "ZQ_ADB_ENABLED", 1);
        Settings.Secure.putInt(this.mContext.getContentResolver(), "adb_enabled", 1);
        propSetEnable("persist.sys.usb_debugging_clickable", true);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isAdbDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "ZQ_ADB_ENABLED", 1) != 1) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setUnlockByFingerprintDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        int i = 0;
        if (Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_fingerprint", disabled ? 1 : 0) && disabled) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_switch", 0);
        }
        if (!disabled) {
            i = 2;
        }
        SystemProperties.set("persist.sys.unlock_by_finger_policy", String.valueOf(i));
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isUnlockByFingerprintDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_settings_manager_fingerprint", 0) == 1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setUnlockByFaceDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        int i = 0;
        if (Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_facelock", disabled ? 1 : 0) && disabled) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_switch", 0);
        }
        if (!disabled) {
            i = 2;
        }
        SystemProperties.set("persist.sys.unlock_by_face_policy", String.valueOf(i));
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isUnlockByFaceDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_settings_manager_facelock", 0) == 1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setUnlockByFingerprintPolicies(ComponentName admin, int policy) {
        PermissionManager.getInstance().checkPermission();
        switch (policy) {
            case 0:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_fingerprint", 1);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_switch", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_force_status", 1);
                break;
            case 1:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_fingerprint", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_switch", 1);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_force_status", 1);
                break;
            case 2:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_fingerprint", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_force_status", 0);
                break;
            case 3:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_fingerprint", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_switch", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_force_status", 0);
                break;
            case 4:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_fingerprint", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_switch", 1);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_fingerprint_unlock_force_status", 0);
                break;
        }
        if (policy < 0 || policy > 4) {
            return false;
        }
        SystemProperties.set("persist.sys.unlock_by_finger_policy", String.valueOf(policy));
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getUnlockByFingerprintPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.unlock_by_finger_policy", 2);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setUnlockByFacePolicies(ComponentName admin, int policy) {
        PermissionManager.getInstance().checkPermission();
        switch (policy) {
            case 0:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_facelock", 1);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_switch", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_force_status", 1);
                break;
            case 1:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_facelock", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_switch", 1);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_force_status", 1);
                break;
            case 2:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_facelock", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_force_status", 0);
                break;
            case 3:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_facelock", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_switch", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_force_status", 0);
                break;
            case 4:
                Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_settings_manager_facelock", 0);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_switch", 1);
                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_face_unlock_force_status", 0);
                break;
        }
        if (policy < 0 || policy > 4) {
            return false;
        }
        SystemProperties.set("persist.sys.unlock_by_face_policy", String.valueOf(policy));
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getUnlockByFacePolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.unlock_by_face_policy", 2);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setUserPasswordPolicies(ComponentName admin, int mode) {
        PermissionManager.getInstance().checkPermission();
        if (mode < 0 || mode > 3) {
            Log.w("DeviceRestrictionManagerImpl", "Method parameter is wrong!");
            return false;
        }
        Settings.Secure.putInt(this.mContext.getContentResolver(), "oppo_custom_settings_policy_user_password", mode);
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getUserPasswordPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "oppo_custom_settings_policy_user_password", 3);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setTaskButtonDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            try {
                SystemProperties.set("persist.sys.custom_task_disable", "true");
                return true;
            } catch (Exception e) {
                Log.e("DeviceRestrictionManagerImpl", "setTaskButtonDisabled error", e);
                return false;
            }
        } else {
            SystemProperties.set("persist.sys.custom_task_disable", "false");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isTaskButtonDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return SystemProperties.getBoolean("persist.sys.custom_task_disable", false);
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "isTaskButtonDisabled error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setBrowserRestriction(int pattern) {
        PermissionManager.getInstance().checkPermission();
        if (pattern == 1) {
            HarmonyNetUtil.getInstance(this.mContext).setHarmonyNetMode(2);
        } else if (pattern == 2) {
            HarmonyNetUtil.getInstance(this.mContext).setHarmonyNetMode(1);
        } else if (pattern == 0) {
            HarmonyNetUtil.getInstance(this.mContext).setHarmonyNetMode(0);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void clearBrowserRestriction(int pattern) {
        PermissionManager.getInstance().checkPermission();
        if (pattern == 1) {
            HarmonyNetUtil.getInstance(this.mContext).clearHarmonyNetRules(false);
        } else if (pattern == 2) {
            HarmonyNetUtil.getInstance(this.mContext).clearHarmonyNetRules(true);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void addBrowserRestriction(int pattern, List<String> urls) {
        PermissionManager.getInstance().checkPermission();
        if (pattern == 1) {
            HarmonyNetUtil.getInstance(this.mContext).addHarmonyNetRules(urls, false);
        } else if (pattern == 2) {
            HarmonyNetUtil.getInstance(this.mContext).addHarmonyNetRules(urls, true);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void removeBrowserRestriction(int pattern, List<String> urls) {
        PermissionManager.getInstance().checkPermission();
        if (pattern == 1) {
            HarmonyNetUtil.getInstance(this.mContext).delHarmonyNetRules(urls, false);
        } else if (pattern == 2) {
            HarmonyNetUtil.getInstance(this.mContext).delHarmonyNetRules(urls, true);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public List<String> queryBrowserHistory(int position, int pageSize) {
        PermissionManager.getInstance().checkPermission();
        Cursor cursor = HarmonyNetUtil.getInstance(this.mContext).queryBrowserHistory(position, pageSize);
        List<String> data = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    data.add(cursor.getString(cursor.getColumnIndex("url")));
                }
            } catch (Throwable th) {
                HarmonyNetUtil.closeQuietly(cursor);
                throw th;
            }
            HarmonyNetUtil.closeQuietly(cursor);
        }
        return data;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public List<String> getBrowserRestrictionUrls(int pattern) {
        PermissionManager.getInstance().checkPermission();
        int mode = 0;
        if (pattern == 1) {
            mode = 2;
        } else if (pattern == 2) {
            mode = 1;
        }
        return HarmonyNetUtil.getInstance(this.mContext).getHarmonyNetRules(mode);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setFloatTaskDisabled(ComponentName admin, boolean disable) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        boolean result = false;
        if (disable) {
            try {
                Settings.System.putInt(this.mContext.getContentResolver(), "floating_ball_switch", 0);
            } catch (Exception e) {
                Log.e("DeviceRestrictionManagerImpl", "setFloatTaskDisabled error", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
        SystemProperties.set("persist.sys.custom_float.disable", disable ? "1" : "0");
        result = true;
        Binder.restoreCallingIdentity(identity);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isFloatTaskDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        boolean isDisabled = false;
        try {
            if (SystemProperties.getInt("persist.sys.custom_float.disable", 0) == 1) {
                isDisabled = true;
            }
            return isDisabled;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setAirplanePolices(ComponentName admin, int policy) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        if (policy == 0) {
            try {
                propSetEnable("persist.sys.airplane_clickable", "0");
                propSetEnable("persist.sys.airplane_grey", "1");
                propSetEnable("persist.sys.airplane_disable", "1");
                return setAirplaneModeEnable(admin, false);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else if (policy == 1) {
            propSetEnable("persist.sys.airplane_clickable", "0");
            propSetEnable("persist.sys.airplane_grey", "0");
            propSetEnable("persist.sys.airplane_disable", "2");
            boolean airplaneModeEnable = setAirplaneModeEnable(admin, true);
            Binder.restoreCallingIdentity(identity);
            return airplaneModeEnable;
        } else if (policy == 2) {
            propSetEnable("persist.sys.airplane_clickable", "-1");
            propSetEnable("persist.sys.airplane_grey", "0");
            propSetEnable("persist.sys.airplane_disable", "0");
            Binder.restoreCallingIdentity(identity);
            return true;
        } else if (policy == 3) {
            propSetEnable("persist.sys.airplane_clickable", "1");
            propSetEnable("persist.sys.airplane_grey", "0");
            propSetEnable("persist.sys.airplane_disable", "0");
            boolean airplaneModeEnable2 = setAirplaneModeEnable(admin, false);
            Binder.restoreCallingIdentity(identity);
            return airplaneModeEnable2;
        } else if (policy == 4) {
            propSetEnable("persist.sys.airplane_clickable", "1");
            propSetEnable("persist.sys.airplane_grey", "0");
            propSetEnable("persist.sys.airplane_disable", "0");
            boolean airplaneModeEnable3 = setAirplaneModeEnable(admin, true);
            Binder.restoreCallingIdentity(identity);
            return airplaneModeEnable3;
        } else {
            Binder.restoreCallingIdentity(identity);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getAirplanePolices(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        long identity = Binder.clearCallingIdentity();
        int result = 1;
        try {
            int value = SystemProperties.getInt("persist.sys.airplane_clickable", 1);
            boolean on = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
            if (value == -1) {
                result = 2;
            } else {
                if (value == 1) {
                    result = on ? 4 : 3;
                } else if (!on) {
                    result = 0;
                }
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean setAirplaneModeEnable(ComponentName admin, boolean enable) {
        try {
            setAirplaneMode(admin, enable);
            return true;
        } catch (Exception ex) {
            Log.e("DeviceRestrictionManagerImpl", "enable setAirplaneModeDisabled Restriction error :" + ex.getMessage());
            return false;
        }
    }

    private void setAirplaneMode(ComponentName admin, boolean on) {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null) {
            Log.d("DeviceRestrictionManagerImpl", "setAirplaneMode : on = " + on);
            connectivityManager.setAirplaneMode(on);
            SystemProperties.set("persist.sys.airplane_on", on ? "1" : "0");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setHomeButtonDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            try {
                SystemProperties.set("persist.sys.custom_home_disable", "true");
                return true;
            } catch (Exception e) {
                Log.e("DeviceRestrictionManagerImpl", "setHomeButtonDisabled error", e);
                return false;
            }
        } else {
            SystemProperties.set("persist.sys.custom_home_disable", "false");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isHomeButtonDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return SystemProperties.getBoolean("persist.sys.custom_home_disable", false);
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "isHomeButtonDisabled error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setBackButtonDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            try {
                SystemProperties.set("persist.sys.custom_back_disable", "true");
                return true;
            } catch (Exception e) {
                Log.e("DeviceRestrictionManagerImpl", "setBackButtonDisabled error", e);
                return false;
            }
        } else {
            SystemProperties.set("persist.sys.custom_back_disable", "false");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isBackButtonDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return SystemProperties.getBoolean("persist.sys.custom_back_disable", false);
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "isBackButtonDisabled error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setSystemBrowserDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        try {
            List<PackageInfo> pinfo = this.mPackageManager.getInstalledPackages(0);
            if (pinfo == null) {
                return true;
            }
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (mBrowser.contains(pn)) {
                    if (disabled) {
                        this.mPackageManager.setApplicationEnabledSetting(pn, 3, 0);
                    } else {
                        this.mPackageManager.setApplicationEnabledSetting(pn, 0, 0);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "setSystemBrowserDisabled error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isSystemBrowserDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            List<PackageInfo> pinfo = this.mPackageManager.getInstalledPackages(0);
            if (pinfo != null) {
                for (int i = 0; i < pinfo.size(); i++) {
                    String pn = pinfo.get(i).packageName;
                    if (mBrowser.contains(pn)) {
                        if (this.mPackageManager.getApplicationEnabledSetting(pn) == 3) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "isSystemBrowserDisabled error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean disableClipboard(ComponentName admin, boolean disable) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.clipboard.disable", disable);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isClipboardDisabled() {
        PermissionManager.getInstance().checkPermission();
        return propGetEnable("persist.sys.clipboard.disable", "false");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setRequiredStrongAuthTime(ComponentName admin, long timeoutMs) {
        PermissionManager.getInstance().checkPermission();
        try {
            if (this.mDpm != null) {
                this.mDpm.setRequiredStrongAuthTimeout(admin, timeoutMs);
            } else {
                Log.e("DeviceRestrictionManagerImpl", "mDpm is null");
            }
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "setRequiredStrongAuth error", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public long getRequiredStrongAuthTime(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            if (this.mDpm != null) {
                return this.mDpm.getRequiredStrongAuthTimeout(admin);
            }
            Log.e("DeviceRestrictionManagerImpl", "mDpm is null");
            return -1;
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "getRequiredStrongAuth error", e);
            return -1;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean allowWifiCellularNetwork(ComponentName componentName, String packageName) {
        PermissionManager.getInstance().checkPermission();
        ApplicationInfo appInfo = null;
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(packageName, 4096);
            if (packageInfo != null) {
                appInfo = packageInfo.applicationInfo;
            }
            if (appInfo == null) {
                Log.d("DeviceRestrictionManagerImpl", "allowWifiCellularNetwork: invalid pakageName");
                return false;
            }
            String pkgArray = Settings.Secure.getString(this.mContext.getContentResolver(), "always_allow_wifi_cellular_app");
            if (pkgArray == null) {
                Settings.Secure.putString(this.mContext.getContentResolver(), "always_allow_wifi_cellular_app", packageName);
            } else if (isMdmNetEnabledApp(packageName)) {
                return true;
            } else {
                Settings.Secure.putString(this.mContext.getContentResolver(), "always_allow_wifi_cellular_app", pkgArray + ";" + packageName);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isMdmNetEnabledApp(String packageName) {
        String[] pkgArray;
        String strPkg = Settings.Secure.getString(this.mContext.getContentResolver(), "always_allow_wifi_cellular_app");
        if (!(strPkg == null || (pkgArray = strPkg.split(";")) == null || pkgArray.length == 0)) {
            for (String temp : pkgArray) {
                if (temp.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getDefaultDataCard(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SubscriptionManager.from(this.mContext).getDefaultDataPhoneId();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public Bundle setDefaultDataCard(ComponentName admin, int slot) {
        PermissionManager.getInstance().checkPermission();
        Bundle bundle = new Bundle();
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
            Log.d("DeviceRestrictionManagerImpl", "AIR_PLANE_MODE_ON");
            bundle.putBoolean("RESULT", false);
            bundle.putString("EXCEPTION", "AIR_PLANE_MODE_ON");
            return bundle;
        }
        int subId = getSubId(slot);
        if (!SubscriptionManager.from(this.mContext).isActiveSubId(subId)) {
            Log.d("DeviceRestrictionManagerImpl", "SUBSCRIPTION_INACTIVE");
            bundle.putBoolean("RESULT", false);
            bundle.putString("EXCEPTION", "SUBSCRIPTION_INACTIVE");
            return bundle;
        }
        SubscriptionManager.from(this.mContext).setDefaultDataSubId(subId);
        Settings.Global.putInt(this.mContext.getContentResolver(), "oppo_multi_sim_network_primary_slot", slot);
        bundle.putBoolean("RESULT", true);
        bundle.putString("EXCEPTION", null);
        Log.d("DeviceRestrictionManagerImpl", "setDefaultDataSubId():  " + subId);
        return bundle;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setDataRoamingDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        return propSetEnable("persist.sys.simsettings.data_roaming", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isDataRoamingDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return propGetEnable("persist.sys.simsettings.data_roaming", "0");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setSlot1DataConnectivityDisabled(ComponentName admin, String value) {
        PermissionManager.getInstance().checkPermission();
        propSetEnable("persist.sys.oem_slot1_db", value);
        setDataConnectivityDisabled(0, value);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setSlot2DataConnectivityDisabled(ComponentName admin, String value) {
        PermissionManager.getInstance().checkPermission();
        propSetEnable("persist.sys.oem_slot2_db", value);
        setDataConnectivityDisabled(1, value);
    }

    public void setDataConnectivityDisabled(int slot, String value) {
        SubscriptionManager.from(this.mContext);
        int phoneId = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultSubscriptionId());
        Log.d("DeviceRestrictionManagerImpl", "setDataConnectivityDisabled value = " + value + " phoneId = " + phoneId + " slot = " + slot);
        if (("0".equals(value) || "3".equals(value)) && phoneId == slot) {
            Message msg = Message.obtain();
            msg.what = 2;
            this.mServiceHandler.sendMessage(msg);
        } else if (("1".equals(value) || "4".equals(value)) && phoneId == slot) {
            Message msg2 = Message.obtain();
            msg2.what = 1;
            this.mServiceHandler.sendMessage(msg2);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getSlot1DataConnectivityDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.oem_slot1_db", -1);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getSlot2DataConnectivityDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.oem_slot2_db", -1);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setVoiceOutgoingDisable(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        propSetEnable("persist.sys.oem_vo", !disabled);
        propSetEnable("persist.sys.oem_s1_vo", !disabled);
        propSetEnable("persist.sys.oem_s2_vo", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setVoiceIncomingDisable(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        propSetEnable("persist.sys.oem_vi", !disabled);
        propSetEnable("persist.sys.oem_s1_vi", !disabled);
        propSetEnable("persist.sys.oem_s2_vi", !disabled);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isVoiceOutgoingDisabled(ComponentName admin, int slotId) {
        PermissionManager.getInstance().checkPermission();
        if (slotId == 0) {
            return true ^ propGetEnable("persist.sys.oem_s1_vo", "-1");
        }
        if (slotId == 1) {
            return true ^ propGetEnable("persist.sys.oem_s2_vo", "-1");
        }
        if (slotId == 2) {
            return true ^ propGetEnable("persist.sys.oem_vo", "-1");
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isVoiceIncomingDisabled(ComponentName admin, int slotId) {
        PermissionManager.getInstance().checkPermission();
        if (slotId == 0) {
            return true ^ propGetEnable("persist.sys.oem_s1_vi", "-1");
        }
        if (slotId == 1) {
            return true ^ propGetEnable("persist.sys.oem_s2_vi", "-1");
        }
        if (slotId == 2) {
            return true ^ propGetEnable("persist.sys.oem_vi", "-1");
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setUsbTetheringDisable(ComponentName compName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            Log.d("DeviceRestrictionManagerImpl", "lock UsbTethering and lock UI...");
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                if (connectivityManager != null) {
                    Log.d("DeviceRestrictionManagerImpl", "close UsbTethering...");
                    connectivityManager.stopTethering(1);
                }
                Thread.sleep(1000);
                SystemProperties.set("persist.sys.usb_tethering_disable", "1");
                SystemProperties.set("persist.sys.usb_tethering_clickable", "0");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("DeviceRestrictionManagerImpl", "unlock UsbTethering and unlock UI...");
            SystemProperties.set("persist.sys.usb_tethering_disable", "0");
            SystemProperties.set("persist.sys.usb_tethering_clickable", "1");
            ConnectivityManager connectivityManager2 = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (connectivityManager2 != null) {
                Log.d("DeviceRestrictionManagerImpl", "open UsbTethering...");
                connectivityManager2.startTethering(1, false, new ConnectivityManager.OnStartTetheringCallback() {
                    /* class com.oppo.enterprise.mdmcoreservice.service.managerimpl.DeviceRestrictionManagerImpl.AnonymousClass3 */

                    public void onTetheringStarted() {
                        Log.d("DeviceRestrictionManagerImpl", "onTetheringStarted ");
                    }

                    public void onTetheringFailed() {
                        Log.d("DeviceRestrictionManagerImpl", "onTetheringFailed ");
                    }
                });
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isUsbTetheringDisabled(ComponentName compName) {
        PermissionManager.getInstance().checkPermission();
        String value = SystemProperties.get("persist.sys.usb_tethering_disable", "0");
        Log.d("DeviceRestrictionManagerImpl", "isUsbTetheringDisabled, value: " + value);
        if (value.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean setSystemUpdatePolicies(ComponentName compName, int mode) {
        PermissionManager.getInstance().checkPermission();
        Settings.Secure.putInt(this.mContext.getContentResolver(), "ota_enable_config_custom", mode);
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public int getSystemUpdatePolicies(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "ota_enable_config_custom", -1);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setSettingsApplicationDisabled(ComponentName compName, boolean disable) {
        PermissionManager.getInstance().checkPermission();
        setSettingsApplicationLauncherComponentDisabled(disable);
        Settings.Secure.putInt(this.mContext.getContentResolver(), "setting_application_disable_state", disable ? 1 : 0);
        Log.d("DeviceRestrictionManagerImpl", "call setSettingsApplicationDisabled finish !!! newState = " + ((int) disable) + "disabled = " + disable);
    }

    private void setSettingsApplicationLauncherComponentDisabled(boolean disable) {
        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings");
        PackageManager packageManager = this.mContext.getPackageManager();
        Log.d("DeviceRestrictionManagerImpl", "setSettingsApplicationLauncherComponentDisabled:" + disable);
        if (disable) {
            try {
                packageManager.setComponentEnabledSetting(componentName, 2, 0);
            } catch (Exception e) {
                Log.d("DeviceRestrictionManagerImpl", "setSettingsApplicationLauncherComponentDisabled Exception :" + e.toString());
            }
        } else {
            packageManager.setComponentEnabledSetting(componentName, 0, 0);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isSettingsApplicationDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        int state = Settings.Secure.getInt(this.mContext.getContentResolver(), "setting_application_disable_state", 0);
        boolean isSettingsApplicationDisabled = true;
        if (state != 1) {
            isSettingsApplicationDisabled = false;
        }
        Log.d("DeviceRestrictionManagerImpl", "call isSettingsApplicationDisabled finish!!! state = " + state + " isChangeWallpaperDisabled =" + isSettingsApplicationDisabled);
        return isSettingsApplicationDisabled;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setNavigationBarDisabled(ComponentName componentName, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        Settings.Secure.putInt(this.mContext.getContentResolver(), "nav_bar_disable", disabled ? 1 : 0);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isNavigationBarDisabled(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "nav_bar_disable", 0) == 1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isMultiAppSupport() {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getBoolean("persist.sys.oplus.multiapp.support", true);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setMultiAppSupport(boolean support) {
        PermissionManager.getInstance().checkPermission();
        SystemProperties.set("persist.sys.oplus.multiapp.support", support ? "true" : "false");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setPowerSavingModeDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceRestrictionManagerImpl", "setPowerSavingModeDisabled: disabled=" + disabled);
        try {
            if (Build.VERSION.SDK_INT <= 28) {
                PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
                if (disabled && pm.isPowerSaveMode()) {
                    pm.setPowerSaveMode(false);
                    Log.d("DeviceRestrictionManagerImpl", "setPowerSavingModeDisabled: setPowerSaveMode false");
                }
            }
            Settings.Secure.putInt(this.mContext.getContentResolver(), "power_save_mode_disable_config_custom", disabled ? 1 : 0);
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "setPowerSavingModeDisabled error", e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public boolean isPowerSavingModeDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            return Settings.Secure.getInt(this.mContext.getContentResolver(), "power_save_mode_disable_config_custom", 0) == 1;
        } catch (Exception e) {
            Log.e("DeviceRestrictionManagerImpl", "isPowerSavingModeDisabled error", e);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public void setApplicationDisabledInLauncherOrRecentTask(List<String> list, int flag) {
        PermissionManager.getInstance().checkPermission();
        Log.e("DeviceRestrictionManagerImpl", "setApplicationDisabledInLauncherOrRecentTask list.size = " + list.size() + ",flag = " + flag);
        if (((flag >> 12) & 1) != 0) {
            if ((flag & 1) != 0) {
                setLauncherOrRecentBannedPkgsToSettings(list, "desktop_icon_banned_pkgs", true);
            } else {
                setLauncherOrRecentBannedPkgsToSettings(list, "desktop_icon_banned_pkgs", false);
            }
        }
        if (((flag >> 28) & 1) == 0) {
            return;
        }
        if (((flag >> 16) & 1) != 0) {
            setLauncherOrRecentBannedPkgsToSettings(list, "recents_banned_pkgs", true);
        } else {
            setLauncherOrRecentBannedPkgsToSettings(list, "recents_banned_pkgs", false);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceRestrictionManager
    public List<String> getApplicationDisabledInLauncherOrRecentTask(int flag) {
        PermissionManager.getInstance().checkPermission();
        Log.e("DeviceRestrictionManagerImpl", "getApplicationDisabledInLauncherOrRecentTask flag = " + flag);
        List<String> result = new ArrayList<>();
        String settingKey = null;
        if (((flag >> 12) & 1) != 0) {
            if ((flag & 1) != 0) {
                settingKey = "desktop_icon_banned_pkgs";
            }
        } else if (!(((flag >> 28) & 1) == 0 || ((flag >> 16) & 1) == 0)) {
            settingKey = "recents_banned_pkgs";
        }
        if (!TextUtils.isEmpty(settingKey)) {
            Set<String> bannedPkgs = getLauncherOrRecentTaskBannedPkgsFromSettings(settingKey);
            if (bannedPkgs.isEmpty()) {
                return Collections.emptyList();
            }
            result.addAll(bannedPkgs);
        }
        return result;
    }

    private Set<String> getLauncherOrRecentTaskBannedPkgsFromSettings(String name) {
        String iconBannedPkgsString = Settings.Secure.getString(this.mContext.getContentResolver(), name);
        if (TextUtils.isEmpty(iconBannedPkgsString)) {
            return Collections.emptySet();
        }
        Set<String> bannedPkgs = new ArraySet<>();
        sBannedStringColonSplitter.setString(iconBannedPkgsString);
        while (sBannedStringColonSplitter.hasNext()) {
            String bannedPkg = sBannedStringColonSplitter.next();
            if (!TextUtils.isEmpty(bannedPkg)) {
                bannedPkgs.add(bannedPkg);
            }
        }
        return bannedPkgs;
    }

    private void setLauncherOrRecentBannedPkgsToSettings(List<String> list, String name, boolean banned) {
        if (!TextUtils.isEmpty(name)) {
            Set<String> bannedPkgs = getLauncherOrRecentTaskBannedPkgsFromSettings(name);
            if (bannedPkgs.isEmpty()) {
                bannedPkgs = new ArraySet();
            }
            if (banned) {
                bannedPkgs.addAll(list);
            } else if (!bannedPkgs.isEmpty()) {
                bannedPkgs.removeAll(list);
            }
            StringBuilder bannedPkgsBuilder = new StringBuilder();
            for (String bannedPkg : bannedPkgs) {
                bannedPkgsBuilder.append(bannedPkg);
                bannedPkgsBuilder.append(':');
            }
            int bannedPkgsBuilderLength = bannedPkgsBuilder.length();
            if (bannedPkgsBuilderLength > 0) {
                bannedPkgsBuilder.deleteCharAt(bannedPkgsBuilderLength - 1);
            }
            Settings.Secure.putString(this.mContext.getContentResolver(), name, bannedPkgsBuilder.toString());
        }
    }
}
