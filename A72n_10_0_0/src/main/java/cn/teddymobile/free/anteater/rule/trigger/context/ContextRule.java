package cn.teddymobile.free.anteater.rule.trigger.context;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.trigger.TriggerRule;
import cn.teddymobile.free.anteater.rule.utils.ViewHierarchyUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ContextRule implements TriggerRule {
    private static final String JSON_FIELD_ACTION = "action";
    private static final String JSON_FIELD_CONTEXT = "context";
    private String mAction = null;
    private String mContext = null;

    public String toString() {
        return "[ContextRule] Context = " + this.mContext + "\nAction = " + this.mAction;
    }

    @Override // cn.teddymobile.free.anteater.rule.trigger.TriggerRule
    public void loadFromJSON(JSONObject ruleObject) throws JSONException {
        this.mAction = ruleObject.getString("action");
        this.mContext = ruleObject.getString(JSON_FIELD_CONTEXT);
    }

    @Override // cn.teddymobile.free.anteater.rule.trigger.TriggerRule
    public boolean fitAction(String action, View view) {
        boolean result = false;
        if (action == null || !this.mAction.equals(action)) {
            Logger.i(ContentValues.TAG, "Action is incorrect.");
            Logger.i(ContentValues.TAG, "Expected = " + this.mAction);
            Logger.i(ContentValues.TAG, "Actual = " + action);
            Logger.i(ContentValues.TAG, "view = " + view);
        } else if (fitContext(view)) {
            result = true;
        } else {
            Logger.i(ContentValues.TAG, "Context is incorrect.");
            Logger.i(ContentValues.TAG, "Expected = " + this.mContext);
            Logger.i(ContentValues.TAG, "Actual = " + view.getContext());
            Logger.i(ContentValues.TAG, "view = " + view);
        }
        Logger.i(ContentValues.TAG, getClass().getSimpleName() + " Result = " + result);
        return result;
    }

    private boolean matchContext(Context context) {
        if (context != null) {
            String name = context.getClass().getName();
            return name.endsWith("." + this.mContext);
        }
        Logger.i(ContentValues.TAG, "matchContext false, context = " + context + ", mContext = " + this.mContext);
        return false;
    }

    private boolean fitContext(View view) {
        Context context = view.getContext();
        if (matchContext(context)) {
            return true;
        }
        Context context2 = ViewHierarchyUtils.getActivityContext(context);
        if (context2 instanceof Activity) {
            return matchContext(context2);
        }
        return false;
    }
}
