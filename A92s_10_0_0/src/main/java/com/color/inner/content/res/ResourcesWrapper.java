package com.color.inner.content.res;

import android.content.res.ColorBaseResources;
import android.content.res.Resources;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class ResourcesWrapper {
    private static final String TAG = "ResourcesWrapper";

    public static float getCompatApplicationScale(Resources res) {
        try {
            return res.getCompatibilityInfo().applicationScale;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1.0f;
        }
    }

    public static boolean getThemeChanged(Resources res) {
        try {
            return typeCasting(res).getThemeChanged();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static void setIsThemeChanged(Resources res, boolean changed) {
        try {
            typeCasting(res).setIsThemeChanged(changed);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    private static ColorBaseResources typeCasting(Resources resources) {
        return (ColorBaseResources) ColorTypeCastingHelper.typeCasting(ColorBaseResources.class, resources);
    }
}
