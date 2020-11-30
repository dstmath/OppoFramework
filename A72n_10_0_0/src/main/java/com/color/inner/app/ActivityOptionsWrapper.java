package com.color.inner.app;

import android.app.ActivityOptions;
import android.util.Log;

public class ActivityOptionsWrapper {
    private static final String TAG = "ActivityOptionsWrapper";

    private ActivityOptionsWrapper() {
    }

    public static void setLaunchWindowingMode(ActivityOptions activityOptions, int windowingMode) {
        if (activityOptions != null) {
            try {
                activityOptions.setLaunchWindowingMode(windowingMode);
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
