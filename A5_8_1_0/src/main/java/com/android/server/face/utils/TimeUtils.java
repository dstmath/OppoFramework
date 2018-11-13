package com.android.server.face.utils;

import android.util.Log;
import com.android.server.face.FaceService;

public class TimeUtils {
    public static void calculateTime(String tagName, String mode, long interval) {
        if (FaceService.DEBUG_PERF) {
            Log.d(tagName, mode + ", TimeConsuming = " + interval);
        }
    }
}
