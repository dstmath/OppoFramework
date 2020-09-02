package com.android.server.wifi;

import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class OppoWifiSauStatistics {
    private static boolean DBG = true;
    private static final String KEY_PUSH_FAIL = "wcn_update_fail";
    private static final String KEY_PUSH_SUCESS = "wcn_update_sucess";
    private static final String TAG = "OppoWifiSauStatistics";
    private static final String WIFI_STATISTIC = "wifi_fool_proof";
    private Context mContext;

    public OppoWifiSauStatistics(Context c) {
        this.mContext = c;
    }

    public void sauPushSucess(String platform, String type, String name, String effectMethod, String reason, String pushTime) {
        HashMap<String, String> logMap = new HashMap<>();
        logMap.put("platform", platform);
        logMap.put("type", type);
        logMap.put("name", name);
        logMap.put("effectMethod", effectMethod);
        logMap.put("reason", reason);
        logMap.put("pushTime", pushTime);
        if (DBG) {
            Log.d(TAG, " onCommon eventId = wcn_update_sucess, logMap = " + logMap);
        }
        OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC, KEY_PUSH_SUCESS, logMap, false);
    }

    public void sauPushFailed(String platform, String type, String name, String effectMethod, String reason, String pushTime, String pushfail) {
        HashMap<String, String> logMap = new HashMap<>();
        logMap.put("platform", platform);
        logMap.put("type", type);
        logMap.put("name", name);
        logMap.put("effectMethod", effectMethod);
        logMap.put("reason", reason);
        logMap.put("pushTime", pushTime);
        logMap.put("pushFailReason", pushfail);
        if (DBG) {
            Log.d(TAG, " onCommon eventId = wcn_update_fail, logMap = " + logMap);
        }
        OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC, KEY_PUSH_FAIL, logMap, false);
    }
}
