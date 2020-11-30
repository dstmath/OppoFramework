package com.android.server.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;

public interface IColorTaskPositionerEx {
    default void init(Point p, DisplayMetrics displayMetrics, Point maxVisibleSize, Display display) {
    }

    default void showRectDisplayMask(Rect windowDragBounds) {
    }

    default void destroyRectDisplayMask() {
    }

    default int getMinWidth() {
        return 0;
    }

    default int getMaxWidth() {
        return 0;
    }

    default int getMinHeight() {
        return 0;
    }

    default int getMaxHeight() {
        return 0;
    }

    default int getRealSizeX() {
        return 0;
    }

    default int getRealSizeY() {
        return 0;
    }
}
