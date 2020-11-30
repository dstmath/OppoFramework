package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiRomUpdateHelper;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class OppoWifiConnectRestriction {
    private static final String BSSID_BLACK_STR = "BSSID_BLACK";
    private static final String BSSID_WHITE_STR = "BSSID_WHITE";
    private static final String DEFAULT_WHITE_APP = " ";
    private static final int ETHER_ADDR_LEN = 6;
    private static final String FULL_MATCH = "FULL_MATCH";
    private static final int MAX_SSID_LEN = 32;
    private static final String PRE_MATCH = "PRE_MATCH";
    private static final String RESTRICTION_LIST_PATH = "/data/misc/wifi/sys_wifi_connect_restriction_list";
    private static final String SSID_BLACK_STR = "SSID_BLACK";
    private static final String SSID_WHITE_STR = "SSID_WHITE";
    private static final String TAG = "OppoWifiConnectRestriction";
    private static Context sContext;
    private static OppoWifiConnectRestriction sInstance;
    private static ScanRequestProxy sScanRequestProxy;
    private static WifiConfigManager sWifiConfigManager;
    private static WifiRomUpdateHelper sWifiRomUpdateHelper;
    private static ClientModeImpl sWifiStateMachine;
    private boolean DEBUG = true;
    private List<String> mAppWhiteList = null;
    private List<String> mBssidBlackList = new ArrayList();
    private boolean mBssidBlackListEnabled = false;
    private List<String> mBssidWhiteList = new ArrayList();
    private boolean mBssidWhiteListEnabled = false;
    private List<String> mSsidBlackList = new ArrayList();
    private boolean mSsidBlackListEnabled = false;
    private List<String> mSsidWhiteList = new ArrayList();
    private boolean mSsidWhiteListEnabled = false;

    private OppoWifiConnectRestriction(Context mCtxt, WifiConfigManager mWcm, ClientModeImpl mWsm, WifiRomUpdateHelper mWru, ScanRequestProxy mSrp) {
        sContext = mCtxt;
        sWifiConfigManager = mWcm;
        sWifiStateMachine = mWsm;
        sWifiRomUpdateHelper = mWru;
        sScanRequestProxy = mSrp;
        try {
            initRestrictionList();
            initWhiteAppList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init(Context mCtxt, WifiConfigManager mWcm, ClientModeImpl mWsm, WifiRomUpdateHelper mWru, ScanRequestProxy mSrp) {
        sContext = mCtxt;
        sWifiConfigManager = mWcm;
        sWifiStateMachine = mWsm;
        sWifiRomUpdateHelper = mWru;
        sScanRequestProxy = mSrp;
    }

    public static OppoWifiConnectRestriction getInstance() {
        if (sContext == null) {
            Log.d(TAG, "sContext is null");
            return null;
        } else if (sWifiConfigManager == null) {
            Log.d(TAG, "sWifiConfigManager is null");
            return null;
        } else if (sWifiStateMachine == null) {
            Log.d(TAG, "sWifiStateMachine is null");
            return null;
        } else if (sWifiRomUpdateHelper == null) {
            Log.d(TAG, "sWifiRomUpdateHelper is null");
            return null;
        } else {
            synchronized (OppoWifiConnectRestriction.class) {
                if (sInstance == null) {
                    sInstance = new OppoWifiConnectRestriction(sContext, sWifiConfigManager, sWifiStateMachine, sWifiRomUpdateHelper, sScanRequestProxy);
                }
            }
            return sInstance;
        }
    }

    private void initRestrictionList() {
        new ArrayList();
        List<String> read = readWifiRestrictionListFromStore();
        if (read == null || read.size() == 0) {
            Log.d(TAG, "The result of readWifiRestrictionListFromStore is empty");
            return;
        }
        for (String str : read) {
            if (this.mSsidWhiteListEnabled) {
                synchronized (this.mSsidWhiteList) {
                    this.mSsidWhiteList.add(str);
                }
            } else if (this.mSsidBlackListEnabled) {
                synchronized (this.mSsidBlackList) {
                    this.mSsidBlackList.add(str);
                }
            } else if (this.mBssidWhiteListEnabled) {
                synchronized (this.mBssidWhiteList) {
                    this.mBssidWhiteList.add(str);
                }
            } else if (this.mBssidBlackListEnabled) {
                synchronized (this.mBssidBlackList) {
                    this.mBssidBlackList.add(str);
                }
            } else {
                continue;
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void saveWifiRestrictionList(String symbol) {
        char c;
        FileWriter fw = null;
        BufferedWriter writer = null;
        List<String> strList = new ArrayList<>();
        switch (symbol.hashCode()) {
            case -1740689893:
                if (symbol.equals(SSID_BLACK_STR)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1721406907:
                if (symbol.equals(SSID_WHITE_STR)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -80395299:
                if (symbol.equals(BSSID_BLACK_STR)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -61112313:
                if (symbol.equals(BSSID_WHITE_STR)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            strList = getSsidWhiteList();
        } else if (c == 1) {
            strList = getBssidWhiteList();
        } else if (c == 2) {
            strList = getSsidBlackList();
        } else if (c == 3) {
            strList = getBssidBlackList();
        }
        Log.d(TAG, "saveWifiRestrictionList, type is: " + symbol + " ,length is: " + strList.size());
        try {
            FileWriter fw2 = new FileWriter(RESTRICTION_LIST_PATH);
            BufferedWriter writer2 = new BufferedWriter(fw2);
            Iterator<String> it = strList.iterator();
            while (it.hasNext()) {
                writer2.write(symbol + it.next() + "\n");
                writer2.flush();
            }
            writer2.close();
            try {
                writer2.close();
            } catch (IOException e) {
            }
            try {
                fw2.close();
            } catch (IOException e2) {
            }
        } catch (FileNotFoundException e3) {
            Log.d(TAG, "FileNotFoundException: " + e3);
            if (0 != 0) {
                try {
                    writer.close();
                } catch (IOException e4) {
                }
            }
            if (0 != 0) {
                fw.close();
            }
        } catch (IOException e5) {
            Log.d(TAG, "IOException: " + e5);
            e5.printStackTrace();
            if (0 != 0) {
                try {
                    writer.close();
                } catch (IOException e6) {
                }
            }
            if (0 != 0) {
                fw.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    writer.close();
                } catch (IOException e7) {
                }
            }
            if (0 != 0) {
                try {
                    fw.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
    }

    private List<String> readWifiRestrictionListFromStore() {
        BufferedReader reader = null;
        List<String> list = new ArrayList<>();
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(RESTRICTION_LIST_PATH));
            String startStr = "";
            Log.d(TAG, "readWifiRestrictionListFromStore start");
            while (true) {
                String line = reader2.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith(SSID_WHITE_STR)) {
                    this.mSsidWhiteListEnabled = true;
                    startStr = SSID_WHITE_STR;
                } else if (line.startsWith(BSSID_WHITE_STR)) {
                    this.mBssidWhiteListEnabled = true;
                    startStr = BSSID_WHITE_STR;
                } else if (line.startsWith(SSID_BLACK_STR)) {
                    this.mSsidBlackListEnabled = true;
                    startStr = SSID_BLACK_STR;
                } else if (line.startsWith(BSSID_BLACK_STR)) {
                    this.mBssidBlackListEnabled = true;
                    startStr = BSSID_BLACK_STR;
                }
                list.add(line.substring(startStr.length()));
            }
            Log.d(TAG, "readWifiRestrictionListFromStore finished");
            try {
                reader2.close();
            } catch (IOException e) {
            }
            return list;
        } catch (FileNotFoundException e2) {
            Log.d(TAG, "readWifiRestrictionListFromStore: FileNotFoundException: " + e2);
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e3) {
                }
            }
            return list;
        } catch (IOException e4) {
            Log.d(TAG, "readWifiRestrictionListFromStore: IOException: " + e4);
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e5) {
                }
            }
            return list;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e6) {
                }
            }
            return list;
        }
    }

    private void updateConfigMap() {
        List<WifiConfiguration> save = sWifiConfigManager.getSavedNetworksAll();
        if (save == null || save.size() == 0) {
            Log.d(TAG, "No saved WifiConfiguration, do nothing");
            return;
        }
        for (WifiConfiguration wc : save) {
            if (needUpdate(wc)) {
                Log.d(TAG, wc.SSID + " need to be deleted from ConfigurationMap");
                sWifiConfigManager.removeNetwork(wc.networkId, 1000);
                if (sWifiStateMachine.matchConnectedNetwork(wc.networkId)) {
                    Log.d(TAG, "Disconnect current connect, ssid: " + wc.SSID + " ,networkId: " + wc.networkId);
                    sWifiStateMachine.disconnectCommand();
                }
            }
        }
    }

    private boolean needUpdate(WifiConfiguration wc) {
        if (wc != null && !netIdAllowedToConnect(wc.networkId)) {
            return true;
        }
        return false;
    }

    public boolean netIdAllowedToConnect(int netId) {
        String srConfigKey;
        List<ScanResult> srList = sScanRequestProxy.syncGetScanResultsList();
        if (srList == null || srList.size() <= 0) {
            Log.d(TAG, "netIdAllowedToConnect:srList is null or empty!!");
            return true;
        }
        WifiConfigManager wifiConfigManager = sWifiConfigManager;
        if (wifiConfigManager == null) {
            Log.d(TAG, "netIdAllowedToConnect:wifiConfigManager is null!!");
            return true;
        }
        WifiConfiguration wConf = wifiConfigManager.getWifiConfigurationForAll(netId);
        if (wConf == null) {
            Log.d(TAG, "netIdAllowedToConnect:wConf is null!!");
            return true;
        }
        String configKey = wConf.configKey();
        if (configKey == null) {
            Log.d(TAG, "netIdAllowedToConnect:configKey is null!!");
            return true;
        } else if (!ssidAllowedToConnect(wConf.SSID)) {
            return false;
        } else {
            int i = WifiConfiguration.INVALID_RSSI;
            for (ScanResult sr : srList) {
                if (sr != null && (srConfigKey = WifiConfiguration.configKey(sr)) != null && srConfigKey.equals(configKey) && !bssidAllowedToConnect(sr.BSSID, netId)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean bssidAllowedToConnect(String bssid, int netId) {
        if ((!this.mBssidWhiteListEnabled || inBssidWhiteList(bssid, PRE_MATCH)) && (!this.mBssidBlackListEnabled || !inBssidBlackList(bssid, PRE_MATCH))) {
            return true;
        }
        Log.d(TAG, bssid + "is not in white list/in black list, not allowed to connect");
        sWifiConfigManager.removeNetwork(netId, 1000);
        return false;
    }

    public boolean ssidAllowedToConnect(String ssid) {
        if ((!this.mSsidWhiteListEnabled || inSsidWhiteList(ssid)) && (!this.mSsidBlackListEnabled || !inSsidBlackList(ssid))) {
            return true;
        }
        Log.d(TAG, ssid + "is not in white list/in black list, not allowed to connect");
        return false;
    }

    private String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = sWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    private void initWhiteAppList() {
        this.mAppWhiteList = new ArrayList();
        String value = getRomUpdateValue("CONNECT_RESTRICTION_APP", DEFAULT_WHITE_APP);
        if (!(value == null || TextUtils.isEmpty(value))) {
            synchronized (this.mAppWhiteList) {
                if (!this.mAppWhiteList.isEmpty()) {
                    this.mAppWhiteList.clear();
                }
                for (String name : value.split(",")) {
                    this.mAppWhiteList.add(name.trim());
                }
            }
        }
    }

    public boolean inWhiteAppList(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.d(TAG, "Failed to check whether in white app list for an empty pkgName");
            return false;
        }
        synchronized (this.mAppWhiteList) {
            for (String name : this.mAppWhiteList) {
                if (pkgName.contains(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b7, code lost:
        if (r13.equals(com.android.server.wifi.OppoWifiConnectRestriction.SSID_WHITE_STR) != false) goto L_0x00c3;
     */
    public boolean setWifiRestrictionList(List<String> list, String type) {
        if (this.DEBUG) {
            Log.d(TAG, "Receive command setWifiRestrictionList, the type is: " + type);
        }
        boolean z = false;
        if (list == null || list.size() == 0) {
            Log.d(TAG, "Failed to set wifi restriction list, the list ready to set is empty");
            return false;
        }
        List<String> listCache = new ArrayList<>();
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (type.equals(BSSID_WHITE_STR) || type.equals(BSSID_BLACK_STR)) {
                str = str.toLowerCase();
                if (!checkMacAddressLegality(str)) {
                    Log.d(TAG, "Illegal macAddress: " + str);
                }
            }
            if ((type.equals(SSID_WHITE_STR) || type.equals(SSID_BLACK_STR)) && str.length() > 32) {
                Log.d(TAG, "Illegal ssid, exceed max length");
            } else {
                listCache.add(str);
            }
        }
        if (listCache.size() == 0) {
            Log.d(TAG, "Input list is empty after check mac address legality");
            return false;
        }
        switch (type.hashCode()) {
            case -1740689893:
                if (type.equals(SSID_BLACK_STR)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -1721406907:
                break;
            case -80395299:
                if (type.equals(BSSID_BLACK_STR)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -61112313:
                if (type.equals(BSSID_WHITE_STR)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            setSsidWhiteList(listCache);
        } else if (z) {
            setSsidBlackList(listCache);
        } else if (z) {
            setBssidWhiteList(listCache);
        } else if (z) {
            setBssidBlackList(listCache);
        }
        updateConfigMap();
        saveWifiRestrictionList(type);
        return true;
    }

    private boolean checkMacAddressLegality(String addr) {
        if (!TextUtils.isEmpty(addr) && addr != null && Pattern.compile("^([0-9a-f]{2}:){2,5}[0-9a-f]{1,2}$").matcher(addr).find()) {
            return true;
        }
        return false;
    }

    public List<String> getWifiRestrictionList(String type) {
        if (this.DEBUG) {
            Log.d(TAG, "Receive command getWifiRestrictionList, the type is: " + type);
        }
        List<String> ret = new ArrayList<>();
        char c = 65535;
        switch (type.hashCode()) {
            case -1740689893:
                if (type.equals(SSID_BLACK_STR)) {
                    c = 1;
                    break;
                }
                break;
            case -1721406907:
                if (type.equals(SSID_WHITE_STR)) {
                    c = 0;
                    break;
                }
                break;
            case -80395299:
                if (type.equals(BSSID_BLACK_STR)) {
                    c = 3;
                    break;
                }
                break;
            case -61112313:
                if (type.equals(BSSID_WHITE_STR)) {
                    c = 2;
                    break;
                }
                break;
        }
        if (c == 0) {
            return getSsidWhiteList();
        }
        if (c == 1) {
            return getSsidBlackList();
        }
        if (c == 2) {
            return getBssidWhiteList();
        }
        if (c != 3) {
            return ret;
        }
        return getBssidBlackList();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0062, code lost:
        if (r11.equals(com.android.server.wifi.OppoWifiConnectRestriction.SSID_WHITE_STR) != false) goto L_0x0070;
     */
    public boolean removeFromRestrictionList(List<String> rmList, String type) {
        if (this.DEBUG) {
            Log.d(TAG, "Receive command removeFromRestrictionList, type is: " + type);
        }
        boolean z = false;
        if (type == BSSID_WHITE_STR || type == BSSID_BLACK_STR) {
            for (String str : rmList) {
                if (!checkMacAddressLegality(str.toLowerCase())) {
                    return false;
                }
            }
        }
        switch (type.hashCode()) {
            case -1740689893:
                if (type.equals(SSID_BLACK_STR)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -1721406907:
                break;
            case -80395299:
                if (type.equals(BSSID_BLACK_STR)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -61112313:
                if (type.equals(BSSID_WHITE_STR)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return removeFromSsidWhiteList(rmList);
        }
        if (z) {
            return removeFromSsidBlackList(rmList);
        }
        if (z) {
            return removeFromBssidWhiteList(rmList);
        }
        if (!z) {
            return false;
        }
        return removeFromBssidBlackList(rmList);
    }

    private void setSsidWhiteList(List<String> list) {
        this.mSsidWhiteListEnabled = true;
        synchronized (this.mSsidWhiteList) {
            for (String str : list) {
                if (!inSsidWhiteList("\"" + str + "\"")) {
                    if (this.DEBUG) {
                        Log.d(TAG, "set ssid white list, add ssid: " + str);
                    }
                    this.mSsidWhiteList.add(str);
                }
            }
            if (this.DEBUG) {
                Log.d(TAG, "mSsidWhiteList: " + this.mSsidWhiteList + ", mBssidWhiteList: " + this.mBssidWhiteList);
            }
        }
        resetSsidBlackList();
        resetBssidBlackList();
    }

    private void resetSsidWhiteList() {
        if (this.DEBUG) {
            Log.d(TAG, "reset ssid white list, clear the list and set mSsidWhiteListEnabled to false");
        }
        this.mSsidWhiteListEnabled = false;
        synchronized (this.mSsidWhiteList) {
            if (!this.mSsidWhiteList.isEmpty()) {
                this.mSsidWhiteList.clear();
            }
        }
    }

    private List<String> getSsidWhiteList() {
        List<String> list;
        synchronized (this.mSsidWhiteList) {
            list = this.mSsidWhiteList;
        }
        return list;
    }

    private boolean removeFromSsidWhiteList(List<String> rmList) {
        if (!this.mSsidWhiteListEnabled) {
            Log.d(TAG, "SsidWhiteList not enabled");
            return false;
        }
        for (String rmSsid : rmList) {
            if (inSsidWhiteList("\"" + rmSsid + "\"")) {
                synchronized (this.mSsidWhiteList) {
                    this.mSsidWhiteList.remove(rmSsid);
                }
            }
        }
        updateConfigMap();
        saveWifiRestrictionList(SSID_WHITE_STR);
        return true;
    }

    private boolean inSsidWhiteList(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "Failed to check whether in ssid white list for an empty ssid");
            return false;
        }
        synchronized (this.mSsidWhiteList) {
            if (this.mSsidWhiteList.isEmpty()) {
                Log.d(TAG, "No need to check because the ssid white list is empty");
                return false;
            }
            Iterator<String> it = this.mSsidWhiteList.iterator();
            while (it.hasNext()) {
                if (ssid.equals("\"" + it.next() + "\"")) {
                    Log.d(TAG, "Match success, ssid: " + ssid + " is in ssid white list");
                    return true;
                }
            }
            Log.d(TAG, "Match failed, ssid: " + ssid + " is not in ssid white list");
            return false;
        }
    }

    private void setBssidWhiteList(List<String> list) {
        this.mBssidWhiteListEnabled = true;
        synchronized (this.mBssidWhiteList) {
            for (String str : list) {
                if (!inBssidWhiteList(str, FULL_MATCH)) {
                    if (this.DEBUG) {
                        Log.d(TAG, "set bssid white list, add bssid: " + str);
                    }
                    this.mBssidWhiteList.add(str);
                }
            }
            if (this.DEBUG) {
                Log.d(TAG, "mBssidWhiteList: " + this.mBssidWhiteList + ", mSsidWhiteList: " + this.mSsidWhiteList);
            }
        }
        resetSsidBlackList();
        resetBssidBlackList();
    }

    private void resetBssidWhiteList() {
        if (this.DEBUG) {
            Log.d(TAG, "reset bssid white list, clear the list and set mBssidWhiteListEnabled to false");
        }
        this.mBssidWhiteListEnabled = false;
        synchronized (this.mBssidWhiteList) {
            if (!this.mBssidWhiteList.isEmpty()) {
                this.mBssidWhiteList.clear();
            }
        }
    }

    private List<String> getBssidWhiteList() {
        List<String> list;
        synchronized (this.mBssidWhiteList) {
            list = this.mBssidWhiteList;
        }
        return list;
    }

    private boolean removeFromBssidWhiteList(List<String> rmList) {
        if (!this.mBssidWhiteListEnabled) {
            Log.d(TAG, "BssidWhiteList not enabled");
            return false;
        }
        if (rmList == null) {
            synchronized (this.mBssidWhiteList) {
                this.mBssidWhiteList.clear();
                Log.d(TAG, "BssidWhiteList remove all");
            }
        } else {
            for (String rmBssid : rmList) {
                String rmBssid2 = rmBssid.toLowerCase();
                if (inBssidWhiteList(rmBssid2, FULL_MATCH)) {
                    synchronized (this.mBssidWhiteList) {
                        this.mBssidWhiteList.remove(rmBssid2);
                    }
                }
            }
        }
        updateConfigMap();
        saveWifiRestrictionList(BSSID_WHITE_STR);
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0085  */
    private boolean inBssidWhiteList(String bssid, String type) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "Failed to check whether in bssid white list for an empty bssid");
            return false;
        }
        synchronized (this.mBssidWhiteList) {
            if (this.mBssidWhiteList.isEmpty()) {
                Log.d(TAG, "No need to check because the bssid white list is empty");
                return false;
            }
            for (String whiteBssid : this.mBssidWhiteList) {
                char c = 65535;
                int hashCode = type.hashCode();
                if (hashCode != -2079765271) {
                    if (hashCode == 947451893 && type.equals(FULL_MATCH)) {
                        c = 0;
                        if (c != 0) {
                            if (c == 1) {
                                if (bssid.toLowerCase().startsWith(whiteBssid)) {
                                    Log.d(TAG, "Match success, bssid: " + bssid + " is in bssid white list");
                                    return true;
                                }
                            }
                        } else if (bssid.equals(whiteBssid)) {
                            Log.d(TAG, "Match success, bssid: " + bssid + " is in bssid white list");
                            return true;
                        }
                    }
                } else if (type.equals(PRE_MATCH)) {
                    c = 1;
                    if (c != 0) {
                    }
                }
                if (c != 0) {
                }
            }
            Log.d(TAG, "Match failed, bssid: " + bssid + " is not in bssid white list");
            return false;
        }
    }

    private void setSsidBlackList(List<String> list) {
        this.mSsidBlackListEnabled = true;
        synchronized (this.mSsidBlackList) {
            for (String str : list) {
                if (!inSsidBlackList("\"" + str + "\"")) {
                    if (this.DEBUG) {
                        Log.d(TAG, "set ssid black list, add ssid: " + str);
                    }
                    this.mSsidBlackList.add(str);
                }
            }
            if (this.DEBUG) {
                Log.d(TAG, "mSsidBlackList: " + this.mSsidBlackList + ", mBssidBlackList: " + this.mBssidBlackList);
            }
        }
        resetSsidWhiteList();
        resetBssidWhiteList();
    }

    private void resetSsidBlackList() {
        if (this.DEBUG) {
            Log.d(TAG, "reset ssid black list, clear the list and set mSsidBlackListEnabled to false");
        }
        this.mSsidBlackListEnabled = false;
        synchronized (this.mSsidBlackList) {
            if (!this.mSsidBlackList.isEmpty()) {
                this.mSsidBlackList.clear();
            }
        }
    }

    private List<String> getSsidBlackList() {
        List<String> list;
        synchronized (this.mSsidBlackList) {
            list = this.mSsidBlackList;
        }
        return list;
    }

    private boolean removeFromSsidBlackList(List<String> rmList) {
        if (!this.mSsidBlackListEnabled) {
            Log.d(TAG, "SsidBlackList not enabled");
            return false;
        }
        for (String rmSsid : rmList) {
            if (inSsidBlackList("\"" + rmSsid + "\"")) {
                synchronized (this.mSsidBlackList) {
                    this.mSsidBlackList.remove(rmSsid);
                }
            }
        }
        updateConfigMap();
        saveWifiRestrictionList(SSID_BLACK_STR);
        return true;
    }

    private boolean inSsidBlackList(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "Failed to check whether in ssid black list for an empty ssid");
            return false;
        }
        synchronized (this.mSsidBlackList) {
            if (this.mSsidBlackList.isEmpty()) {
                Log.d(TAG, "No need to check because the ssid black list is empty");
                return false;
            }
            Iterator<String> it = this.mSsidBlackList.iterator();
            while (it.hasNext()) {
                if (ssid.equals("\"" + it.next() + "\"")) {
                    Log.d(TAG, "Match success, ssid: " + ssid + " is in ssid black list");
                    return true;
                }
            }
            Log.d(TAG, "Match failed, ssid: " + ssid + " is not in ssid black list");
            return false;
        }
    }

    private void setBssidBlackList(List<String> list) {
        this.mBssidBlackListEnabled = true;
        synchronized (this.mBssidBlackList) {
            for (String str : list) {
                if (!inBssidBlackList(str, FULL_MATCH)) {
                    if (this.DEBUG) {
                        Log.d(TAG, "set bssid black list, add bssid: " + str);
                    }
                    this.mBssidBlackList.add(str);
                }
            }
            if (this.DEBUG) {
                Log.d(TAG, "mBssidBlackList: " + this.mBssidBlackList + ", mSsidBlackList: " + this.mSsidBlackList);
            }
        }
        resetSsidWhiteList();
        resetBssidWhiteList();
    }

    private void resetBssidBlackList() {
        if (this.DEBUG) {
            Log.d(TAG, "reset bssid black list, clear the list and set mBssidBlackListEnabled to false");
        }
        this.mBssidBlackListEnabled = false;
        synchronized (this.mBssidBlackList) {
            if (!this.mBssidBlackList.isEmpty()) {
                this.mBssidBlackList.clear();
            }
        }
    }

    private List<String> getBssidBlackList() {
        List<String> list;
        synchronized (this.mBssidBlackList) {
            list = this.mBssidBlackList;
        }
        return list;
    }

    private boolean removeFromBssidBlackList(List<String> rmList) {
        if (!this.mBssidBlackListEnabled) {
            Log.d(TAG, "BssidBlackList not enabled");
            return false;
        }
        if (rmList == null) {
            synchronized (this.mBssidBlackList) {
                this.mBssidBlackList.clear();
                Log.d(TAG, "BssidBlackList remove all");
            }
        } else {
            for (String rmBssid : rmList) {
                String rmBssid2 = rmBssid.toLowerCase();
                if (inBssidBlackList(rmBssid2, FULL_MATCH)) {
                    synchronized (this.mBssidBlackList) {
                        this.mBssidBlackList.remove(rmBssid2);
                    }
                }
            }
        }
        updateConfigMap();
        saveWifiRestrictionList(BSSID_BLACK_STR);
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0085  */
    private boolean inBssidBlackList(String bssid, String type) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "Failed to check whether in bssid black list for an empty bssid");
            return false;
        }
        synchronized (this.mBssidBlackList) {
            if (this.mBssidBlackList.isEmpty()) {
                Log.d(TAG, "No need to check because the bssid black list is empty");
                return false;
            }
            for (String blackBssid : this.mBssidBlackList) {
                char c = 65535;
                int hashCode = type.hashCode();
                if (hashCode != -2079765271) {
                    if (hashCode == 947451893 && type.equals(FULL_MATCH)) {
                        c = 0;
                        if (c != 0) {
                            if (c == 1) {
                                if (bssid.toLowerCase().startsWith(blackBssid)) {
                                    Log.d(TAG, "Match success, bssid: " + bssid + " is in bssid black list");
                                    return true;
                                }
                            }
                        } else if (bssid.equals(blackBssid)) {
                            Log.d(TAG, "Match success, bssid: " + bssid + " is in bssid black list");
                            return true;
                        }
                    }
                } else if (type.equals(PRE_MATCH)) {
                    c = 1;
                    if (c != 0) {
                    }
                }
                if (c != 0) {
                }
            }
            Log.d(TAG, "Match failed, bssid: " + bssid + " is not in bssid black list");
            return false;
        }
    }

    public void enableVerboseLogging(int level) {
        this.DEBUG = level > 0;
    }
}
