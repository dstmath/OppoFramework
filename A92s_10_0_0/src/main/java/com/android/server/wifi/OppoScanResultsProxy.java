package com.android.server.wifi;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoScanResultsProxy implements Handler.Callback {
    private static final int CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS_DEFAULT = 45000;
    private static final int CACHED_SCAN_RESULTS_SCAN_MISSED_COUNT = 3;
    public static final String CONNECT_WIFIINFO_PACKAGE = "CONNECT_WIFIINFO_PACKAGE";
    private static final String DEFAULT_GET_SCAN_RESULTS_PACKAGE = "com.coloros.assistantscreen,com.oppo.ohome,com.coloros.musiclink,com.coloros.sceneservice";
    private static final String DEFAULT_LOCATION_APP = "baidu.map,baidu.location,BaiduMap,com.amap,android.gms,android.location,com.didi.es.psngr,com.sdu.didi.psnger,com.sdu.didi.gsui,com.jingyao.easybike,com.didapinche.booking,com.mobike.mobikeapp,so.ofo.labofo,com.lalamove.huolala.client,me.ele,com.sankuai.meituan.takeoutnew,com.sankuai.meituan.dispatch.crowdsource,com.meituan.banma.errand,com.sankuai.meituan.dispatch.homebrew,com.dianping.v1,com.taobao.mobile.dipei,com.sankuai.meituan,com.tencent.mm,com.tencent.mobileqq,com.sina.weibo";
    private static final String DEFAULT_LONG_CACHE_APP = "com.coloros.wirelesssettings";
    private static final String DEFAULT_NAVIGATION_APP = "com.baidu.BaiduMap,com.autonavi.minimap,com.sogou.map.android.maps,com.tencent.map,com.google.android.apps.maps,com.google.android.gms,com.baidu.map.location,com.tencent.android.location,com.amap.android.location,com.amap.android.ams,com.coloros.sceneservice";
    private static final int DEFAULT_SCAN_REQUEST_THROTTLE_MAX_IN_TIME_WINDOW = 5;
    private static final int DEFAULT_SCAN_REQUEST_THROTTLE_TIME_WINDOW = 120000;
    private static final String DEFAULT_WIFIINFO_PACKAGE = "com.android.server.wifi,com.coloros.wifisecuredetect";
    private static final String DEFAULT_WIFI_APP = "com.coloros.assistantscreen,com.lenovo.anyshare,com.snda.wifilocating,com.qihoo.freewificom.tencent.wifimanager,com.farproc.wifi.analyzer,com.coloros.backuprestore,com.tencent.tmgp.pubgmhd";
    private static final String DEFAULT_WIFI_APP_BLACKLIST = null;
    private static final int FEW_AP_NUM = 5;
    private static final String GET_SCAN_RESULTS_PACKAGE = "GET_SCAN_RESULTS_PACKAGE";
    private static final int LONG_CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS_DEFAULT = 90000;
    private static final String SCAN_LOCATION = "scan_location";
    private static final String SCAN_NUM = "scan_num";
    protected static final Comparator<ScanResult> SCAN_RESULT_SORT_COMPARATOR = new Comparator<ScanResult>() {
        /* class com.android.server.wifi.OppoScanResultsProxy.AnonymousClass1 */

        public int compare(ScanResult r1, ScanResult r2) {
            return r2.level - r1.level;
        }
    };
    private static final String SCAN_RSSI_CHANNEL = "scan_rssi_channel";
    private static final String TAG = "OppoScanResultsProxy";
    private static final String WIRELESS_SETTINGS_PACKAGE_NAME = "com.coloros.wirelesssettings";
    private static int mCachedScanResultsMaxAgeInMillis = CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS_DEFAULT;
    private static List<String> mLocationAppList = new ArrayList();
    private static List<String> mLongCacheAppList = new ArrayList();
    private static int mLongCachedScanResultsMaxAgeInMillis = 90000;
    private static final String[] mNLPPackages = {"com.baidu.map.location", "com.amap.android.location", "com.amap.android.ams"};
    private static List<String> mNavigationAppList = new ArrayList();
    private static final Object mScanResultLock = new Object();
    private static long mScanUpdateIndex = 0;
    private static List<String> mWifiAppBlackList = new ArrayList();
    private static List<String> mWifiAppList = new ArrayList();
    private static WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    public final String WIFI_PACKEG_NAME = "com.android.server.wifi";
    private int count = 0;
    private int fewApNum = 5;
    private ClientModeImpl mClientModeImpl;
    private Clock mClock;
    private final Context mContext;
    private Handler mEventHandler;
    private String mIfaceName;
    private List<ScanResult> mLastScanResults = new ArrayList();
    private long mLastScanTimeStamp = 0;
    private long mLastScanTriggerTime = 0;
    private ArrayList<ScanDetail> mNativePnoScanResults = new ArrayList<>();
    private ArrayList<ScanDetail> mNativeScanResults = new ArrayList<>();
    private HashMap<String, OppoScanResult> mOppoScanResultHashMap = new HashMap<>();
    private BroadcastReceiver mReceiver;
    private boolean mReportScanCache = false;
    private HashMap<String, ArrayList> mScanRecrodHashMap = new HashMap<>();
    private int mScanThreshMaxInTimeWindow = 5;
    private int mScanThreshTimeWindow = 120000;
    private final Object mSettingsLock = new Object();
    private boolean mVerboseLoggingEnabled = false;
    private WifiConfigManager mWifiConfigManager;
    private WifiInjector mWifiInjector;
    private WifiManager mWifiManager;
    private WifiMonitor mWifiMonitor;
    private WifiNative mWifiNative;
    private boolean scanFewApFlag = false;
    private boolean turnOnWifiFlag = false;

    private class OppoScanResult {
        String mId;
        long mLastUpdateIndex;
        int mScanMissedCount;
        ScanResult mScanResult;

        public OppoScanResult(ScanResult scanResult) {
            if (scanResult != null) {
                this.mId = scanResult.BSSID + scanResult.SSID;
                this.mScanMissedCount = 0;
                this.mLastUpdateIndex = 0;
                this.mScanResult = new ScanResult(scanResult);
            }
        }

        /* access modifiers changed from: private */
        public long getLastScannedTime() {
            ScanResult scanResult = this.mScanResult;
            if (scanResult != null) {
                return scanResult.timestamp;
            }
            return 0;
        }

        /* access modifiers changed from: private */
        public ScanResult getScanResult() {
            return this.mScanResult;
        }

        /* access modifiers changed from: private */
        public int getScanMissedCount() {
            return this.mScanMissedCount;
        }
    }

    public OppoScanResultsProxy(Context context, WifiNative wifiNative, WifiMonitor wifiMonitor, Looper looper, WifiInjector wifiInjector, Clock clock) {
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mWifiNative = wifiNative;
        this.mWifiMonitor = wifiMonitor;
        this.mClock = clock;
        WifiInjector wifiInjector2 = this.mWifiInjector;
        if (wifiInjector2 != null) {
            this.mClientModeImpl = wifiInjector2.getClientModeImpl();
            this.mWifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        }
        this.mEventHandler = new Handler(looper, this);
        mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        initList();
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mWifiManager = (WifiManager) context2.getSystemService("wifi");
        }
        registerForWifiMonitorEvents();
        registerForBroadcasts();
    }

    private long getCurrentTimeStamp() {
        Clock clock = this.mClock;
        if (clock != null) {
            return clock.getElapsedSinceBootMillis();
        }
        return 0;
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
    }

    private void logd(String s) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, s);
        }
    }

    private void loge(String s) {
        if (this.mVerboseLoggingEnabled) {
            Log.e(TAG, s);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WifiMonitor.SCAN_RESULTS_EVENT /*{ENCODED_INT: 147461}*/:
                pollLatestScanData(true);
                break;
            case WifiMonitor.SCAN_FAILED_EVENT /*{ENCODED_INT: 147473}*/:
                handleScanFailEvent();
                break;
            case WifiMonitor.PNO_SCAN_RESULTS_EVENT /*{ENCODED_INT: 147474}*/:
                pollLatestScanDataForPno();
                break;
        }
        return true;
    }

    public void registerForWifiMonitorEvents() {
        WifiNative wifiNative;
        logd("registerForWifiMonitorEvents");
        if (this.mWifiMonitor == null || (wifiNative = this.mWifiNative) == null) {
            loge("registerForWifiMonitorEvents mWifiMonitor/mWifiNative is null!");
            return;
        }
        this.mIfaceName = wifiNative.getClientInterfaceName();
        if (this.mIfaceName == null) {
            loge("mIfaceName = null ,using default wlan0 to registerForWifiMonitorEvents");
            this.mIfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN0;
        }
        this.mWifiMonitor.registerHandler(this.mIfaceName, WifiMonitor.SCAN_FAILED_EVENT, this.mEventHandler);
        this.mWifiMonitor.registerHandler(this.mIfaceName, WifiMonitor.PNO_SCAN_RESULTS_EVENT, this.mEventHandler);
        this.mWifiMonitor.registerHandler(this.mIfaceName, WifiMonitor.SCAN_RESULTS_EVENT, this.mEventHandler);
    }

    public void deregisterForWifiMonitorEvents() {
        WifiMonitor wifiMonitor;
        String str = this.mIfaceName;
        if (str == null || (wifiMonitor = this.mWifiMonitor) == null) {
            loge("mIfaceName/mWifiMonitor is null ,fail to deregisterForWifiMonitorEvents");
            return;
        }
        wifiMonitor.deregisterHandler(str, WifiMonitor.SCAN_FAILED_EVENT, this.mEventHandler);
        this.mWifiMonitor.deregisterHandler(this.mIfaceName, WifiMonitor.PNO_SCAN_RESULTS_EVENT, this.mEventHandler);
        this.mWifiMonitor.deregisterHandler(this.mIfaceName, WifiMonitor.SCAN_RESULTS_EVENT, this.mEventHandler);
    }

    public List<ScanResult> syncGetScanResultsList() {
        return getScanResults("com.android.server.wifi", 1010);
    }

    private void filterScanResultsByAge(int ageTimeout) {
        OppoScanResult oppoScanResult;
        Iterator iterator = this.mOppoScanResultHashMap.keySet().iterator();
        long curTime = getCurrentTimeStamp();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!(key == null || (oppoScanResult = this.mOppoScanResultHashMap.get(key)) == null)) {
                if (curTime - (oppoScanResult.getLastScannedTime() / 1000) > ((long) ageTimeout)) {
                    iterator.remove();
                } else if (oppoScanResult.getScanMissedCount() > 3) {
                    iterator.remove();
                }
            }
        }
    }

    private List<ScanResult> fetchScanResults(int ageTimeout) {
        OppoScanResult oppoScanResult;
        List<ScanResult> results = new ArrayList<>();
        Iterator iterator = null;
        if (this.mOppoScanResultHashMap.keySet() != null) {
            iterator = this.mOppoScanResultHashMap.keySet().iterator();
        }
        if (iterator == null) {
            return results;
        }
        long curTime = getCurrentTimeStamp();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!(key == null || (oppoScanResult = this.mOppoScanResultHashMap.get(key)) == null || curTime - (oppoScanResult.getLastScannedTime() / 1000) > ((long) ageTimeout))) {
                results.add(oppoScanResult.getScanResult());
            }
        }
        return results;
    }

    public List<ScanResult> getScanResults(String packageName, int callingUid) {
        List<ScanResult> scanList = new ArrayList<>();
        synchronized (mScanResultLock) {
            if (this.mLastScanResults != null) {
                if (this.mOppoScanResultHashMap != null) {
                    this.mReportScanCache = true;
                    if ((inLocationWhiteList(packageName) || inNavigationWhiteList(packageName)) && getCurrentTimeStamp() - this.mLastScanTimeStamp < ((long) mCachedScanResultsMaxAgeInMillis) && this.mLastScanResults.size() > 0) {
                        this.mReportScanCache = false;
                    }
                    if (this.mReportScanCache) {
                        int ageTimeout = mCachedScanResultsMaxAgeInMillis;
                        if (inLongCacheWhiteList(packageName)) {
                            ageTimeout = mLongCachedScanResultsMaxAgeInMillis;
                        }
                        scanList = fetchScanResults(ageTimeout);
                        if (scanList.size() <= 0 && (inLongCacheWhiteList(packageName) || isWifiConnected())) {
                            logd("make sure UI show last ap list!!");
                            scanList.addAll(this.mLastScanResults);
                        }
                    } else {
                        scanList.addAll(this.mLastScanResults);
                    }
                    logd("getScanResults() callingUid=" + callingUid + " packageName:" + packageName + " mReportCache = " + this.mReportScanCache + " size:" + scanList.size());
                    return scanList;
                }
            }
            loge("mLastScanResults or mOppoScanResultHashMap is null!!");
            return scanList;
        }
    }

    private void handleScanResultEvent(List<ScanResult> scanResultList, boolean success) {
        logd("handleScanResultEvent recevied size = " + scanResultList.size());
        this.mLastScanTimeStamp = getCurrentTimeStamp();
        synchronized (mScanResultLock) {
            if (success) {
                mScanUpdateIndex++;
            }
            for (ScanResult sr : scanResultList) {
                if (sr.BSSID != null) {
                    OppoScanResult oppoScanResult = this.mOppoScanResultHashMap.get(sr.BSSID + sr.SSID);
                    if (oppoScanResult == null) {
                        oppoScanResult = new OppoScanResult(sr);
                    }
                    oppoScanResult.mScanMissedCount = 0;
                    oppoScanResult.mScanResult = new ScanResult(sr);
                    oppoScanResult.mLastUpdateIndex = mScanUpdateIndex;
                    this.mOppoScanResultHashMap.put(oppoScanResult.mId, oppoScanResult);
                }
            }
            if (success) {
                for (String key : this.mOppoScanResultHashMap.keySet()) {
                    if (key != null) {
                        OppoScanResult oppoScanResult2 = this.mOppoScanResultHashMap.get(key);
                        if (oppoScanResult2 != null) {
                            if (oppoScanResult2.mLastUpdateIndex < mScanUpdateIndex) {
                                oppoScanResult2.mScanMissedCount++;
                                this.mOppoScanResultHashMap.put(key, oppoScanResult2);
                            }
                        }
                    }
                }
            }
            filterScanResultsByAge(mLongCachedScanResultsMaxAgeInMillis);
            this.mLastScanResults.clear();
            this.mLastScanResults.addAll(scanResultList);
            if (this.mClientModeImpl == null) {
                this.mClientModeImpl = this.mWifiInjector.getClientModeImpl();
            }
            if (this.mClientModeImpl != null) {
                this.mClientModeImpl.handleScanResultEvent(this.mLastScanResults);
                WifiInjector.getInstance().getOppoClientModeImpl2().handleScanResultEvent();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0073, code lost:
        return;
     */
    private void pollLatestScanData(boolean success) {
        synchronized (this.mSettingsLock) {
            if (this.mWifiNative == null) {
                loge("pollLatestScanData mWifiNative is null");
                return;
            }
            if (this.mWifiManager == null) {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            }
            if (this.mWifiManager == null || this.mWifiManager.getWifiState() != 0) {
                this.mNativeScanResults = this.mWifiNative.getScanResults(this.mIfaceName);
                List<ScanResult> singleScanResults = new ArrayList<>();
                for (int i = 0; i < this.mNativeScanResults.size(); i++) {
                    singleScanResults.add(this.mNativeScanResults.get(i).getScanResult());
                }
                Collections.sort(singleScanResults, SCAN_RESULT_SORT_COMPARATOR);
                if (success || singleScanResults.size() != 0) {
                    handleScanResultEvent(singleScanResults, success);
                } else {
                    loge("scan fail and got 0 scan results, do not handleScanResultEvent");
                }
            } else {
                loge("Wifi in turning OFF state, dont get scanresults");
            }
        }
    }

    private void pollLatestScanDataForPno() {
        synchronized (this.mSettingsLock) {
            if (this.mWifiNative == null) {
                loge("pollLatestScanDataForPno mWifiNative is null!!");
                return;
            }
            this.mNativePnoScanResults = this.mWifiNative.getPnoScanResults(this.mIfaceName);
            List<ScanResult> hwPnoScanResults = new ArrayList<>();
            for (int i = 0; i < this.mNativePnoScanResults.size(); i++) {
                hwPnoScanResults.add(this.mNativePnoScanResults.get(i).getScanResult());
            }
            handleScanResultEvent(hwPnoScanResults, true);
        }
    }

    private void handleScanFailEvent() {
        pollLatestScanData(false);
    }

    private void registerForBroadcasts() {
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoScanResultsProxy.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null && "oppo.intent.action.WIFI_ROM_UPDATE_CHANGED".equals(intent.getAction())) {
                    OppoScanResultsProxy.this.initList();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED");
        new IntentFilter();
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public boolean inScanWhiteList(String pkgName) {
        return inList(pkgName, mWifiAppList);
    }

    public boolean inScanBlackList(String pkgName) {
        return inList(pkgName, mWifiAppBlackList);
    }

    public boolean inLocationWhiteList(String pkgName) {
        return inList(pkgName, mLocationAppList);
    }

    public boolean inNavigationWhiteList(String pkgName) {
        return inList(pkgName, mNavigationAppList);
    }

    public boolean inLongCacheWhiteList(String pkgName) {
        return inList(pkgName, mLongCacheAppList);
    }

    private boolean inList(String pkgName, List<String> appList) {
        if (pkgName == null || appList == null) {
            loge("pkgName = null");
            return false;
        } else if (appList.size() <= 0) {
            return false;
        } else {
            synchronized (appList) {
                for (String name : appList) {
                    if (pkgName.contains(name)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void initList() {
        logd("initList!");
        String value = getRomUpdateValue("NETWORK_LOCATION_APP", DEFAULT_LOCATION_APP);
        if (value != null) {
            synchronized (mLocationAppList) {
                if (!mLocationAppList.isEmpty()) {
                    mLocationAppList.clear();
                }
                for (String name : value.split(",")) {
                    mLocationAppList.add(name.trim());
                }
            }
        }
        String value2 = getRomUpdateValue("NETWORK_WIFI_APP", DEFAULT_WIFI_APP);
        if (value2 != null) {
            synchronized (mWifiAppList) {
                if (!mWifiAppList.isEmpty()) {
                    mWifiAppList.clear();
                }
                for (String name2 : value2.split(",")) {
                    mWifiAppList.add(name2.trim());
                }
            }
        }
        String value3 = getRomUpdateValue("NETWORK_WIFI_APP_BLACKLIST", DEFAULT_WIFI_APP_BLACKLIST);
        if (value3 != null) {
            synchronized (mWifiAppBlackList) {
                if (!mWifiAppBlackList.isEmpty()) {
                    mWifiAppBlackList.clear();
                }
                for (String name3 : value3.split(",")) {
                    mWifiAppBlackList.add(name3.trim());
                }
            }
        }
        String value4 = getRomUpdateValue("NETWORK_NAVIGATION_APP", DEFAULT_NAVIGATION_APP);
        if (value4 != null) {
            synchronized (mNavigationAppList) {
                if (!mNavigationAppList.isEmpty()) {
                    mNavigationAppList.clear();
                }
                for (String name4 : value4.split(",")) {
                    mNavigationAppList.add(name4.trim());
                }
            }
        }
        String value5 = getRomUpdateValue("NETWORK_LONG_CACHE_APP", "com.coloros.wirelesssettings");
        if (value5 != null) {
            synchronized (mLongCacheAppList) {
                if (!mLongCacheAppList.isEmpty()) {
                    mLongCacheAppList.clear();
                }
                for (String name5 : value5.split(",")) {
                    mLongCacheAppList.add(name5.trim());
                }
            }
        }
        this.mScanThreshMaxInTimeWindow = getRomUpdateIntegerValue("SCAN_REQUEST_THROTTLE_MAX_IN_TIME_WINDOW", 5).intValue();
        this.mScanThreshTimeWindow = getRomUpdateIntegerValue("SCAN_REQUEST_THROTTLE_TIME_WINDOW", 120000).intValue();
        mCachedScanResultsMaxAgeInMillis = getRomUpdateIntegerValue("CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS", Integer.valueOf((int) CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS_DEFAULT)).intValue();
        mLongCachedScanResultsMaxAgeInMillis = getRomUpdateIntegerValue("LONG_CACHED_SCAN_RESULTS_MAX_AGE_IN_MILLIS", 90000).intValue();
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    public void scanTriggerStatistics(int uid, String pkgName) {
        long currentTimeMillis = getCurrentTimeStamp();
        if (pkgName == null) {
            loge("pkgName is null!!");
            this.mLastScanTriggerTime = currentTimeMillis;
            return;
        }
        HashMap<String, ArrayList> hashMap = this.mScanRecrodHashMap;
        if (hashMap != null) {
            synchronized (hashMap) {
                if (currentTimeMillis - this.mLastScanTriggerTime > ((long) this.mScanThreshTimeWindow)) {
                    this.mScanRecrodHashMap.clear();
                }
                ArrayList tmpList = this.mScanRecrodHashMap.get(pkgName);
                if (tmpList == null) {
                    tmpList = new ArrayList();
                }
                tmpList.add(Long.valueOf(currentTimeMillis));
                if (tmpList.size() >= this.mScanThreshMaxInTimeWindow) {
                    Iterator<Long> timestampsIter = tmpList.iterator();
                    while (timestampsIter.hasNext()) {
                        if (currentTimeMillis - timestampsIter.next().longValue() > ((long) this.mScanThreshTimeWindow)) {
                            timestampsIter.remove();
                        }
                    }
                    if (tmpList.size() >= this.mScanThreshMaxInTimeWindow) {
                        HashMap<String, String> reportEvents = new HashMap<>();
                        reportEvents.put("pkgName", pkgName);
                        reportEvents.put("count", String.valueOf(tmpList.size()));
                        logd("warning: " + pkgName + " scan " + tmpList.size() + " times in " + this.mScanThreshTimeWindow + "ms");
                        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "scan_freq", reportEvents, false);
                    }
                }
                this.mScanRecrodHashMap.put(pkgName, tmpList);
                this.mLastScanTriggerTime = currentTimeMillis;
            }
        }
    }

    private void tryUpdateFewApNum() {
        this.fewApNum = getRomUpdateIntegerValue("OPPO_FEW_AP_NUM", 5).intValue();
    }

    private void oppoStatisticsForScanFewAP() {
        HashMap<String, String> map = new HashMap<>();
        List<ScanResult> srList = syncGetScanResultsList();
        String scanNum = Integer.toString(srList.size());
        String allRssiAndChannel = "";
        StringBuffer tmpAllRssiAndChannel = new StringBuffer("");
        if (srList.size() > 0) {
            for (ScanResult sr : srList) {
                tmpAllRssiAndChannel.append("(" + Integer.toString(sr.level) + "," + Integer.toString(sr.frequency) + ")");
            }
            allRssiAndChannel = tmpAllRssiAndChannel.toString();
        }
        map.put(SCAN_NUM, scanNum);
        map.put(SCAN_RSSI_CHANNEL, allRssiAndChannel);
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "scan_few_ap", map, false);
    }

    public void checkScanFewAP(List<ScanResult> scanResults, String stateName) {
        if ("DisconnectedState".equalsIgnoreCase(stateName) || "ScanModeState".equalsIgnoreCase(stateName)) {
            tryUpdateFewApNum();
            if ((scanResults == null || scanResults.size() < this.fewApNum) && isInWirelessSettings() && !isScanConfigedAp()) {
                if (!this.scanFewApFlag || !this.turnOnWifiFlag) {
                    this.scanFewApFlag = true;
                    return;
                }
                oppoStatisticsForScanFewAP();
                this.scanFewApFlag = false;
                this.turnOnWifiFlag = false;
                return;
            }
        }
        this.scanFewApFlag = false;
        this.turnOnWifiFlag = false;
    }

    public void checkTurnOnWifi(String packageName, boolean enable) {
        if (!this.scanFewApFlag) {
            this.turnOnWifiFlag = false;
        } else if (TextUtils.isEmpty(packageName) || !packageName.equals("com.coloros.wirelesssettings") || !enable) {
            this.turnOnWifiFlag = false;
        } else {
            this.turnOnWifiFlag = true;
        }
    }

    private boolean isInWirelessSettings() {
        List<ActivityManager.RunningTaskInfo> runningTaskInfos;
        ActivityManager.RunningTaskInfo info;
        ActivityManager am = null;
        Context context = this.mContext;
        if (context != null) {
            am = (ActivityManager) context.getSystemService("activity");
        }
        if (!(am == null || (runningTaskInfos = am.getRunningTasks(1)) == null || runningTaskInfos.size() <= 0 || (info = runningTaskInfos.get(0)) == null || info.topActivity == null)) {
            String pkg = info.topActivity.getPackageName();
            if (TextUtils.isEmpty(pkg) || !pkg.equals("com.coloros.wirelesssettings")) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isScanConfigedAp() {
        List<ScanResult> srList = syncGetScanResultsList();
        List<WifiConfiguration> wcList = null;
        if (this.mWifiConfigManager == null) {
            this.mWifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        }
        WifiConfigManager wifiConfigManager = this.mWifiConfigManager;
        if (wifiConfigManager != null) {
            wcList = wifiConfigManager.getConfiguredNetworks();
        }
        if (srList == null || srList.size() <= 0 || wcList == null || wcList.size() <= 0) {
            return false;
        }
        for (WifiConfiguration wc : wcList) {
            String wcConfigKey = wc.configKey();
            if (wcConfigKey != null) {
                for (ScanResult sr : srList) {
                    String srConfigKey = WifiConfiguration.configKey(sr);
                    if (srConfigKey != null && wcConfigKey.equals(srConfigKey)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    public static boolean isPackageCanGetScanResults(String pkg) {
        String pkgList;
        WifiRomUpdateHelper wifiRomUpdateHelper = mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            pkgList = wifiRomUpdateHelper.getValue(GET_SCAN_RESULTS_PACKAGE, DEFAULT_GET_SCAN_RESULTS_PACKAGE);
        } else {
            pkgList = DEFAULT_GET_SCAN_RESULTS_PACKAGE;
        }
        if (pkgList == null || !pkgList.contains(pkg)) {
            return false;
        }
        return true;
    }

    private boolean isWifiConnected() {
        ClientModeImpl clientModeImpl = this.mClientModeImpl;
        if (clientModeImpl == null) {
            return false;
        }
        if (clientModeImpl.isConnected() || this.mClientModeImpl.isWifiConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isNLP(String callingPackage) {
        for (String name : mNLPPackages) {
            if (name.equals(callingPackage)) {
                return true;
            }
        }
        return false;
    }
}
