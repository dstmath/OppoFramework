package cn.teddymobile.free.anteater.resources;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.Rule;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RuleResources {
    private static final String POSTFIX_PARSER = "Parser";
    private static final String TAG = RuleResources.class.getSimpleName();
    private static final Uri URI_RULE = new Uri.Builder().scheme("content").authority(UriConstants.AUTHORITY).path(UriConstants.PATH_RULE).build();
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mInited = false;
    private final ArrayList<Rule> mRuleList = new ArrayList<>();
    private RulesObserver mRulesObserver = null;
    private final ArrayList<String> mSceneList = new ArrayList<>();
    private final String[] mSelectionArgs = new String[1];

    public interface InitCallback {
        Handler createWorkHandler(Context context, String str, int i);

        void onLoadResult(Context context, boolean z, ArrayList<String> arrayList);
    }

    public RuleResources(Handler handler) {
        this.mHandler = handler;
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
        synchronized (RuleResources.class) {
            z = this.mInited;
        }
        return z;
    }

    public void init(final Context context, final InitCallback callback) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class cn.teddymobile.free.anteater.resources.RuleResources.AnonymousClass1 */

                public void run() {
                    synchronized (RuleResources.class) {
                        if (RuleResources.this.mInited) {
                            RuleResources.this.loadFromCache(context, callback);
                        } else {
                            RuleResources.this.loadFromProvider(context, callback);
                            boolean unused = RuleResources.this.mInited = true;
                        }
                    }
                }
            });
        }
    }

    public void registerObserver(Context context) {
        Handler handler = this.mHandler;
        if (handler != null) {
            if (this.mRulesObserver == null) {
                this.mRulesObserver = new RulesObserver(handler, context, URI_RULE);
            }
            try {
                context.getContentResolver().registerContentObserver(this.mRulesObserver.getUri(), false, this.mRulesObserver);
                String str = TAG;
                Logger.i(str, "registerObserver : " + this.mRulesObserver.getUri());
            } catch (Exception e) {
                Logger.i(TAG, e.toString());
            }
        }
    }

    public void unregisterObserver() {
        RulesObserver rulesObserver = this.mRulesObserver;
        if (rulesObserver != null) {
            try {
                rulesObserver.getContext().getContentResolver().unregisterContentObserver(this.mRulesObserver);
                String str = TAG;
                Logger.i(str, "unregisterObserver : " + this.mRulesObserver.getUri());
            } catch (Exception e) {
                Logger.i(TAG, e.toString());
            }
            this.mRulesObserver = null;
        }
    }

    private String extractScene(Rule rule) {
        String parser = rule.getParser();
        if (parser.endsWith(POSTFIX_PARSER)) {
            return parser.substring(0, parser.length() - POSTFIX_PARSER.length());
        }
        return parser;
    }

    /* access modifiers changed from: private */
    public void updateRules(Context context) {
        loadFromProvider(context, null);
    }

    private void onLoadResult(Context context, InitCallback callback, String tag) {
        int ruleSize;
        synchronized (this.mRuleList) {
            ruleSize = this.mRuleList.size();
        }
        String str = TAG;
        Logger.i(str, tag + ". Size = " + ruleSize);
        if (callback != null) {
            synchronized (this.mSceneList) {
                callback.onLoadResult(context, ruleSize > 0, this.mSceneList);
            }
        }
    }

    /* access modifiers changed from: private */
    public void loadFromCache(Context context, InitCallback callback) {
        onLoadResult(context, callback, "loadFromCache");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00f4, code lost:
        if (r9 != null) goto L_0x00f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f6, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0106, code lost:
        if (r9 == null) goto L_0x0109;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0109, code lost:
        if (r15 == null) goto L_0x010e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x010b, code lost:
        r2 = "loadFromProvider";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x010e, code lost:
        r2 = "updateFromProvider";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0111, code lost:
        onLoadResult(r14, r15, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0114, code lost:
        return;
     */
    public void loadFromProvider(Context context, InitCallback callback) {
        String str;
        String str2;
        synchronized (this.mRuleList) {
            this.mRuleList.clear();
        }
        synchronized (this.mSceneList) {
            this.mSceneList.clear();
        }
        this.mSelectionArgs[0] = context.getPackageName();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_RULE, null, "package_name = ?", this.mSelectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                String data = cursor.getString(cursor.getColumnIndex("data"));
                long start = SystemClock.uptimeMillis();
                try {
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
                    str2 = TAG;
                    str = "loadRuleFromJSON=" + (SystemClock.uptimeMillis() - start) + "ms";
                } catch (JSONException e) {
                    Logger.w(TAG, e.getMessage(), e);
                    str2 = TAG;
                    str = "loadRuleFromJSON=" + (SystemClock.uptimeMillis() - start) + "ms";
                } catch (Throwable th) {
                    Logger.d(TAG, "loadRuleFromJSON=" + (SystemClock.uptimeMillis() - start) + "ms");
                    throw th;
                }
                Logger.d(str2, str);
            }
        } catch (Exception e2) {
            Logger.i(TAG, e2.toString());
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
            throw th2;
        }
    }

    private class RulesObserver extends ContentObserver {
        private final Context mContext;
        private final Uri mUri;

        public RulesObserver(Handler handler, Context context, Uri uri) {
            super(handler);
            this.mContext = context;
            this.mUri = uri;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            RuleResources.this.updateRules(this.mContext);
        }

        public Context getContext() {
            return this.mContext;
        }

        public Uri getUri() {
            return this.mUri;
        }
    }
}
