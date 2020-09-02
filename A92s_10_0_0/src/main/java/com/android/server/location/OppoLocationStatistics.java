package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.gatewayconflict.OppoArpPeer;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.LocationRequestStatistics;
import com.android.server.location.interfaces.IPswLocationStatistics;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OppoLocationStatistics implements IPswLocationStatistics {
    private static final String CMD_ALL = "CMD_ALL";
    private static final String CMD_BIG_DATA_STATUS = "CMD_BIG_DATA_STATUS";
    private static final String CMD_FORCE_DOING_STATISTICS = "CMD_FORCE_DOING_STATISTICS";
    private static final String CMD_GEOCODER_DATA = "CMD_GEOCODER_DATA";
    private static final String CMD_GNSS_DATA = "CMD_GNSS_DATA";
    private static final String CMD_LOCATION_REQUEST_INFO = "CMD_LOCATION_REQUEST_INFO";
    private static final String CMD_NLP_DATA = "CMD_NLP_DATA";
    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String ERROR_COUNT = "ERROR_COUNT";
    private static final String FUSED_LOCATION_REQUEST = "FUSED_LOCATION_REQUEST";
    private static final String FW_VERSION = "1.2.1";
    private static final String GEOCODER_ERROR_LIST = "GEOCODER_ERROR_LIST";
    private static final int GEOCODER_REPEAT_TIME_LIMIT = 1000;
    private static final String[] GEOCODER_RESPONSE_DISTRIBUTION_NAMES = {"GEOCODER_RESPONSE_EXCELLENT", "GEOCODER_RESPONSE_GOOD", "GEOCODER_RESPONSE_AVERAGE", "GEOCODER_RESPONSE_WEAK", "GEOCODER_RESPONSE_BAD"};
    private static final String GEOCODER_SUCCEED_REQUEST = "GEOCODER_SUCCEED_REQUEST";
    private static final String GEOCODER_TOTAL_REQUEST = "GEOCODER_TOTAL_REQUEST";
    private static final String GNSS_ACTIVE_DURATION = "GNSS_ACTIVE_DURATION";
    private static final int GNSS_INTERVAL_MIN = 1000;
    private static final String GNSS_LOCATION_REQUEST = "GNSS_LOCATION_REQUEST";
    private static final String GNSS_POWER_SAVE_LIST = "GNSS_POWER_SAVE_LIST";
    private static final String[] GNSS_REQUEST_DISTRIBUTION_NAMES = {"GNSS_DURATION_EXCELLENT", "GNSS_DURATION_GOOD", "GNSS_DURATION_AVERAGE", "GNSS_DURATION_WEAK", "GNSS_DURATION_BAD"};
    private static final String GNSS_TOTAL_COUNT = "GNSS_TOTAL_COUNT";
    private static final String GNSS_WAKE_LOCK_LIST = "GNSS_WAKE_LOCK_LIST";
    private static final int LOCATION_REQUEST_REPORTED_COUNT = 5;
    private static final String MASK_LOCATION_BIG_DATA = "config_locationBigDataMask";
    private static final String NLP_COUNT = "NLP_COUNT";
    private static final String NLP_ERROR_LIST = "NLP_ERROR_LIST";
    private static final String NLP_LOCATION_REQUEST = "NLP_LOCATION_REQUEST";
    private static final String NLP_PACKAGE = "NLP_PACKAGE";
    private static final String NLP_PACKAGE_AMAP = "com.amap.android.location";
    private static final String NLP_PACKAGE_BAIDU = "com.baidu.map.location";
    private static final String NLP_PACKAGE_GMS = "com.google.android.gms";
    private static final String NLP_PACKAGE_QUALCOMM = "com.qualcomm.location";
    private static final String NLP_PACKAGE_TENCENT = "com.tencent.android.location";
    private static final int NLP_REPEAT_TIME_LIMIT = 1000;
    private static final String[] NLP_RESPONSE_DISTRIBUTION_NAMES = {"NLP_RESPONSE_EXCELLENT", "NLP_RESPONSE_GOOD", "NLP_RESPONSE_AVERAGE", "NLP_RESPONSE_WEAK", "NLP_RESPONSE_BAD"};
    private static final String NLP_SCAN_WIFI = "NLP_SCAN_WIFI";
    private static final String NLP_SUCCEED_COUNT = "NLP_SUCCEED_COUNT";
    private static final String NLP_SUCCEED_REQUESTS = "NLP_SUCCEED_REQUESTS";
    private static final String NLP_TOTAL_REQUESTS = "NLP_TOTAL_REQUESTS";
    private static final String POWER_SAVE_DURATION = "POWER_SAVE_DURATION";
    private static final String POWER_SAVE_STRATEGY = "POWER_SAVE_STRATEGY";
    private static final String PROVIDER_LOCATION_BIG_DATA = "LOCATION_BIG_DATA";
    private static final String PS_GNSS_ACTIVE_DURATION = "PS_GNSS_ACTIVE_DURATION";
    private static final String[] PS_GNSS_REQUEST_DISTRIBUTION_NAMES = {"PS_REQUEST_EXCELLENT_COUNT", "PS_REQUEST_GOOD_COUNT", "PS_REQUEST_AVERAGE_COUNT", "PS_REQUEST_WEAK_COUNT", "PS_REQUEST_BAD_COUNT"};
    private static final String PS_NLP_SCAN_WIFI_SUCCEED = "PS_NLP_SCAN_WIFI_SUCCEED";
    private static final String PS_NLP_SCAN_WIFI_TOTAL = "PS_NLP_SCAN_WIFI_TOTAL";
    private static final String PS_NLP_TOTAL_REQUESTS = "PS_NLP_TOTAL_REQUESTS";
    private static final String PS_POWER_SAVE_DATA = "PS_POWER_SAVE_DATA";
    private static final String PS_POWER_SAVE_DURATION = "PS_POWER_SAVE_DURATION";
    private static final String PS_POWER_SAVE_STRATEGY = "PS_POWER_SAVE_STRATEGY";
    private static final String PS_REQUEST_ARRAY = "PS_REQUEST_ARRAY";
    private static final String PS_REQUEST_COUNT = "PS_REQUEST_COUNT";
    private static final String PS_REQUEST_PACKAGE = "PS_REQUEST_PACKAGE";
    private static final String[] PS_WAKELOCK_DISTRIBUTION_NAMES = {"PS_WAKE_LOCK_EXCELLENT_COUNT", "PS_WAKE_LOCK_GOOD_COUNT", "PS_WAKE_LOCK_AVERAGE_COUNT", "PS_WAKE_LOCK_WEAK_COUNT", "PS_WAKE_LOCK_BAD_COUNT"};
    private static final String REQUEST_COUNT = "REQUEST_COUNT";
    private static final String REQUEST_DURATION = "REQUEST_DURATION";
    private static final String REQUEST_PACKAGE = "REQUEST_PACKAGE";
    private static final String REQUEST_PROVIDER = "REQUEST_PROVIDER";
    private static final String REQUEST_REPEAT = "REQUEST_REPEAT";
    private static final int STATISTIC_MODE_NORMAL = 0;
    private static final int STATISTIC_MODE_POWER_SAVE = 1;
    private static final String TAG = "OppoLocationStatistics";
    private static final String VERSION = "VERSION";
    private static final String WAKE_LOCK_DURATION = "WAKE_LOCK_DURATION";
    private static final String WAKE_LOCK_HELD_COUNT = "WAKE_LOCK_HELD_COUNT";
    private static final String WAKE_LOCK_NAME = "WAKE_LOCK_NAME";
    private static final String WAKE_LOCK_RELEASED_COUNT = "WAKE_LOCK_RELEASED_COUNT";
    /* access modifiers changed from: private */
    public static DataDistribution[] mDataDistributions = new DataDistribution[2];
    /* access modifiers changed from: private */
    public static boolean mDebug = Log.isLoggable(TAG, 3);
    private static OppoLocationStatistics sInstance = new OppoLocationStatistics();
    private int mBigDataStatus = 0;
    private boolean mBigDataSwitchedOff = true;
    private Set<Integer> mCmdWhiteListSet = new HashSet();
    private AtomicLong mFixInterval = new AtomicLong(1000);
    private final Map<Integer, Integer> mGeocoderErrorMap = new HashMap();
    private final Object mGeocoderLock = new Object();
    private RequestTime mGeocoderRequestTime = new RequestTime();
    private AtomicLong mGeocoderStartedElapsedTime = new AtomicLong(-1);
    private AtomicInteger mGeocoderSucceedRequests = new AtomicInteger(0);
    private AtomicInteger mGeocoderTotalRequests = new AtomicInteger(0);
    private AtomicLong mGnssActiveDuration = new AtomicLong(0);
    private final Map<Integer, PowerSaveData> mGnssPowerSaveMap = new HashMap();
    private AtomicInteger mGnssShouldReportedCount = new AtomicInteger(0);
    private AtomicLong mGnssStartedElapsedTime = new AtomicLong(-1);
    private final Map<String, WakelockData> mGnssWakeLockMap = new HashMap();
    private LocationRequestStatistics mLocationRequestStatistics;
    private final Object mLock = new Object();
    /* access modifiers changed from: private */
    public boolean mNetworkConnected = false;
    private final HashMap<Integer, Integer> mNlpErrorMap = new HashMap<>();
    private Map<String, Integer> mNlpScanSucceedMap = new HashMap();
    private Map<String, Integer> mNlpScanTotalMap = new HashMap();
    private long mNlpStartedElapsedTime = -1;
    private int mNlpSucceedRequests = 0;
    private int mNlpTotalRequests = 0;
    private final Object mPowerSaveModeLock = new Object();
    private AtomicBoolean mPsEnabled = new AtomicBoolean(false);
    private AtomicLong mPsGnssActiveDuration = new AtomicLong(0);
    private final Map<Integer, PowerSaveData> mPsGnssPowerSaveMap = new HashMap();
    private AtomicLong mPsGnssStartedElapsedTime = new AtomicLong(-1);
    private final Map<String, WakelockData> mPsGnssWakeLockMap = new HashMap();
    private AtomicInteger mPsNlpScanSucceedCount = new AtomicInteger(0);
    private AtomicInteger mPsNlpScanTotalCount = new AtomicInteger(0);
    private AtomicInteger mPsNlpTotalRequests = new AtomicInteger(0);
    private final Map<LocationRequestStatistics.PackageProviderKey, RequestStatistics> mPsRequestStatistics = new HashMap();
    private final HashMap<LocationRequestStatistics.PackageProviderKey, RequestStatistics> mRequestStatistics = new HashMap<>();
    private final Object mWiFiLock = new Object();

    private OppoLocationStatistics() {
        this.mCmdWhiteListSet.add(1001);
        int i = 0;
        while (true) {
            DataDistribution[] dataDistributionArr = mDataDistributions;
            if (i < dataDistributionArr.length) {
                dataDistributionArr[i] = new DataDistribution();
                i++;
            } else {
                return;
            }
        }
    }

    public static OppoLocationStatistics getInstance() {
        return sInstance;
    }

    public void init(Context context, LocationRequestStatistics statistics) {
        this.mBigDataStatus = OppoLbsRomUpdateUtil.getInstall(context).getInt(MASK_LOCATION_BIG_DATA);
        this.mBigDataSwitchedOff = this.mBigDataStatus <= 0;
        this.mLocationRequestStatistics = statistics;
        Log.i(TAG, "status: " + this.mBigDataStatus);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        context.registerReceiver(new NetworkChangedReceiver(), filter);
    }

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }

    public String getName() {
        return PROVIDER_LOCATION_BIG_DATA;
    }

    public boolean handleCommand(String provider, String command, Bundle extras) {
        if (!this.mCmdWhiteListSet.contains(Integer.valueOf(Binder.getCallingUid())) || TextUtils.isEmpty(provider) || TextUtils.isEmpty(command) || !PROVIDER_LOCATION_BIG_DATA.equals(provider)) {
            return false;
        }
        if (extras == null) {
            extras = new Bundle();
        }
        handleCommand(command, extras);
        return true;
    }

    private void handleCommand(String cmd, Bundle extras) {
        if (mDebug) {
            Log.i(TAG, cmd);
        }
        char c = 65535;
        switch (cmd.hashCode()) {
            case -2033115452:
                if (cmd.equals(CMD_FORCE_DOING_STATISTICS)) {
                    c = 5;
                    break;
                }
                break;
            case -1643979012:
                if (cmd.equals(CMD_NLP_DATA)) {
                    c = 3;
                    break;
                }
                break;
            case -1381096605:
                if (cmd.equals(CMD_LOCATION_REQUEST_INFO)) {
                    c = 1;
                    break;
                }
                break;
            case -332189904:
                if (cmd.equals(CMD_GEOCODER_DATA)) {
                    c = 4;
                    break;
                }
                break;
            case 1603343580:
                if (cmd.equals(CMD_ALL)) {
                    c = 0;
                    break;
                }
                break;
            case 1878158973:
                if (cmd.equals(CMD_GNSS_DATA)) {
                    c = 2;
                    break;
                }
                break;
            case 2007153699:
                if (cmd.equals(CMD_BIG_DATA_STATUS)) {
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                fillAll(extras);
                break;
            case 1:
                fillLocationRequestInfo(extras);
                break;
            case 2:
                fillGnssData(extras);
                break;
            case 3:
                fillNlpData(extras);
                break;
            case 4:
                fillGeocoderData(extras);
                break;
            case LOCATION_REQUEST_REPORTED_COUNT /*{ENCODED_INT: 5}*/:
                fillAllByForceStop(extras);
                break;
            case 6:
                fillStatusData(extras);
                break;
        }
        extras.putString(VERSION, FW_VERSION);
    }

    private void fillAll(Bundle result) {
        fillLocationRequestInfo(result);
        fillGnssData(result);
        fillNlpData(result);
        fillGeocoderData(result);
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x013e  */
    private void fillLocationRequestInfo(Bundle result) {
        Set<LocationRequestStatistics.PackageProviderKey> set;
        HashMap<LocationRequestStatistics.PackageProviderKey, RequestStatistics> savedRequest = new HashMap<>();
        List<JSONObject> gnssRequestList = new LinkedList<>();
        List<JSONObject> nlpRequestList = new LinkedList<>();
        List<JSONObject> fusedRequestList = new LinkedList<>();
        Set<LocationRequestStatistics.PackageProviderKey> set2 = this.mRequestStatistics.keySet();
        for (LocationRequestStatistics.PackageProviderKey key : set2) {
            RequestStatistics requestStatistics = this.mRequestStatistics.get(key);
            JSONObject packageJsonObject = new JSONObject();
            try {
                packageJsonObject.put(REQUEST_PACKAGE, key.packageName);
                packageJsonObject.put(REQUEST_PROVIDER, key.providerName);
                packageJsonObject.put(REQUEST_COUNT, requestStatistics.getRequestNum());
                packageJsonObject.put(REQUEST_REPEAT, requestStatistics.getRepeatRequestNum());
                long requestDuration = 0;
                Set<String> requestTimeSet = requestStatistics.mRequestTimeMap.keySet();
                for (String hash : requestTimeSet) {
                    ReceiverRequestTime receiverRequestTime = (ReceiverRequestTime) requestStatistics.mRequestTimeMap.get(hash);
                    RequestTime requestTime = receiverRequestTime.getRequestTime();
                    boolean splitRequestTime = false;
                    if (requestTime.isActive()) {
                        set = set2;
                        try {
                            if (((LocationRequestStatistics) this.mLocationRequestStatistics).statistics.containsKey(key) && ((LocationRequestStatistics.PackageStatistics) this.mLocationRequestStatistics.statistics.get(key)).isActive()) {
                                splitRequestTime = true;
                                requestTime.stopRequest();
                            }
                        } catch (JSONException e) {
                            e = e;
                            if (mDebug) {
                                Log.e(TAG, e.toString());
                            }
                            e.printStackTrace();
                            set2 = set;
                        }
                    } else {
                        set = set2;
                    }
                    requestDuration += receiverRequestTime.getRequestDuration();
                    if (splitRequestTime) {
                        requestTime.restart();
                        if (mDebug) {
                            Log.d(TAG, hash + " will be saved ");
                        }
                    }
                    requestTimeSet = requestTimeSet;
                    set2 = set;
                }
                packageJsonObject.put(REQUEST_DURATION, requestDuration);
                requestStatistics.reset();
                boolean needSave = requestStatistics.mRequestTimeMap.size() > 0;
                if ("gps".equals(key.providerName)) {
                    gnssRequestList.add(packageJsonObject);
                } else if ("network".equals(key.providerName)) {
                    nlpRequestList.add(packageJsonObject);
                } else if ("fused".equals(key.providerName)) {
                    fusedRequestList.add(packageJsonObject);
                }
                if (needSave) {
                    savedRequest.put(key, requestStatistics);
                }
                set2 = set2;
            } catch (JSONException e2) {
                e = e2;
                set = set2;
                if (mDebug) {
                }
                e.printStackTrace();
                set2 = set;
            }
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(GNSS_LOCATION_REQUEST, getRequests(gnssRequestList));
            jsonObject.put(NLP_LOCATION_REQUEST, getRequests(nlpRequestList));
            jsonObject.put(FUSED_LOCATION_REQUEST, getRequests(fusedRequestList));
        } catch (JSONException e3) {
            if (mDebug) {
                Log.e(TAG, e3.toString());
            }
            e3.printStackTrace();
        }
        result.putString(CMD_LOCATION_REQUEST_INFO, jsonObject.toString());
        if (mDebug) {
            Log.i(TAG, jsonObject.toString());
        }
        this.mRequestStatistics.clear();
        this.mRequestStatistics.putAll(savedRequest);
        if (mDebug) {
            for (RequestStatistics requestStatistics2 : this.mRequestStatistics.values()) {
                Iterator<String> it = requestStatistics2.mRequestTimeMap.keySet().iterator();
                while (it.hasNext()) {
                    Log.d(TAG, it.next() + " has been saved");
                }
            }
        }
    }

    private JSONArray getRequests(List<JSONObject> requests) throws JSONException {
        JSONArray gnssArray = new JSONArray();
        int reportedCount = Math.min((int) LOCATION_REQUEST_REPORTED_COUNT, requests.size());
        for (int i = 0; i < reportedCount; i++) {
            JSONObject target = requests.get(0);
            for (JSONObject object : requests) {
                if (object.getLong(REQUEST_DURATION) > target.getLong(REQUEST_DURATION)) {
                    target = object;
                }
            }
            gnssArray.put(target);
            requests.remove(target);
        }
        return gnssArray;
    }

    private void fillGnssData(Bundle result) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(GNSS_TOTAL_COUNT, this.mGnssShouldReportedCount.intValue());
            jsonObject.put(GNSS_ACTIVE_DURATION, this.mGnssActiveDuration.longValue());
            fillDistributionData(jsonObject, GNSS_REQUEST_DISTRIBUTION_NAMES, 0, 0);
            jsonObject.put(GNSS_POWER_SAVE_LIST, getGnssPowerSaveData());
            jsonObject.put(GNSS_WAKE_LOCK_LIST, getGnssWakeLockData());
        } catch (JSONException e) {
            if (mDebug) {
                Log.e(TAG, e.toString());
            }
            e.printStackTrace();
        }
        result.putString(CMD_GNSS_DATA, jsonObject.toString());
        if (mDebug) {
            Log.i(TAG, jsonObject.toString());
        }
        this.mGnssShouldReportedCount.set(0);
        this.mGnssActiveDuration.set(0);
        mDataDistributions[0].resetDistributions(0);
    }

    private JSONArray getGnssWakeLockData() {
        HashMap<String, WakelockData> savedWakelock = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        synchronized (this.mLock) {
            for (String key : this.mGnssWakeLockMap.keySet()) {
                WakelockData data = this.mGnssWakeLockMap.get(key);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(WAKE_LOCK_NAME, key);
                    jsonObject.put(WAKE_LOCK_HELD_COUNT, data.getHeldCount());
                    jsonObject.put(WAKE_LOCK_RELEASED_COUNT, data.getReleasedCount());
                    jsonObject.put(WAKE_LOCK_DURATION, data.getDuration());
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    if (mDebug) {
                        Log.e(TAG, e.toString());
                    }
                    e.printStackTrace();
                }
                if (!data.isFinished()) {
                    savedWakelock.put(key, data);
                }
            }
            if (mDebug) {
                Log.i(TAG, jsonArray.toString());
            }
            this.mGnssWakeLockMap.clear();
            this.mGnssWakeLockMap.putAll(savedWakelock);
        }
        return jsonArray;
    }

    private JSONArray getGnssPowerSaveData() {
        HashMap<Integer, PowerSaveData> savedPowerSave = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        synchronized (this.mLock) {
            for (Integer num : this.mGnssPowerSaveMap.keySet()) {
                int key = num.intValue();
                PowerSaveData data = this.mGnssPowerSaveMap.get(Integer.valueOf(key));
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(POWER_SAVE_STRATEGY, key);
                    jsonObject.put(POWER_SAVE_DURATION, data.getDuration());
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    if (mDebug) {
                        Log.e(TAG, e.toString());
                    }
                    e.printStackTrace();
                }
                if (!data.isFinished()) {
                    savedPowerSave.put(Integer.valueOf(key), data);
                }
            }
            if (mDebug) {
                Log.i(TAG, jsonArray.toString());
            }
            this.mGnssPowerSaveMap.clear();
            this.mGnssPowerSaveMap.putAll(savedPowerSave);
        }
        return jsonArray;
    }

    private void fillNlpData(Bundle result) {
        JSONArray totalArray;
        JSONArray errorArray = new JSONArray();
        for (Integer num : this.mNlpErrorMap.keySet()) {
            int key = num.intValue();
            JSONObject errorObj = new JSONObject();
            try {
                errorObj.put(ERROR_CODE, key);
                errorObj.put(ERROR_COUNT, this.mNlpErrorMap.get(Integer.valueOf(key)));
                errorArray.put(errorObj);
            } catch (JSONException e) {
                if (mDebug) {
                    Log.e(TAG, e.toString());
                }
                e.printStackTrace();
            }
        }
        synchronized (this.mWiFiLock) {
            totalArray = getNlpScanWifiDataLocked(this.mNlpScanTotalMap, this.mNlpScanSucceedMap);
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(NLP_ERROR_LIST, errorArray);
            jsonObject.put(NLP_SCAN_WIFI, totalArray);
            jsonObject.put(NLP_SUCCEED_REQUESTS, this.mNlpSucceedRequests);
            jsonObject.put(NLP_TOTAL_REQUESTS, this.mNlpTotalRequests);
            fillDistributionData(jsonObject, NLP_RESPONSE_DISTRIBUTION_NAMES, 2, 0);
        } catch (JSONException e2) {
            if (mDebug) {
                Log.e(TAG, e2.toString());
            }
            e2.printStackTrace();
        }
        result.putString(CMD_NLP_DATA, jsonObject.toString());
        if (mDebug) {
            Log.i(TAG, jsonObject.toString());
        }
        resetNlpRequest();
    }

    private JSONArray getNlpScanWifiDataLocked(Map<String, Integer> map, Map<String, Integer> succeedMap) {
        if (map == null) {
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        for (String key : map.keySet()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(NLP_PACKAGE, key);
                jsonObject.put(NLP_COUNT, map.get(key));
                jsonObject.put(NLP_SUCCEED_COUNT, succeedMap.getOrDefault(key, 0));
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                if (mDebug) {
                    Log.e(TAG, e.toString());
                }
                e.printStackTrace();
            }
        }
        map.clear();
        return jsonArray;
    }

    private void fillGeocoderData(Bundle result) {
        JSONArray errorArray = new JSONArray();
        synchronized (this.mGeocoderLock) {
            for (Integer num : this.mGeocoderErrorMap.keySet()) {
                int key = num.intValue();
                JSONObject errorObj = new JSONObject();
                try {
                    errorObj.put(ERROR_CODE, key);
                    errorObj.put(ERROR_COUNT, this.mGeocoderErrorMap.get(Integer.valueOf(key)));
                    errorArray.put(errorObj);
                } catch (JSONException e) {
                    if (mDebug) {
                        Log.e(TAG, e.toString());
                    }
                    e.printStackTrace();
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(GEOCODER_ERROR_LIST, errorArray);
            jsonObject.put(GEOCODER_TOTAL_REQUEST, this.mGeocoderTotalRequests);
            jsonObject.put(GEOCODER_SUCCEED_REQUEST, this.mGeocoderSucceedRequests);
            fillDistributionData(jsonObject, GEOCODER_RESPONSE_DISTRIBUTION_NAMES, 3, 0);
        } catch (JSONException e2) {
            if (mDebug) {
                Log.e(TAG, e2.toString());
            }
            e2.printStackTrace();
        }
        result.putString(CMD_GEOCODER_DATA, jsonObject.toString());
        if (mDebug) {
            Log.i(TAG, jsonObject.toString());
        }
        resetGeocoderRequest();
    }

    private void fillAllByForceStop(Bundle result) {
        forceStopStatistics();
        fillAll(result);
    }

    private void fillStatusData(Bundle result) {
        result.putInt(CMD_BIG_DATA_STATUS, this.mBigDataStatus);
    }

    private class NetworkChangedReceiver extends BroadcastReceiver {
        private NetworkChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                boolean z = true;
                NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(1);
                NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(0);
                OppoLocationStatistics oppoLocationStatistics = OppoLocationStatistics.this;
                if (!mWiFiNetworkInfo.isConnected() && !mMobileNetworkInfo.isConnected()) {
                    z = false;
                }
                boolean unused = oppoLocationStatistics.mNetworkConnected = z;
            }
        }
    }

    public void startRequesting(String packageName, String providerName, long intervalMs, boolean isForeground, String hash) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "startRequesting");
            }
            if (!"passive".equals(providerName)) {
                LocationRequestStatistics.PackageProviderKey key = new LocationRequestStatistics.PackageProviderKey(packageName, providerName);
                RequestStatistics requestStatistics = this.mRequestStatistics.get(key);
                if (requestStatistics == null) {
                    requestStatistics = new RequestStatistics();
                    this.mRequestStatistics.put(key, requestStatistics);
                }
                requestStatistics.startRequesting(hash, providerName, 0);
                if (this.mPsEnabled.get() && "gps".equals(providerName)) {
                    synchronized (this.mPowerSaveModeLock) {
                        RequestStatistics psRequestStatistics = this.mPsRequestStatistics.get(key);
                        if (psRequestStatistics == null) {
                            psRequestStatistics = new RequestStatistics();
                            this.mPsRequestStatistics.put(key, psRequestStatistics);
                        }
                        psRequestStatistics.startRequesting(hash, providerName, 1);
                    }
                }
            }
        }
    }

    public void stopRequesting(String packageName, String providerName, String hash) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "stopRequesting");
            }
            if (!"passive".equals(providerName)) {
                LocationRequestStatistics.PackageProviderKey key = new LocationRequestStatistics.PackageProviderKey(packageName, providerName);
                RequestStatistics requestStatistics = this.mRequestStatistics.get(key);
                if (requestStatistics != null) {
                    requestStatistics.stopRequesting(hash);
                }
                if (this.mPsEnabled.get() && "gps".equals(providerName)) {
                    synchronized (this.mPowerSaveModeLock) {
                        RequestStatistics psRequestStatistics = this.mPsRequestStatistics.get(key);
                        if (psRequestStatistics != null) {
                            psRequestStatistics.stopRequesting(hash);
                        }
                    }
                }
            }
        }
    }

    public void recordGnssNavigatingStarted(long interval) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordGnssNavigatingStarted interval = " + interval);
            }
            if (interval < 1000) {
                interval = 1000;
            }
            boolean updateInterval = this.mFixInterval.longValue() != interval;
            if (this.mGnssStartedElapsedTime.longValue() < 0) {
                this.mGnssStartedElapsedTime.set(SystemClock.elapsedRealtime());
                if (updateInterval) {
                    this.mFixInterval.set(interval);
                }
            } else if (updateInterval) {
                long currentTime = SystemClock.elapsedRealtime();
                long duration = currentTime - this.mGnssStartedElapsedTime.longValue();
                this.mGnssStartedElapsedTime.set(currentTime);
                if (duration > 0) {
                    this.mGnssActiveDuration.addAndGet(duration);
                    this.mGnssShouldReportedCount.addAndGet((int) (duration / this.mFixInterval.longValue()));
                }
                this.mFixInterval.set(interval);
            }
            if (this.mPsEnabled.get() && this.mPsGnssStartedElapsedTime.longValue() < 0) {
                this.mPsGnssStartedElapsedTime.set(SystemClock.elapsedRealtime());
            }
        }
    }

    public void recordGnssNavigatingStopped() {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordGnssNavigatingStopped");
            }
            if (this.mGnssStartedElapsedTime.longValue() >= 0) {
                long duration = SystemClock.elapsedRealtime() - this.mGnssStartedElapsedTime.longValue();
                this.mGnssStartedElapsedTime.set(-1);
                if (duration > 0) {
                    this.mGnssActiveDuration.addAndGet(duration);
                    this.mGnssShouldReportedCount.addAndGet((int) (duration / this.mFixInterval.longValue()));
                    if (this.mPsEnabled.get() && this.mPsGnssStartedElapsedTime.longValue() > 0) {
                        this.mPsGnssActiveDuration.addAndGet(SystemClock.elapsedRealtime() - this.mPsGnssStartedElapsedTime.longValue());
                        this.mPsGnssStartedElapsedTime.set(-1);
                    }
                }
            }
        }
    }

    public void recordGnssPowerSaveStarted(int strategyCode) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordGnssPowerSaveStarted");
            }
            synchronized (this.mLock) {
                if (this.mGnssPowerSaveMap.containsKey(Integer.valueOf(strategyCode))) {
                    this.mGnssPowerSaveMap.get(Integer.valueOf(strategyCode)).startPowerSave();
                } else {
                    PowerSaveData data = new PowerSaveData();
                    data.startPowerSave();
                    this.mGnssPowerSaveMap.put(Integer.valueOf(strategyCode), data);
                }
            }
            if (this.mPsEnabled.get()) {
                synchronized (this.mPowerSaveModeLock) {
                    if (this.mPsGnssPowerSaveMap.containsKey(Integer.valueOf(strategyCode))) {
                        this.mPsGnssPowerSaveMap.get(Integer.valueOf(strategyCode)).startPowerSave();
                    } else {
                        PowerSaveData data2 = new PowerSaveData();
                        data2.startPowerSave();
                        this.mPsGnssPowerSaveMap.put(Integer.valueOf(strategyCode), data2);
                    }
                }
            }
        }
    }

    public void recordGnssPowerSaveStopped(int strategyCode) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordGnssPowerSaveStopped");
            }
            synchronized (this.mLock) {
                PowerSaveData powerSaveData = this.mGnssPowerSaveMap.get(Integer.valueOf(strategyCode));
                if (powerSaveData != null) {
                    powerSaveData.stopPowerSave();
                }
            }
            if (this.mPsEnabled.get()) {
                synchronized (this.mPowerSaveModeLock) {
                    PowerSaveData powerSaveData2 = this.mPsGnssPowerSaveMap.get(Integer.valueOf(strategyCode));
                    if (powerSaveData2 != null) {
                        powerSaveData2.stopPowerSave();
                    }
                }
            }
        }
    }

    public void recordNlpNavigatingStarted() {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordNlpNavigatingStarted");
            }
            if (!this.mNetworkConnected) {
                recordNlpError(LOCATION_REQUEST_REPORTED_COUNT);
                return;
            }
            long currentTime = SystemClock.elapsedRealtime();
            this.mNlpTotalRequests++;
            long j = this.mNlpStartedElapsedTime;
            if (j < 0) {
                this.mNlpStartedElapsedTime = currentTime;
            } else if (currentTime - j > 1000) {
                recordNlpError(2);
                this.mNlpStartedElapsedTime = currentTime;
            } else {
                recordNlpError(4);
            }
            if (this.mPsEnabled.get()) {
                this.mPsNlpTotalRequests.incrementAndGet();
            }
        }
    }

    public void recordNlpNavigatingStopped() {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordNlpNavigatingStopped");
            }
            if (this.mNlpStartedElapsedTime >= 0) {
                long currentTime = SystemClock.elapsedRealtime();
                this.mNlpSucceedRequests++;
                setNlpRequestDuration(currentTime - this.mNlpStartedElapsedTime);
                this.mNlpStartedElapsedTime = -1;
            }
        }
    }

    public void recordNlpError(int code) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordNlpError: " + code);
            }
            if (this.mNlpErrorMap.containsKey(Integer.valueOf(code))) {
                this.mNlpErrorMap.put(Integer.valueOf(code), Integer.valueOf(this.mNlpErrorMap.get(Integer.valueOf(code)).intValue() + 1));
            } else {
                this.mNlpErrorMap.put(Integer.valueOf(code), 1);
            }
        }
    }

    private void setNlpRequestDuration(long duration) {
        if (mDebug) {
            Log.i(TAG, "setNlpRequestDuration: " + duration);
        }
        if (duration > 0) {
            mDataDistributions[0].setData(2, duration);
        }
    }

    private void resetNlpRequest() {
        this.mNlpStartedElapsedTime = -1;
        this.mNlpTotalRequests = 0;
        this.mNlpSucceedRequests = 0;
        this.mNlpErrorMap.clear();
        mDataDistributions[0].resetDistributions(2);
    }

    public void recordGeocoderRequestStarted() {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordGeocoderRequestStarted");
            }
            long currentTime = SystemClock.elapsedRealtime();
            this.mGeocoderTotalRequests.incrementAndGet();
            synchronized (this.mGeocoderLock) {
                if (this.mGeocoderStartedElapsedTime.longValue() < 0) {
                    this.mGeocoderStartedElapsedTime.set(currentTime);
                    this.mGeocoderRequestTime.restart();
                } else if (currentTime - this.mGeocoderStartedElapsedTime.longValue() > 1000) {
                    recordGeocoderError(2);
                    this.mGeocoderStartedElapsedTime.set(currentTime);
                    this.mGeocoderRequestTime.restart();
                } else {
                    recordGeocoderError(4);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0045, code lost:
        return;
     */
    public void recordGeocoderRequestStopped() {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordGeocoderRequestStopped");
            }
            synchronized (this.mGeocoderLock) {
                if (this.mGeocoderStartedElapsedTime.longValue() >= 0) {
                    if (!this.mGeocoderRequestTime.isStopped()) {
                        this.mGeocoderRequestTime.stopRequest();
                        setGeocoderRequestDuration(this.mGeocoderRequestTime.getDurationMs());
                        this.mGeocoderSucceedRequests.incrementAndGet();
                        this.mGeocoderStartedElapsedTime.set(-1);
                    }
                }
            }
        }
    }

    public void recordGeocoderError(int code) {
        if (!this.mBigDataSwitchedOff) {
            synchronized (this.mGeocoderLock) {
                if (this.mGeocoderErrorMap.containsKey(Integer.valueOf(code))) {
                    this.mGeocoderErrorMap.put(Integer.valueOf(code), Integer.valueOf(this.mGeocoderErrorMap.get(Integer.valueOf(code)).intValue() + 1));
                } else {
                    this.mGeocoderErrorMap.put(Integer.valueOf(code), 1);
                }
            }
        }
    }

    private void setGeocoderRequestDuration(long duration) {
        if (mDebug) {
            Log.i(TAG, "setGeocoderRequestDuration: " + duration);
        }
        if (duration >= 0) {
            mDataDistributions[0].setData(3, duration);
        }
    }

    private void resetGeocoderRequest() {
        this.mGeocoderTotalRequests.set(0);
        this.mGeocoderSucceedRequests.set(0);
        synchronized (this.mGeocoderLock) {
            this.mGeocoderStartedElapsedTime.set(-1);
            this.mGeocoderErrorMap.clear();
            this.mGeocoderRequestTime.stopRequest();
        }
        mDataDistributions[0].resetDistributions(3);
    }

    public void recordHeldWakelock(String name) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordHeldWakelock");
            }
            if (!TextUtils.isEmpty(name)) {
                synchronized (this.mLock) {
                    WakelockData wakelockData = this.mGnssWakeLockMap.get(name);
                    if (wakelockData == null) {
                        wakelockData = new WakelockData();
                        this.mGnssWakeLockMap.put(name, wakelockData);
                    }
                    wakelockData.acquire();
                }
                if (this.mPsEnabled.get()) {
                    synchronized (this.mPowerSaveModeLock) {
                        WakelockData wakelockData2 = this.mPsGnssWakeLockMap.get(name);
                        if (wakelockData2 == null) {
                            wakelockData2 = new WakelockData();
                            this.mPsGnssWakeLockMap.put(name, wakelockData2);
                        }
                        wakelockData2.acquire();
                    }
                }
            }
        }
    }

    public void recordReleaseWakelock(String name) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordReleaseWakelock");
            }
            if (!TextUtils.isEmpty(name)) {
                synchronized (this.mLock) {
                    WakelockData wakelockData = this.mGnssWakeLockMap.get(name);
                    if (wakelockData != null) {
                        wakelockData.release();
                    }
                }
                if (this.mPsEnabled.get()) {
                    synchronized (this.mPowerSaveModeLock) {
                        WakelockData wakelockData2 = this.mPsGnssWakeLockMap.get(name);
                        if (wakelockData2 != null) {
                            wakelockData2.release();
                        }
                    }
                }
            }
        }
    }

    public void forceStopStatistics() {
        if (mDebug) {
            Log.i(TAG, "forceStopStatistics");
        }
        for (RequestStatistics requestStatistics : this.mRequestStatistics.values()) {
            requestStatistics.forceStop();
        }
        recordGnssNavigatingStopped();
        synchronized (this.mLock) {
            for (WakelockData wakelockData : this.mGnssWakeLockMap.values()) {
                wakelockData.release();
            }
            for (PowerSaveData powerSaveData : this.mGnssPowerSaveMap.values()) {
                powerSaveData.stopPowerSave();
            }
        }
        recordNlpNavigatingStopped();
        recordGeocoderRequestStopped();
    }

    public void recordNlpScanWifiTotal(String packageName) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordNlpScanWifiTotal: " + packageName);
            }
            if (!TextUtils.isEmpty(packageName)) {
                if (NLP_PACKAGE_TENCENT.equals(packageName) || NLP_PACKAGE_BAIDU.equals(packageName) || NLP_PACKAGE_AMAP.equals(packageName) || NLP_PACKAGE_GMS.equals(packageName) || NLP_PACKAGE_QUALCOMM.equals(packageName)) {
                    synchronized (this.mWiFiLock) {
                        if (this.mNlpScanTotalMap.containsKey(packageName)) {
                            this.mNlpScanTotalMap.put(packageName, Integer.valueOf(this.mNlpScanTotalMap.get(packageName).intValue() + 1));
                        } else {
                            this.mNlpScanTotalMap.put(packageName, 1);
                        }
                    }
                    if (this.mPsEnabled.get()) {
                        this.mPsNlpScanTotalCount.incrementAndGet();
                    }
                }
            }
        }
    }

    public void recordNlpScanWifiSucceed(String packageName) {
        if (!this.mBigDataSwitchedOff) {
            if (mDebug) {
                Log.i(TAG, "recordNlpScanWifiSucceed: " + packageName);
            }
            if (!TextUtils.isEmpty(packageName)) {
                if (NLP_PACKAGE_TENCENT.equals(packageName) || NLP_PACKAGE_BAIDU.equals(packageName) || NLP_PACKAGE_AMAP.equals(packageName) || NLP_PACKAGE_GMS.equals(packageName) || NLP_PACKAGE_QUALCOMM.equals(packageName)) {
                    synchronized (this.mWiFiLock) {
                        if (this.mNlpScanSucceedMap.containsKey(packageName)) {
                            this.mNlpScanSucceedMap.put(packageName, Integer.valueOf(this.mNlpScanSucceedMap.get(packageName).intValue() + 1));
                        } else {
                            this.mNlpScanSucceedMap.put(packageName, 1);
                        }
                    }
                    if (this.mPsEnabled.get()) {
                        this.mPsNlpScanSucceedCount.incrementAndGet();
                    }
                }
            }
        }
    }

    private static class PowerSaveData {
        private long mDuration;
        private long mStartedElapsedTime;

        private PowerSaveData() {
            this.mStartedElapsedTime = -1;
            this.mDuration = 0;
        }

        /* access modifiers changed from: private */
        public void startPowerSave() {
            if (this.mStartedElapsedTime < 0) {
                this.mStartedElapsedTime = SystemClock.elapsedRealtime();
            }
        }

        public void stopPowerSave() {
            if (this.mStartedElapsedTime > 0) {
                this.mDuration += SystemClock.elapsedRealtime() - this.mStartedElapsedTime;
                this.mStartedElapsedTime = -1;
            }
        }

        public long getDuration() {
            long activeDuration = 0;
            if (this.mStartedElapsedTime > 0) {
                activeDuration = SystemClock.elapsedRealtime() - this.mStartedElapsedTime;
                this.mStartedElapsedTime = SystemClock.elapsedRealtime();
            }
            return this.mDuration + activeDuration;
        }

        public boolean isFinished() {
            return this.mStartedElapsedTime < 0;
        }
    }

    private static class WakelockData {
        private int mActiveHeldCount;
        private long mDuration;
        private int mHeldCount;
        private int mReleasedCount;
        private long mStartedElapsedTime;

        private WakelockData() {
            this.mActiveHeldCount = 0;
            this.mHeldCount = 0;
            this.mReleasedCount = 0;
            this.mStartedElapsedTime = -1;
            this.mDuration = 0;
        }

        /* access modifiers changed from: private */
        public void acquire() {
            this.mHeldCount++;
            if (this.mActiveHeldCount == 0) {
                this.mStartedElapsedTime = SystemClock.elapsedRealtime();
            }
            this.mActiveHeldCount++;
        }

        /* access modifiers changed from: private */
        public void release() {
            this.mReleasedCount++;
            this.mActiveHeldCount--;
            if (this.mActiveHeldCount == 0) {
                long currentDuration = SystemClock.elapsedRealtime() - this.mStartedElapsedTime;
                this.mDuration += currentDuration;
                this.mStartedElapsedTime = -1;
                distribute(currentDuration);
            }
        }

        /* access modifiers changed from: private */
        public void forceRelease() {
            this.mReleasedCount++;
            this.mActiveHeldCount--;
            long currentDuration = SystemClock.elapsedRealtime() - this.mStartedElapsedTime;
            this.mDuration += currentDuration;
            this.mStartedElapsedTime = -1;
            distribute(currentDuration);
        }

        /* access modifiers changed from: private */
        public long getDuration() {
            long activeDuration = 0;
            if (this.mStartedElapsedTime > 0) {
                activeDuration = SystemClock.elapsedRealtime() - this.mStartedElapsedTime;
                this.mStartedElapsedTime = SystemClock.elapsedRealtime();
            }
            return this.mDuration + activeDuration;
        }

        private void distribute(long duration) {
            OppoLocationStatistics.mDataDistributions[0].setData(4, duration);
        }

        /* access modifiers changed from: private */
        public int getHeldCount() {
            return this.mHeldCount;
        }

        /* access modifiers changed from: private */
        public int getReleasedCount() {
            return this.mReleasedCount;
        }

        /* access modifiers changed from: private */
        public boolean isFinished() {
            return this.mStartedElapsedTime < 0 && this.mActiveHeldCount == 0;
        }
    }

    public static class RequestStatistics {
        private int mNumActiveRequests;
        private int mRepeatRequestCount;
        private int mRequestCount;
        /* access modifiers changed from: private */
        public Map<String, ReceiverRequestTime> mRequestTimeMap;

        private RequestStatistics() {
            this.mRequestTimeMap = new LinkedHashMap();
            this.mNumActiveRequests = 0;
            this.mRequestCount = 0;
            this.mRepeatRequestCount = 0;
        }

        /* access modifiers changed from: private */
        public void startRequesting(String hash, String providerName, int statisticMode) {
            if (this.mNumActiveRequests > 0) {
                this.mRepeatRequestCount++;
            }
            if (OppoLocationStatistics.mDebug) {
                Log.d(OppoLocationStatistics.TAG, "start mNumActiveRequests = " + this.mNumActiveRequests);
            }
            if (this.mNumActiveRequests == 0) {
                if (this.mRequestTimeMap.containsKey(hash)) {
                    this.mRequestTimeMap.get(hash).startRequest();
                } else {
                    this.mRequestTimeMap.put(hash, new ReceiverRequestTime(providerName, statisticMode));
                }
            }
            this.mNumActiveRequests++;
            this.mRequestCount++;
        }

        /* access modifiers changed from: private */
        public void stopRequesting(String hash) {
            int i = this.mNumActiveRequests;
            if (i > 0) {
                this.mNumActiveRequests = i - 1;
                if (OppoLocationStatistics.mDebug) {
                    Log.d(OppoLocationStatistics.TAG, "stop mNumActiveRequests = " + this.mNumActiveRequests);
                }
                if (this.mNumActiveRequests == 0 && this.mRequestTimeMap.containsKey(hash)) {
                    this.mRequestTimeMap.get(hash).stopRequest();
                }
            }
        }

        /* access modifiers changed from: private */
        public int getRequestNum() {
            return this.mRequestCount;
        }

        /* access modifiers changed from: private */
        public int getRepeatRequestNum() {
            return this.mRepeatRequestCount;
        }

        /* access modifiers changed from: private */
        public void reset() {
            this.mNumActiveRequests = 0;
            this.mRequestCount = 0;
            this.mRepeatRequestCount = 0;
            Map<String, ReceiverRequestTime> savedRequestTimes = new LinkedHashMap<>();
            for (Map.Entry<String, ReceiverRequestTime> requestTimeEntry : this.mRequestTimeMap.entrySet()) {
                requestTimeEntry.getValue().resetDuration();
                if (requestTimeEntry.getValue().getRequestTime().isActive()) {
                    savedRequestTimes.put(requestTimeEntry.getKey(), requestTimeEntry.getValue());
                }
            }
            this.mRequestTimeMap.clear();
            this.mRequestTimeMap.putAll(savedRequestTimes);
        }

        /* access modifiers changed from: private */
        public void forceStop() {
            this.mNumActiveRequests = 0;
            for (ReceiverRequestTime time : this.mRequestTimeMap.values()) {
                time.stopRequest();
            }
        }

        public boolean isActive() {
            return this.mNumActiveRequests > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ReceiverRequestTime {
        private String mProviderName;
        private long mRequestDuration = 0;
        private RequestTime mRequestTime = new RequestTime();
        private int mStatisticMode;

        ReceiverRequestTime(String providerName, int statisticMode) {
            this.mProviderName = providerName;
            this.mStatisticMode = statisticMode;
            startRequest();
        }

        /* access modifiers changed from: private */
        public void startRequest() {
            if (this.mRequestTime.isStopped()) {
                this.mRequestTime.restart();
            }
        }

        /* access modifiers changed from: private */
        public void stopRequest() {
            if (this.mRequestTime.isActive()) {
                this.mRequestTime.stopRequest();
                this.mRequestDuration += this.mRequestTime.getDurationMs();
                distribute(this.mRequestTime.getDurationMs());
            }
        }

        /* access modifiers changed from: private */
        public long getRequestDuration() {
            return this.mRequestDuration;
        }

        /* access modifiers changed from: private */
        public RequestTime getRequestTime() {
            return this.mRequestTime;
        }

        /* access modifiers changed from: private */
        public void resetDuration() {
            this.mRequestDuration = 0;
        }

        private void distribute(long duration) {
            if ("gps".equals(this.mProviderName)) {
                OppoLocationStatistics.mDataDistributions[this.mStatisticMode].setData(0, duration);
            } else if ("network".equals(this.mProviderName)) {
                OppoLocationStatistics.mDataDistributions[this.mStatisticMode].setData(1, duration);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class RequestTime {
        private long mDuration;
        private long mStartedElapsedTime;
        private long mStoppedElapsedTime;

        private RequestTime() {
            this.mStoppedElapsedTime = -1;
            this.mDuration = -1;
            this.mStartedElapsedTime = SystemClock.elapsedRealtime();
        }

        /* access modifiers changed from: private */
        public void stopRequest() {
            if (!isStopped()) {
                this.mStoppedElapsedTime = SystemClock.elapsedRealtime();
                this.mDuration += this.mStoppedElapsedTime - this.mStartedElapsedTime;
            }
        }

        /* access modifiers changed from: private */
        public boolean isStopped() {
            return this.mStoppedElapsedTime > 0;
        }

        public boolean isActive() {
            return this.mStoppedElapsedTime < 0;
        }

        public long getDurationMs() {
            return this.mDuration;
        }

        public void restart() {
            this.mStartedElapsedTime = SystemClock.elapsedRealtime();
            this.mStoppedElapsedTime = -1;
            this.mDuration = -1;
        }
    }

    private void fillDistributionData(JSONObject data, String[] names, int[] distributions) throws JSONException {
        for (int i = 0; i < names.length; i++) {
            data.put(names[i], distributions[i]);
        }
    }

    private void fillDistributionData(JSONObject data, String[] names, int distributionType, int statisticMode) throws JSONException {
        fillDistributionData(data, names, mDataDistributions[statisticMode].getDistributions(distributionType));
    }

    /* access modifiers changed from: private */
    public static class DataDistribution {
        static final int DATA_AVERAGE = 2;
        static final int DATA_BAD = 4;
        static final int DATA_EXCELLENT = 0;
        static final int DATA_GOOD = 1;
        static final int DATA_WEAK = 3;
        private static final int[][] SEPARATED_VALUES;
        static final int TYPE_GEOCODER_FLOW = 3;
        static final int TYPE_GNSS_REQ = 0;
        static final int TYPE_NLP_FLOW = 2;
        static final int TYPE_NLP_REQ = 1;
        static final int TYPE_WAKELOCK = 4;
        private int[][] mDataDistributions;

        static {
            int[][] iArr = new int[OppoLocationStatistics.LOCATION_REQUEST_REPORTED_COUNT][];
            iArr[0] = new int[]{10000, 60000, 600000, 3600000};
            iArr[1] = new int[]{OppoArpPeer.ARP_DUP_RESPONSE_TIMEOUT, 20000, 60000, 300000};
            iArr[2] = new int[]{30, 250, 550, 1000};
            iArr[3] = new int[]{30, 250, 550, 1000};
            iArr[4] = new int[]{100, 1000, 10000, 60000};
            SEPARATED_VALUES = iArr;
        }

        DataDistribution() {
            int[][] iArr = SEPARATED_VALUES;
            this.mDataDistributions = (int[][]) Array.newInstance(int.class, iArr.length, iArr[0].length + 1);
        }

        /* access modifiers changed from: private */
        public void setData(int dataType, long duration) {
            int[][] iArr = SEPARATED_VALUES;
            if (duration < ((long) iArr[dataType][1])) {
                if (duration < ((long) iArr[dataType][0])) {
                    int[] iArr2 = this.mDataDistributions[dataType];
                    iArr2[0] = iArr2[0] + 1;
                    return;
                }
                int[] iArr3 = this.mDataDistributions[dataType];
                iArr3[1] = iArr3[1] + 1;
            } else if (duration < ((long) iArr[dataType][2])) {
                int[] iArr4 = this.mDataDistributions[dataType];
                iArr4[2] = iArr4[2] + 1;
            } else if (duration < ((long) iArr[dataType][3])) {
                int[] iArr5 = this.mDataDistributions[dataType];
                iArr5[3] = iArr5[3] + 1;
            } else {
                int[] iArr6 = this.mDataDistributions[dataType];
                iArr6[4] = iArr6[4] + 1;
            }
        }

        /* access modifiers changed from: private */
        public int[] getDistributions(int dataType) {
            return this.mDataDistributions[dataType];
        }

        /* access modifiers changed from: private */
        public void resetDistributions(int dataType) {
            Arrays.fill(this.mDataDistributions[dataType], 0);
        }

        /* access modifiers changed from: private */
        public void resetAll() {
            Arrays.fill(this.mDataDistributions[0], 0);
            Arrays.fill(this.mDataDistributions[1], 0);
            Arrays.fill(this.mDataDistributions[2], 0);
            Arrays.fill(this.mDataDistributions[3], 0);
            Arrays.fill(this.mDataDistributions[4], 0);
        }
    }

    public void startPowerStatistics() {
        if (!this.mBigDataSwitchedOff) {
            if (!this.mPsEnabled.get()) {
                this.mPsEnabled.set(true);
                if (this.mGnssStartedElapsedTime.longValue() > 0) {
                    this.mPsGnssStartedElapsedTime.set(SystemClock.elapsedRealtime());
                }
                Map<Integer, PowerSaveData> psGnssPowerSaveMap = new HashMap<>();
                Map<String, WakelockData> psGnssWakeLockMap = new HashMap<>();
                synchronized (this.mLock) {
                    for (Integer num : this.mGnssPowerSaveMap.keySet()) {
                        int key = num.intValue();
                        if (!this.mGnssPowerSaveMap.get(Integer.valueOf(key)).isFinished()) {
                            PowerSaveData data = new PowerSaveData();
                            data.startPowerSave();
                            psGnssPowerSaveMap.put(Integer.valueOf(key), data);
                        }
                    }
                    for (String wakelockKey : this.mGnssWakeLockMap.keySet()) {
                        if (!this.mGnssWakeLockMap.get(wakelockKey).isFinished()) {
                            WakelockData data2 = new WakelockData();
                            data2.acquire();
                            psGnssWakeLockMap.put(wakelockKey, data2);
                        }
                    }
                }
                synchronized (this.mPowerSaveModeLock) {
                    this.mPsGnssPowerSaveMap.putAll(psGnssPowerSaveMap);
                    this.mPsGnssWakeLockMap.putAll(psGnssWakeLockMap);
                }
            }
            Log.i(TAG, "startPowerStatistics");
        }
    }

    public String collectPowerStatistics() {
        if (this.mBigDataSwitchedOff) {
            return StringUtils.EMPTY;
        }
        JSONObject resultObj = new JSONObject();
        try {
            resultObj.put(PS_NLP_SCAN_WIFI_SUCCEED, this.mPsNlpScanSucceedCount.intValue());
            resultObj.put(PS_NLP_SCAN_WIFI_TOTAL, this.mPsNlpScanTotalCount.intValue());
            resultObj.put(PS_NLP_TOTAL_REQUESTS, this.mPsNlpTotalRequests.intValue());
            resultObj.put(PS_GNSS_ACTIVE_DURATION, this.mPsGnssActiveDuration.longValue());
        } catch (JSONException e) {
            if (mDebug) {
                Log.e(TAG, e.toString());
            }
            e.printStackTrace();
        }
        fillPsPowerSaveData(resultObj);
        fillPsWakelockData(resultObj);
        fillPsLocationRequestData(resultObj);
        Log.i(TAG, "collectPowerStatistics " + this.mPsEnabled);
        if (mDebug) {
            Log.d(TAG, resultObj.toString());
        }
        return resultObj.toString();
    }

    public void stopPowerStatistics() {
        if (!this.mBigDataSwitchedOff && this.mPsEnabled.get()) {
            this.mPsEnabled.set(true);
            if (this.mPsGnssStartedElapsedTime.longValue() > 0) {
                this.mPsGnssActiveDuration.addAndGet(SystemClock.elapsedRealtime() - this.mPsGnssStartedElapsedTime.longValue());
            }
            synchronized (this.mPowerSaveModeLock) {
                for (PowerSaveData powerSaveData : this.mPsGnssPowerSaveMap.values()) {
                    powerSaveData.stopPowerSave();
                }
                for (WakelockData wakelockData : this.mPsGnssWakeLockMap.values()) {
                    wakelockData.forceRelease();
                }
                for (RequestStatistics requestStatistics : this.mPsRequestStatistics.values()) {
                    requestStatistics.forceStop();
                }
            }
        }
    }

    private void fillPsPowerSaveData(JSONObject resultObj) {
        JSONArray powerSaveArray = new JSONArray();
        synchronized (this.mPowerSaveModeLock) {
            for (Integer num : this.mPsGnssPowerSaveMap.keySet()) {
                int key = num.intValue();
                PowerSaveData data = this.mPsGnssPowerSaveMap.get(Integer.valueOf(key));
                try {
                    JSONObject powerSaveObj = new JSONObject();
                    powerSaveObj.put(PS_POWER_SAVE_STRATEGY, key);
                    powerSaveObj.put(PS_POWER_SAVE_DURATION, data.getDuration());
                    powerSaveArray.put(powerSaveObj);
                } catch (JSONException e) {
                    if (mDebug) {
                        Log.e(TAG, e.toString());
                    }
                    e.printStackTrace();
                }
            }
        }
        if (mDebug) {
            Log.i(TAG, powerSaveArray.toString());
        }
        try {
            resultObj.put(PS_POWER_SAVE_DATA, powerSaveArray);
        } catch (JSONException e2) {
            if (mDebug) {
                Log.e(TAG, e2.toString());
            }
            e2.printStackTrace();
        }
    }

    private void fillPsWakelockData(JSONObject resultObj) {
        try {
            fillDistributionData(resultObj, PS_WAKELOCK_DISTRIBUTION_NAMES, 4, 1);
        } catch (JSONException e) {
            if (mDebug) {
                Log.e(TAG, e.toString());
            }
            e.printStackTrace();
        }
    }

    private void fillPsLocationRequestData(JSONObject resultObj) {
        JSONArray requestArray = new JSONArray();
        synchronized (this.mPowerSaveModeLock) {
            for (LocationRequestStatistics.PackageProviderKey key : this.mPsRequestStatistics.keySet()) {
                RequestStatistics requestStatistics = this.mPsRequestStatistics.get(key);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(PS_REQUEST_PACKAGE, key.packageName);
                    jsonObject.put(PS_REQUEST_COUNT, requestStatistics.getRequestNum() + requestStatistics.getRepeatRequestNum());
                } catch (JSONException e) {
                    if (mDebug) {
                        Log.e(TAG, e.toString());
                    }
                    e.printStackTrace();
                }
                requestStatistics.reset();
                requestArray.put(jsonObject);
            }
        }
        try {
            resultObj.put(PS_REQUEST_ARRAY, requestArray);
            fillDistributionData(resultObj, PS_GNSS_REQUEST_DISTRIBUTION_NAMES, 0, 1);
        } catch (JSONException e2) {
            if (mDebug) {
                Log.e(TAG, e2.toString());
            }
            e2.printStackTrace();
        }
    }

    private void resetPsField() {
        this.mPsNlpScanSucceedCount.set(0);
        this.mPsNlpScanTotalCount.set(0);
        this.mPsNlpTotalRequests.set(0);
        this.mPsGnssStartedElapsedTime.set(-1);
        this.mPsGnssActiveDuration.set(0);
        synchronized (this.mPowerSaveModeLock) {
            this.mPsGnssPowerSaveMap.clear();
            this.mPsGnssWakeLockMap.clear();
            this.mPsRequestStatistics.clear();
        }
        mDataDistributions[1].resetAll();
    }

    public void resetPowerStatistics() {
        if (!this.mBigDataSwitchedOff) {
            resetPsField();
        }
    }
}
