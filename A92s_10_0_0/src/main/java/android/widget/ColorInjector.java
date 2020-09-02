package android.widget;

import android.content.res.ColorStateList;
import android.text.TextUtils;

class ColorInjector {
    ColorInjector() {
    }

    static class TextView {
        private static final String[] VIEWS_SPECIFIED_COLORS = {"com.alipay.android.app.ui.quickpay.widget.CustomPasswordEditText", "com.alipay.android.app.ui.quickpay.widget.CustomEditText", null};

        TextView() {
        }

        static ColorStateList getSpecifiedColors(TextView textView, ColorStateList textColor) {
            if (isViewSpecifiedColors(textView.getClass().getName())) {
                return ColorStateList.valueOf(textView.getResources().getColor(17170435));
            }
            return textColor;
        }

        private static boolean isViewSpecifiedColors(String viewName) {
            String[] strArr = VIEWS_SPECIFIED_COLORS;
            for (String name : strArr) {
                if (!TextUtils.isEmpty(name) && name.equals(viewName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
