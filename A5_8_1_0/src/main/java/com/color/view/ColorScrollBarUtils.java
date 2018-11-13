package com.color.view;

import com.android.internal.widget.ScrollBarUtils;

public class ColorScrollBarUtils {
    public static int getThumbLength(int size, int thickness, int extent, int range) {
        return ScrollBarUtils.getThumbLength(size, thickness, extent, range);
    }
}
