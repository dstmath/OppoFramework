package com.android.server;

import android.content.Context;
import java.util.HashMap;

public class ColorFeatureManager {
    private static final String TAG = "ColorFeatureManager";
    private static final HashMap<String, Boolean> sFeatureSwitchMap = new HashMap<>();
    private static final HashMap<String, Boolean> sFeatureTraceMap = new HashMap<>();
    private static ColorFeatureManager sInstance = null;

    public ColorFeatureManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorFeatureManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorFeatureManager();
                }
            }
        }
        return sInstance;
    }

    public static void init(Context context) {
    }

    public static boolean isSupport(String name) {
        synchronized (sFeatureSwitchMap) {
        }
        return true;
    }

    public static boolean isTracing(String name) {
        synchronized (sFeatureTraceMap) {
        }
        return false;
    }
}
