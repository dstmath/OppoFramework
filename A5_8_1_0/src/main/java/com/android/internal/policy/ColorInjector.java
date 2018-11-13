package com.android.internal.policy;

import android.R;
import android.content.res.TypedArray;
import android.view.Window;
import com.color.util.ColorContextUtil;

class ColorInjector {

    static class PhoneWindow {
        PhoneWindow() {
        }

        static int overlayStartingLayout(Window window, int layoutResource) {
            if (ColorContextUtil.isOppoStyle(window.getContext()) && window.getAttributes().type == 3) {
                TypedArray a = window.getContext().obtainStyledAttributes(R.styleable.ActionBar);
                int displayOptions = a.getInt(8, 0);
                a.recycle();
                if ((displayOptions & 8) == 0) {
                    return 201917508;
                }
            }
            return layoutResource;
        }
    }

    ColorInjector() {
    }
}
