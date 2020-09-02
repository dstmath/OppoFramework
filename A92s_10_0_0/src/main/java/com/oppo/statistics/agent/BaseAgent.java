package com.oppo.statistics.agent;

import com.oppo.statistics.util.LogUtil;
import java.util.Map;
import org.json.JSONObject;

public class BaseAgent {
    public static JSONObject map2JsonObject(Map<String, String> logMap) {
        JSONObject jsonObject = new JSONObject();
        if (logMap == null || logMap.isEmpty()) {
            return jsonObject;
        }
        try {
            for (String key : logMap.keySet()) {
                jsonObject.put(key, logMap.get(key));
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return jsonObject;
    }
}
