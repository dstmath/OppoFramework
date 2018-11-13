package com.android.server;

import android.content.Context;
import android.os.ServiceManager;

class ColorInjector {

    static class SystemServer {
        SystemServer() {
        }

        static void addService(Context context) {
            ServiceManager.addService("color_screenshot", new ColorScreenshotManagerService(context));
        }
    }

    ColorInjector() {
    }
}
