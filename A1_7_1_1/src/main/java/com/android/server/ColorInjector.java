package com.android.server;

import android.content.Context;
import android.os.ServiceManager;
import com.color.screenshot.ColorScreenshotManagerService;

class ColorInjector {

    static class SystemServer {
        SystemServer() {
        }

        static void addService(Context context) {
            ServiceManager.addService("color_screenshot", ColorScreenshotManagerService.getInstance(context));
        }
    }

    ColorInjector() {
    }
}
