package com.android.server.wifi;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.IpConfiguration;
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
    private static final int ASSOC_REJECT_LOW_RSSI = 127;
    private static final int ASSOC_REJECT_OUT_OF_SEQ = 14;
    private static final int ASSOC_REJECT_REQUEST_DECLINED = 37;
    private static final String COLOR_SETTING_PKG = "com.coloros.wirelesssettings";
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
    private static final String WIFI_DIAGNOSIS_CONNECT_APK_NAME = "wifi_diagnosis_conpkgname";
    private static final String WIFI_DIAGNOSIS_CONNECT_FAIL_REASON = "wifi_diagnosis_failreason";
    private static final String WIFI_DIAGNOSIS_CONNECT_RESULT = "wifi_diagnosis_conresult";
    private static final String WIFI_DIAGNOSIS_ORIGINAL_CONNECT_APK_NAME = "wifi_diagnosis_originalconpkgname";
    private static final String WIFI_DIAGNOSIS_STATISTIC_KEY = "060203";
    private static String dirPath = DIR_PATH_DEFAULT;
    private HashMap<String, Integer> mConnectFailData = new HashMap<>();
    private final Context mContext;
    private String mOldDate = "";
    private String mOriginalStartConPkgName = null;
    private int mReasonCode = 0;
    private int mScanResultRssiAssocReject = -80;
    private List<ScanResult> mScanResults = new ArrayList();
    private SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd");
    private String mStartConPkgName = null;
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
            logd("hidden add ssid = " + config.SSID + " numassoc = " + config.numAssociation + " lastconncted = " + config.lastConnected);
            hiddenList.add(new WifiScanner.ScanSettings.HiddenNetwork(config.SSID));
            maxSsidCount += -1;
            if (maxSsidCount <= 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("discard scan ");
                sb.append(networks.size() - 16);
                sb.append(" ssids");
                logd(sb.toString());
                return;
            }
        }
    }

    public static String adjustHiddenSsidIfNeed(String ssid) {
        if (ssid == null || ssid.length() <= 3 || !ssid.endsWith(" \"")) {
            return null;
        }
        String ssidToAdjust = ssid.replaceAll("\\s+\"+$", "\"");
        Log.d(TAG, "change the hiedden ssid by delete one blank : " + ssidToAdjust);
        return ssidToAdjust;
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
                    logd("conn-track, found a Homony AP scanResult = " + scanResult);
                    tmpResult = scanResult;
                } else {
                    logd("conn-track, ESS found more Homony AP scanResult = " + scanResult);
                    if (scanResult.level > tmpResult.level) {
                        tmpResult = scanResult;
                    }
                }
            }
        }
        logd("conn-track, tmpResult = " + tmpResult);
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

    public void checkRestoreConnectFailInfo(WifiConfiguration network, int reason, ClientModeImpl clientModeImpl) {
        if (SystemProperties.get("ro.secure").equals("0")) {
            logd("dbg version, no need info");
            return;
        }
        ScanResult sr = findHomonyAPFromScanResults(network, clientModeImpl);
        Log.d(TAG, "< trigger brd >");
        HashMap<String, String> map = new HashMap<>();
        map.put("connect_fail", Integer.toString(reason));
        map.put("reason_code", Integer.toString(this.mReasonCode));
        if (network != null) {
            map.put("config_key", network.configKey());
            map.put("psk_end_space", String.valueOf(isPskEndWithSpace(network)));
            map.put("hidden", String.valueOf(network.hiddenSSID));
            map.put("random_mac", String.valueOf(network.macRandomizationSetting));
            map.put("static_ip", String.valueOf(IpConfiguration.IpAssignment.STATIC == network.getIpAssignment()));
            map.put("last_update_name", network.lastUpdateName);
            map.put("last_connect_name", this.mContext.getPackageManager().getNameForUid(network.lastConnectUid));
        }
        map.put("manu_connect", String.valueOf(OppoManuConnectManager.getInstance().isManuConnect()));
        map.put("rssi", sr == null ? "-127" : Integer.toString(sr.level));
        map.put("wifi_standard", OppoInformationElementUtil.getWifiStandard(sr));
        map.put("vendor_spec", OppoInformationElementUtil.getVendorSpec(sr));
        WifiInjector.getInstance().getWifiDisconStat().generateTimeStampMessage(map);
        logd("get data exp Connection Statistics map: " + map);
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "wifi_connect_fail", map, false);
        if (isReachDumpinfoThreshold(reason, clientModeImpl)) {
            startDumpInfo(clientModeImpl);
        }
    }

    private void startDumpInfo(ClientModeImpl clientModeImpl) {
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
    }

    private boolean isReachDumpinfoThreshold(int reason, ClientModeImpl clientModeImpl) {
        String date = this.mSdf.format(Long.valueOf(System.currentTimeMillis()));
        if (TextUtils.isEmpty(date)) {
            return false;
        }
        String reasonDate = date + "-" + reason;
        synchronized (this.mConnectFailData) {
            int recordCount = 1;
            int compareResult = compareDate(date, this.mOldDate);
            logd("date = " + compareResult);
            if (compareResult == DATE_UNKNOWN) {
                return false;
            }
            if (compareResult == 1 || compareResult == -1) {
                this.mOldDate = date;
                this.mConnectFailData.clear();
            }
            if (this.mConnectFailData.containsKey(reasonDate)) {
                Integer count = this.mConnectFailData.get(reasonDate);
                if (count == null) {
                    logd("get data exp");
                    return false;
                }
                recordCount = count.intValue() + 1;
            }
            logd("recordCount = " + recordCount);
            this.mConnectFailData.put(reasonDate, Integer.valueOf(recordCount));
            if (recordCount <= clientModeImpl.getRomUpdateIntegerValue("CONNECT_TRIGGER_DUMPINFO_THRESHOLD", 1).intValue()) {
                return true;
            }
            logd("same type");
            return false;
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
        storethirdAPKWifiDiagnosis(config, clientModeImpl, false, reason);
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

    public void storethirdAPKWifiDiagnosis(WifiConfiguration network, ClientModeImpl clientModeImpl, boolean isSuccess, int failReason) {
        int i;
        HashMap<String, String> resultmap = new HashMap<>();
        int reason = failReason;
        if (!isSuccess) {
            if (reason == 2 && ((i = this.mReasonCode) == 13 || i == 14 || i == 15 || i == 17 || i == 37)) {
                reason = 17;
            }
            if (reason == 2 || reason == 3) {
                ScanResult sr = findHomonyAPFromScanResults(network, clientModeImpl);
                if (sr == null) {
                    if (this.mVerboseLoggingEnabled) {
                        Log.d(TAG, "cannot scan. no need info");
                        return;
                    }
                    return;
                } else if (sr.level < this.mScanResultRssiAssocReject) {
                    if (this.mVerboseLoggingEnabled) {
                        Log.d(TAG, "rssi is weak. no need info");
                    }
                    reason = 127;
                }
            }
        }
        if (this.mOriginalStartConPkgName != null) {
            Log.d(TAG, "mOriginalStartConPkgName=" + this.mOriginalStartConPkgName + ",mStartConPkgName =" + this.mStartConPkgName + ",isSuccess =" + isSuccess + ",reason=" + reason);
            resultmap.put(WIFI_DIAGNOSIS_CONNECT_APK_NAME, this.mStartConPkgName);
            resultmap.put(WIFI_DIAGNOSIS_ORIGINAL_CONNECT_APK_NAME, this.mOriginalStartConPkgName);
            resultmap.put(WIFI_DIAGNOSIS_CONNECT_RESULT, String.valueOf(isSuccess));
            resultmap.put(WIFI_DIAGNOSIS_CONNECT_FAIL_REASON, String.valueOf(reason));
            OppoManager.onStamp(WIFI_DIAGNOSIS_STATISTIC_KEY, resultmap);
            this.mStartConPkgName = null;
            this.mOriginalStartConPkgName = null;
        }
    }

    public void statisticsStartConPkgName(int connectUid) {
        String[] pkgs;
        if (connectUid >= 0) {
            PackageManager mPackageManager = this.mContext.getPackageManager();
            this.mStartConPkgName = null;
            this.mOriginalStartConPkgName = null;
            if (!(mPackageManager == null || (pkgs = mPackageManager.getPackagesForUid(connectUid)) == null)) {
                for (String mPkgName : pkgs) {
                    this.mOriginalStartConPkgName = mPkgName;
                    if (connectUid < 10000) {
                        this.mOriginalStartConPkgName = COLOR_SETTING_PKG;
                    }
                    if (mPkgName != null) {
                        try {
                            ApplicationInfo applicationInfo = mPackageManager.getApplicationInfo(this.mOriginalStartConPkgName, 0);
                            if (applicationInfo != null) {
                                this.mStartConPkgName = mPackageManager.getApplicationLabel(applicationInfo).toString();
                                return;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public boolean isThirdAppConnect() {
        if (OppoManuConnectManager.getInstance() == null) {
            return false;
        }
        if (OppoManuConnectManager.getInstance().getType() == 1) {
            return true;
        }
        int operatorUid = OppoManuConnectManager.getInstance().getManuConnectUid();
        if (operatorUid == 1000 || operatorUid == 1010) {
            return false;
        }
        return true;
    }

    private void logd(String str) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, str);
        }
    }
}
