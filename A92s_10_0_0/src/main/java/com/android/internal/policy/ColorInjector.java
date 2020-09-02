package com.android.internal.policy;

import android.content.Context;
import android.text.TextUtils;
import android.view.WindowManager;
import com.color.util.ColorDisplayCompatUtils;

class ColorInjector {
    ColorInjector() {
    }

    static class PhoneWindow {
        PhoneWindow() {
        }

        static void generateLayoutInPhoneWindow(Context context, WindowManager.LayoutParams params) {
            String name = context.getPackageName();
            if (!TextUtils.isEmpty(name) && !name.startsWith("android.server.cts") && !name.startsWith("android.server.am") && !ColorDisplayCompatUtils.getInstance().shouldNonImmersiveAdjustForPkg(name)) {
                params.layoutInDisplayCutoutMode = 3;
            }
        }
    }
}
