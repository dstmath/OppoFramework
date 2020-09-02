package cn.teddymobile.free.anteater;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.resources.RuleResources;
import cn.teddymobile.free.anteater.resources.UriConstants;
import cn.teddymobile.free.anteater.rule.Rule;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Anteater {
    public static final String ACTION_CLICK = "click";
    public static final String ACTION_DISPLAY = "display";
    private static final String EMPTY = "";
    private static final String JSON_FIELD_PARSER = "parser";
    private static final String JSON_FIELD_RESULT = "result";
    /* access modifiers changed from: private */
    public static final String TAG = Anteater.class.getSimpleName();
    private static volatile Anteater sInstance = null;
    /* access modifiers changed from: private */
    public RuleResources mResources = null;
    private Handler mWorkHandler = null;

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

    public interface ProcessCallback {
        boolean isSettingOff(Context context);

        void onProcessResult(Context context, ResultData resultData);
    }

    public interface SaveCallback {
        boolean isSettingOff(Context context);

        void onSaveResult(Context context, ResultData resultData);
    }

    public static Anteater getInstance() {
        if (sInstance == null) {
            synchronized (Anteater.class) {
                if (sInstance == null) {
                    sInstance = new Anteater();
                }
            }
        }
        return sInstance;
    }

    private Anteater() {
        Logger.d(TAG, "Anteater <init>()");
    }

    public void init(Context context, RuleResources.InitCallback callback) {
        try {
            if (this.mWorkHandler == null) {
                this.mWorkHandler = callback.createWorkHandler(context, "anteater", 1);
            }
            if (this.mResources == null) {
                this.mResources = new RuleResources(this.mWorkHandler);
            }
            this.mResources.init(context, callback);
            this.mResources.registerObserver(context);
        } catch (Exception e) {
            String str = TAG;
            Logger.e(str, "init failed : " + e.toString());
        }
    }

    public void release() {
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        RuleResources ruleResources = this.mResources;
        if (ruleResources != null) {
            ruleResources.unregisterObserver();
        }
    }

    public Handler getWorkHandler() {
        return this.mWorkHandler;
    }

    public void process(final String action, final View view, final ProcessCallback callback) {
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class cn.teddymobile.free.anteater.Anteater.AnonymousClass1 */

                public void run() {
                    View view = view;
                    if (view == null) {
                        Logger.w(Anteater.TAG, "Process [No View]");
                        return;
                    }
                    Context context = view.getContext();
                    String msg = "action=" + action + ", context=" + context + ", view=" + context.getPackageName() + "/" + view;
                    if (callback.isSettingOff(context)) {
                        Logger.w(Anteater.TAG, "Process [Setting Off] : " + msg);
                        ResultData result = new ResultData();
                        result.setError(ErrorCode.SETTING_OFF);
                        callback.onProcessResult(context, result);
                        return;
                    }
                    View view2 = view;
                    if (view2 != null) {
                        String viewName = view2.getClass().getName();
                        if (Anteater.this.mResources.isInited()) {
                            Logger.d(Anteater.TAG, "Process Begin : View = " + viewName + ". Action = " + action);
                            ArrayList<QueryData> queryList = new ArrayList<>();
                            ArrayList<Rule> ruleList = Anteater.this.copyRuleList();
                            int ruleSize = ruleList.size();
                            for (int i = 0; i < ruleSize; i++) {
                                Rule rule = ruleList.get(i);
                                if (!rule.check(view)) {
                                    Logger.i(Anteater.TAG, "\n----------Process Skip Rule " + i + "----------\n" + rule);
                                } else {
                                    Logger.i(Anteater.TAG, "\n----------Process Apply Rule " + i + "----------\n" + rule);
                                    try {
                                        JSONObject resultObject = rule.extract(action, view);
                                        if (resultObject != null) {
                                            QueryData data = new QueryData();
                                            data.setTitle(Anteater.this.extractTitle(resultObject));
                                            data.setUrl(Anteater.this.extractUrl(resultObject));
                                            if (data.isValid() && !queryList.contains(data)) {
                                                queryList.add(data);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        Logger.w(Anteater.TAG, e.getMessage(), e);
                                    } catch (Exception e2) {
                                        Logger.e(Anteater.TAG, e2.getMessage(), e2);
                                    }
                                }
                            }
                            if (queryList.size() > 0) {
                                Anteater.this.processSuccess(context, callback, queryList);
                            } else {
                                Anteater.this.processFailed(context, callback, ruleSize);
                            }
                            Logger.d(Anteater.TAG, "Process Finish : View = " + viewName + ". Action = " + action);
                            return;
                        }
                        Logger.w(Anteater.TAG, "Process [Not Init] : action=" + action + ", view=" + viewName);
                        ResultData result2 = new ResultData();
                        result2.setError(ErrorCode.NOT_INIT);
                        callback.onProcessResult(context, result2);
                        return;
                    }
                    Logger.w(Anteater.TAG, "Process [No View] : " + msg);
                    ResultData result3 = new ResultData();
                    result3.setError(ErrorCode.NO_VIEW);
                    callback.onProcessResult(context, result3);
                }
            });
        } else {
            Logger.e(TAG, "Process : No WorkHandler");
        }
    }

    public void save(final String action, final View view, final SaveCallback callback) {
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class cn.teddymobile.free.anteater.Anteater.AnonymousClass2 */

                public void run() {
                    View view = view;
                    if (view == null) {
                        Logger.w(Anteater.TAG, "Save [No View] ");
                        return;
                    }
                    Context context = view.getContext();
                    String msg = "action=" + action + ", context=" + context + ", view=" + context.getPackageName() + "/" + view;
                    View view2 = view;
                    if (view2 != null) {
                        view2.getClass().getName();
                        Context viewContext = view.getContext();
                        if (Anteater.this.mResources.isInited()) {
                            Logger.d(Anteater.TAG, "Save Begin : " + msg);
                            boolean result = false;
                            ArrayList<Rule> ruleList = Anteater.this.copyRuleList();
                            int ruleSize = ruleList.size();
                            for (int i = 0; i < ruleSize; i++) {
                                Rule rule = ruleList.get(i);
                                if (!rule.check(view)) {
                                    Logger.i(Anteater.TAG, "\n----------Save Skip Rule " + i + "----------\n" + rule);
                                } else {
                                    Logger.i(Anteater.TAG, "\n----------Save Apply Rule " + i + "----------\n" + rule);
                                    try {
                                        JSONObject resultObject = rule.extract(action, view);
                                        if (resultObject != null) {
                                            result |= Anteater.this.saveResult(viewContext, rule, resultObject);
                                        }
                                    } catch (JSONException e) {
                                        Logger.w(Anteater.TAG, e.getMessage(), e);
                                    } catch (Exception e2) {
                                        Logger.e(Anteater.TAG, e2.getMessage(), e2);
                                    }
                                }
                            }
                            if (result) {
                                Anteater.this.saveSuccess(viewContext, callback);
                            } else {
                                Anteater.this.saveFailed(viewContext, callback, ruleSize);
                            }
                            Logger.d(Anteater.TAG, "Save Finish : " + msg);
                            return;
                        }
                        Logger.w(Anteater.TAG, "Save [Not Init] : " + msg);
                        ResultData result2 = new ResultData();
                        result2.setError(ErrorCode.NOT_INIT);
                        callback.onSaveResult(viewContext, result2);
                        return;
                    }
                    Logger.w(Anteater.TAG, "Save [No View] : " + msg);
                    ResultData result3 = new ResultData();
                    result3.setError(ErrorCode.NO_VIEW);
                    callback.onSaveResult(context, result3);
                }
            });
        } else {
            Logger.e(TAG, "Save : No WorkHandler");
        }
    }

    public void check(final View view, final CheckCallback checkCallback) {
        Handler handler = this.mWorkHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class cn.teddymobile.free.anteater.Anteater.AnonymousClass3 */

                public void run() {
                    String viewName = view.getClass().getName();
                    if (Anteater.this.mResources.isInited()) {
                        String access$000 = Anteater.TAG;
                        Logger.i(access$000, "Check View = " + viewName);
                        ArrayList<Rule> ruleList = Anteater.this.copyRuleList();
                        int ruleSize = ruleList.size();
                        for (int i = 0; i < ruleSize; i++) {
                            Rule rule = ruleList.get(i);
                            String access$0002 = Anteater.TAG;
                            Logger.i(access$0002, "\n----------Check Rule " + i + "----------\n" + rule);
                            if (rule.check(view)) {
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
                            return;
                        }
                        return;
                    }
                    String access$0003 = Anteater.TAG;
                    Logger.d(access$0003, "Check [Not Init] : view=" + viewName);
                    CheckCallback checkCallback3 = checkCallback;
                    if (checkCallback3 != null) {
                        checkCallback3.onFailure();
                    }
                }
            });
        } else {
            Logger.e(TAG, "Check : No WorkHandler");
        }
    }

    /* access modifiers changed from: private */
    public ArrayList<Rule> copyRuleList() {
        ArrayList<Rule> arrayList;
        ArrayList<Rule> ruleList = this.mResources.getRuleList();
        synchronized (ruleList) {
            arrayList = new ArrayList<>(ruleList);
        }
        return arrayList;
    }

    private String formatText(String text) {
        if (text != null && !"true".equals(text) && !"false".equals(text)) {
            return text;
        }
        return "";
    }

    /* access modifiers changed from: private */
    public String extractTitle(JSONObject resultObject) {
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
    public String extractUrl(JSONObject resultObject) {
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
        int i = jsonArray.length() - 1;
        while (i >= 0) {
            try {
                String result = jsonArray.getJSONObject(i).getString(key);
                if (!TextUtils.isEmpty(result)) {
                    Logger.d(TAG, "loadFromWindowList-->" + key + "-->" + result);
                    return result;
                }
                i--;
            } catch (Exception e) {
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void processSuccess(Context context, ProcessCallback callback, ArrayList<QueryData> queryList) {
        String packageName = context.getPackageName();
        String str = TAG;
        Logger.i(str, "processSuccess : " + packageName + ", queryList=" + queryList.size());
        ResultData result = new ResultData();
        result.setQueryList(queryList);
        callback.onProcessResult(context, result);
    }

    /* access modifiers changed from: private */
    public void processFailed(Context context, ProcessCallback callback, int ruleSize) {
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
    public boolean saveResult(Context context, Rule rule, JSONObject resultObject) throws JSONException {
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
    public void saveSuccess(Context context, SaveCallback callback) {
        callback.onSaveResult(context, new ResultData());
    }

    /* access modifiers changed from: private */
    public void saveFailed(Context context, SaveCallback callback, int ruleSize) {
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
