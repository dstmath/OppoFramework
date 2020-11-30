package com.oppo.antiburn;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class OppoAntiBurnUtils {
    private static final String TAG = "OppoAntiBurnUtil";

    static Activity getActivity(View view) {
        if (view == null) {
            return null;
        }
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            if (baseContext == context) {
                break;
            }
            context = baseContext;
        }
        return null;
    }

    static List<View> getTargetViews(ViewGroup root, String clzName) {
        List l;
        if (root == null) {
            return null;
        }
        List<View> result = new ArrayList<>();
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = root.getChildAt(i);
            if (v.getClass().getName().contains(clzName)) {
                result.add(v);
            } else if ((v instanceof ViewGroup) && (l = getTargetViews((ViewGroup) v, clzName)) != null) {
                result.addAll(l);
            }
        }
        return result;
    }

    static String getViewID(View view) {
        Object id = resolveId(view.getContext(), view.getId());
        if (id instanceof String) {
            return (String) id;
        }
        return id + "";
    }

    /* JADX INFO: Multiple debug info for r1v2 java.lang.String: [D('fieldValue' java.lang.Object), D('e' android.content.res.Resources$NotFoundException)] */
    static Object resolveId(Context context, int id) {
        Resources resources = context.getResources();
        if (id < 0) {
            return "NO_ID";
        }
        try {
            return resources.getResourceEntryName(id);
        } catch (Resources.NotFoundException e) {
            return "0x" + Integer.toHexString(id).toUpperCase();
        }
    }

    private static class Holder {
        private static final OppoAntiBurnUtils INSTANCE = new OppoAntiBurnUtils();

        private Holder() {
        }
    }

    public static OppoAntiBurnUtils getInstance() {
        return Holder.INSTANCE;
    }
}
