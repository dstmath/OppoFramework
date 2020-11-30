package cn.teddymobile.free.anteater.rule.attribute.intent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.attribute.AttributeRule;
import cn.teddymobile.free.anteater.rule.utils.ViewHierarchyUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IntentRule implements AttributeRule {
    private static final String JSON_FIELD_ATTRIBUTE_LIST = "attribute_list";
    private static final String JSON_FIELD_DATA_LIST = "data_list";
    private static final String JSON_FIELD_EXTRA_LIST = "extra_list";
    private static final String TAG = IntentRule.class.getSimpleName();
    private List<Attribute> mAttributeList;
    private List<Data> mDataList;
    private List<Extra> mExtraList;

    public String toString() {
        return "[IntentRule] ExtraList = " + this.mExtraList.toString() + "\nAttributeList = " + this.mAttributeList.toString();
    }

    @Override // cn.teddymobile.free.anteater.rule.attribute.AttributeRule
    public void loadFromJSON(JSONObject ruleObject) throws JSONException {
        this.mDataList = new ArrayList();
        JSONArray dataArray = ruleObject.getJSONArray(JSON_FIELD_DATA_LIST);
        for (int i = 0; i < dataArray.length(); i++) {
            this.mDataList.add(new Data(dataArray.getJSONObject(i)));
        }
        this.mExtraList = new ArrayList();
        JSONArray extraArray = ruleObject.getJSONArray(JSON_FIELD_EXTRA_LIST);
        for (int i2 = 0; i2 < extraArray.length(); i2++) {
            this.mExtraList.add(new Extra(extraArray.getJSONObject(i2)));
        }
        this.mAttributeList = new ArrayList();
        JSONArray attributeArray = ruleObject.getJSONArray(JSON_FIELD_ATTRIBUTE_LIST);
        for (int i3 = 0; i3 < attributeArray.length(); i3++) {
            this.mAttributeList.add(new Attribute(attributeArray.getJSONObject(i3)));
        }
    }

    @Override // cn.teddymobile.free.anteater.rule.attribute.AttributeRule
    public JSONObject extractAttribute(View view) {
        JSONObject result = null;
        Intent intent = ViewHierarchyUtils.getIntent(view);
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            Map<String, String> aliasMap = new HashMap<>();
            if (intent.getData() != null) {
                for (Data data : this.mDataList) {
                    String str = TAG;
                    Logger.i(str, "Extract Data.\n" + data.toString());
                    Pair<String, String> aliasValuePair = data.getValue(intent.getData().toString());
                    if (!(aliasValuePair == null || aliasValuePair.first == null || aliasValuePair.second == null)) {
                        aliasMap.put(aliasValuePair.first, aliasValuePair.second);
                    }
                }
            } else {
                Logger.w(TAG, "Intent data is null.");
            }
            if (intent.getExtras() != null) {
                for (Extra extra : this.mExtraList) {
                    String str2 = TAG;
                    Logger.i(str2, "Extract Extra.\n" + extra.toString());
                    Pair<String, String> aliasValuePair2 = extra.getValue(bundle);
                    if (!(aliasValuePair2 == null || aliasValuePair2.first == null || aliasValuePair2.second == null)) {
                        aliasMap.put(aliasValuePair2.first, aliasValuePair2.second);
                    }
                }
            } else {
                Logger.w(TAG, "Intent extra is null.");
            }
            try {
                result = new JSONObject();
                for (Attribute attribute : this.mAttributeList) {
                    String str3 = TAG;
                    Logger.i(str3, "Create Attribute.\n" + attribute.toString());
                    Pair<String, String> aliasResultPair = attribute.getResult(aliasMap);
                    if (aliasResultPair != null) {
                        String str4 = TAG;
                        Logger.i(str4, "Attribute created. " + ((String) aliasResultPair.first) + " = " + ((String) aliasResultPair.second));
                        result.put(aliasResultPair.first, aliasResultPair.second);
                    }
                }
            } catch (JSONException e) {
            }
        } else {
            Logger.w(TAG, "Intent is null.");
        }
        String str5 = TAG;
        Logger.i(str5, getClass().getSimpleName() + " Result = " + result);
        return result;
    }
}
