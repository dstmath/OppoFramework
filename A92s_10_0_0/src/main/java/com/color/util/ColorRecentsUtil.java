package com.color.util;

import android.util.Log;

public class ColorRecentsUtil {
    public static final String LOCK_APPS_FILE_NAME = "locked_apps.xml";
    public static final int NAVIGATION_MODE_GUESTURE = 2;
    public static final String RECENT_TASK_FILES_PATH = "/data/oppo/coloros/recenttask";
    private static final String TAG = "ColorRecentsUtil";
    public static boolean sDebug = false;

    public static boolean isThumbnailNeedStretch(int thumbnailWidth, int thumbnailHeight, int navigationMode, int displayWidth, int diplayHeight, int navigationBarHeight, int statusBarHeight) {
        if (sDebug) {
            Log.d(TAG, "isThumbnailNeedStretch thumbnailWidth:" + thumbnailWidth + " thumbnailHeight:" + thumbnailHeight + " navigationMode:" + navigationMode + " navigationMode:" + navigationMode + " displayWidth:" + displayWidth + " diplayHeight:" + diplayHeight + " navigationBarHight:" + navigationBarHeight + " statusBarHight:" + statusBarHeight);
        }
        int dh = Math.max(displayWidth, diplayHeight);
        int dw = Math.min(displayWidth, diplayHeight);
        double thumbnailRatio = ((double) Math.min(thumbnailWidth, thumbnailHeight)) / ((double) Math.max(thumbnailWidth, thumbnailHeight));
        if (navigationMode == 2) {
            if (sDebug) {
                Log.d(TAG, "isThumbnailNeedStretch thumbnailRatio:" + thumbnailRatio + " (dw / dh):" + (((double) dw) / ((double) dh)) + " ((dw - statusBarHight) / dh):" + (((double) (dw - statusBarHeight)) / ((double) dh)));
            }
            return thumbnailRatio == ((double) dw) / ((double) dh) || thumbnailRatio == ((double) (dw - statusBarHeight)) / ((double) dh);
        }
        if (sDebug) {
            Log.d(TAG, "isThumbnailNeedStretch thumbnailRatio:" + thumbnailRatio + " (dw / (dh - navigationBarHight)):" + (((double) dw) / ((double) (dh - navigationBarHeight))) + " ((dw - statusBarHight) / (dh - navigationBarHight)):" + (((double) (dw - statusBarHeight)) / ((double) (dh - navigationBarHeight))));
        }
        return thumbnailRatio == ((double) dw) / ((double) (dh - navigationBarHeight)) || thumbnailRatio == ((double) (dw - statusBarHeight)) / ((double) (dh - navigationBarHeight));
    }
}
