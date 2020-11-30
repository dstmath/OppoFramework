package com.oppo.statistics.agent;

import android.content.Context;
import com.oppo.statistics.data.PeriodDataBean;
import com.oppo.statistics.data.SettingKeyBean;
import com.oppo.statistics.data.SettingKeyDataBean;
import com.oppo.statistics.record.RecordHandler;
import com.oppo.statistics.util.LogUtil;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class StaticPeriodDataRecord extends BaseAgent {
    public static void updateData(Context context, String logTag, String eventID, Map<String, String> logMap) {
        RecordHandler.addTask(context, new PeriodDataBean(logTag, eventID, map2JsonObject(logMap).toString()));
    }

    public static void updateSettingKey(Context context, String logTag, String eventID, Map<String, String> keyMap) {
        RecordHandler.addTask(context, new SettingKeyDataBean(logTag, eventID, map2JsonObject(keyMap).toString()));
    }

    public static void updateSettingKeyList(Context context, String logTag, String eventID, List<SettingKeyBean> list) {
        RecordHandler.addTask(context, new SettingKeyDataBean(logTag, eventID, list2JsonObject(list).toString()));
    }

    public static JSONArray list2JsonObject(List<SettingKeyBean> list) {
        JSONArray jsonArray = new JSONArray();
        if (list == null || list.isEmpty()) {
            return jsonArray;
        }
        try {
            for (SettingKeyBean settingKeyBean : list) {
                if (settingKeyBean != null) {
                    JSONObject beanJson = new JSONObject();
                    beanJson.put(SettingKeyBean.SETTING_KEY, settingKeyBean.getSettingKey());
                    beanJson.put(SettingKeyBean.HTTP_POST_KEY, settingKeyBean.getHttpPostKey());
                    beanJson.put(SettingKeyBean.METHOD_NAME, settingKeyBean.getMethodName());
                    beanJson.put(SettingKeyBean.DEFAULE_VALUE, settingKeyBean.getDefaultValue());
                    jsonArray.put(beanJson);
                }
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
        return jsonArray;
    }
}
