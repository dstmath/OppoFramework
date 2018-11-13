package com.oppo.statistics.agent;

import android.content.Context;
import com.oppo.statistics.data.CommonBean;
import com.oppo.statistics.record.RecordHandler;
import com.oppo.statistics.util.LogUtil;
import java.util.Map;
import org.json.JSONObject;

public class CommonAgent {
    public static void recordCommon(Context context, String logTag, String eventID, Map<String, String> logMap) {
        RecordHandler.addTask(context, new CommonBean(logTag, eventID, getCommonObject(logMap).toString()));
    }

    public static void recordCommon(Context context, String logTag, String eventID, Map<String, String> logMap, boolean uploadNow) {
        RecordHandler.addTask(context, new CommonBean(logTag, eventID, getCommonObject(logMap).toString(), uploadNow));
    }

    public static JSONObject getCommonObject(Map<String, String> logMap) {
        if (logMap == null || logMap.isEmpty()) {
            return new JSONObject();
        }
        JSONObject jsonObject = new JSONObject();
        try {
            for (String key : logMap.keySet()) {
                jsonObject.put(key, logMap.get(key));
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return jsonObject;
    }
}
