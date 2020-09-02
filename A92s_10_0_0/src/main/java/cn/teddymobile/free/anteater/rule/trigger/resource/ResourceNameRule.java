package cn.teddymobile.free.anteater.rule.trigger.resource;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.trigger.TriggerRule;
import org.json.JSONException;
import org.json.JSONObject;

public class ResourceNameRule implements TriggerRule {
    private static final String JSON_FIELD_ACTION = "action";
    private static final String JSON_FIELD_RECURSIVE = "recursive";
    private static final String JSON_FIELD_RESOURCE_NAME = "resource_name";
    private static final String TAG = ResourceNameRule.class.getSimpleName();
    private String mAction = null;
    private boolean mRecursive = false;
    private String mResourceName = null;

    public String toString() {
        return "[ResourceNameRule] ResourceName = " + this.mResourceName + "\nAction = " + this.mAction + "\nRecursive = " + this.mRecursive;
    }

    @Override // cn.teddymobile.free.anteater.rule.trigger.TriggerRule
    public void loadFromJSON(JSONObject ruleObject) throws JSONException {
        this.mAction = ruleObject.getString("action");
        this.mResourceName = ruleObject.getString(JSON_FIELD_RESOURCE_NAME);
        this.mRecursive = ruleObject.optBoolean(JSON_FIELD_RECURSIVE, false);
    }

    @Override // cn.teddymobile.free.anteater.rule.trigger.TriggerRule
    public boolean fitAction(String action, View view) {
        boolean result = false;
        if (action == null || !action.equals(this.mAction)) {
            Logger.w(TAG, "Action is incorrect.");
            String str = TAG;
            Logger.w(str, "Expected = " + this.mAction);
            String str2 = TAG;
            Logger.w(str2, "Actual = " + action);
        } else {
            try {
                String resourceName = view.getContext().getResources().getResourceName(view.getId());
                if (resourceName == null || !resourceName.endsWith(this.mResourceName)) {
                    Logger.w(TAG, "ResourceName is incorrect.");
                    String str3 = TAG;
                    Logger.w(str3, "Expected = " + this.mResourceName);
                    String str4 = TAG;
                    Logger.w(str4, "Actual = " + resourceName);
                } else {
                    result = true;
                }
            } catch (Resources.NotFoundException e) {
                Logger.w(TAG, "ResourceName is not found.");
                String str5 = TAG;
                Logger.w(str5, "Expected = " + this.mResourceName);
            }
            if (!result && this.mRecursive && (view instanceof ViewGroup)) {
                Logger.i(TAG, "Check ResourceName recursive.");
                ViewGroup viewGroup = (ViewGroup) view;
                int i = 0;
                while (true) {
                    if (i >= viewGroup.getChildCount()) {
                        break;
                    } else if (fitAction(action, viewGroup.getChildAt(i))) {
                        result = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
        String str6 = TAG;
        Logger.i(str6, getClass().getSimpleName() + " Result = " + result);
        return result;
    }
}
