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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoClientModeImplUtil2 {
    private static final String TAG = "OppoClientModeImplUtil2";
    private static final int WPA2_FALLBACK_THRESHOLD = 1;
    private OppoAssertTip mAssertProxy = null;
    private OppoClientModeImpl2 mClientModeImpl;
    private Context mContext;
    private int mSaeNetworkConsecutiveAssocRejectCounter = 0;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiNative mWifiNative;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private final String mapKey = "mapKey-";

    public OppoClientModeImplUtil2(Context context, WifiNative wifiNative, OppoClientModeImpl2 clientModeImpl) {
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

    public OppoClientModeImplUtil2(Context context, WifiNative wifiNative) {
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

    public void convertToQuotedSSID(WifiConfiguration config) {
        if (config != null && !TextUtils.isEmpty(config.SSID)) {
            if (config.SSID.charAt(0) != '\"' || (config.SSID.charAt(0) == '\"' && config.SSID.charAt(config.SSID.length() - 1) != '\"')) {
                config.SSID = "\"" + config.SSID + "\"";
            }
        }
    }

    public String convertToQuotedSSID(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return "";
        }
        if (ssid.charAt(0) == '\"' && (ssid.charAt(0) != '\"' || ssid.charAt(ssid.length() - 1) == '\"')) {
            return ssid;
        }
        return "\"" + ssid + "\"";
    }

    public void needSaveAsHiddenAP(OppoWifiConnectionAlert wifiConnectionAlert, Message message, WifiConfiguration config) {
        int sendingUid = -1;
        String sendingPktName = null;
        PackageManager pm = null;
        if (!(message == null || this.mContext == null)) {
            sendingUid = message.sendingUid;
            pm = this.mContext.getPackageManager();
        }
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
        if (config != null && (configSSID = config.SSID) != null && !configSSID.equals("") && (configBssid = config.BSSID) != null && !configBssid.equals("00:00:00:00:00:00") && (srList = scanRequestProxy.getScanResults()) != null) {
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
                    if (sr2 != null && bssid.equals(sr2.BSSID)) {
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
}
