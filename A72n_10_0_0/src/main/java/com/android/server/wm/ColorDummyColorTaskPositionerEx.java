package com.android.server.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;

public class ColorDummyColorTaskPositionerEx implements IColorTaskPositionerEx {
    int mDelta = 12;
    int mMaxHeight;
    int mMaxWidth;
    int mMinHeight = 222;
    int mMinWidth = 222;
    int mRealSizeX;
    int mRealSizeY;
    WindowManagerService mWms;

    public ColorDummyColorTaskPositionerEx(WindowManagerService wms) {
        this.mWms = wms;
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public void init(Point p, DisplayMetrics displayMetrics, Point maxVisibleSize, Display display) {
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public void showRectDisplayMask(Rect windowDragBounds) {
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public void destroyRectDisplayMask() {
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public int getMinWidth() {
        return this.mMinWidth;
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public int getMinHeight() {
        return this.mMinHeight;
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public int getRealSizeX() {
        return this.mRealSizeX;
    }

    @Override // com.android.server.wm.IColorTaskPositionerEx
    public int getRealSizeY() {
        return this.mRealSizeY;
    }
}
