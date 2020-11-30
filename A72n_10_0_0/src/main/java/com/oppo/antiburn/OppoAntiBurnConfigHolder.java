package com.oppo.antiburn;

import android.app.Activity;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OppoAntiBurnConfigHolder {
    private static final String KEY_ACT_CLZ_NAME = "activityLocalClzName";
    private static final String KEY_PKG_NAME = "pkgName";
    private static final String KEY_SPECIAL_ACTION = "specialActions";
    private static final String KEY_VIEW_CONFIG_ARRAY = "views";
    private static final String TAG = "OppoAntiBurnConfigHolder";
    private static final String TAG_VIEW_CMDS = "cmds";
    private static final String TAG_VIEW_DESC = "desc";
    private static final String TAG_VIEW_ID = "viewID";
    private long mConfigTime;
    private final ViewConfigBook mViewConfigBook;

    private OppoAntiBurnConfigHolder() {
        this.mViewConfigBook = new ViewConfigBook();
        this.mConfigTime = 0;
    }

    private static class Holder {
        private static final OppoAntiBurnConfigHolder INSTANCE = new OppoAntiBurnConfigHolder();

        private Holder() {
        }
    }

    public static OppoAntiBurnConfigHolder getInstance() {
        return Holder.INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public void updateConfig(String jsonStr) {
        try {
            JSONObject curAppConfigInfos = new JSONObject(jsonStr);
            String bookJsonString = curAppConfigInfos.optString(KEY_SPECIAL_ACTION);
            synchronized (this.mViewConfigBook.mAccessLock) {
                try {
                    this.mViewConfigBook.clear();
                    int i = 0;
                    this.mViewConfigBook.mHasSpecialViewsConfig = false;
                    JSONArray bookArray = null;
                    if (bookJsonString != null) {
                        try {
                            if (!"".equals(bookJsonString)) {
                                bookArray = new JSONArray(bookJsonString);
                            }
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                    if (bookArray != null) {
                        int i2 = 0;
                        while (i2 < bookArray.length()) {
                            String activityPage = bookArray.optJSONObject(i2).optString(KEY_ACT_CLZ_NAME);
                            JSONArray pageArray = bookArray.optJSONObject(i2).optJSONArray(KEY_VIEW_CONFIG_ARRAY);
                            int j = i;
                            while (j < pageArray.length()) {
                                JSONArray cmdsArray = pageArray.optJSONObject(j).optJSONArray(TAG_VIEW_CMDS);
                                List cmds = new ArrayList();
                                for (int z = i; z < cmdsArray.length(); z++) {
                                    cmds.add(cmdsArray.optString(z));
                                }
                                this.mViewConfigBook.addViewConfigItem(activityPage, new ViewConfigItem(pageArray.optJSONObject(j).optString(TAG_VIEW_ID), activityPage, pageArray.optJSONObject(j).optString(TAG_VIEW_DESC), cmds));
                                this.mViewConfigBook.mHasSpecialViewsConfig = true;
                                j++;
                                bookArray = bookArray;
                                curAppConfigInfos = curAppConfigInfos;
                                i = 0;
                            }
                            i2++;
                            bookArray = bookArray;
                            i = 0;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage(), e);
        }
        this.mConfigTime = SystemClock.uptimeMillis();
        return;
    }

    /* access modifiers changed from: package-private */
    public boolean isTargetActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        try {
            return this.mViewConfigBook.activityCatalog.keySet().contains(activity.getLocalClassName());
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public long getLatestConfigTime() {
        return this.mConfigTime;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSpecialViewsConfig() {
        boolean z;
        synchronized (this.mViewConfigBook.mAccessLock) {
            z = this.mViewConfigBook.mHasSpecialViewsConfig;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public ViewConfigItem getViewConfigItem(View targetView) {
        return this.mViewConfigBook.getViewConfigItem(targetView);
    }

    /* access modifiers changed from: package-private */
    public List getOPFDActionCmds(View targetView) {
        ViewConfigItem item = getViewConfigItem(targetView);
        if (item == null) {
            return null;
        }
        return item.getActions(targetView);
    }

    /* access modifiers changed from: private */
    public class ViewConfigBook {
        HashMap<String, HashMap> activityCatalog;
        private final Object mAccessLock;
        boolean mHasSpecialViewsConfig;

        private ViewConfigBook() {
            this.mAccessLock = new Object();
            this.mHasSpecialViewsConfig = false;
            this.activityCatalog = new HashMap<>();
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            try {
                synchronized (this.mAccessLock) {
                    this.activityCatalog.clear();
                }
            } catch (Exception e) {
                Log.w(OppoAntiBurnConfigHolder.TAG, "View Config Book Clear Exception " + e.getMessage());
                e.printStackTrace();
            }
        }

        /* access modifiers changed from: package-private */
        public void addViewConfigItem(String actName, ViewConfigItem newItem) {
            if (!TextUtils.isEmpty(actName) && newItem != null) {
                synchronized (this.mAccessLock) {
                    HashMap<String, ViewConfigItem> activityPage = this.activityCatalog.get(actName);
                    if (activityPage == null) {
                        activityPage = new HashMap<>();
                        this.activityCatalog.put(actName, activityPage);
                    }
                    if (activityPage.get(newItem.viewID) == null) {
                        activityPage.put(newItem.viewID, newItem);
                    } else {
                        activityPage.put(newItem.viewID, newItem);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public final ViewConfigItem getViewConfigItem(View view) {
            synchronized (this.mAccessLock) {
                String viewID = OppoAntiBurnUtils.getViewID(view);
                Activity activity = OppoAntiBurnUtils.getActivity(view);
                if (activity == null) {
                    return null;
                }
                HashMap<String, ViewConfigItem> targetActivityPage = this.activityCatalog.get(activity.getLocalClassName());
                if (targetActivityPage != null) {
                    if (targetActivityPage.size() >= 1) {
                        return targetActivityPage.get(viewID);
                    }
                }
                return null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class ViewConfigItem {
        final String actLocalClzName;
        final List actions;
        String desc;
        final String viewID;

        ViewConfigItem(String id, String actClzName, String viewDesc, String... actions2) {
            this.viewID = id;
            this.actLocalClzName = actClzName;
            this.actions = new ArrayList();
            for (String one : actions2) {
                this.actions.add(one);
            }
            this.desc = viewDesc;
        }

        ViewConfigItem(String id, String actClzName, String viewDesc, List actions2) {
            this.viewID = id;
            this.actLocalClzName = actClzName;
            this.actions = actions2;
            this.desc = viewDesc;
        }

        /* access modifiers changed from: package-private */
        public List getActions(View v) {
            return this.actions;
        }
    }
}
