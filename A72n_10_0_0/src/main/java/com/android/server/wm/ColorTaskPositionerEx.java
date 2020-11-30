package com.android.server.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;

public class ColorTaskPositionerEx extends ColorDummyColorTaskPositionerEx {
    ColorFreeFormRect mRectDisplayMask;

    public ColorTaskPositionerEx(WindowManagerService wms) {
        super(wms);
    }

    public void init(Point p, DisplayMetrics displayMetrics, Point maxVisibleSize, Display display2) {
        this.mMinWidth = WindowManagerService.dipToPixel(this.mMinWidth, displayMetrics);
        this.mMinHeight = WindowManagerService.dipToPixel(this.mMinHeight, displayMetrics);
        if (maxVisibleSize.x > maxVisibleSize.y) {
            maxVisibleSize.set(maxVisibleSize.x / 2, maxVisibleSize.y);
            this.mMaxWidth = maxVisibleSize.y;
            this.mMaxHeight = maxVisibleSize.x;
        } else {
            maxVisibleSize.set(maxVisibleSize.x, maxVisibleSize.y / 2);
            this.mMaxWidth = maxVisibleSize.x;
            this.mMaxHeight = maxVisibleSize.y;
        }
        int screenHeight = this.mWms.getDefaultDisplayContentLocked().getDisplayInfo().logicalHeight;
        int screenWidth = this.mWms.getDefaultDisplayContentLocked().getDisplayInfo().logicalWidth;
        int screenWidth2 = screenWidth > screenHeight ? screenHeight : screenWidth;
        if (this.mMaxWidth > screenWidth2) {
            this.mMaxWidth = screenWidth2 - this.mDelta;
        }
        if (this.mMaxHeight > screenWidth2) {
            int i = screenWidth2 - this.mDelta;
            this.mMaxHeight = i;
            this.mMaxWidth = i;
        }
        this.mRectDisplayMask = new ColorFreeFormRect(display2, this.mWms, "ColorTaskPositioner", this.mMinWidth, this.mMinHeight, new Point(this.mMaxWidth, this.mMaxHeight));
    }

    public void showRectDisplayMask(Rect windowDragBounds) {
        this.mRectDisplayMask.showSurface(this.mWms.getDragLayerLocked(), windowDragBounds);
    }

    public void destroyRectDisplayMask() {
        ColorFreeFormRect colorFreeFormRect = this.mRectDisplayMask;
        if (colorFreeFormRect != null) {
            colorFreeFormRect.destroySurface();
            this.mRectDisplayMask = null;
        }
    }

    public int getMinWidth() {
        return this.mMinWidth;
    }

    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    public int getMinHeight() {
        return this.mMinHeight;
    }

    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    public int getRealSizeX() {
        return this.mRealSizeX;
    }

    public int getRealSizeY() {
        return this.mRealSizeY;
    }
}
