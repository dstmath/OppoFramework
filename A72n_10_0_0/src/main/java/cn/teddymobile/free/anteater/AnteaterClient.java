package cn.teddymobile.free.anteater;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.resources.RuleResourcesClient;
import cn.teddymobile.free.anteater.resources.UriConstants;
import cn.teddymobile.free.anteater.rule.Rule;
import cn.teddymobile.free.anteater.rule.utils.RuleUtils;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnteaterClient {
    public static final String ACTION_CLICK = "click";
    public static final String ACTION_DISPLAY = "display";
    private static final String EMPTY = "";
    private static final String JSON_FIELD_PARSER = "parser";
    private static final String JSON_FIELD_RESULT = "result";
    private static final String TAG = AnteaterClient.class.getSimpleName();
    private static volatile AnteaterClient sInstance = null;
    private Handler mWorkHandler = null;

    public interface BaseCallback {
        Handler createWorkHandler(Context context, String str, int i);

        boolean isSettingOff(Context context);
    }

    public interface CheckCallback {
        void onFailure();

        void onSuccess();
    }

    public enum ErrorCode {
        NO_VIEW,
        NOT_INIT,
        NOT_FOUND,
        UNSUPPORT,
        SETTING_OFF,
        SAVE_FAILED,
        NONE
    }

    public interface ProcessCallback extends BaseCallback {
        void onProcessResult(Context context, ResultData resultData);
    }

    public interface SaveCallback extends BaseCallback {
        void onSaveResult(Context context, ResultData resultData);
    }

    public static AnteaterClient getInstance() {
        if (sInstance == null) {
            synchronized (AnteaterClient.class) {
                if (sInstance == null) {
                    sInstance = new AnteaterClient();
                }
            }
        }
        return sInstance;
    }

    private AnteaterClient() {
        Logger.d(TAG, "<init>()");
    }

    public void init() {
    }

    public void release() {
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public Handler getWorkHandler() {
        return this.mWorkHandler;
    }

    public void loadRule(Context context, String data, RuleResourcesClient.LoadCallback callback) {
    }

    public void process(final String action, final View view, final ProcessCallback callback) {
        if (view == null) {
            Logger.w(TAG, "Process [No View]");
            return;
        }
        final Context context = view.getContext();
        ensureWorkHandler(context, callback);
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class cn.teddymobile.free.anteater.AnteaterClient.AnonymousClass1 */

                public void run() {
                    String msg = "action=" + action + ", context=" + context + ", view=" + context.getPackageName() + "/" + view;
                    String viewName = view.getClass().getName();
                    if (callback.isSettingOff(context)) {
                        Context viewContext = context;
                        Logger.w(AnteaterClient.TAG, "Process [Setting Off] : " + msg);
                        ResultData result = new ResultData();
                        result.setError(ErrorCode.SETTING_OFF);
                        callback.onProcessResult(viewContext, result);
                        return;
                    }
                    Logger.d(AnteaterClient.TAG, "Process Begin : View = " + viewName + ". Action = " + action);
                    ArrayList<QueryData> queryList = new ArrayList<>();
                    List<Rule> ruleList = AnteaterClient.this.getRuleList(context);
                    int ruleSize = ruleList.size();
                    for (int i = 0; i < ruleSize; i++) {
                        Rule rule = ruleList.get(i);
                        if (!rule.check(action, view)) {
                            Logger.i(AnteaterClient.TAG, "\n----------Process Skip Rule " + i + "----------\n" + rule);
                        } else {
                            Logger.i(AnteaterClient.TAG, "\n----------Process Apply Rule " + i + "----------\n" + rule);
                            try {
                                JSONObject resultObject = rule.extract(action, view);
                                if (resultObject != null) {
                                    QueryData data = new QueryData();
                                    data.setTitle(AnteaterClient.this.extractTitle(resultObject));
                                    data.setUrl(AnteaterClient.this.extractUrl(resultObject));
                                    if (data.isValid() && !queryList.contains(data)) {
                                        queryList.add(data);
                                    }
                                }
                            } catch (JSONException e) {
                                Logger.w(AnteaterClient.TAG, e.getMessage(), e);
                            } catch (Exception e2) {
                                Logger.e(AnteaterClient.TAG, e2.getMessage(), e2);
                            }
                        }
                    }
                    if (queryList.size() > 0) {
                        AnteaterClient.this.processSuccess(context, callback, queryList);
                    } else {
                        AnteaterClient.this.processFailed(context, callback, ruleSize);
                    }
                    Logger.d(AnteaterClient.TAG, "Process Finish : View = " + viewName + ". Action = " + action);
                }
            });
        } else {
            Logger.e(TAG, "Process : No WorkHandler");
        }
    }

    public void save(final String action, final View view, final SaveCallback callback) {
        if (view == null) {
            Logger.w(TAG, "Save [No View] ");
            return;
        }
        final Context context = view.getContext();
        ensureWorkHandler(context, callback);
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class cn.teddymobile.free.anteater.AnteaterClient.AnonymousClass2 */

                public void run() {
                    String msg = "action=" + action + ", context=" + context + ", view=" + context.getPackageName() + "/" + view;
                    view.getClass().getName();
                    Context viewContext = view.getContext();
                    Logger.d(AnteaterClient.TAG, "Save Begin : " + msg);
                    boolean result = false;
                    List<Rule> ruleList = AnteaterClient.this.getRuleList(context);
                    int ruleSize = ruleList.size();
                    for (int i = 0; i < ruleSize; i++) {
                        Rule rule = ruleList.get(i);
                        if (!rule.check(action, view)) {
                            Logger.i(AnteaterClient.TAG, "\n----------Save Skip Rule " + i + "----------\n" + rule);
                        } else {
                            Logger.i(AnteaterClient.TAG, "\n----------Save Apply Rule " + i + "----------\n" + rule);
                            try {
                                JSONObject resultObject = rule.extract(action, view);
                                if (resultObject != null) {
                                    result |= AnteaterClient.this.saveResult(viewContext, rule, resultObject);
                                }
                            } catch (JSONException e) {
                                Logger.w(AnteaterClient.TAG, e.getMessage(), e);
                            } catch (Exception e2) {
                                Logger.e(AnteaterClient.TAG, e2.getMessage(), e2);
                            }
                        }
                    }
                    if (result) {
                        AnteaterClient.this.saveSuccess(viewContext, callback);
                    } else {
                        AnteaterClient.this.saveFailed(viewContext, callback, ruleSize);
                    }
                    Logger.d(AnteaterClient.TAG, "Save Finish : " + msg);
                }
            });
        } else {
            Logger.e(TAG, "Save : No WorkHandler");
        }
    }

    public void check(final View view, final String action, final CheckCallback checkCallback) {
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class cn.teddymobile.free.anteater.AnteaterClient.AnonymousClass3 */

                public void run() {
                    View view = view;
                    if (view == null) {
                        Logger.w(AnteaterClient.TAG, "Check [No View] ");
                        return;
                    }
                    String viewName = view.getClass().getName();
                    String str = AnteaterClient.TAG;
                    Logger.i(str, "Check View = " + viewName);
                    List<Rule> ruleList = AnteaterClient.this.getRuleList(view.getContext());
                    int ruleSize = ruleList.size();
                    for (int i = 0; i < ruleSize; i++) {
                        Rule rule = ruleList.get(i);
                        String str2 = AnteaterClient.TAG;
                        Logger.i(str2, "\n----------Check Rule " + i + "----------\n" + rule);
                        if (rule.check(action, view)) {
                            CheckCallback checkCallback = checkCallback;
                            if (checkCallback != null) {
                                checkCallback.onSuccess();
                                return;
                            }
                            return;
                        }
                    }
                    CheckCallback checkCallback2 = checkCallback;
                    if (checkCallback2 != null) {
                        checkCallback2.onFailure();
                    }
                }
            });
        } else {
            Logger.e(TAG, "Check : No WorkHandler");
        }
    }

    private void ensureWorkHandler(Context context, BaseCallback callback) {
        if (this.mWorkHandler == null) {
            this.mWorkHandler = callback.createWorkHandler(context, "anteater", 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<Rule> getRuleList(Context context) {
        return RuleUtils.parseRule(RuleUtils.queryRuleFromProvider(context));
    }

    private String formatText(String text) {
        if (text != null && !"true".equals(text) && !"false".equals(text)) {
            return text;
        }
        return "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String extractTitle(JSONObject resultObject) {
        String data = null;
        try {
            data = resultObject.getString("title");
        } catch (JSONException e) {
            Logger.w(TAG, e.getMessage(), e);
        } catch (Exception e2) {
            Logger.e(TAG, e2.getMessage(), e2);
        }
        if (TextUtils.isEmpty(data)) {
            Logger.d(TAG, "loadFromWindowList-->title");
            data = extractValueFromWindowList(resultObject, "title");
        }
        return formatText(data);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String extractUrl(JSONObject resultObject) {
        String data = null;
        try {
            data = resultObject.getString("url");
        } catch (JSONException e) {
            Logger.w(TAG, e.getMessage(), e);
        } catch (Exception e2) {
            Logger.e(TAG, e2.getMessage(), e2);
        }
        if (TextUtils.isEmpty(data)) {
            Logger.d(TAG, "loadFromWindowList-->url");
            data = extractValueFromWindowList(resultObject, "url");
        }
        return formatText(data);
    }

    private String extractValueFromWindowList(JSONObject resultObject, String key) {
        JSONArray jsonArray = null;
        try {
            jsonArray = resultObject.getJSONArray("window_list");
        } catch (Exception ex) {
            Logger.w(TAG, ex.getMessage(), ex);
        }
        if (jsonArray == null) {
            return null;
        }
        for (int i = jsonArray.length() - 1; i >= 0; i--) {
            try {
                String result = jsonArray.getJSONObject(i).getString(key);
                if (!TextUtils.isEmpty(result)) {
                    Logger.d(TAG, "loadFromWindowList-->" + key + "-->" + result);
                    return result;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processSuccess(Context context, ProcessCallback callback, ArrayList<QueryData> queryList) {
        String packageName = context.getPackageName();
        String str = TAG;
        Logger.i(str, "processSuccess : " + packageName + ", queryList=" + queryList.size());
        ResultData result = new ResultData();
        result.setQueryList(queryList);
        callback.onProcessResult(context, result);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processFailed(Context context, ProcessCallback callback, int ruleSize) {
        String packageName = context.getPackageName();
        String str = TAG;
        Logger.i(str, "processFailed : " + packageName);
        ResultData result = new ResultData();
        if (ruleSize > 0) {
            result.setError(ErrorCode.NOT_FOUND);
        } else {
            result.setError(ErrorCode.UNSUPPORT);
        }
        callback.onProcessResult(context, result);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean saveResult(Context context, Rule rule, JSONObject resultObject) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("result", resultObject);
        object.put(JSON_FIELD_PARSER, rule.getParser());
        Uri uri = new Uri.Builder().scheme("content").authority(UriConstants.AUTHORITY).path("result").build();
        ContentValues values = new ContentValues();
        values.put("package_name", rule.getPackageName());
        values.put("version", rule.getVersion());
        values.put(UriConstants.RESULT_COLUMN_SCENE, rule.getScene());
        values.put("data", object.toString());
        Uri insertUri = context.getContentResolver().insert(uri, values);
        String str = TAG;
        Logger.i(str, "\n----------saveResult : uri=" + uri + ", insertUri=" + insertUri + "\n");
        String str2 = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("\n----------saveResult : values=");
        sb.append(values);
        sb.append("\n");
        Logger.i(str2, sb.toString());
        return insertUri != null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveSuccess(Context context, SaveCallback callback) {
        callback.onSaveResult(context, new ResultData());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveFailed(Context context, SaveCallback callback, int ruleSize) {
        ResultData result = new ResultData();
        if (ruleSize > 0) {
            result.setError(ErrorCode.SAVE_FAILED);
        } else {
            result.setError(ErrorCode.UNSUPPORT);
        }
        callback.onSaveResult(context, result);
    }

    public static class ResultData {
        private ErrorCode mError = ErrorCode.NONE;
        private final ArrayList<QueryData> mQueryList = new ArrayList<>();

        public void setQueryList(ArrayList<QueryData> queryList) {
            synchronized (this.mQueryList) {
                this.mQueryList.clear();
                if (queryList != null) {
                    synchronized (queryList) {
                        this.mQueryList.addAll(queryList);
                    }
                }
            }
        }

        public void setError(ErrorCode error) {
            this.mError = error;
        }

        public ArrayList<QueryData> getQueryList() {
            ArrayList<QueryData> arrayList;
            synchronized (this.mQueryList) {
                arrayList = this.mQueryList;
            }
            return arrayList;
        }

        public ErrorCode getError() {
            return this.mError;
        }
    }

    public static class QueryData {
        private String mTitle = null;
        private String mUrl = null;

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            QueryData other = (QueryData) object;
            if (TextUtils.equals(this.mTitle, other.mTitle) && TextUtils.equals(this.mUrl, other.mUrl)) {
                return true;
            }
            return false;
        }

        public boolean isValid() {
            if (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mUrl)) {
                return false;
            }
            return true;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public void setUrl(String url) {
            this.mUrl = url;
        }

        public String getTitle() {
            return this.mTitle;
        }

        public String getUrl() {
            return this.mUrl;
        }
    }
}
