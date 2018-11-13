package com.color.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.color.util.ColorContextUtil;
import com.color.util.ColorDialogUtil;
import oppo.R;

class ColorAlertInjector {

    static class AlertController {
        private static final boolean DBG = false;
        private static final String TAG = "ColorInjector.AlertController";

        AlertController() {
        }

        static void setDialogListener(ColorAlertController dialog, Context context, Window window, int option) {
            if (ColorContextUtil.isOppoStyle(context)) {
                new ColorDialogUtil(context).setDialogDrag(dialog, window, option);
            }
        }

        static void updateWindow(ColorAlertController dialog, Context context, Window window) {
            if (ColorContextUtil.isOppoStyle(context) && window.getAttributes().type != 2021) {
                int layoutWidth;
                boolean oriention = context.getResources().getConfiguration().orientation == 1 ? true : DBG;
                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                LayoutParams params = window.getAttributes();
                TypedArray a = context.obtainStyledAttributes(null, R.styleable.ColorAlertDialog, 16842845, 0);
                if (oriention) {
                    layoutWidth = dm.widthPixels;
                } else {
                    layoutWidth = dm.heightPixels;
                }
                int layoutHeight = a.getLayoutDimension(3, -2);
                if (dialog.getDeleteDialogOption() != 0 || context == null || context.getThemeResId() == 201524238) {
                    window.setGravity(81);
                } else {
                    params.windowAnimations = 201524237;
                    window.setAttributes(params);
                    window.setGravity(a.getInt(1, 17));
                }
                window.setLayout(layoutWidth, layoutHeight);
                a.recycle();
            }
        }
    }

    ColorAlertInjector() {
    }
}
