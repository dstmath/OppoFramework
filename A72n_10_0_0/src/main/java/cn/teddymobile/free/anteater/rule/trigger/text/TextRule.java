package cn.teddymobile.free.anteater.rule.trigger.text;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.trigger.TriggerRule;
import org.json.JSONException;
import org.json.JSONObject;

public class TextRule implements TriggerRule {
    private static final String JSON_FIELD_ACTION = "action";
    private static final String JSON_FIELD_KEYWORD = "keyword";
    private static final String JSON_FIELD_RECURSIVE = "recursive";
    private static final String JSON_FIELD_USE_CONTENT_DESCRIPTION = "use_content_description";
    private static final String TAG = TextRule.class.getSimpleName();
    private String mAction = null;
    private String mKeyword = null;
    private boolean mRecursive = false;
    private boolean mUseContentDescription = false;

    public String toString() {
        return "[TextRule] Action = " + this.mAction + "\nKeyword = " + this.mKeyword + "\nRecursive = " + this.mRecursive + "\nUseContentDescription = " + this.mUseContentDescription;
    }

    @Override // cn.teddymobile.free.anteater.rule.trigger.TriggerRule
    public void loadFromJSON(JSONObject ruleObject) throws JSONException {
        this.mAction = ruleObject.getString("action");
        this.mKeyword = ruleObject.getString(JSON_FIELD_KEYWORD);
        this.mRecursive = ruleObject.optBoolean(JSON_FIELD_RECURSIVE, true);
        this.mUseContentDescription = ruleObject.optBoolean(JSON_FIELD_USE_CONTENT_DESCRIPTION, true);
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
            if (view instanceof TextView) {
                String str3 = TAG;
                Logger.i(str3, view.getClass().getName() + " is TextView.");
                TextView textView = (TextView) view;
                if (textView.getText() == null) {
                    Logger.w(TAG, "Text is null.");
                } else if (textView.getText().toString().contains(this.mKeyword)) {
                    result = true;
                } else {
                    Logger.w(TAG, "Text is incorrect.");
                    String str4 = TAG;
                    Logger.w(str4, "Expected = " + this.mKeyword);
                    String str5 = TAG;
                    Logger.w(str5, "Actual = " + textView.getText().toString());
                }
            } else {
                String str6 = TAG;
                Logger.i(str6, view.getClass().getName() + " is not TextView.");
            }
            if (!result && this.mUseContentDescription && view.getContentDescription() != null) {
                Logger.i(TAG, "Check ContentDescription.");
                if (view.getContentDescription().toString().contains(this.mKeyword)) {
                    result = true;
                } else {
                    Logger.w(TAG, "ContentDescription is incorrect.");
                    String str7 = TAG;
                    Logger.w(str7, "Expected = " + this.mKeyword);
                    String str8 = TAG;
                    Logger.w(str8, "Actual = " + view.getContentDescription().toString());
                }
            }
            if (!result && this.mRecursive && (view instanceof ViewGroup)) {
                Logger.i(TAG, "Check Action recursive.");
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
        String str9 = TAG;
        Logger.i(str9, getClass().getSimpleName() + " Result = " + result);
        return result;
    }
}
