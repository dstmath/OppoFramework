package com.color.inner.widget;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.OppoBaseAbsListView;
import com.color.util.ColorTypeCastingHelper;

public class AbsListViewWrapper {
    private static final String TAG = "AbsListViewWrapper";

    public static int getTouchMode(AbsListView absListView) {
        try {
            return typeCasting(absListView).getTouchMode();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setTouchMode(AbsListView absListView, int mode) {
        try {
            typeCasting(absListView).setTouchMode(mode);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void colorStartSpringback(AbsListView absListView) {
        try {
            typeCasting(absListView).colorStartSpringback();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void setOppoFlingMode(AbsListView absListView, int mode) {
        try {
            typeCasting(absListView).setOppoFlingMode(mode);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private static OppoBaseAbsListView typeCasting(AbsListView absListView) {
        return (OppoBaseAbsListView) ColorTypeCastingHelper.typeCasting(OppoBaseAbsListView.class, absListView);
    }
}
