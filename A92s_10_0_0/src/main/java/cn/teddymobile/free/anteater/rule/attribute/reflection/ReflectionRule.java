package cn.teddymobile.free.anteater.rule.attribute.reflection;

import android.util.Pair;
import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.attribute.AttributeRule;
import cn.teddymobile.free.anteater.rule.trigger.hierarchy.ViewHierarchyNode;
import cn.teddymobile.free.anteater.rule.utils.ViewHierarchyUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReflectionRule implements AttributeRule {
    private static final String JSON_FIELD_ATTRIBUTE_HIERARCHY = "attribute_hierarchy";
    private static final String JSON_FIELD_VIEW_HIERARCHY = "view_hierarchy";
    private static final String TAG = ReflectionRule.class.getSimpleName();
    private List<ObjectNode> mAttributeHierarchy = null;
    private List<ViewHierarchyNode> mViewHierarchy = null;

    public String toString() {
        return "[ReflectionRule]";
    }

    @Override // cn.teddymobile.free.anteater.rule.attribute.AttributeRule
    public void loadFromJSON(JSONObject ruleObject) throws JSONException {
        this.mViewHierarchy = new ArrayList();
        JSONArray viewHierarchyArray = ruleObject.getJSONArray(JSON_FIELD_VIEW_HIERARCHY);
        for (int i = 0; i < viewHierarchyArray.length(); i++) {
            this.mViewHierarchy.add(new ViewHierarchyNode(viewHierarchyArray.getJSONObject(i)));
        }
        this.mAttributeHierarchy = new ArrayList();
        JSONArray attributeHierarchyArray = ruleObject.getJSONArray(JSON_FIELD_ATTRIBUTE_HIERARCHY);
        for (int i2 = 0; i2 < attributeHierarchyArray.length(); i2++) {
            this.mAttributeHierarchy.add(new ObjectNode(attributeHierarchyArray.getJSONObject(i2)));
        }
    }

    @Override // cn.teddymobile.free.anteater.rule.attribute.AttributeRule
    public JSONObject extractAttribute(View view) {
        JSONObject result = null;
        Logger.i(TAG, "Check ViewHierarchy.");
        View targetView = view;
        if (this.mViewHierarchy.size() > 0) {
            View currentView = ViewHierarchyUtils.getDecorView(view);
            for (ViewHierarchyNode viewHierarchyNode : this.mViewHierarchy) {
                String str = TAG;
                Logger.i(str, "Check ViewHierarchyNode.\n" + viewHierarchyNode);
                currentView = viewHierarchyNode.getView(currentView);
                if (currentView == null) {
                    break;
                }
            }
            targetView = currentView;
        }
        if (targetView != null) {
            String str2 = TAG;
            Logger.i(str2, "TargetView = " + targetView.getClass().getName());
            Logger.i(TAG, "Reflect attribute.");
            try {
                result = new JSONObject();
                for (ObjectNode objectNode : this.mAttributeHierarchy) {
                    String str3 = TAG;
                    Logger.i(str3, "Check ObjectNode.\n" + objectNode.toString());
                    Pair<String, Object> attribute = objectNode.extractAttribute(targetView, targetView.getClass());
                    if (attribute != null) {
                        if (attribute.first != null) {
                            result.put(attribute.first, attribute.second);
                        } else if (attribute.second instanceof JSONObject) {
                            JSONObject attributeObject = attribute.second;
                            Iterator<String> iterator = attributeObject.keys();
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                result.put(key, attributeObject.get(key));
                            }
                        }
                    }
                }
                if (!result.keys().hasNext()) {
                    result = null;
                }
            } catch (JSONException e) {
                Logger.w(TAG, e.getMessage(), e);
            }
        } else {
            Logger.w(TAG, "Cannot find target view.");
        }
        String str4 = TAG;
        Logger.i(str4, getClass().getSimpleName() + " Result = " + result);
        return result;
    }
}
