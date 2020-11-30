package com.android.server.wifi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.MacAddress;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiRomUpdateHelper;
import android.net.wifi.WifiSsid;
import android.os.Message;
import android.os.OppoAssertTip;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoClientModeImplUtil {
    private static final String TAG = "OppoWifiClientModeImpl";
    private static final String WIFI_SMARTGEAR_DISABLE_SERVICE = "disableSmartGear";
    private static final String WIFI_SMARTGEAR_ENABLE_SERVICE = "enableSmartGear";
    private static final int WPA2_FALLBACK_THRESHOLD = 1;
    private final String DEFAULT_FORBIDDEN_WIFI_DISNETWORK_APP = "me.ele.crowdsource";
    private final String DEFAULT_FORBIDDEN_WIFI_ENNETWORK_APP = "com.huawei.health";
    private OppoAssertTip mAssertProxy = null;
    private ClientModeImpl mClientModeImpl;
    private Context mContext;
    private final List<String> mForbiddenDisNetworkApplist = new ArrayList();
    private final List<String> mForbiddenEnNetworkApplist = new ArrayList();
    private int mSaeNetworkConsecutiveAssocRejectCounter = 0;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiNative mWifiNative;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private final String mapKey = "mapKey-";

    public OppoClientModeImplUtil(Context context, WifiNative wifiNative, ClientModeImpl clientModeImpl) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mClientModeImpl = clientModeImpl;
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        this.mAssertProxy = OppoAssertTip.getInstance();
    }

    public OppoClientModeImplUtil(Context context, WifiNative wifiNative) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        this.mAssertProxy = OppoAssertTip.getInstance();
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Double getRomUpdateFloatValue(String key, Double defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getFloatValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Long getRomUpdateLongValue(String key, Long defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getLongValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return Boolean.valueOf(wifiRomUpdateHelper.getBooleanValue(key, defaultVal.booleanValue()));
        }
        return defaultVal;
    }

    public void initRomupdateHelperBroadcastReceiver() {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.initUpdateBroadcastReceiver();
        }
    }

    public void handlePermanentWrongPasswordFailure(int disableReason, OppoWifiConnectionAlert WifiConnectionAlert) {
        WifiConnectionAlert.sendWrongKeyEvent();
    }

    public void setTargetNetworkId(int id, int targetNetworkId) {
        if (id == -1) {
        }
    }

    public boolean isManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnect();
        }
        return false;
    }

    public int getManuConnectNetId() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().getManuConnectNetId();
        }
        return -1;
    }

    public boolean isSupplicantStateDisconnected(SupplicantStateTracker supplicantStateTracker) {
        if (supplicantStateTracker == null) {
            return false;
        }
        String supplicantState = supplicantStateTracker.getSupplicantStateName();
        if ("DisconnectedState".equals(supplicantState) || "ScanState".equals(supplicantState) || "InactiveState".equals(supplicantState)) {
            return true;
        }
        return false;
    }

    public boolean isSupplicantAvailable(int operationalMode) {
        int wifiState = this.mClientModeImpl.syncGetWifiState();
        if (wifiState == 0 || 1 == wifiState) {
            Log.d(TAG, "wifi in disabled or disabling state!");
            return false;
        } else if (3 != operationalMode) {
            return true;
        } else {
            Log.d(TAG, "wifi in disable pending state!");
            return false;
        }
    }

    public boolean isNetworkConnecting(int netId, NetworkInfo networkInfo, OppoWifiConnectionAlert WifiConnectionAlert) {
        return isNetworkManuConnecting(netId) || isNetworkAutoConnecting(netId, networkInfo, WifiConnectionAlert);
    }

    public boolean isNetworkManuConnecting(int netId) {
        if (OppoAutoConnectManager.getInstance() == null || !OppoAutoConnectManager.getInstance().isManuConnect()) {
            return false;
        }
        return true;
    }

    public boolean isNetworkAutoConnecting(int netId, NetworkInfo networkInfo, OppoWifiConnectionAlert WifiConnectionAlert) {
        if ((networkInfo == null || !networkInfo.isConnectedOrConnecting()) && !WifiConnectionAlert.isSelectingNetwork()) {
            return false;
        }
        return true;
    }

    public boolean isNetworkAutoConnectingOrConnected(int netId, ExtendedWifiInfo wifiInfo, NetworkInfo networkInfo, OppoWifiConnectionAlert WifiConnectionAlert) {
        return isNetworkAutoConnecting(netId, networkInfo, WifiConnectionAlert) || isNetworkConnected(netId, wifiInfo);
    }

    public boolean isNetworkConnected(int netId, ExtendedWifiInfo WifiInfo) {
        if (WifiInfo.getNetworkId() == netId) {
            return true;
        }
        return false;
    }

    public void needSaveAsHiddenAP(OppoWifiConnectionAlert wifiConnectionAlert, Message message, WifiConfiguration config) {
        String sendingPktName = null;
        if (message == null || this.mContext == null) {
            Log.d(TAG, "message = null OR mContext = null,just return, needSaveAsHiddenAP fail");
            return;
        }
        int sendingUid = message.sendingUid;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            sendingPktName = pm.getNameForUid(sendingUid);
        }
        if (!"com.coloros.backuprestore".equals(sendingPktName) && wifiConnectionAlert.needSaveAsHiddenAP((WifiConfiguration) message.obj)) {
            config.hiddenSSID = true;
        }
    }

    public void setSsidInConnecting(ExtendedWifiInfo wifiInfo, SupplicantState state, WifiConfigManager wifiConfigManager) {
        String ssidStr = null;
        if (wifiInfo != null) {
            ssidStr = wifiInfo.getSSID();
        }
        if (!SupplicantState.isConnecting(state)) {
            return;
        }
        if (ssidStr == null || ssidStr.equals("<unknown ssid>")) {
            WifiConfiguration wc = null;
            if (!(wifiConfigManager == null || wifiInfo == null)) {
                wc = wifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
            }
            if (wc != null && wc.SSID != null) {
                WifiSsid ssid = null;
                String configSsidStr = wc.SSID;
                if (configSsidStr != null && configSsidStr.startsWith("\"") && configSsidStr.endsWith("\"")) {
                    configSsidStr = configSsidStr.substring(1, configSsidStr.length() - 1);
                }
                if (configSsidStr != null) {
                    ssid = WifiSsid.createFromByteArray(NativeUtil.byteArrayFromArrayList(NativeUtil.stringToByteArrayList(configSsidStr)));
                }
                if (ssid != null) {
                    Log.d(TAG, "reset wifissid to" + ssid.toString());
                    wifiInfo.setSSID(ssid);
                }
            }
        }
    }

    public void setKeylogVerbose(boolean verboseLoggingEnabled, boolean keylogVerbose) {
        if (SystemProperties.get("ro.secure", "0").equals("1") && SystemProperties.get("debug.wifi.prdebug", "0").equals("0")) {
            SystemProperties.set("debug.wifi.prdebug", "1");
            if (!verboseLoggingEnabled) {
                this.mClientModeImpl.enableVerboseLogging(1);
            }
        }
    }

    public void resetVerbose(boolean keylogVerbose) {
        if (SystemProperties.get("debug.wifi.prdebug", "0").equals("1")) {
            SystemProperties.set("debug.wifi.prdebug", "0");
            if (keylogVerbose) {
                this.mClientModeImpl.enableVerboseLogging(0);
            }
        }
    }

    public void checkAndSetSsidForConfig(WifiConfiguration config, ScanRequestProxy scanRequestProxy) {
        String configSSID;
        String configBssid;
        List<ScanResult> srList;
        if (!(config == null || (configSSID = config.SSID) == null || configSSID.equals("") || (configBssid = config.BSSID) == null || configBssid.equals("00:00:00:00:00:00") || (srList = scanRequestProxy.getScanResults()) == null)) {
            Long newestTimeStamp = 0L;
            String targetSsid = null;
            int count = 0;
            boolean isExists = false;
            for (ScanResult sr : srList) {
                if (sr != null) {
                    String ssid = "\"" + sr.SSID + "\"";
                    if (configBssid.equals(sr.BSSID)) {
                        count++;
                        if (sr.timestamp > newestTimeStamp.longValue()) {
                            targetSsid = ssid;
                            newestTimeStamp = Long.valueOf(sr.timestamp);
                            if (ssid != null && ssid.equals(configSSID)) {
                                isExists = true;
                            }
                        }
                    }
                }
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "same bssid count = " + count);
            }
            if (count <= 1 && isExists) {
                return;
            }
            if (targetSsid == null || targetSsid.equals("\"\"")) {
                Log.d(TAG, "target = " + targetSsid);
                return;
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "set manu connect from " + configSSID + " ssid to " + targetSsid);
            }
            config.SSID = targetSsid;
        }
    }

    public WifiInfo syncRequestConnectionInfo(ExtendedWifiInfo wifiInfo, boolean enableRssiPolling, ScanRequestProxy scanRequestProxy) {
        WifiInfo result = new WifiInfo(wifiInfo);
        if ((!enableRssiPolling || result.getRssi() == -127) && result.getBSSID() != null) {
            Iterator<ScanResult> it = scanRequestProxy.getScanResults().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ScanResult scanResult = it.next();
                if (result.getBSSID().equals(scanResult.BSSID)) {
                    int level = scanResult.level;
                    Log.d(TAG, "Adjust rssi from " + result.getRssi() + " to " + level);
                    result.setRssi(level);
                    break;
                }
            }
        }
        return result;
    }

    public void disconnectWithWifiStateDisabled(SupplicantState state, String interfaceName) {
        if (1 == this.mClientModeImpl.syncGetWifiState() && SupplicantState.isConnecting(state)) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "wrong supplicant action, disconnect supplicant!!");
            }
            WifiNative wifiNative = this.mWifiNative;
            if (wifiNative != null) {
                wifiNative.disconnect(interfaceName);
            }
        }
    }

    public String getBestBssidForNetId(int netId, ScanRequestProxy scanRequestProxy, WifiConfigManager wifiConfigManager) {
        String srConfigKey;
        List<ScanResult> srList = scanRequestProxy.syncGetScanResultsList();
        if (srList == null || srList.size() <= 0) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "getBestBssidForNetId:srList is null or empty!!");
            }
            return null;
        } else if (wifiConfigManager == null) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "getBestBssidForNetId:wifiConfigManager is null!!");
            }
            return null;
        } else {
            WifiConfiguration wConf = wifiConfigManager.getWifiConfigurationForAll(netId);
            if (wConf == null) {
                if (this.mVerboseLoggingEnabled) {
                    Log.d(TAG, "getBestBssidForNetId:wConf is null!!");
                }
                return null;
            }
            String configKey = wConf.configKey();
            if (configKey == null) {
                if (this.mVerboseLoggingEnabled) {
                    Log.d(TAG, "getBestBssidForNetId:configKey is null!!");
                }
                return null;
            }
            int bestLevel = WifiConfiguration.INVALID_RSSI;
            String bssid = null;
            for (ScanResult sr : srList) {
                if (sr != null && (srConfigKey = WifiConfiguration.configKey(sr)) != null && srConfigKey.equals(configKey) && sr.level > bestLevel) {
                    bssid = sr.BSSID;
                    bestLevel = sr.level;
                }
            }
            if (bssid != null) {
                int sameBssidCount = 0;
                for (ScanResult sr2 : srList) {
                    if (sr2 != null && sr2.SSID != null && !sr2.SSID.equals("") && bssid.equals(sr2.BSSID)) {
                        sameBssidCount++;
                    }
                }
                if (sameBssidCount > 1) {
                    bssid = null;
                }
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "getBestBssidForNetId bssid = " + bssid);
            }
            return bssid;
        }
    }

    public void setStatistics(String mapValue, String eventId) {
        HashMap<String, String> map = new HashMap<>();
        map.put("mapKey-", mapValue);
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "fool-proof, onCommon eventId = " + eventId);
        }
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", eventId, map, false);
    }

    public void reportFoolProofException() {
        if (!this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support")) {
            RuntimeException excp = new RuntimeException("Please send this log to Yuanliu.Tang of wifi team,thank you!");
            excp.fillInStackTrace();
            this.mAssertProxy.requestShowAssertMessage(Log.getStackTraceString(excp));
        } else if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "fool-proof, CTA version don't reportFoolProofException");
        }
    }

    public boolean noNeedLoginByPkgname(int uid) {
        String sendingPktName = null;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            sendingPktName = pm.getNameForUid(uid);
        }
        if ("com.coloros.wifisecuredetect".equals(sendingPktName)) {
            return true;
        }
        return false;
    }

    public static boolean isNotChineseOperator() {
        String mcc = SystemProperties.get("android.telephony.mcc_change", "");
        String mcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (TextUtils.isEmpty(mcc) && TextUtils.isEmpty(mcc2)) {
            return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
        }
        if ("460".equals(mcc) || "460".equals(mcc2)) {
            return false;
        }
        return true;
    }

    public boolean inForbiddenEnNetworkApplist(String pkgName) {
        List<String> list;
        if (pkgName == null || (list = this.mForbiddenEnNetworkApplist) == null) {
            Log.d(TAG, "pkgName = null");
            return false;
        } else if (list.size() <= 0) {
            return false;
        } else {
            synchronized (this.mForbiddenEnNetworkApplist) {
                for (String name : this.mForbiddenEnNetworkApplist) {
                    if (pkgName.contains(name)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void initForbiddenEnNetworkApplist() {
        String value = getRomUpdateValue("FORBIDDEN_WIFI_ENNETWORK_APP_LIST", "com.huawei.health");
        if (value != null) {
            synchronized (this.mForbiddenEnNetworkApplist) {
                if (!this.mForbiddenEnNetworkApplist.isEmpty()) {
                    this.mForbiddenEnNetworkApplist.clear();
                }
                for (String name : value.split(",")) {
                    this.mForbiddenEnNetworkApplist.add(name.trim());
                }
            }
        }
    }

    public boolean inForbiddenDisNetworkApplist(String pkgName) {
        List<String> list;
        if (pkgName == null || (list = this.mForbiddenDisNetworkApplist) == null) {
            Log.d(TAG, "pkgName = null");
            return false;
        } else if (list.size() <= 0) {
            return false;
        } else {
            synchronized (this.mForbiddenDisNetworkApplist) {
                for (String name : this.mForbiddenDisNetworkApplist) {
                    if (pkgName.contains(name)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void initForbiddenDisNetworkApplist() {
        String value = getRomUpdateValue("FORBIDDEN_WIFI_DISNETWORK_APP_LIST", "me.ele.crowdsource");
        if (value != null) {
            synchronized (this.mForbiddenDisNetworkApplist) {
                if (!this.mForbiddenDisNetworkApplist.isEmpty()) {
                    this.mForbiddenDisNetworkApplist.clear();
                }
                for (String name : value.split(",")) {
                    this.mForbiddenDisNetworkApplist.add(name.trim());
                }
            }
        }
    }

    private void configureRandomizedMacAddressForWpa2Fallback(WifiConfiguration config, WifiConfigManager wifiConfigManager, String interfaceName) {
        try {
            MacAddress currentMac = MacAddress.fromString(this.mWifiNative.getMacAddress(interfaceName));
            MacAddress newMac = MacAddress.createRandomUnicastAddress();
            wifiConfigManager.setNetworkRandomizedMacAddress(config.networkId, newMac);
            if (!WifiConfiguration.isValidMacAddressForRandomization(newMac)) {
                Log.wtf(TAG, "Config generated an invalid MAC address");
            } else if (currentMac.equals(newMac)) {
                Log.d(TAG, "No changes in MAC address");
            } else {
                boolean setMacSuccess = this.mWifiNative.setMacAddress(interfaceName, newMac);
                Log.d(TAG, "Wpa2FallbackMacRandomization SSID(" + config.getPrintableSsid() + "). setMacAddress(" + newMac.toString() + ") from " + currentMac.toString() + " = " + setMacSuccess);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e(TAG, "Exception in configureRandomizedMacAddressForWpa2Fallback: " + e.toString());
        }
    }

    public WifiConfiguration attemptWpa2FallbackConnectionIfRequired(WifiConfiguration targetWifiConfiguration, WifiConfigManager wifiConfigManager, int targetNetworkId, String interfaceName, ExtendedWifiInfo wifiInfo, OppoScanResultsProxy scanResultsProxy) {
        if (targetWifiConfiguration != null) {
            if (targetWifiConfiguration.allowedKeyManagement.get(8) && scanResultsProxy != null) {
                this.mSaeNetworkConsecutiveAssocRejectCounter++;
                List<ScanResult> srList = scanResultsProxy.syncGetScanResultsList();
                if (srList != null && srList.size() > 0) {
                    boolean transitionModeApFound = false;
                    String wcConfigKey = targetWifiConfiguration.configKey();
                    Iterator<ScanResult> it = srList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ScanResult sr = it.next();
                        String srConfigKey = WifiConfiguration.configKey(sr);
                        if (srConfigKey != null && wcConfigKey != null && wcConfigKey.equals(srConfigKey) && ScanResultUtil.isScanResultForPskSaeTransitionNetwork(sr)) {
                            transitionModeApFound = true;
                            break;
                        }
                    }
                    if (transitionModeApFound && this.mSaeNetworkConsecutiveAssocRejectCounter >= 1) {
                        Log.i(TAG, "Attempt WPA2 fallback connection");
                        this.mSaeNetworkConsecutiveAssocRejectCounter = 0;
                        WifiConfiguration config = wifiConfigManager.getConfiguredNetworkWithoutMasking(targetNetworkId);
                        if (config == null) {
                            return null;
                        }
                        config.allowedKeyManagement.clear(8);
                        config.allowedKeyManagement.set(1);
                        config.requirePMF = false;
                        String currentMacAddress = this.mWifiNative.getMacAddress(interfaceName);
                        wifiInfo.setMacAddress(currentMacAddress);
                        wifiConfigManager.updateNetworkSelectionStatus(targetNetworkId, 0);
                        Log.i(TAG, "Connecting with " + currentMacAddress + " as the mac address");
                        if (this.mWifiNative.connectToNetwork(interfaceName, config)) {
                            return config;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void resetSaeNetworkConsecutiveAssocRejectCounter() {
        Log.d(TAG, "reset mSaeNetworkConsecutiveAssocRejectCounter");
        this.mSaeNetworkConsecutiveAssocRejectCounter = 0;
    }

    public void updateWifiConfigToSaeOrOweIfRequired(WifiConfiguration config, WifiConfigManager wifiConfigManager, OppoScanResultsProxy scanResultsProxy, String bssid) {
        List<ScanResult> srList;
        if (config != null && scanResultsProxy != null && bssid != null && wifiConfigManager != null) {
            try {
                int authType = config.getAuthType();
                if ((authType == 1 || authType == 4 || authType == 0 || authType == 9) && (srList = scanResultsProxy.syncGetScanResultsList()) != null && srList.size() > 0) {
                    for (ScanResult sr : srList) {
                        if (bssid.equals(sr.BSSID)) {
                            if (ScanResultUtil.isScanResultForPskSaeTransitionNetwork(sr)) {
                                Log.i(TAG, "find a PSK-SAE AP connected with PSK, update to SAE, config:" + config);
                                config.allowedKeyManagement.clear(1);
                                config.allowedKeyManagement.set(8);
                                config.requirePMF = true;
                                wifiConfigManager.addOrUpdateNetwork(config, 1000);
                                return;
                            } else if (ScanResultUtil.isScanResultForOweNetwork(sr)) {
                                Log.i(TAG, "find a OWE AP connected with NONE, update to OWE, config:" + config);
                                config.allowedKeyManagement.clear(0);
                                config.allowedKeyManagement.set(9);
                                config.requirePMF = true;
                                wifiConfigManager.addOrUpdateNetwork(config, 1000);
                                return;
                            } else if (ScanResultUtil.isScanResultForOpenNetwork(sr)) {
                                Log.i(TAG, "find a OPEN AP connected with OWE, update to NONE, config:" + config);
                                config.allowedKeyManagement.clear(9);
                                config.allowedKeyManagement.set(0);
                                config.requirePMF = false;
                                wifiConfigManager.addOrUpdateNetwork(config, 1000);
                                return;
                            }
                        }
                    }
                }
            } catch (IllegalStateException e) {
            }
        }
    }

    public boolean isSmartGearEnable() {
        return WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("OPPO_WIFI_SMARTGEAR_FEATURE", false);
    }

    public void setSmartGearFeature() {
        if (isSmartGearEnable()) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "SmartGear feature enabled");
            }
            SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, WIFI_SMARTGEAR_ENABLE_SERVICE);
            return;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "SmartGear feature disabled");
        }
        SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, WIFI_SMARTGEAR_DISABLE_SERVICE);
    }

    public boolean isScanresulContainConfigkey(OppoScanResultsProxy scanResultsProxy, WifiConfigManager wifiConfigManager, int networkId, String ssidToAdjust) {
        WifiConfiguration config = wifiConfigManager.getConfiguredNetwork(networkId);
        List<ScanResult> srList = scanResultsProxy.syncGetScanResultsList();
        if (srList == null || srList.size() <= 0 || config == null) {
            Log.i(TAG, "srList or config is null/empty!!");
            return false;
        }
        config.SSID = ssidToAdjust;
        String wcConfigKey = config.configKey();
        if (wcConfigKey == null) {
            Log.i(TAG, "wcConfigKey is null!!");
            return false;
        }
        for (ScanResult sr : srList) {
            String srConfigKey = WifiConfiguration.configKey(sr);
            if (srConfigKey != null && wcConfigKey.equals(srConfigKey)) {
                return true;
            }
        }
        return false;
    }
}
