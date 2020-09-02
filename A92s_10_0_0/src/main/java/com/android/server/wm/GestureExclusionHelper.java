package com.android.server.wm;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class GestureExclusionHelper {
    /* access modifiers changed from: private */
    public static String TAG = "GestureExclusionHelper";
    private static GestureExclusionHelper sGestureExclusionHelper = new GestureExclusionHelper();
    private final int ACTIVITY_LIST_INDEX = 1;
    private final int PACKAGE_NAME_INDEX = 0;
    /* access modifiers changed from: private */
    public boolean mBeginLoad = false;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public List<List<String>> mGestureExclusionInfo;
    /* access modifiers changed from: private */
    public boolean mInit = false;

    public static GestureExclusionHelper getInstance() {
        return sGestureExclusionHelper;
    }

    private GestureExclusionHelper() {
    }

    public void init(Context context) {
        if (!this.mInit && !this.mBeginLoad) {
            this.mGestureExclusionInfo = new ArrayList();
            this.mContext = context;
            new Thread() {
                /* class com.android.server.wm.GestureExclusionHelper.AnonymousClass1 */

                public void run() {
                    if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Log.d(GestureExclusionHelper.TAG, "start thread to load gesture exclusion info.");
                    }
                    Cursor cursor = GestureExclusionHelper.this.mContext.getContentResolver().query(Uri.parse("content://com.oppo.systemui/gesture_exclusion"), null, null, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            ArrayList arrayList = new ArrayList();
                            String pkgInfo = cursor.getString(0);
                            String activityInfo = cursor.getString(1);
                            arrayList.add(pkgInfo);
                            arrayList.add(activityInfo);
                            GestureExclusionHelper.this.mGestureExclusionInfo.add(arrayList);
                        }
                        boolean unused = GestureExclusionHelper.this.mInit = true;
                        cursor.close();
                        boolean unused2 = GestureExclusionHelper.this.mBeginLoad = false;
                        if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            String access$000 = GestureExclusionHelper.TAG;
                            Log.d(access$000, "init gesture exclusion info complete. info size:" + GestureExclusionHelper.this.mGestureExclusionInfo.size());
                        }
                    } else if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                        Log.e(GestureExclusionHelper.TAG, "cursor is null.");
                    }
                }
            }.start();
            this.mBeginLoad = true;
        }
    }

    public boolean checkGestureExclusion(Context context, String pkg, String activity) {
        if (!this.mInit && !this.mBeginLoad) {
            init(context);
            if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Log.d(TAG, "gesture exclusion data did not begin init.Now begin init");
            }
            return false;
        } else if (this.mInit || !this.mBeginLoad) {
            if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                String str = TAG;
                Log.d(str, "checkGestureExclusion pkg:" + pkg + " ,activity:" + activity);
            }
            for (List<String> infoItem : this.mGestureExclusionInfo) {
                if (infoItem.get(0) != null && pkg.equals(infoItem.get(0))) {
                    if (activity.isEmpty()) {
                        if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            String str2 = TAG;
                            Log.d(str2, "checkGestureExclusion package is in the list:" + pkg);
                        }
                        return true;
                    } else if (infoItem.get(1) == null) {
                        return true;
                    } else {
                        boolean containActivity = infoItem.get(1).contains(activity);
                        if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                            String str3 = TAG;
                            Log.d(str3, "checkGestureExclusion activity is in the list:" + activity);
                        }
                        return !containActivity;
                    }
                }
            }
            if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                String str4 = TAG;
                Log.d(str4, "not in white list pkg:" + pkg + " ,activity:" + activity);
            }
            return false;
        } else {
            if (OppoWindowManagerDebugConfig.DEBUG_OPPO_SYSTEMBAR) {
                Log.d(TAG, "gesture exclusion data did not finish init");
            }
            return false;
        }
    }
}
