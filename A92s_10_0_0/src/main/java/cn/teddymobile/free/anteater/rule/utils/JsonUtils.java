package cn.teddymobile.free.anteater.rule.utils;

import cn.teddymobile.free.anteater.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {
    private static final String TAG = JsonUtils.class.getSimpleName();

    public static String getValue(String json, String jsonPattern) {
        if (json == null || jsonPattern == null) {
            return null;
        }
        try {
            String[] keys = jsonPattern.split("#");
            boolean match = true;
            int length = keys.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String key = keys[i];
                JSONObject object = new JSONObject(json);
                if (!object.has(key)) {
                    match = false;
                    break;
                }
                json = String.valueOf(object.get(key));
                i++;
            }
            if (match) {
                return json;
            }
            return null;
        } catch (JSONException e) {
            Logger.w(TAG, e.getMessage(), e);
            return null;
        }
    }
}
