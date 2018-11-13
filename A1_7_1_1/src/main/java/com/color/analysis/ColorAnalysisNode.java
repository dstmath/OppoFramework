package com.color.analysis;

import android.graphics.Rect;

public interface ColorAnalysisNode {
    public static final int COLOR_DEFAULT = -256;
    public static final int COLOR_IMAGE = -16776961;
    public static final int COLOR_ITEM = -16711936;
    public static final int COLOR_TEXT = -65536;
    public static final int COLOR_UNKNOWN = 0;

    Rect getClipRect();

    int getColor();

    Rect getFullRect();

    boolean isHighLight();

    void setHighLight(boolean z);
}
