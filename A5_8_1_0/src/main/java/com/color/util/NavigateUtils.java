package com.color.util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.text.TextUtils;
import android.util.Log;
import com.color.actionbar.app.ColorActionBarUtil;

public class NavigateUtils {
    public static final String NAVIGATE_UP_PACKAGE = "navigate_parent_package";
    public static final String NAVIGATE_UP_TITLE_ID = "navigate_title_id";
    public static final String NAVIGATE_UP_TITLE_TEXT = "navigate_title_text";
    private static final String TAG = "NavigateUtils";

    public static void setNavigateTitle(Activity activity, Intent intent) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setNavigateTitle(activity, actionBar, intent);
        }
    }

    public static int getLabelRes(Activity activity) throws NameNotFoundException {
        ActivityInfo info = activity.getPackageManager().getActivityInfo(activity.getComponentName(), 0);
        if (info != null) {
            return info.labelRes;
        }
        return -1;
    }

    public static void setNavigateTitle(Context context, ActionBar actionBar, Intent intent) {
        if (intent == null || actionBar == null) {
            Log.i(TAG, "intent or action bar is null");
            return;
        }
        String contentDescripton = getContentDescriptonById(context, intent);
        if (TextUtils.isEmpty(contentDescripton)) {
            contentDescripton = intent.getStringExtra(NAVIGATE_UP_TITLE_TEXT);
        }
        Log.i(TAG, "contentDescripton " + contentDescripton);
        if (TextUtils.isEmpty(contentDescripton)) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            ColorActionBarUtil.setBackTitle(actionBar, contentDescripton);
        }
    }

    private static String getContentDescriptonById(Context context, Intent intent) {
        if (context == null || intent == null) {
            return null;
        }
        int id = intent.getIntExtra(NAVIGATE_UP_TITLE_ID, 0);
        Log.d(TAG, "getContentDescriptonById: id = " + id);
        if (id == 0) {
            return null;
        }
        String packageName = intent.getStringExtra(NAVIGATE_UP_PACKAGE);
        if (TextUtils.isEmpty(packageName) || (packageName.equals(context.getPackageName()) ^ 1) == 0) {
            return getTitle(context, id);
        }
        Context parentContext = null;
        try {
            parentContext = context.createPackageContext(packageName, 3);
        } catch (NameNotFoundException e) {
        }
        return getTitle(parentContext, id);
    }

    private static String getTitle(Context context, int id) {
        String title = null;
        if (context == null) {
            return null;
        }
        try {
            title = context.getResources().getString(id);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return title;
    }
}
