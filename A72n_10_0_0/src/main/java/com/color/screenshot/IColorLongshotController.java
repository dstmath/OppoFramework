package com.color.screenshot;

public interface IColorLongshotController {
    boolean findInfo(ColorLongshotViewInfo colorLongshotViewInfo);

    default boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent, int oldScrollY, boolean result) {
        return false;
    }

    default boolean isLongshotConnected() {
        return false;
    }

    default int getOverScrollMode(int overScrollMode) {
        return overScrollMode;
    }
}
