package cn.teddymobile.free.anteater.rule.trigger;

import android.view.View;
import org.json.JSONException;
import org.json.JSONObject;

public interface TriggerRule {
    boolean fitAction(String str, View view);

    void loadFromJSON(JSONObject jSONObject) throws JSONException;
}
