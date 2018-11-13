package android.widget;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import com.android.internal.R;

class ColorInjector {

    static class TextView {
        private static final String[] VIEWS_SPECIFIED_COLORS = new String[]{"com.alipay.android.app.ui.quickpay.widget.CustomPasswordEditText", "com.alipay.android.app.ui.quickpay.widget.CustomEditText", null};

        TextView() {
        }

        static ColorStateList getSpecifiedColors(TextView textView, ColorStateList textColor) {
            if (isViewSpecifiedColors(textView.getClass().getName())) {
                return ColorStateList.valueOf(textView.getResources().getColor(R.color.primary_text_light));
            }
            return textColor;
        }

        private static boolean isViewSpecifiedColors(String viewName) {
            for (String name : VIEWS_SPECIFIED_COLORS) {
                if (!TextUtils.isEmpty(name) && name.equals(viewName)) {
                    return true;
                }
            }
            return false;
        }
    }

    ColorInjector() {
    }
}
