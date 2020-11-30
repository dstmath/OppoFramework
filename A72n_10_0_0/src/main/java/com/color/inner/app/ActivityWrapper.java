package com.color.inner.app;

import android.app.Activity;
import android.app.ActivityOptions;

public class ActivityWrapper {
    private static final String TAG = "ActivityWrapper";

    private ActivityWrapper() {
    }

    public static boolean convertToTranslucent(Activity activity, ActivityOptions options) {
        return activity.convertToTranslucent(null, options);
    }

    public static void convertFromTranslucent(Activity activity) {
        activity.convertFromTranslucent();
    }

    public static final boolean isResumed(Activity activity) {
        return activity.isResumed();
    }
}
