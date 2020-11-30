package cn.teddymobile.free.anteater.resources;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.Rule;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RuleResourcesClient {
    private static final String POSTFIX_PARSER = "Parser";
    private static final String TAG = RuleResourcesClient.class.getSimpleName();
    private boolean mInited = false;
    private final ArrayList<Rule> mRuleList = new ArrayList<>();
    private final ArrayList<String> mSceneList = new ArrayList<>();

    public interface LoadCallback {
        void onLoadResult(Context context, boolean z, boolean z2, ArrayList<String> arrayList);
    }

    public ArrayList<Rule> getRuleList() {
        ArrayList<Rule> arrayList;
        synchronized (this.mRuleList) {
            arrayList = this.mRuleList;
        }
        return arrayList;
    }

    public ArrayList<String> getSceneList() {
        ArrayList<String> arrayList;
        synchronized (this.mSceneList) {
            arrayList = this.mSceneList;
        }
        return arrayList;
    }

    public boolean isInited() {
        boolean z;
        synchronized (RuleResourcesClient.class) {
            z = this.mInited;
        }
        return z;
    }

    public void loadRule(Context context, String data, LoadCallback callback) {
        String str;
        String str2;
        synchronized (RuleResourcesClient.class) {
            long start = SystemClock.uptimeMillis();
            boolean noRule = true;
            try {
                synchronized (this.mRuleList) {
                    this.mRuleList.clear();
                }
                synchronized (this.mSceneList) {
                    this.mSceneList.clear();
                }
                if (!TextUtils.isEmpty(data)) {
                    JSONArray ruleArray = new JSONArray(data);
                    for (int i = 0; i < ruleArray.length(); i++) {
                        JSONObject ruleObject = ruleArray.getJSONObject(i);
                        Rule rule = new Rule();
                        rule.loadFromJSON(ruleObject);
                        synchronized (this.mRuleList) {
                            this.mRuleList.add(rule);
                        }
                        String scene = extractScene(rule);
                        synchronized (this.mSceneList) {
                            if (!this.mSceneList.contains(scene)) {
                                this.mSceneList.add(scene);
                            }
                        }
                    }
                    noRule = false;
                }
                str = TAG;
                str2 = "loadRule : spend=" + (SystemClock.uptimeMillis() - start) + "ms";
            } catch (JSONException e) {
                Logger.w(TAG, e.getMessage(), e);
                str = TAG;
                str2 = "loadRule : spend=" + (SystemClock.uptimeMillis() - start) + "ms";
            } catch (Exception e2) {
                Logger.e(TAG, e2.getMessage(), e2);
                str = TAG;
                str2 = "loadRule : spend=" + (SystemClock.uptimeMillis() - start) + "ms";
            } catch (Throwable th) {
                Logger.d(TAG, "loadRule : spend=" + (SystemClock.uptimeMillis() - start) + "ms");
                throw th;
            }
            Logger.d(str, str2);
            onLoadResult(context, noRule, callback);
        }
    }

    private String extractScene(Rule rule) {
        String parser = rule.getParser();
        if (parser.endsWith(POSTFIX_PARSER)) {
            return parser.substring(0, parser.length() - POSTFIX_PARSER.length());
        }
        return parser;
    }

    private void onLoadResult(Context context, boolean noRule, LoadCallback callback) {
        int ruleSize;
        synchronized (this.mRuleList) {
            ruleSize = this.mRuleList.size();
        }
        String str = TAG;
        Logger.i(str, "onLoadResult : Size = " + ruleSize);
        boolean emptyRule = true;
        this.mInited = true;
        if (ruleSize >= 1) {
            emptyRule = false;
        }
        if (callback != null) {
            callback.onLoadResult(context, noRule, emptyRule, this.mSceneList);
        }
    }
}
