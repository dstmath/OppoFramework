package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IOppoCustomizeService;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.parser.JSONToken;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceConnectivityManager;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceRestrictionManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import com.oppo.enterprise.mdmcoreservice.utils.WifiProfileXmlUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONObject;

public class DeviceConnectivityManagerImpl extends IDeviceConnectivityManager.Stub {
    private int BT_BLACK = 1;
    private int BT_WHITE = 0;
    private Context mContext;
    private IOppoCustomizeService mCustService;
    private LocationListener mGlobalLocationListeners = new LocationListener() {
        /* class com.oppo.enterprise.mdmcoreservice.service.managerimpl.DeviceConnectivityManagerImpl.AnonymousClass3 */

        public void onLocationChanged(Location newLocation) {
            Log.i("DeviceConnectivityManagerImpl", "GlobalLocationListeners, onLocationChanged, newLocation : " + newLocation);
            DeviceConnectivityManagerImpl.this.stopGlobalLocation();
        }

        public void onProviderEnabled(String provider) {
            Log.i("DeviceConnectivityManagerImpl", "mGlobalLocationListeners nProviderEnabled provider = " + provider);
        }

        public void onProviderDisabled(String provider) {
            Log.i("DeviceConnectivityManagerImpl", "mGlobalLocationListeners nProviderDisabled provider = " + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("DeviceConnectivityManagerImpl", "mGlobalLocationListeners nStatusChanged provider = " + provider + ", status = " + status + ", extras = " + extras);
        }
    };
    private LocationManager mLocationManager;
    private WifiManager mWifiManager;

    public DeviceConnectivityManagerImpl(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mCustService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean addBluetoothDevicesToBlackList(ComponentName admin, List<String> devices) {
        PermissionManager.getInstance().checkPermission();
        if (!checkBtDevicelist(devices)) {
            return false;
        }
        try {
            this.mCustService.addBluetoothDevicesToBlackList(devices);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean addBluetoothDevicesToWhiteList(ComponentName admin, List<String> devices) {
        PermissionManager.getInstance().checkPermission();
        if (!checkBtDevicelist(devices)) {
            return false;
        }
        try {
            this.mCustService.addBluetoothDevicesToWhiteList(devices);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean removeBluetoothDevicesFromBlackList(ComponentName admin, List<String> devices) {
        PermissionManager.getInstance().checkPermission();
        if (!checkBtDevicelist(admin, this.BT_BLACK, devices)) {
            return false;
        }
        try {
            this.mCustService.removeBluetoothDevicesFromBlackList(devices);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean removeBluetoothDevicesFromWhiteList(ComponentName admin, List<String> devices) {
        PermissionManager.getInstance().checkPermission();
        if (!checkBtDevicelist(admin, this.BT_WHITE, devices)) {
            return false;
        }
        try {
            this.mCustService.removeBluetoothDevicesFromWhiteList(devices);
            List<String> whiteList = this.mCustService.getBluetoothDevicesFromWhiteLists();
            if (whiteList != null && !whiteList.isEmpty()) {
                return true;
            }
            ArrayList<String> devicess = new ArrayList<>();
            devicess.add("aa:bb:cc:dd:ee:ff");
            this.mCustService.addBluetoothDevicesToBlackList(devicess);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean checkBtDevicelist(List<String> devices) {
        if (devices == null || devices.isEmpty()) {
            return false;
        }
        return checkMacAddressLegality(devices);
    }

    private boolean checkBtDevicelist(ComponentName admin, int mode, List<String> devices) {
        if (devices != null && !devices.isEmpty()) {
            return checkMacAddressLegality(devices);
        }
        if (mode == this.BT_WHITE) {
            List<String> mBTDeviceW = getBluetoothDevicesFromWhiteLists(admin);
            if (mBTDeviceW != null && !mBTDeviceW.isEmpty()) {
                removeBluetoothDevicesFromWhiteList(admin, mBTDeviceW);
            }
        } else {
            List<String> mBTDeviceB = getBluetoothDevicesFromBlackLists(admin);
            if (mBTDeviceB != null && !mBTDeviceB.isEmpty()) {
                removeBluetoothDevicesFromBlackList(admin, mBTDeviceB);
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0014  */
    private boolean checkMacAddressLegality(List<String> devices) {
        if (devices == null || devices.isEmpty()) {
            return false;
        }
        for (String device : devices) {
            if (device == null || device.length() > 17 || !matchFuzzy(device)) {
                return false;
            }
            while (r1.hasNext()) {
            }
        }
        return true;
    }

    private boolean matchFuzzy(String device) {
        if (device.contains("*")) {
            return Pattern.compile("^[0-9a-fA-F]{2}((:[0-9a-fA-F]{2})|(-[0-9a-fA-F]{2})){2,5}([/*]+)$").matcher(device).find() || Pattern.compile("^(([0-9a-fA-F]{2}-)|(([0-9a-fA-F]{2}:))){3,5}([0-9a-fA-F]){0,1}([/*]+)$").matcher(device).find();
        }
        return Pattern.compile("^[0-9a-fA-F]{2}((:[0-9a-fA-F]{2})|(-[0-9a-fA-F]{2})){5}$").matcher(device).find();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getBluetoothDevicesFromBlackLists(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<String> resolveList = new ArrayList<>();
        try {
            return this.mCustService.getBluetoothDevicesFromBlackLists();
        } catch (RemoteException e) {
            e.printStackTrace();
            return resolveList;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getBluetoothDevicesFromWhiteLists(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<String> resolveList = new ArrayList<>();
        try {
            return this.mCustService.getBluetoothDevicesFromWhiteLists();
        } catch (RemoteException e) {
            e.printStackTrace();
            return resolveList;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isBlackListedDevice(ComponentName admin, String device) {
        PermissionManager.getInstance().checkPermission();
        List<String> macLists = getBluetoothDevicesFromBlackLists(admin);
        if (device == null || macLists == null || macLists.isEmpty()) {
            return false;
        }
        for (String addr : macLists) {
            if (device.equalsIgnoreCase(addr)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isWhiteListedDevice(ComponentName admin, String device) {
        PermissionManager.getInstance().checkPermission();
        List<String> macLists = getBluetoothDevicesFromWhiteLists(admin);
        if (device == null || macLists == null || macLists.isEmpty()) {
            return false;
        }
        for (String addr : macLists) {
            if (device.equalsIgnoreCase(addr)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean addSSIDToBlackList(ComponentName admin, List<String> ssids) {
        PermissionManager.getInstance().checkPermission();
        if (!checkSsidLegality(ssids)) {
            return false;
        }
        try {
            return this.mCustService.setWifiSsidBlackList(ssids);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean removeSSIDFromBlackList(ComponentName admin, List<String> ssids) {
        PermissionManager.getInstance().checkPermission();
        if (!checkSsidLegality(ssids)) {
            return false;
        }
        try {
            return this.mCustService.removeSSIDFromBlackList(ssids);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getSSIDBlackList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<String> ret = new ArrayList<>();
        try {
            return this.mCustService.getWifiSsidBlackList();
        } catch (RemoteException e) {
            e.printStackTrace();
            return ret;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isBlackListedSSID(ComponentName admin, String ssid) {
        PermissionManager.getInstance().checkPermission();
        new ArrayList();
        List<String> ssidList = getSSIDBlackList(admin);
        if (ssid == null || ssidList == null || ssidList.isEmpty()) {
            return false;
        }
        for (String id : ssidList) {
            if (ssid.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean addSSIDToWhiteList(ComponentName admin, List<String> ssids) {
        PermissionManager.getInstance().checkPermission();
        if (!checkSsidLegality(ssids)) {
            return false;
        }
        try {
            return this.mCustService.setWifiSsidWhiteList(ssids);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean removeSSIDFromWhiteList(ComponentName admin, List<String> ssids) {
        List<String> whiteList;
        PermissionManager.getInstance().checkPermission();
        boolean ret = false;
        if (!checkSsidLegality(ssids)) {
            return false;
        }
        try {
            ret = this.mCustService.removeSSIDFromWhiteList(ssids);
            if (ret && ((whiteList = this.mCustService.getWifiSsidWhiteList()) == null || whiteList.isEmpty())) {
                ArrayList<String> devicess = new ArrayList<>();
                devicess.add("aa:bb:cc:dd:ee:ff");
                this.mCustService.setWifiBssidBlackList(devicess);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getSSIDWhiteList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<String> ret = new ArrayList<>();
        try {
            return this.mCustService.getWifiSsidWhiteList();
        } catch (RemoteException e) {
            e.printStackTrace();
            return ret;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isWhiteListedSSID(String ssid) {
        PermissionManager.getInstance().checkPermission();
        new ArrayList();
        List<String> ssidList = getSSIDWhiteList(null);
        if (ssid == null || ssidList == null || ssidList.isEmpty()) {
            return false;
        }
        for (String id : ssidList) {
            if (ssid.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0014  */
    private boolean checkSsidLegality(List<String> ssids) {
        if (ssids == null || ssids.size() == 0) {
            Log.d("DeviceConnectivityManagerImpl", "ssids is null or size is zero");
            return false;
        }
        for (String str : ssids) {
            if (str == null || str.length() <= 0 || str.length() > 32) {
                Log.d("DeviceConnectivityManagerImpl", "there is illegal ssid");
                return false;
            }
            while (r1.hasNext()) {
            }
        }
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean addBSSIDToWhiteList(List<String> bssids) {
        PermissionManager.getInstance().checkPermission();
        try {
            List<String> bssids2 = fuzzyMac2Policy(bssids, 8, 17);
            if (bssids2 != null) {
                if (bssids2.size() > 0) {
                    Log.d("DeviceConnectivityManagerImpl", "addBSSIDToWhiteList: " + bssids2.toString());
                    return this.mCustService.setWifiBssidWhiteList(bssids2);
                }
            }
            return false;
        } catch (RemoteException e) {
            Log.d("DeviceConnectivityManagerImpl", "addBSSIDToWhiteList fail!");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean addBSSIDToBlackList(List<String> bssids) {
        PermissionManager.getInstance().checkPermission();
        try {
            List<String> bssids2 = fuzzyMac2Policy(bssids, 8, 17);
            if (bssids2 != null) {
                if (bssids2.size() > 0) {
                    Log.d("DeviceConnectivityManagerImpl", "addBSSIDToBlackList: " + bssids2.toString());
                    return this.mCustService.setWifiBssidBlackList(bssids2);
                }
            }
            return false;
        } catch (RemoteException e) {
            Log.d("DeviceConnectivityManagerImpl", "addBSSIDToBlackList fail!");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean removeBSSIDFromWhiteList(List<String> bssids) {
        PermissionManager.getInstance().checkPermission();
        if (bssids != null) {
            try {
                int bssidsSize = bssids.size();
                bssids = fuzzyMac2Policy(bssids, 8, 17);
                if (bssidsSize <= 0 || bssids.size() < bssidsSize) {
                    Log.d("DeviceConnectivityManagerImpl", "removeBSSIDFromWhiteList bssids is " + bssids + ", bssidsSize is " + bssidsSize);
                    return false;
                }
            } catch (RemoteException e) {
                Log.d("DeviceConnectivityManagerImpl", "removeBSSIDFromWhiteList fail!");
                return false;
            }
        }
        Log.d("DeviceConnectivityManagerImpl", "removeBSSIDFromWhiteList bssids is " + bssids);
        return this.mCustService.removeBSSIDFromWhiteList(bssids);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean removeBSSIDFromBlackList(List<String> bssids) {
        PermissionManager.getInstance().checkPermission();
        if (bssids != null) {
            try {
                int bssidsSize = bssids.size();
                bssids = fuzzyMac2Policy(bssids, 8, 17);
                if (bssidsSize <= 0 || bssids.size() < bssidsSize) {
                    Log.d("DeviceConnectivityManagerImpl", "removeBSSIDFromWhiteList bssids is " + bssids + ", bssidsSize is " + bssidsSize);
                    return false;
                }
            } catch (RemoteException e) {
                Log.d("DeviceConnectivityManagerImpl", "removeBSSIDFromBlackList fail!");
                return false;
            }
        }
        Log.d("DeviceConnectivityManagerImpl", "removeBSSIDFromWhiteList bssids is " + bssids);
        return this.mCustService.removeBSSIDFromBlackList(bssids);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getBSSIDBlackList() {
        PermissionManager.getInstance().checkPermission();
        List<String> resolveList = new ArrayList<>();
        try {
            resolveList = addSuffix(this.mCustService.getWifiBssidBlackList(), "*", 17);
        } catch (RemoteException e) {
            Log.d("DeviceConnectivityManagerImpl", "getBSSIDBlackList fail!");
        }
        if (resolveList == null || resolveList.isEmpty()) {
            return null;
        }
        return resolveList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getBSSIDWhiteList() {
        PermissionManager.getInstance().checkPermission();
        List<String> resolveList = new ArrayList<>();
        try {
            resolveList = addSuffix(this.mCustService.getWifiBssidWhiteList(), "*", 17);
        } catch (RemoteException e) {
            Log.d("DeviceConnectivityManagerImpl", "getBSSIDWhiteList fail!");
        }
        if (resolveList == null || resolveList.isEmpty()) {
            return null;
        }
        return resolveList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isWhiteListedBSSID(String bssid) {
        PermissionManager.getInstance().checkPermission();
        StringBuffer temp = fuzzyAddress2StringBuffer(bssid, ':', '-', '*');
        if (temp != null) {
            try {
                return this.mCustService.isWhiteListedBSSID(temp.toString());
            } catch (RemoteException e) {
                Log.d("DeviceConnectivityManagerImpl", "isWhiteListedBSSID fail!");
                return false;
            }
        } else {
            Log.d("DeviceConnectivityManagerImpl", "bssid " + bssid + " is wrong!");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isBlackListedBSSID(String bssid) {
        PermissionManager.getInstance().checkPermission();
        StringBuffer temp = fuzzyAddress2StringBuffer(bssid, ':', '-', '*');
        if (temp != null) {
            try {
                return this.mCustService.isBlackListedBSSID(temp.toString());
            } catch (RemoteException e) {
                Log.d("DeviceConnectivityManagerImpl", "isBlackListedBSSID fail!");
                return false;
            }
        } else {
            Log.d("DeviceConnectivityManagerImpl", "bssid " + bssid + " is wrong!");
            return false;
        }
    }

    private List<String> addSuffix(List<String> list, String suffix, int maxLength) {
        List<String> newList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (String str : list) {
                if (str.length() < maxLength) {
                    newList.add(str + suffix);
                } else {
                    newList.add(str);
                }
            }
        }
        return newList;
    }

    private List<String> fuzzyMac2Policy(List<String> list, int minLength, int maxLength) {
        List<String> macList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (String mac : list) {
                StringBuffer temp = fuzzyAddress2StringBuffer(mac, ':', '-', '*');
                if (temp != null) {
                    String mac2 = temp.toString();
                    if (mac2.length() >= minLength && mac2.length() <= maxLength) {
                        macList.add(mac2);
                    }
                }
            }
        }
        return macList;
    }

    private StringBuffer fuzzyAddress2StringBuffer(String address, char from, char to, char end) {
        StringBuffer temp = new StringBuffer();
        for (int i = 0; i < address.length(); i++) {
            char c = address.charAt(i);
            if (c == to) {
                temp.append(from);
            } else if (c == end) {
                break;
            } else {
                temp.append(c);
            }
        }
        return temp;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setWlanPolicies(ComponentName admin, int mode) {
        PermissionManager.getInstance().checkPermission();
        int prePolicy = getWlanPolicies(null);
        try {
            setProp("persist.sys.wifi_policy", "2");
            switch (mode) {
                case 0:
                    Log.d("DeviceConnectivityManagerImpl", "lock wifi and lock UI...");
                    this.mWifiManager.setWifiEnabled(false);
                    setProp("persist.sys.wifi_clickable", "0");
                    setProp("persist.sys.wifi_grey", "1");
                    break;
                case 1:
                    Log.d("DeviceConnectivityManagerImpl", "close wifi with scan always mode and lock UI ...");
                    setDB("wifi_scan_always_enabled", 1);
                    this.mWifiManager.setWifiEnabled(false);
                    setProp("persist.sys.wifi_clickable", "0");
                    setProp("persist.sys.wifi_grey", "1");
                    break;
                case 2:
                    Log.d("DeviceConnectivityManagerImpl", "unlock wifi and unlock UI...");
                    setProp("persist.sys.wifi_clickable", "1");
                    setProp("persist.sys.wifi_grey", "0");
                    break;
                case 3:
                    this.mWifiManager.setWifiEnabled(false);
                    setProp("persist.sys.wifi_clickable", "1");
                    setProp("persist.sys.wifi_grey", "0");
                    break;
                case 4:
                    this.mWifiManager.setWifiEnabled(true);
                    setProp("persist.sys.wifi_clickable", "1");
                    setProp("persist.sys.wifi_grey", "0");
                    break;
                case 5:
                    this.mWifiManager.setWifiEnabled(true);
                    setProp("persist.sys.wifi_clickable", "0");
                    setProp("persist.sys.wifi_grey", "0");
                    break;
                default:
                    setProp("persist.sys.wifi_policy", String.valueOf(prePolicy));
                    return false;
            }
            setProp("persist.sys.wifi_policy", String.valueOf(mode));
            return true;
        } catch (Exception ex) {
            setProp("persist.sys.wifi_policy", String.valueOf(prePolicy));
            Log.e("DeviceConnectivityManagerImpl", "setWlanPolicies exception!", ex);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public int getWlanPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return Integer.valueOf(getProp("persist.sys.wifi_policy", "2")).intValue();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setWifiApPolicies(ComponentName admin, int mode) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceConnectivityManagerImpl", "setWifiApPolicies, Mode: " + mode);
        ConnectivityManager mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (mConnectivityManager == null) {
            Log.e("DeviceConnectivityManagerImpl", "setWifiApPolicies mConnectivityManager is null");
            return false;
        }
        int prePolicy = getWifiApPolicies(admin);
        try {
            SystemProperties.set("persist.sys.wifi_ap_policy", String.valueOf(0));
            switch (mode) {
                case 0:
                    setProp("persist.sys.ap_clickable", "1");
                    setProp("persist.sys.ap_grey", "0");
                    break;
                case 1:
                    if (this.mWifiManager.getWifiApState() == 13) {
                        mConnectivityManager.stopTethering(0);
                        Thread.sleep(1000);
                    }
                    setProp("persist.sys.ap_clickable", "0");
                    setProp("persist.sys.ap_grey", "1");
                    break;
                case 2:
                    if (this.mWifiManager.getWifiApState() == 11) {
                        mConnectivityManager.startTethering(0, false, new ConnectivityManager.OnStartTetheringCallback() {
                            /* class com.oppo.enterprise.mdmcoreservice.service.managerimpl.DeviceConnectivityManagerImpl.AnonymousClass1 */

                            public void onTetheringStarted() {
                                Log.d("DeviceConnectivityManagerImpl", "onTetheringStarted ");
                            }

                            public void onTetheringFailed() {
                                Log.d("DeviceConnectivityManagerImpl", "onTetheringFailed ");
                            }
                        });
                        Thread.sleep(1000);
                    }
                    setProp("persist.sys.ap_clickable", "0");
                    setProp("persist.sys.ap_grey", "0");
                    break;
                case 3:
                    if (this.mWifiManager.getWifiApState() == 13) {
                        mConnectivityManager.stopTethering(0);
                        Thread.sleep(1000);
                    }
                    setProp("persist.sys.ap_clickable", "1");
                    setProp("persist.sys.ap_grey", "0");
                    break;
                case 4:
                    if (this.mWifiManager.getWifiApState() == 11) {
                        mConnectivityManager.startTethering(0, false, new ConnectivityManager.OnStartTetheringCallback() {
                            /* class com.oppo.enterprise.mdmcoreservice.service.managerimpl.DeviceConnectivityManagerImpl.AnonymousClass2 */

                            public void onTetheringStarted() {
                                Log.d("DeviceConnectivityManagerImpl", "onTetheringStarted ");
                            }

                            public void onTetheringFailed() {
                                Log.d("DeviceConnectivityManagerImpl", "onTetheringFailed ");
                            }
                        });
                        Thread.sleep(1000);
                    }
                    setProp("persist.sys.ap_clickable", "1");
                    setProp("persist.sys.ap_grey", "0");
                    break;
                default:
                    Log.e("DeviceConnectivityManagerImpl", "setWifiApPolicies, Invalid Mode: " + mode);
                    setProp("persist.sys.wifi_ap_policy", String.valueOf(prePolicy));
                    return false;
            }
            setProp("persist.sys.wifi_ap_policy", String.valueOf(mode));
            return true;
        } catch (Exception e) {
            Log.e("DeviceConnectivityManagerImpl", "setWifiApPolicies exception!");
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public int getWifiApPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.wifi_ap_policy", 3);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setBluetoothPolicies(ComponentName admin, int mode) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceConnectivityManagerImpl", "setBluetoothPolicies, Mode: " + mode);
        DeviceRestrictionManager deviceRestrictionManager = DeviceRestrictionManager.getInstance(this.mContext);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (deviceRestrictionManager == null || bluetoothAdapter == null) {
            Log.i("DeviceConnectivityManagerImpl", "getBluetoothPolicies deviceRestrictionManager or bluetoothAdapter == null");
            return false;
        }
        setProp("persist.sys.bluetooth_policy", String.valueOf(mode));
        String clickable = SystemProperties.get("persist.sys.bt_clickable", "1");
        String grep = SystemProperties.get("persist.sys.bt_grey", "0");
        if (clickable.equals("0")) {
            SystemProperties.set("persist.sys.bt_clickable", "1");
        }
        if (grep.equals("1")) {
            SystemProperties.set("persist.sys.bt_grey", "0");
        }
        switch (mode) {
            case 0:
                deviceRestrictionManager.setBluetoothDisabled(admin, true);
                break;
            case 1:
            case 2:
                break;
            case 3:
                deviceRestrictionManager.setBluetoothEnabled(admin, true);
                break;
            case 4:
                bluetoothAdapter.disable();
                break;
            case 5:
                try {
                    bluetoothAdapter.enable();
                    break;
                } catch (Exception ex) {
                    Log.e("DeviceConnectivityManagerImpl", "setWlanPolicies exception!", ex);
                    setProp("persist.sys.bluetooth_policy", String.valueOf(2));
                    return false;
                }
            default:
                setProp("persist.sys.bluetooth_policy", String.valueOf(2));
                return false;
        }
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public int getBluetoothPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return SystemProperties.getInt("persist.sys.bluetooth_policy", 2);
    }

    /* JADX INFO: finally extract failed */
    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getWlanApClientBlackList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Log.d("DeviceConnectivityManagerImpl", "getWlanApClientBlackList uid: " + Binder.getCallingUid() + " ,pid: " + Binder.getCallingPid() + " ,packageName: " + packageName);
        long identity = Binder.clearCallingIdentity();
        try {
            List<String> wlanApClientBlackList = this.mCustService.getWlanApClientBlackList();
            Binder.restoreCallingIdentity(identity);
            return wlanApClientBlackList;
        } catch (Exception ex) {
            ex.printStackTrace();
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public void removeWlanApClientBlackList(ComponentName admin, List<String> list) {
        PermissionManager.getInstance().checkPermission();
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Log.d("DeviceConnectivityManagerImpl", "removeWlanApClientBlackList uid: " + Binder.getCallingUid() + " ,pid: " + Binder.getCallingPid() + " ,packageName: " + packageName);
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustService.removeWlanApClientBlackList(mac2Policy(list));
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public void setWlanApClientBlackList(ComponentName admin, List<String> list) {
        PermissionManager.getInstance().checkPermission();
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Log.d("DeviceConnectivityManagerImpl", "setWlanApClientBlackList uid: " + Binder.getCallingUid() + " ,pid: " + Binder.getCallingPid() + " ,packageName: " + packageName);
        long identity = Binder.clearCallingIdentity();
        try {
            this.mCustService.setWlanApClientBlackList(mac2Policy(list));
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setWlanConfiguration(ComponentName admin, List<String> wlanConfigs) {
        PermissionManager.getInstance().checkPermission();
        int netId = -1;
        if (wlanConfigs == null || wlanConfigs.size() <= 0) {
            return false;
        }
        for (String wlanconfig : wlanConfigs) {
            netId = addNetwork(wlanconfig);
            if (netId == -1) {
                Log.i("DeviceConnectivityManagerImpl", "setWlanConfiguration error:" + wlanconfig);
                return false;
            }
        }
        return this.mWifiManager.enableNetwork(netId, true);
    }

    private int addNetwork(String wlanConfig) {
        if (wlanConfig == null || wlanConfig.isEmpty()) {
            return -1;
        }
        try {
            JSONObject macAddress = new JSONObject(wlanConfig);
            WifiConfiguration wifiConf = createConfig((String) macAddress.get("ssid"), (String) macAddress.get("bssid"), (String) macAddress.get("pwd"));
            if (wifiConf != null) {
                return this.mWifiManager.addNetwork(wifiConf);
            }
            return -1;
        } catch (Exception e) {
            Log.e("DeviceConnectivityManagerImpl", "parese setWlanConfigReal error:" + wlanConfig, e);
            return -1;
        }
    }

    private WifiConfiguration createConfig(String ssid, String bssid, String pwd) {
        WifiConfiguration wifiConf = new WifiConfiguration();
        if (TextUtils.isEmpty(ssid) || ssid.length() > 32) {
            Log.i("DeviceConnectivityManagerImpl", "ssid is error!");
            return null;
        } else if (TextUtils.isEmpty(pwd) || pwd.length() < 8) {
            Log.i("DeviceConnectivityManagerImpl", "pwd is error!");
            return null;
        } else if (TextUtils.isEmpty(bssid)) {
            Log.i("DeviceConnectivityManagerImpl", "bssid is null!");
            return null;
        } else {
            wifiConf.SSID = '\"' + ssid + '\"';
            wifiConf.BSSID = bssid;
            wifiConf.status = 2;
            wifiConf.allowedKeyManagement.set(1);
            wifiConf.preSharedKey = '\"' + pwd + '\"';
            return wifiConf;
        }
    }

    private List<String> mac2Policy(List<String> list) {
        List<String> macList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (String mac : list) {
                if (address2StringBuffer(mac, ':', '-') != null) {
                    macList.add(address2StringBuffer(mac, ':', '-').toString());
                }
            }
        }
        return macList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getWlanConfiguration(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<String> wlanConfigs = new ArrayList<>();
        try {
            List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
            if (configs.size() <= 0) {
                return null;
            }
            for (WifiConfiguration config : configs) {
                if (config.SSID != null || config.BSSID != null || config.preSharedKey != null) {
                    Log.i("DeviceConnectivityManagerImpl", "ssid:" + config.SSID + ",bssid:" + config.BSSID + ".psd:" + config.preSharedKey);
                    try {
                        JSONObject object = new JSONObject();
                        object.put("ssid", config.SSID);
                        object.put("bssid", config.BSSID);
                        object.put("psd", config.preSharedKey);
                        Log.d("DeviceConnectivityManagerImpl", object.toString());
                        wlanConfigs.add(object.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return wlanConfigs;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public String getWifiMacAddress(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        WifiInfo mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            return mWifiInfo.getMacAddress();
        }
        Log.e("DeviceConnectivityManagerImpl", "getWifiMacAddress error!");
        return null;
    }

    private StringBuffer address2StringBuffer(String address, char from, char to) {
        StringBuffer temp = new StringBuffer();
        if (address.length() < 17) {
            return null;
        }
        for (int i = 0; i < 17; i++) {
            char c = address.charAt(i);
            if ((c >= '0' && c <= '9') || ((c >= 'A' && c <= 'F') || ((c >= 'a' && c <= 'f') || c == ':'))) {
                temp.append(c);
            } else if (c == to) {
                temp.append(from);
            }
        }
        return temp;
    }

    private void setDB(String key, int value) {
        if (key != null) {
            Log.d("DeviceConnectivityManagerImpl", "set database,key:" + key + ",value:" + value);
        }
        long identity = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            switch (key.hashCode()) {
                case -1861103900:
                    if (key.equals("OTG_ENABLED")) {
                        c = 6;
                        break;
                    }
                    break;
                case -1719770601:
                    if (key.equals("oppo_settings_manager_facelock")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1629069963:
                    if (key.equals("oppo_settings_manager_fingerprint")) {
                        c = 0;
                        break;
                    }
                    break;
                case -1256362148:
                    if (key.equals("oppo_settings_manager_time")) {
                        c = 2;
                        break;
                    }
                    break;
                case -970351711:
                    if (key.equals("adb_enabled")) {
                        c = 4;
                        break;
                    }
                    break;
                case -50521511:
                    if (key.equals("ZQ_ADB_ENABLED")) {
                        c = 3;
                        break;
                    }
                    break;
                case 937026755:
                    if (key.equals("MTP_TRANSFER_ENABLED")) {
                        c = 5;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case JSONToken.TRUE /* 6 */:
                    if (value == 0 || value == 1) {
                        Settings.Secure.putInt(this.mContext.getContentResolver(), key, value);
                        break;
                    }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String getProp(String prop, String defaultValue) {
        PermissionManager.getInstance().checkPermission();
        if (prop == null || prop.length() <= 0) {
            return defaultValue;
        }
        Log.d("DeviceConnectivityManagerImpl", "get prop:" + prop + ", value:" + defaultValue);
        long identity = Binder.clearCallingIdentity();
        try {
            return SystemProperties.get(prop, defaultValue);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setProp(String prop, String value) {
        PermissionManager.getInstance().checkPermission();
        if (prop != null && prop.length() > 0 && value != null) {
            Log.d("DeviceConnectivityManagerImpl", "set prop:" + prop + ",value:" + value);
            long identity = Binder.clearCallingIdentity();
            try {
                SystemProperties.set(prop, value);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setNfcPolicies(ComponentName admin, int mode) {
        PermissionManager.getInstance().checkPermission();
        DeviceRestrictionManager deviceRestrictionManager = DeviceRestrictionManager.getInstance(this.mContext);
        if (deviceRestrictionManager == null) {
            Log.i("DeviceConnectivityManagerImpl", "setNfcPolicies deviceRestrictionManager == null");
            return false;
        }
        switch (mode) {
            case 0:
                deviceRestrictionManager.openCloseNFC(admin, false);
                setProp("persist.sys.nfc_disable", "1");
                setProp("persist.sys.nfc.always_enable", "0");
                setProp("persist.sys.nfc_clickable", "0");
                setProp("persist.sys.nfc_grey", "1");
                break;
            case 1:
                setProp("persist.sys.nfc_disable", "0");
                setProp("persist.sys.nfc.always_enable", "1");
                setProp("persist.sys.nfc_clickable", "0");
                setProp("persist.sys.nfc_grey", "0");
                deviceRestrictionManager.openCloseNFC(admin, true);
                break;
            case 2:
                setProp("persist.sys.nfc_disable", "0");
                setProp("persist.sys.nfc.always_enable", "0");
                setProp("persist.sys.nfc_clickable", "1");
                setProp("persist.sys.nfc_grey", "0");
                break;
            default:
                return false;
        }
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public int getNfcPolicies(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String disable = getProp("persist.sys.nfc_disable", "0");
        String forceEnable = getProp("persist.sys.nfc.always_enable", "0");
        if (disable.equals("1") && forceEnable.equals("0")) {
            return 0;
        }
        if (!disable.equals("0") || !forceEnable.equals("1")) {
            return 2;
        }
        return 1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setSecurityLevel(ComponentName admin, int level) {
        PermissionManager.getInstance().checkPermission();
        if (level < 0 || level > 3) {
            Log.d("DeviceConnectivityManagerImpl", "level is illegal = " + level);
            return false;
        } else if (level == 0) {
            setProp("persist.sys.wifi_secure", "0");
            setProp("persist.sys.wifi_secure_level", "0");
            return true;
        } else {
            setProp("persist.sys.wifi_secure", "1");
            setProp("persist.sys.wifi_secure_level", String.valueOf(level));
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public int getSecurityLevel(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String disable = getProp("persist.sys.wifi_secure", "0");
        String level = getProp("persist.sys.wifi_secure_level", "0");
        if (disable.equals("0")) {
            return 0;
        }
        if (disable.equals("1")) {
            return Integer.parseInt(level);
        }
        return -1;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setUnSecureSoftApDisabled(ComponentName admin, boolean disable) {
        WifiConfiguration apConfig;
        PermissionManager.getInstance().checkPermission();
        if (disable) {
            if (this.mWifiManager.isWifiApEnabled() && (apConfig = this.mWifiManager.getWifiApConfiguration()) != null && apConfig.getAuthType() == 0) {
                try {
                    ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                    if (connectivityManager != null) {
                        Log.d("DeviceConnectivityManagerImpl", "close softap...");
                        connectivityManager.stopTethering(0);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            setProp("persist.sys.softap_disable_open", "1");
            return true;
        }
        setProp("persist.sys.softap_disable_open", "0");
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isUnSecureSoftApDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String disable = getProp("persist.sys.softap_disable_open", "0");
        if (!disable.equals("0") && disable.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setWifiAutoConnectionDisabled(boolean disable) {
        PermissionManager.getInstance().checkPermission();
        if (disable) {
            setProp("persist.sys.autocon_disable_open", "1");
            for (WifiConfiguration config : this.mWifiManager.getConfiguredNetworks()) {
                this.mWifiManager.save(config, null);
            }
            return true;
        }
        setProp("persist.sys.autocon_disable_open", "0");
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isWifiAutoConnectionDisabled() {
        PermissionManager.getInstance().checkPermission();
        return getProp("persist.sys.autocon_disable_open", "0").equals("1");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public String getDevicePosition(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String provider = getLocationProvider(this.mLocationManager);
        this.mLocationManager.requestLocationUpdates(provider, 1000, 0.0f, this.mGlobalLocationListeners, Looper.getMainLooper());
        Location loc = this.mLocationManager.getLastKnownLocation(provider);
        Log.d("DeviceConnectivityManagerImpl", "mdm service IDeviceConnectivityManager getDevicePosition, return null");
        if (loc == null) {
            Log.i("DeviceConnectivityManagerImpl", "getDevicePosition null");
            return null;
        }
        try {
            JSONObject object = new JSONObject();
            object.put("longitude", loc.getLongitude());
            object.put("latitude", loc.getLatitude());
            object.put("height", loc.getAltitude());
            Log.i("DeviceConnectivityManagerImpl", "logcation:" + object.toString());
            Log.d("DeviceConnectivityManagerImpl", object.toString());
            return object.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getLocationProvider(LocationManager locationManager) {
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains("network")) {
            Log.i("DeviceConnectivityManagerImpl", "network location");
            return "network";
        } else if (providers.contains("gps")) {
            Log.i("DeviceConnectivityManagerImpl", "GPS location");
            return "gps";
        } else {
            Log.i("DeviceConnectivityManagerImpl", "getLocationProvider null");
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopGlobalLocation() {
        if (this.mLocationManager != null) {
            Log.d("DeviceConnectivityManagerImpl", "stopGlobalLocation");
            try {
                this.mLocationManager.removeUpdates(this.mGlobalLocationListeners);
            } catch (SecurityException ex) {
                Log.e("DeviceConnectivityManagerImpl", "stopGlobalLocation fail to remove location listeners, ignore" + ex.toString());
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isUserProfilesDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        String disable = getProp("persist.sys.wifi_user_prof_disable", "0");
        if (!disable.equals("0") && disable.equals("1")) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setUserProfilesDisabled(ComponentName admin, boolean disable) {
        PermissionManager.getInstance().checkPermission();
        if (!disable) {
            setProp("persist.sys.wifi_user_prof_disable", "0");
        } else if (disable) {
            setProp("persist.sys.wifi_user_prof_disable", "1");
        }
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setWifiProfile(ComponentName admin, WifiConfiguration config) {
        PermissionManager.getInstance().checkPermission();
        if (this.mWifiManager != null) {
            if (this.mWifiManager.getWifiState() != 3) {
                Log.w("DeviceConnectivityManagerImpl", "setWifiProfile(): wifi state is not enabled");
                return false;
            } else if (admin == null || config == null) {
                throw new NullPointerException("check ComponentName or WifiConfiguration is null");
            } else if (this.mWifiManager.addNetwork(config) >= 0) {
                boolean result = WifiProfileXmlUtils.addProfileToXml(this.mContext.getFilesDir().getAbsolutePath(), "wifi_profile_list.xml", admin.getPackageName(), config.configKey());
                Log.d("DeviceConnectivityManagerImpl", "setWifiProfile result:" + result);
                return result;
            }
        }
        Log.w("DeviceConnectivityManagerImpl", "setWifiProfile is failed");
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean removeWifiProfile(ComponentName admin, WifiConfiguration config) {
        PermissionManager.getInstance().checkPermission();
        if (config == null) {
            throw new NullPointerException("WifiConfiguration is null");
        } else if (this.mWifiManager == null) {
            Log.w("DeviceConnectivityManagerImpl", "removeWifiProfile(): WifiManager is null");
            return false;
        } else if (this.mWifiManager.getWifiState() != 3) {
            Log.w("DeviceConnectivityManagerImpl", "removeWifiProfile(): wifi state is not enabled");
            return false;
        } else {
            String packageName = admin == null ? null : admin.getPackageName();
            if (config.networkId != -1) {
                boolean ret = this.mWifiManager.removeNetwork(config.networkId);
                if (ret) {
                    return WifiProfileXmlUtils.removeProfileToXml(this.mContext.getFilesDir().getAbsolutePath(), "wifi_profile_list.xml", packageName, config.configKey());
                }
                return ret;
            }
            String configKey = config.configKey();
            for (WifiConfiguration configuration : this.mWifiManager.getConfiguredNetworks()) {
                if (TextUtils.equals(configuration.configKey(), configKey)) {
                    boolean ret2 = this.mWifiManager.removeNetwork(configuration.networkId);
                    if (ret2) {
                        return WifiProfileXmlUtils.removeProfileToXml(this.mContext.getFilesDir().getAbsolutePath(), "wifi_profile_list.xml", packageName, config.configKey());
                    }
                    return ret2;
                }
            }
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public List<String> getWifiProfileList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        ArrayList<String> ssidList = new ArrayList<>();
        if (this.mWifiManager == null) {
            Log.w("DeviceConnectivityManagerImpl", "getWifiProfileList(): WifiManager is null");
            return ssidList;
        } else if (this.mWifiManager.getWifiState() != 3) {
            Log.w("DeviceConnectivityManagerImpl", "getWifiProfileList(): wifi state is not enabled");
            return ssidList;
        } else {
            HashMap<String, String> wifiProfiles = WifiProfileXmlUtils.readXMLFile(this.mContext.getFilesDir().getAbsolutePath(), "wifi_profile_list.xml");
            List<String> packageNameList = new ArrayList<>();
            if (admin == null) {
                for (String key : wifiProfiles.keySet()) {
                    packageNameList.add(key);
                }
            } else {
                String packageName = admin.getPackageName();
                Log.d("DeviceConnectivityManagerImpl", "getWifiProfileList--packageName:" + packageName);
                for (String key2 : wifiProfiles.keySet()) {
                    if (wifiProfiles.get(key2).contains(packageName)) {
                        packageNameList.add(key2);
                    }
                }
            }
            for (WifiConfiguration config : this.mWifiManager.getConfiguredNetworks()) {
                if (packageNameList.contains(config.configKey())) {
                    String ssid = config.SSID;
                    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length() - 1);
                    }
                    ssidList.add(ssid);
                }
            }
            return ssidList;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public void turnOnGPS(ComponentName admin, boolean on) {
        PermissionManager.getInstance().checkPermission();
        UserHandle user = Process.myUserHandle();
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        if (this.mLocationManager == null) {
            Log.d("DeviceConnectivityManagerImpl", "mLocationManager null");
        } else if (on) {
            Log.d("DeviceConnectivityManagerImpl", "open gps");
            this.mLocationManager.setLocationEnabledForUser(true, user);
        } else {
            Log.d("DeviceConnectivityManagerImpl", "close gps");
            this.mLocationManager.setLocationEnabledForUser(false, user);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isGPSTurnOn(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        UserHandle user = Process.myUserHandle();
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        if (this.mLocationManager == null) {
            Log.d("DeviceConnectivityManagerImpl", "mLocationManager null");
            return false;
        }
        boolean gps = this.mLocationManager.isLocationEnabledForUser(user);
        Log.d("DeviceConnectivityManagerImpl", "isGPSTurnOn, gps: " + gps);
        if (gps) {
            return true;
        }
        return false;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public void setGPSDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        DeviceConnectivityManager.getInstance(this.mContext);
        if (disabled) {
            Log.d("DeviceConnectivityManagerImpl", "lock gps and lock UI...");
            try {
                Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps.always_enable", 0);
                Thread.sleep(100);
                turnOnGPS(admin, false);
                Thread.sleep(300);
                SystemProperties.set("persist.sys.gps_disable", "1");
                SystemProperties.set("persist.sys.gps.always_enable", "0");
                Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps_disable", 1);
                setProp("persist.sys.gps_clickable", "0");
                setProp("persist.sys.gps_grey", "1");
                setProp("persist.sys.mdm_gps_mode", "2");
            } catch (InterruptedException e) {
                Log.d("DeviceConnectivityManagerImpl", "setGPSDisabled error");
            }
        } else {
            Log.d("DeviceConnectivityManagerImpl", "unlock gps and unlock UI...");
            SystemProperties.set("persist.sys.gps_disable", "0");
            SystemProperties.set("persist.sys.gps.always_enable", "0");
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps_disable", 0);
            Settings.Global.putInt(this.mContext.getContentResolver(), "persist.sys.gps.always_enable", 0);
            setProp("persist.sys.gps_clickable", "1");
            setProp("persist.sys.gps_grey", "0");
            setProp("persist.sys.mdm_gps_mode", "0");
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isGPSDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        int value = SystemProperties.getInt("persist.sys.mdm_gps_mode", 2);
        Log.d("DeviceConnectivityManagerImpl", "isGPSDisabled, value: " + value);
        if (value < 2 || value > 4) {
            return false;
        }
        return true;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean setWifiEditDisabled(ComponentName admin, boolean disabled) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        try {
            SystemProperties.set("persist.sys.wifi_edit_disable", String.valueOf(disabled));
            return true;
        } catch (Exception e) {
            Log.d("DeviceConnectivityManagerImpl", e.toString());
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isWifiEditDisabled(ComponentName admin) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        try {
            return SystemProperties.getBoolean("persist.sys.wifi_edit_disable", false);
        } catch (Exception e) {
            Log.d("DeviceConnectivityManagerImpl", e.toString());
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceConnectivityManager
    public boolean isWifiProfileSet(ComponentName admin, WifiConfiguration config) throws RemoteException {
        PermissionManager.getInstance().checkPermission();
        if (config != null) {
            HashMap<String, String> profileHashMap = WifiProfileXmlUtils.readXMLFile(this.mContext.getFilesDir().getAbsolutePath(), "wifi_profile_list.xml");
            String configKey = config.configKey();
            boolean isContains = profileHashMap.containsKey(configKey);
            if (admin == null) {
                return isContains;
            }
            if (isContains) {
                return profileHashMap.get(configKey).contains(admin.getPackageName());
            }
            return false;
        }
        throw new NullPointerException("check ComponentName or WifiConfiguration is null");
    }
}
