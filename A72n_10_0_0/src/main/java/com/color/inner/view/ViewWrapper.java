package com.color.inner.view;

import android.util.Log;
import android.view.OppoBaseView;
import android.view.View;
import com.color.util.ColorTypeCastingHelper;

public class ViewWrapper {
    private static final String TAG = "ViewWrapper";

    public static boolean isVisibleToUser(View view) {
        try {
            return view.isVisibleToUser();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean isLayoutRtl(View view) {
        try {
            return view.isLayoutRtl();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static void setScrollXForColor(View view, int x) {
        try {
            typeCasting(view).setScrollXForColor(x);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void setScrollYForColor(View view, int y) {
        try {
            typeCasting(view).setScrollYForColor(y);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static boolean requestAccessibilityFocus(View view) {
        try {
            return view.requestAccessibilityFocus();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    private static OppoBaseView typeCasting(View view) {
        return (OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, view);
    }
}
