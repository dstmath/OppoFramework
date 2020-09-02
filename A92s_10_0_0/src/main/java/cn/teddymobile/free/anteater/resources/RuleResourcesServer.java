package cn.teddymobile.free.anteater.resources;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import cn.teddymobile.free.anteater.logger.Logger;
import java.util.HashMap;
import java.util.Map;

public class RuleResourcesServer {
    private static final String POSTFIX_PARSER = "Parser";
    private static final String[] PROJECTION = {"package_name", "data"};
    private static final String TAG = RuleResourcesServer.class.getSimpleName();
    private static final Uri URI_RULE = new Uri.Builder().scheme("content").authority(UriConstants.AUTHORITY).path(UriConstants.PATH_RULE).build();
    /* access modifiers changed from: private */
    public final Map<String, RuleServerCallback> mCallbackCache = new HashMap();
    private final Handler mHandler;
    private boolean mInited = false;
    private boolean mObserved = false;
    private final Map<String, String> mRuleCache = new HashMap();
    private RulesObserver mRulesObserver = null;

    public interface QueryCallback {
        Handler createWorkHandler(Context context, String str, int i);

        void linkBinderToDeath(IBinder.DeathRecipient deathRecipient);

        void onQueryResult(Context context, String str);

        void unlinkBinderToDeath(IBinder.DeathRecipient deathRecipient);
    }

    public RuleResourcesServer(Handler handler) {
        Logger.d(TAG, "<init>()");
        this.mHandler = handler;
    }

    public void startQuery(final Context context, final String packageName, final QueryCallback callback) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class cn.teddymobile.free.anteater.resources.RuleResourcesServer.AnonymousClass1 */

                public void run() {
                    RuleResourcesServer.this.registerObserver(context);
                    RuleResourcesServer.this.updateCache(context, false, "startQuery");
                    RuleResourcesServer.this.updateCallback(packageName, callback);
                    RuleResourcesServer ruleResourcesServer = RuleResourcesServer.this;
                    ruleResourcesServer.onQueryResult(context, ruleResourcesServer.getRule(packageName), callback);
                }
            });
        }
    }

    public void registerObserver(Context context) {
        Handler handler = this.mHandler;
        if (handler != null && !this.mObserved) {
            Uri uri = URI_RULE;
            if (this.mRulesObserver == null) {
                this.mRulesObserver = new RulesObserver(handler, context, uri);
            }
            try {
                context.getContentResolver().registerContentObserver(uri, false, this.mRulesObserver);
                String str = TAG;
                Logger.i(str, "registerObserver : " + uri);
                this.mObserved = true;
            } catch (Exception e) {
                Logger.i(TAG, e.toString());
            }
        }
    }

    public void unregisterObserver() {
        RulesObserver rulesObserver = this.mRulesObserver;
        if (rulesObserver != null) {
            rulesObserver.getContext().getContentResolver().unregisterContentObserver(this.mRulesObserver);
            String str = TAG;
            Logger.i(str, "unregisterObserver : " + this.mRulesObserver.getUri());
            this.mRulesObserver = null;
        }
    }

    /* access modifiers changed from: private */
    public String getRule(String packageName) {
        String str;
        synchronized (this.mRuleCache) {
            str = this.mRuleCache.get(packageName);
        }
        return str;
    }

    /* access modifiers changed from: private */
    public void onQueryResult(Context context, String rule, QueryCallback callback) {
        if (callback != null) {
            callback.onQueryResult(context, rule);
        }
    }

    private String getString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndex(column));
    }

    /* access modifiers changed from: private */
    public void updateCache(Context context, boolean update, String tag) {
        String str;
        String str2;
        synchronized (RuleResourcesServer.class) {
            if (!this.mInited || update) {
                long start = SystemClock.uptimeMillis();
                synchronized (this.mRuleCache) {
                    this.mRuleCache.clear();
                }
                Cursor cursor = null;
                try {
                    Cursor cursor2 = context.getContentResolver().query(URI_RULE, PROJECTION, null, null, null);
                    if (cursor2 != null && cursor2.moveToFirst()) {
                        do {
                            String packageName = getString(cursor2, "package_name");
                            String data = getString(cursor2, "data");
                            if (!(packageName == null || data == null)) {
                                synchronized (this.mRuleCache) {
                                    this.mRuleCache.put(packageName, data);
                                }
                            }
                        } while (cursor2.moveToNext());
                        this.mInited = true;
                    }
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    str2 = TAG;
                    str = "updateCache [" + tag + "] : spend=" + (SystemClock.uptimeMillis() - start) + "ms";
                } catch (Exception e) {
                    Logger.i(TAG, e.toString());
                    if (cursor != null) {
                        cursor.close();
                    }
                    str2 = TAG;
                    str = "updateCache [" + tag + "] : spend=" + (SystemClock.uptimeMillis() - start) + "ms";
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    Logger.d(TAG, "updateCache [" + tag + "] : spend=" + (SystemClock.uptimeMillis() - start) + "ms");
                    throw th;
                }
                Logger.d(str2, str);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateCallback(String packageName, QueryCallback callback) {
        long start = SystemClock.uptimeMillis();
        RuleServerCallback serverCallback = null;
        if (getRule(packageName) != null) {
            synchronized (this.mCallbackCache) {
                serverCallback = this.mCallbackCache.get(packageName);
                if (serverCallback == null && callback != null) {
                    serverCallback = new RuleServerCallback(this.mCallbackCache);
                    this.mCallbackCache.put(packageName, serverCallback);
                }
            }
        }
        long spend = SystemClock.uptimeMillis() - start;
        if (serverCallback != null) {
            String str = TAG;
            Logger.d(str, "updateCallback : " + packageName + " : spend=" + spend + "ms");
            serverCallback.setCallback(callback);
            return;
        }
        String str2 = TAG;
        Logger.d(str2, "noRuleData : " + packageName + " : spend=" + spend + "ms");
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
            RuleResourcesServer.this.updateCache(this.mContext, true, "onChange");
            synchronized (RuleResourcesServer.this.mCallbackCache) {
                for (Map.Entry<String, RuleServerCallback> entry : RuleResourcesServer.this.mCallbackCache.entrySet()) {
                    RuleResourcesServer.this.onQueryResult(this.mContext, RuleResourcesServer.this.getRule(entry.getKey()), entry.getValue().getCallback());
                }
            }
        }

        public Context getContext() {
            return this.mContext;
        }

        public Uri getUri() {
            return this.mUri;
        }
    }
}
