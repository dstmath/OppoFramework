package com.android.server.policy;

import android.content.Context;
import android.os.Bundle;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.ColorScreenshotManager;
import com.color.util.ColorLog;

class ColorInjector {

    static class PhoneWindowManager {
        PhoneWindowManager() {
        }

        static void takeScreenshot(Context context, Bundle extras) {
            ColorScreenshotManager sm = ColorLongshotUtils.getScreenshotManager(context);
            if (sm != null) {
                sm.takeScreenshot(extras);
                ColorLog.d("LongshotDump", "takeScreenshot : SUCCESS");
                return;
            }
            ColorLog.e("LongshotDump", "takeScreenshot : FAILED");
        }
    }

    ColorInjector() {
    }
}
