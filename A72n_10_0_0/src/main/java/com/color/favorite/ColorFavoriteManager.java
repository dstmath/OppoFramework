package com.color.favorite;

import android.app.Activity;
import android.app.OppoActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.View;
import com.color.util.ColorContextUtil;
import com.color.util.ColorLog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorFavoriteManager implements IColorFavoriteManager {
    private static final String ACTION_FAVORITE_NOTIFY_FAILED = "com.coloros.favorite.FavoriteBroadcast.NotifyFailed";
    private static final String ACTION_FAVORITE_NOTIFY_START = "com.coloros.favorite.FavoriteService.NotifyStart";
    private static final String EXTRA_PACKAGE_NAME = "package_name";
    private static final String EXTRA_SCENE_LIST = "scene_list";
    private static final boolean NOTIFY_DEBUG = SystemProperties.getBoolean("feature.favorite.notify", false);
    private static final String PACKAGE_FAVORITE = "com.coloros.favorite";
    private static final String RESULT_AUTHORITY = "com.coloros.favorite.result.provider";
    private static final String RESULT_PATH = "result";
    private static final Uri RESULT_URI = new Uri.Builder().scheme("content").authority(RESULT_AUTHORITY).path(RESULT_PATH).build();
    private static final String SETTINGS_FAVORITE = "coloros_favorite";
    private static final String SETTING_FIRST_START = "first_start";
    public static final String TAG = "ColorFavoriteManager";
    private static volatile ColorFavoriteManager sInstance = null;
    private IColorFavoriteEngine mEngine = null;
    private final ColorFavoriteFactory mFactory = new ColorFavoriteFactory();

    private ColorFavoriteManager() {
        setEngine(ColorFavoriteEngines.TEDDY);
    }

    public static ColorFavoriteManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorFavoriteManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorFavoriteManager();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
    }

    public void release() {
        IColorFavoriteEngine iColorFavoriteEngine = this.mEngine;
        if (iColorFavoriteEngine != null) {
            iColorFavoriteEngine.release();
        }
    }

    public void processClick(View clickView, ColorFavoriteCallback callback) {
        IColorFavoriteEngine iColorFavoriteEngine = this.mEngine;
        if (iColorFavoriteEngine != null && iColorFavoriteEngine.isTestOn()) {
            this.mEngine.processClick(clickView, callback);
        }
    }

    public void processCrawl(View rootView, ColorFavoriteCallback callback) {
        if (this.mEngine != null) {
            ColorFavoriteCallback callback2 = new FavoriteProcessCallback(callback);
            new FavoriteStart(callback2, rootView).run();
            this.mEngine.processCrawl(rootView, callback2);
        }
    }

    public void processSave(View rootView, ColorFavoriteCallback callback) {
        if (this.mEngine != null) {
            this.mEngine.processSave(rootView, new FavoriteSaveCallback(callback));
        }
    }

    public void logActivityInfo(Activity activity) {
        IColorFavoriteEngine iColorFavoriteEngine = this.mEngine;
        if (iColorFavoriteEngine != null && iColorFavoriteEngine.isLogOn()) {
            this.mEngine.logActivityInfo(activity);
        }
    }

    public void logViewInfo(View view) {
        IColorFavoriteEngine iColorFavoriteEngine = this.mEngine;
        if (iColorFavoriteEngine != null && iColorFavoriteEngine.isLogOn()) {
            this.mEngine.logViewInfo(view);
        }
    }

    public Handler getWorkHandler() {
        return this.mEngine.getWorkHandler();
    }

    public void setEngine(ColorFavoriteEngines engine) {
        IColorFavoriteEngine iColorFavoriteEngine = this.mEngine;
        if (iColorFavoriteEngine != null) {
            iColorFavoriteEngine.release();
        }
        this.mEngine = this.mFactory.setEngine(engine);
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00d8  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00db A[EDGE_INSN: B:51:0x00db->B:49:0x00db ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x000a A[SYNTHETIC] */
    public boolean isSaved(Context context, String packageName, List<Bundle> data) {
        Throwable th;
        Exception e;
        ContentResolver contentResolver = context.getContentResolver();
        Iterator<Bundle> it = data.iterator();
        boolean isSaved = false;
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Bundle bundle = it.next();
            Cursor cursor = null;
            try {
                StringBuilder selectionBuilder = new StringBuilder();
                selectionBuilder.append("packageName = ?");
                List<String> selectionArgList = new ArrayList<>();
                try {
                    selectionArgList.add(packageName);
                    String title = bundle.getString("data_title");
                    String url = bundle.getString("data_url");
                    boolean z = false;
                    boolean hasTitle = !TextUtils.isEmpty(title);
                    boolean hasUrl = !TextUtils.isEmpty(url);
                    if (hasTitle && hasUrl) {
                        selectionBuilder.append(" AND title = ? AND url = ?");
                        selectionArgList.add(title);
                        selectionArgList.add(url);
                    } else if (hasTitle) {
                        selectionBuilder.append(" AND title = ?");
                        selectionArgList.add(title);
                    } else if (hasUrl) {
                        selectionBuilder.append(" AND url = ?");
                        selectionArgList.add(url);
                    } else {
                        selectionArgList.clear();
                    }
                    if (!selectionArgList.isEmpty() && (cursor = contentResolver.query(RESULT_URI, null, selectionBuilder.toString(), (String[]) selectionArgList.toArray(new String[selectionArgList.size()]), null)) != null) {
                        if (cursor.getCount() > 0) {
                            z = true;
                        }
                        isSaved = z;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (isSaved) {
                        break;
                    }
                } catch (Exception e2) {
                    e = e2;
                    try {
                        ColorLog.e(TAG, e.getMessage(), e);
                        if (0 != 0) {
                        }
                        if (!isSaved) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (0 != 0) {
                        }
                        if (!isSaved) {
                        }
                        return isSaved;
                    }
                }
            } catch (Exception e3) {
                e = e3;
                ColorLog.e(TAG, e.getMessage(), e);
                if (0 != 0) {
                    cursor.close();
                }
                if (!isSaved) {
                    return isSaved;
                }
            } catch (Throwable th3) {
                th = th3;
                if (0 != 0) {
                    cursor.close();
                }
                if (!isSaved) {
                    throw th;
                }
                return isSaved;
            }
        }
        return isSaved;
    }

    private void queryRule(Context context) {
        StringBuilder sb;
        boolean z;
        long spend;
        long start = SystemClock.uptimeMillis();
        try {
            new OppoActivityManager().favoriteQueryRule(context.getPackageName(), new FavoriteQueryCallback(context, this.mEngine));
            spend = SystemClock.uptimeMillis() - start;
            z = DBG;
            sb = new StringBuilder();
        } catch (RemoteException e) {
            ColorLog.e(TAG, e.getMessage(), e);
            spend = SystemClock.uptimeMillis() - start;
            z = DBG;
            sb = new StringBuilder();
        } catch (Exception e2) {
            ColorLog.e(TAG, e2.getMessage(), e2);
            spend = SystemClock.uptimeMillis() - start;
            z = DBG;
            sb = new StringBuilder();
        } catch (Throwable th) {
            boolean z2 = DBG;
            ColorLog.d(z2, TAG, "queryRule : spend=" + (SystemClock.uptimeMillis() - start) + "ms");
            throw th;
        }
        sb.append("queryRule : spend=");
        sb.append(spend);
        sb.append("ms");
        ColorLog.d(z, TAG, sb.toString());
    }

    private void initRule(Context context) {
        IColorFavoriteEngine iColorFavoriteEngine = this.mEngine;
        if (iColorFavoriteEngine != null) {
            iColorFavoriteEngine.init();
            queryRule(context);
        }
    }

    private static class FavoriteStart implements Runnable {
        private final WeakReference<ColorFavoriteCallback> mCallback;
        private final WeakReference<View> mView;

        public FavoriteStart(ColorFavoriteCallback callback, View view) {
            this.mCallback = new WeakReference<>(callback);
            this.mView = new WeakReference<>(view);
        }

        public void run() {
            ColorFavoriteCallback callback = this.mCallback.get();
            View view = this.mView.get();
            if (callback != null && view != null) {
                Context context = view.getContext();
                ColorFavoriteResult result = new ColorFavoriteResult();
                result.setPackageName(context.getPackageName());
                callback.onFavoriteStart(context, result);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class FavoriteQueryCallback extends ColorFavoriteQueryCallback {
        private final WeakReference<Context> mContext;
        private final WeakReference<IColorFavoriteEngine> mEngine;

        public FavoriteQueryCallback(Context context, IColorFavoriteEngine engine) {
            this.mContext = new WeakReference<>(context);
            this.mEngine = new WeakReference<>(engine);
        }

        public void onQueryResult(ColorFavoriteQueryResult result) {
            Context context = this.mContext.get();
            IColorFavoriteEngine engine = this.mEngine.get();
            if (context != null && engine != null) {
                engine.loadRule(context, result.getBundle().getString("data", null), new FavoriteLoadCallback(null));
            }
        }
    }

    private static class FavoriteCallback extends ColorFavoriteCallback {
        private final WeakReference<ColorFavoriteCallback> mCallback;

        public boolean isSettingOn(Context context) {
            return ColorFavoriteHelper.isSettingOn(context);
        }

        public FavoriteCallback(ColorFavoriteCallback callback) {
            this.mCallback = new WeakReference<>(callback);
        }

        public void onFavoriteStart(Context context, ColorFavoriteResult result) {
            ColorFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                callback.onFavoriteStart(context, result);
            }
        }

        public void onFavoriteFinished(Context context, ColorFavoriteResult result) {
            ColorFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                callback.onFavoriteFinished(context, result);
            }
        }
    }

    private static class FavoriteLoadCallback extends FavoriteCallback {
        public FavoriteLoadCallback(ColorFavoriteCallback callback) {
            super(callback);
        }

        @Override // com.color.favorite.ColorFavoriteManager.FavoriteCallback
        public boolean isSettingOn(Context context) {
            return ColorFavoriteHelper.isSettingOn(context);
        }

        public void onLoadFinished(Context context, boolean noRule, boolean emptyRule, ArrayList<String> sceneList) {
            super.onLoadFinished(context, noRule, emptyRule, sceneList);
            Activity activity = ColorContextUtil.getActivityContext(context);
            if (activity == null) {
                logI("onLoadFinished", activity, "Not Activity");
            } else if (noRule) {
                logI("onLoadFinished", activity, "No Rule List");
            } else if (emptyRule) {
                logE("onLoadFinished", activity, "Empty Rule List");
            } else if (!isFirstStart(context)) {
                logI("onLoadFinished", activity, "Load Again");
            } else if (!isSettingOn(context)) {
                logI("onLoadFinished", activity, "Setting Off");
            } else {
                logI("onLoadFinished", activity, "Load First");
                clearFirstStart(context);
                notifyStart(context, context.getPackageName(), sceneList);
            }
        }

        private String buildMessage(String tag, Activity activity, String msg) {
            try {
                return "[" + this.TAG + "][" + tag + "] " + activity + " : " + msg;
            } catch (Exception e) {
                ColorLog.w(false, "AnteaterFavorite", "buildMessage exception! tag = " + tag + ", msg " + msg + ", e = " + e);
                return e.getMessage();
            }
        }

        private void logI(String tag, Activity activity, String msg) {
            ColorLog.i(false, "AnteaterFavorite", buildMessage(tag, activity, msg));
        }

        private void logE(String tag, Activity activity, String msg) {
            ColorLog.e(false, "AnteaterFavorite", buildMessage(tag, activity, msg));
        }

        private SharedPreferences getSharedPreferences(Context context) {
            return context.getSharedPreferences(ColorFavoriteManager.SETTINGS_FAVORITE, 0);
        }

        private void clearFirstStart(Context context) {
            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putBoolean(ColorFavoriteManager.SETTING_FIRST_START, false);
            editor.commit();
        }

        private boolean isFirstStart(Context context) {
            if (ColorFavoriteManager.NOTIFY_DEBUG) {
                return true;
            }
            return getSharedPreferences(context).getBoolean(ColorFavoriteManager.SETTING_FIRST_START, true);
        }

        private void notifyStart(Context context, String packageName, ArrayList<String> sceneList) {
            ColorLog.i(false, "AnteaterFavorite", "[" + this.TAG + "] notifyStart : " + packageName);
            try {
                Intent intent = new Intent();
                intent.setAction(ColorFavoriteManager.ACTION_FAVORITE_NOTIFY_START);
                intent.setPackage(ColorFavoriteManager.PACKAGE_FAVORITE);
                intent.putExtra(ColorFavoriteManager.EXTRA_PACKAGE_NAME, packageName);
                intent.putExtra(ColorFavoriteManager.EXTRA_SCENE_LIST, sceneList);
                context.startForegroundService(intent);
            } catch (SecurityException e) {
                ColorLog.w(this.TAG, e.getMessage(), e);
            } catch (Exception e2) {
                ColorLog.e(this.TAG, e2.getMessage(), e2);
            }
        }
    }

    private static class FavoriteProcessCallback extends FavoriteCallback {
        public FavoriteProcessCallback(ColorFavoriteCallback callback) {
            super(callback);
        }
    }

    private static class FavoriteSaveCallback extends FavoriteCallback {
        public FavoriteSaveCallback(ColorFavoriteCallback callback) {
            super(callback);
        }

        @Override // com.color.favorite.ColorFavoriteManager.FavoriteCallback
        public void onFavoriteFinished(Context context, ColorFavoriteResult result) {
            super.onFavoriteFinished(context, result);
            if (TextUtils.isEmpty(result.getError())) {
                onSaveSuccess(context);
            } else {
                onSaveFailed(context);
            }
        }

        private void onSaveSuccess(Context context) {
            String packageName = context.getPackageName();
            ColorLog.i(false, "AnteaterFavorite", "[" + this.TAG + "] onSaveSuccess : " + packageName);
        }

        private void onSaveFailed(Context context) {
            String packageName = context.getPackageName();
            ColorLog.i(false, "AnteaterFavorite", "[" + this.TAG + "] onSaveFailed : " + packageName);
            try {
                Intent intent = new Intent();
                intent.setAction(ColorFavoriteManager.ACTION_FAVORITE_NOTIFY_FAILED);
                intent.setPackage(ColorFavoriteManager.PACKAGE_FAVORITE);
                intent.putExtra(ColorFavoriteManager.EXTRA_PACKAGE_NAME, packageName);
                context.sendBroadcast(intent);
            } catch (SecurityException e) {
                ColorLog.w(this.TAG, e.getMessage(), e);
            } catch (Exception e2) {
                ColorLog.e(this.TAG, e2.getMessage(), e2);
            }
        }
    }
}
