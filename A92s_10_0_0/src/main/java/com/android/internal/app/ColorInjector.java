package com.android.internal.app;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import com.color.util.ColorContextUtil;

class ColorInjector {
    ColorInjector() {
    }

    /* access modifiers changed from: private */
    public static void updateWindowInternal(boolean isCustomGravity, Context context, Window window, int defaultGravity) {
        if (ColorContextUtil.isOppoStyle(context) && window.getAttributes().type != 2021) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            WindowManager.LayoutParams params = window.getAttributes();
            Point realSize = getScreenSize(context);
            int width = dm.widthPixels;
            int realScreenWidth = realSize.x;
            int realScreenHeight = realSize.y;
            int layoutWidth = Math.min(width, Math.min(realScreenWidth, realScreenHeight));
            int paddingLeft = context.getResources().getDimensionPixelSize(201655677);
            if (isCustomGravity) {
                params.windowAnimations = 201524237;
                window.setAttributes(params);
                window.setGravity(17);
            } else if (defaultGravity != 0) {
                if (realScreenWidth < realScreenHeight) {
                    window.setGravity(81);
                    params.width = layoutWidth;
                } else {
                    int fixedWidth = (paddingLeft * 2) + layoutWidth;
                    if (fixedWidth > dm.widthPixels) {
                        params.width = layoutWidth;
                    } else {
                        params.width = fixedWidth;
                    }
                    window.setGravity(17);
                }
                params.height = -2;
                window.setAttributes(params);
            }
        }
    }

    static Point getScreenSize(Context context) {
        Point point = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        return point;
    }

    static class AlertController {
        AlertController() {
        }

        static void updateWindow(ColorBaseAlertController dialog, Context context, Window window) {
            ColorInjector.updateWindowInternal(isCustomGravity(dialog, context), context, window, 81);
        }

        private static boolean isCustomGravity(ColorBaseAlertController dialog, Context context) {
            if (dialog.getDialogType() != 0 || context == null || context.getThemeResId() == 201524238) {
                return false;
            }
            return true;
        }
    }

    static class ResolverActivity {
        ResolverActivity() {
        }

        static void setTheme(Context context) {
            context.setTheme(201524238);
        }
    }
}
