package cn.teddymobile.free.anteater.rule.trigger.hierarchy;

import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.trigger.TriggerRule;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewHierarchyRule implements TriggerRule {
    private static final String JSON_FIELD_ACTION = "action";
    private static final String JSON_FIELD_VIEW_HIERARCHY = "view_hierarchy";
    private static final String TAG = ViewHierarchyRule.class.getSimpleName();
    private String mAction = null;
    private List<ViewHierarchyNode> mViewHierarchy = null;

    public String toString() {
        List<ViewHierarchyNode> list = this.mViewHierarchy;
        int viewHierarchySize = list != null ? list.size() : 0;
        return "[ViewHierarchyRule] ViewHierarchy size = " + viewHierarchySize + "\nAction = " + this.mAction;
    }

    @Override // cn.teddymobile.free.anteater.rule.trigger.TriggerRule
    public void loadFromJSON(JSONObject ruleObject) throws JSONException {
        this.mAction = ruleObject.getString("action");
        this.mViewHierarchy = new ArrayList();
        JSONArray viewHierarchyArray = ruleObject.getJSONArray(JSON_FIELD_VIEW_HIERARCHY);
        for (int i = 0; i < viewHierarchyArray.length(); i++) {
            this.mViewHierarchy.add(new ViewHierarchyNode(viewHierarchyArray.getJSONObject(i)));
        }
    }

    @Override // cn.teddymobile.free.anteater.rule.trigger.TriggerRule
    public boolean fitAction(String action, View view) {
        boolean result = false;
        if (action == null || !action.equals(this.mAction)) {
            Logger.i(TAG, "Action is incorrect.");
            Logger.i(TAG, "Expected = " + this.mAction);
            Logger.i(TAG, "Actual = " + action);
        } else {
            Logger.i(TAG, "Action is correct.");
            boolean match = true;
            List<ViewHierarchyNode> list = this.mViewHierarchy;
            if (list != null) {
                View currentView = view;
                int i = list.size() - 1;
                while (true) {
                    if (i < 0) {
                        break;
                    }
                    ViewHierarchyNode node = this.mViewHierarchy.get(i);
                    Logger.i(TAG, "Check ViewHierarchyNode.\n" + node.toString());
                    if (!node.fitView(currentView)) {
                        match = false;
                        break;
                    }
                    currentView = (View) currentView.getParent();
                    i--;
                }
            } else {
                Logger.i(TAG, "ViewHierarchy is null.");
            }
            result = match;
        }
        Logger.i(TAG, getClass().getSimpleName() + " Result = " + result);
        return result;
    }
}
