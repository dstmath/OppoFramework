package com.oppo.statistics.agent;

import android.content.Context;
import android.text.TextUtils;
import com.oppo.statistics.NearMeStatistics;
import com.oppo.statistics.data.AppLogBean;
import com.oppo.statistics.data.DynamicEventBean;
import com.oppo.statistics.data.StaticEventBean;
import com.oppo.statistics.record.RecordHandler;
import com.oppo.statistics.storage.PreferenceHandler;
import com.oppo.statistics.util.LogUtil;
import com.oppo.statistics.util.TimeInfoUtil;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class OnEventAgent {
    private static final String EVENT_ID = "eventid";

    public static void onEvent(Context context, String eventID, String eventTag, int eventCount, long duration) {
        recordEvent(context, eventID, eventTag, eventCount, TimeInfoUtil.getFormatTime(), duration);
    }

    public static void onEventStart(Context context, String eventID, String eventTag) {
        PreferenceHandler.setEventStart(context, eventID, eventTag, TimeInfoUtil.getCurrentTime());
    }

    public static void onEventEnd(Context context, String eventID, String eventTag) {
        recordEventEnd(context, eventID, eventTag, TimeInfoUtil.getCurrentTime());
    }

    public static void onKVEvent(Context context, String eventID, Map<String, String> eventMap, long duration) {
        recordKVEvent(context, eventID, eventMap, TimeInfoUtil.getFormatTime(), duration);
    }

    public static void onDynamicEvent(Context context, int uploadMode, int statId, Map<String, String> infoMap, Map<String, String> eventMap) {
        recordDynamicEvent(context, uploadMode, statId, TimeInfoUtil.getFormatTime(), infoMap, eventMap);
    }

    public static void onStaticEvent(Context context, int uploadMode, int statId, String setId, String setValue, String remark, Map<String, String> eventMap) {
        recordStaticLog(context, uploadMode, statId, TimeInfoUtil.getFormatTime(), setId, setValue, remark, eventMap);
    }

    public static void onKVEventStart(Context context, String eventID, Map<String, String> eventMap, String eventTag) {
        long startTime = TimeInfoUtil.getCurrentTime();
        PreferenceHandler.setKVEventStart(context, eventID, getKVEventObject(eventID, eventMap, TimeInfoUtil.getFormatTime(startTime), startTime).toString(), eventTag);
    }

    public static void onKVEventEnd(Context context, String eventID, String eventTag) {
        recordKVEventEnd(context, eventID, eventTag, TimeInfoUtil.getCurrentTime());
    }

    public static void recordEvent(Context context, String eventID, String eventTag, int eventCount, String eventTime, long duration) {
        recordAppLog(context, "event", getEventObject(eventID, eventTag, eventCount, eventTime, duration));
    }

    public static void recordEventEnd(Context context, String eventID, String eventTag, long currentTime) {
        try {
            long startTime = PreferenceHandler.getEventStart(context, eventID, eventTag);
            String eventTime = TimeInfoUtil.getFormatTime(startTime);
            long duration = currentTime - startTime;
            if (duration > TimeInfoUtil.MILLISECOND_OF_A_WEEK || duration < 0) {
                PreferenceHandler.setEventStart(context, eventID, eventTag, 0);
                return;
            }
            NearMeStatistics.onEvent(context, eventID, eventTag, 1, duration);
            PreferenceHandler.setEventStart(context, eventID, eventTag, 0);
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void recordKVEvent(Context context, String eventID, Map<String, String> eventMap, String eventTime, long duration) {
        recordAppLog(context, "ekv", getKVEventObject(eventID, eventMap, eventTime, duration));
    }

    public static void recordKVEventEnd(Context context, String eventID, String eventTag, long currentTime) {
        Throwable e;
        try {
            String jsonStr = PreferenceHandler.getKVEventStart(context, eventID, eventTag);
            if (!TextUtils.isEmpty(jsonStr)) {
                JSONObject jsonEvent = new JSONObject(jsonStr);
                try {
                    long duration = currentTime - jsonEvent.getLong("duration");
                    if (duration > TimeInfoUtil.MILLISECOND_OF_A_WEEK || duration < 0) {
                        PreferenceHandler.setKVEventStart(context, eventID, "", eventTag);
                        return;
                    }
                    Map<String, String> logMap = new HashMap();
                    if (eventTag != null) {
                        logMap.put(EVENT_ID, eventTag);
                    }
                    NearMeStatistics.onKVEvent(context, eventID, logMap, duration);
                    PreferenceHandler.setKVEventStart(context, eventID, "", eventTag);
                    JSONObject jSONObject = jsonEvent;
                } catch (Exception e2) {
                    e = e2;
                    LogUtil.e("NearMeStatistics", e);
                }
            }
        } catch (Exception e3) {
            e = e3;
            LogUtil.e("NearMeStatistics", e);
        }
    }

    private static void recordAppLog(Context context, String type, JSONObject jsonEvent) {
        RecordHandler.addTask(context, new AppLogBean(type, jsonEvent.toString()));
    }

    public static void recordDynamicEvent(Context context, int uploadMode, int statId, String eventTime, Map<String, String> infoMap, Map<String, String> eventMap) {
        recordDynamicEventLog(context, uploadMode, getDynamicEventObject(statId, eventTime, infoMap, eventMap));
    }

    public static void recordStaticLog(Context context, int uploadMode, int statId, String eventTime, String setId, String setValue, String remark, Map<String, String> eventMap) {
        recordStaticLog(context, uploadMode, getStaticLogObject(statId, eventTime, setId, setValue, remark, eventMap));
    }

    private static void recordDynamicEventLog(Context context, int uploadMode, JSONObject jsonEvent) {
        RecordHandler.addTask(context, new DynamicEventBean(uploadMode, jsonEvent.toString()));
    }

    private static void recordStaticLog(Context context, int uploadMode, JSONObject jsonEvent) {
        RecordHandler.addTask(context, new StaticEventBean(uploadMode, jsonEvent.toString()));
    }

    public static JSONObject getEventObject(String eventID, String eventTag, int eventCount, String eventTime, long duration) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eventID", eventID);
            jsonObject.put("eventCount", eventCount);
            jsonObject.put("eventTime", eventTime);
            if (!TextUtils.isEmpty(eventTag)) {
                jsonObject.put("eventTag", eventTag);
            }
            if (duration != 0) {
                jsonObject.put("duration", duration);
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return jsonObject;
    }

    public static JSONObject getKVEventObject(String eventID, Map<String, String> eventMap, String eventTime, long duration) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eventID", eventID);
            jsonObject.put("eventTime", eventTime);
            if (duration > 0) {
                jsonObject.put("duration", duration);
            }
            if (eventMap != null && eventMap.size() > 0) {
                for (String key : eventMap.keySet()) {
                    jsonObject.put(key, eventMap.get(key));
                }
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return jsonObject;
    }

    public static JSONObject getDynamicEventObject(int statId, String eventTime, Map<String, String> infoMap, Map<String, String> eventMap) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("statID", statId);
            jsonObject.put("clientTime", eventTime);
            getDynamicInfo(jsonObject, infoMap);
            getKVEventInfo(jsonObject, eventMap);
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return jsonObject;
    }

    public static JSONObject getStaticLogObject(int statId, String eventTime, String setId, String setValue, String remark, Map<String, String> eventMap) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("statID", statId);
            jsonObject.put("clientTime", eventTime);
            jsonObject.put("setID", setId);
            jsonObject.put("setValue", setValue);
            if (!TextUtils.isEmpty(remark)) {
                jsonObject.put("remark", remark);
            }
            getKVEventInfo(jsonObject, eventMap);
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return jsonObject;
    }

    private static void getKVEventInfo(JSONObject jsonObject, Map<String, String> eventMap) {
        if (eventMap != null && eventMap.size() != 0) {
            JSONObject json = new JSONObject();
            try {
                for (String key : eventMap.keySet()) {
                    json.put(key, eventMap.get(key));
                }
                String info = json.toString().replaceAll("\"", "");
                jsonObject.put("eventInfo", info.substring(1, info.length() - 1));
            } catch (Throwable e) {
                LogUtil.e("NearMeStatistics", e);
            }
        }
    }

    private static void getDynamicInfo(JSONObject jsonObject, Map<String, String> infoMap) {
        if (infoMap != null && infoMap.size() != 0) {
            try {
                for (String key : infoMap.keySet()) {
                    jsonObject.put(key, infoMap.get(key));
                }
            } catch (Throwable e) {
                LogUtil.e("NearMeStatistics", e);
            }
        }
    }
}
