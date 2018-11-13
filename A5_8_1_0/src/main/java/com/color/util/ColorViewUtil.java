package com.color.util;

import android.content.res.Resources;
import android.view.View;
import android.view.View.MeasureSpec;

public class ColorViewUtil {
    private static final String TAG = "ColorViewUtil";

    public static int makeUnspecifiedMeasureSpec() {
        return MeasureSpec.makeMeasureSpec(0, 0);
    }

    public static int makeAtMostMeasureSpec(int measureSize) {
        return MeasureSpec.makeMeasureSpec(measureSize, Integer.MIN_VALUE);
    }

    public static int makeExactlyMeasureSpec(int measureSize) {
        return MeasureSpec.makeMeasureSpec(measureSize, 1073741824);
    }

    public static String dumpViewDetail(View view) {
        StringBuilder out = new StringBuilder();
        try {
            out.append(view.getClass().getName());
            out.append('{');
            out.append(Integer.toHexString(System.identityHashCode(view)));
            out.append(' ');
            out.append(view.getLeft());
            out.append(',');
            out.append(view.getTop());
            out.append('-');
            out.append(view.getRight());
            out.append(',');
            out.append(view.getBottom());
            int id = view.getId();
            if (id != -1) {
                out.append(" #");
                out.append(Integer.toHexString(id));
                ColorResourcesUtil.dumpResourceInternal(view.getResources(), id, out, false);
            }
            out.append("}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public static String dumpView(View view) {
        StringBuilder out = new StringBuilder();
        out.append(view.getClass().getName());
        int id = view.getId();
        if (id != -1) {
            out.append("[");
            ColorResourcesUtil.dumpResourceInternal(view.getResources(), id, out, false);
            out.append("]");
        }
        return out.toString();
    }

    public static String getIdName(Resources res, int id) {
        StringBuilder out = new StringBuilder();
        if (id != -1) {
            ColorResourcesUtil.dumpResourceInternal(res, id, out, true);
        }
        return out.toString();
    }

    public static String getViewIdName(View view) {
        return getIdName(view.getResources(), view.getId());
    }
}
