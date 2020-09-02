package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiScanner;
import android.os.OppoManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.util.NativeUtil;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoWifiConfigManagerUtil {
    private static final int ASSOC_REJECT_ALGORITHM_NOT_SUPPORTED = 13;
    private static final int ASSOC_REJECT_AP_OVERLOAD = 17;
    private static final int ASSOC_REJECT_CHALLENGE_FAIL = 15;
    private static final int ASSOC_REJECT_OUT_OF_SEQ = 14;
    private static final int DATE_EQUAL = 0;
    private static final int DATE_LARGER = 1;
    private static final int DATE_SMALLER = -1;
    private static final int DATE_UNKNOWN = -100;
    private static final String DIR_PATH_DEFAULT = "/data/oppo/coloros/dcs/netlog/";
    private static final String DIR_PATH_EXEU = "/data/oppo/log/DCS/network_logs/NET_LOG/";
    private static final int HIDDEN_AP_MAX_SCAN_COUNTS = 16;
    private static final int MAX_FILES = 100;
    private static final int REJECT_CODE_MANY_CLIENTS = 17;
    private static final String RESTORE_FILE_PATH = "/data/misc/wifi/reports/";
    private static final String TAG = "OppoWifiConfigManagerUtil";
    private static final int TRIGGER_DUMP_COUNT = 1;
    private static String dirPath = DIR_PATH_DEFAULT;
    private HashMap<String, Integer> mConnectFailData = new HashMap<>();
    private final Context mContext;
    private String mOldDate = "";
    private int mReasonCode = 0;
    private int mScanResultRssiAssocReject = -80;
    private List<ScanResult> mScanResults = new ArrayList();
    private SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd");
    private boolean mVerboseLoggingEnabled = false;
    private final WifiConfigManager mWifiConfigManager;
    private OppoWifiAssistantStateTraker mWifiStateTracker;

    OppoWifiConfigManagerUtil(Context context, WifiConfigManager mWcm) {
        this.mContext = context;
        this.mWifiConfigManager = mWcm;
        this.mOldDate = this.mSdf.format(Long.valueOf(System.currentTimeMillis()));
    }

    public void enableVerboseLogging(boolean verbose) {
        this.mVerboseLoggingEnabled = verbose;
    }

    public void limitHiddenNetworkList(List<WifiScanner.ScanSettings.HiddenNetwork> hiddenList, List<WifiConfiguration> networks) {
        int maxSsidCount = 16;
        for (WifiConfiguration config : networks) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "hidden add ssid = " + config.SSID + " numassoc = " + config.numAssociation + " lastconncted = " + config.lastConnected);
            }
            hiddenList.add(new WifiScanner.ScanSettings.HiddenNetwork(config.SSID));
            maxSsidCount--;
            if (maxSsidCount <= 0) {
                if (this.mVerboseLoggingEnabled) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("discard scan ");
                    sb.append(networks.size() - 16);
                    sb.append(" ssids");
                    Log.d(TAG, sb.toString());
                    return;
                }
                return;
            }
        }
    }

    public void setReasonCode(int reason) {
        this.mReasonCode = reason;
    }

    public ScanResult findHomonyAPFromScanResults(WifiConfiguration config, ClientModeImpl clientModeImpl) {
        String cfgKey;
        if (config == null || (cfgKey = config.configKey()) == null) {
            return null;
        }
        this.mScanResults = clientModeImpl.getScanResults();
        if (this.mScanResults.size() <= 0) {
            return null;
        }
        ScanResult tmpResult = null;
        for (int i = 0; i < this.mScanResults.size(); i++) {
            ScanResult scanResult = this.mScanResults.get(i);
            if (cfgKey.equals(WifiConfiguration.configKey(scanResult))) {
                if (tmpResult == null) {
                    Log.d(TAG, "conn-track, found a Homony AP scanResult = " + scanResult);
                    tmpResult = scanResult;
                } else {
                    Log.d(TAG, "conn-track, ESS found more Homony AP scanResult = " + scanResult);
                    if (scanResult.level > tmpResult.level) {
                        tmpResult = scanResult;
                    }
                }
            }
        }
        Log.d(TAG, "conn-track, tmpResult = " + tmpResult);
        return tmpResult;
    }

    private boolean isPskEndWithSpace(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        String str = null;
        boolean isPsk = false;
        if (config.allowedKeyManagement.get(1) || config.allowedKeyManagement.get(4) || config.allowedKeyManagement.get(8)) {
            str = config.preSharedKey;
            isPsk = true;
        } else if (config.allowedKeyManagement.get(13)) {
            if (!TextUtils.isEmpty(config.wapiPsk)) {
                str = config.wapiPsk;
            } else {
                str = config.preSharedKey;
            }
            isPsk = true;
        } else if (config.wepKeys != null) {
            boolean ret = false;
            for (int i = 0; i < config.wepKeys.length; i++) {
                String str2 = config.wepKeys[i];
                if (!TextUtils.isEmpty(str2)) {
                    if (str2.startsWith("\"")) {
                        str2 = NativeUtil.removeEnclosingQuotes(str2);
                    }
                    ret = ret || str2.endsWith(" ");
                }
            }
            return ret;
        }
        if (!isPsk || TextUtils.isEmpty(str)) {
            return false;
        }
        if (str.startsWith("\"")) {
            str = NativeUtil.removeEnclosingQuotes(str);
        }
        return str.endsWith(" ");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00e6, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x020c, code lost:
        return;
     */
    public void checkRestoreConnectFailInfo(WifiConfiguration network, int reason, ClientModeImpl clientModeImpl) {
        if (!SystemProperties.get("ro.secure").equals("0")) {
            ScanResult sr = findHomonyAPFromScanResults(network, clientModeImpl);
            String date = this.mSdf.format(Long.valueOf(System.currentTimeMillis()));
            if (!TextUtils.isEmpty(date)) {
                String reasonDate = date + "-" + reason;
                synchronized (this.mConnectFailData) {
                    int recordCount = 1;
                    int compareResult = compareDate(date, this.mOldDate);
                    if (this.mVerboseLoggingEnabled) {
                        Log.d(TAG, "date = " + compareResult);
                    }
                    if (compareResult != DATE_UNKNOWN) {
                        if (compareResult == 1 || compareResult == -1) {
                            this.mOldDate = date;
                            this.mConnectFailData.clear();
                        }
                        if (this.mConnectFailData.containsKey(reasonDate)) {
                            Integer count = this.mConnectFailData.get(reasonDate);
                            if (count == null) {
                                Log.d(TAG, "get data exp");
                                return;
                            }
                            recordCount = count.intValue() + 1;
                        }
                        if (this.mVerboseLoggingEnabled) {
                            Log.d(TAG, "recordCount = " + recordCount);
                        }
                        this.mConnectFailData.put(reasonDate, Integer.valueOf(recordCount));
                        if (recordCount <= clientModeImpl.getRomUpdateIntegerValue("CONNECT_TRIGGER_DUMPINFO_THRESHOLD", 1).intValue()) {
                            Log.d(TAG, "< trigger brd >");
                            HashMap<String, String> map = new HashMap<>();
                            map.put("fwk defined reason:", Integer.toString(reason));
                            map.put("reason_code", Integer.toString(this.mReasonCode));
                            if (network != null) {
                                map.put("config_key", network.configKey());
                                map.put("psk_end_space", String.valueOf(isPskEndWithSpace(network)));
                                map.put("hidden", String.valueOf(network.hiddenSSID));
                            }
                            map.put("rssi", sr == null ? "-127" : Integer.toString(sr.level));
                            if (reason == 2) {
                                map.put("WiFi defined reason:", Integer.toString(this.mReasonCode));
                            }
                            if (reason == 13) {
                                map.put("WRONG KEY", "DUHHH");
                            }
                            if (sr == null) {
                                map.put("Target AP doesn't exist", "DUHHH");
                            } else if (sr.level < this.mScanResultRssiAssocReject) {
                                map.put("rssi level is too weak", Integer.toString(sr.level));
                            }
                            Log.d(TAG, "get data exp Connection Statistics map: " + map);
                            OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "wifi_connect_fail", map, false);
                            String region = SystemProperties.get("ro.oppo.regionmark", "CN");
                            if ("EUEX".equals(region)) {
                                dirPath = DIR_PATH_EXEU;
                            } else {
                                dirPath = DIR_PATH_DEFAULT;
                            }
                            File dir = new File(dirPath);
                            if (dir.exists() && dir.listFiles().length > 100) {
                                Log.d(TAG, "too many saved file,ignore dump!");
                            } else if (clientModeImpl.getRomUpdateBooleanValue("CONNECT_DUMPWIFI_WITH_SCREENSHOT", false).booleanValue()) {
                                if ("EUEX".equals(region)) {
                                    SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "dumpwifiscreen-euex");
                                } else {
                                    SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "dumpwifiscreen");
                                }
                            } else if ("EUEX".equals(region)) {
                                SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "dumpwifi-euex");
                            } else {
                                SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "dumpwifi");
                            }
                        } else if (this.mVerboseLoggingEnabled) {
                            Log.d(TAG, "same type");
                        }
                    }
                }
            }
        } else if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "dbg version, no need info");
        }
    }

    private int compareDate(String currentDate, String oldDate) {
        Date curDate = null;
        Date lastDate = null;
        try {
            curDate = this.mSdf.parse(currentDate);
            lastDate = this.mSdf.parse(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (curDate == null || lastDate == null) {
            return DATE_UNKNOWN;
        }
        if (curDate.getTime() > lastDate.getTime()) {
            return 1;
        }
        if (curDate.getTime() < lastDate.getTime()) {
            return -1;
        }
        return 0;
    }

    public void handleReasonRejection(WifiConfiguration config, ClientModeImpl clientModeImpl) {
        int rssi;
        if (config != null) {
            ScanResult sr = findHomonyAPFromScanResults(config, clientModeImpl);
            if (sr == null) {
                rssi = 0;
                Log.e(TAG, "chuck writeLogToPartition:TYPE_WIFI_ASSOC_REJECT-can't get rssi");
            } else {
                rssi = sr.level;
                Log.e(TAG, "chuck writeLogToPartition:TYPE_WIFI_ASSOC_REJECT rssi = " + rssi);
            }
            if (this.mContext != null) {
                int i = this.mReasonCode;
                if (i == 13 || i == 14 || i == 15 || i == 17 || rssi < this.mScanResultRssiAssocReject) {
                    Log.e(TAG, "chuck writeLogToPartition:not really failed");
                } else {
                    Log.e(TAG, "chuck writeLogToPartition:TYPE_WIFI_CONNECT_FAILED");
                    OppoManager.writeLogToPartition(OppoManager.TYPE_WIFI_CONNECT_FAILED, (String) null, "CONNECTIVITY", "wifi_connecting_failure", this.mContext.getResources().getString(201653531));
                }
            }
            this.mReasonCode = 0;
        }
    }

    public void handleReasonDhcpFail(WifiConfiguration config, int reason, ClientModeImpl clientModeImpl) {
        if (this.mVerboseLoggingEnabled) {
            Log.e(TAG, "chuck writeLogToPartition:TYPE_WIFI_CONNECT_FAILED");
        }
        checkRestoreConnectFailInfo(config, reason, clientModeImpl);
        OppoManager.writeLogToPartition(OppoManager.TYPE_WIFI_CONNECT_FAILED, (String) null, "CONNECTIVITY", "wifi_connecting_failure", this.mContext.getResources().getString(201653531));
    }

    public boolean checkPermission(int uid) {
        String pkgName = null;
        Context context = this.mContext;
        if (context != null) {
            pkgName = context.getPackageManager().getNameForUid(uid);
        }
        if (pkgName == null || !pkgName.contains("com.coloros.wifisecuredetect")) {
            return false;
        }
        Log.d(TAG, "wifisecuredetect---fliter");
        return true;
    }

    public void setWifiNetworkAvailable(OppoWifiAssistantStateTraker wst) {
        this.mWifiStateTracker = wst;
    }
}
