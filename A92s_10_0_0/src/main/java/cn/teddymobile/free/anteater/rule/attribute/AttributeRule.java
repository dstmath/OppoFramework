package cn.teddymobile.free.anteater.rule.attribute;

import android.view.View;
import org.json.JSONException;
import org.json.JSONObject;

public interface AttributeRule {
    JSONObject extractAttribute(View view);

    void loadFromJSON(JSONObject jSONObject) throws JSONException;
}
