package com.color.inner.view.accessibility;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.IOppoAccessibilityNodeInfoEx;
import com.color.util.ColorTypeCastingHelper;

public class AccessibilityNodeInfoWrapper {
    private static final String TAG = "AccessibilityNodeInfoWrapper";

    private AccessibilityNodeInfoWrapper() {
    }

    public static CharSequence getRealClassName(AccessibilityNodeInfo nodeInfo) {
        try {
            return typeCasting(nodeInfo).getRealClassName();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    private static IOppoAccessibilityNodeInfoEx typeCasting(AccessibilityNodeInfo nodeInfo) {
        return (IOppoAccessibilityNodeInfoEx) ColorTypeCastingHelper.typeCasting(IOppoAccessibilityNodeInfoEx.class, nodeInfo);
    }
}
