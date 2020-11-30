package com.android.server;

import android.util.Log;

public class ColorFeatureStatistics {
    private static final String CLASSNAME = "com.android.server.ColorFeatureStatisticsImpl";
    private static String TAG = "ColorFeatureStatistics";
    private static ColorFeatureStatistics sInstance;

    public static ColorFeatureStatistics getInstance() {
        if (sInstance == null) {
            synchronized (ColorFeatureStatistics.class) {
                try {
                    if (sInstance == null) {
                        sInstance = (ColorFeatureStatistics) newInstance(CLASSNAME);
                    }
                } catch (Exception e) {
                    String str = TAG;
                    Log.e(str, " Reflect exception getInstance: " + e.toString());
                    if (sInstance == null) {
                        sInstance = new ColorFeatureStatistics();
                    }
                }
            }
        }
        return sInstance;
    }

    public void startExecution(String feature, String method, long time) {
    }

    public void finishExecution(String feature, String method, long time) {
    }

    protected static Object newInstance(String className) throws Exception {
        return Class.forName(className).getConstructor(new Class[0]).newInstance(new Object[0]);
    }
}
