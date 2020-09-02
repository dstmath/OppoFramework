package com.color.screenshot;

import android.view.IColorBaseViewRoot;
import android.view.OppoBaseView;

public class ColorLongshotViewController extends ColorLongshotController {
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String TAG = "LongshotDump";
    private final OppoBaseView mView;

    public ColorLongshotViewController(OppoBaseView view) {
        super(view, "View");
        this.mView = view;
    }

    @Override // com.color.screenshot.IColorLongshotController
    public boolean isLongshotConnected() {
        IColorBaseViewRoot viewRoot = this.mView.getViewRootImpl();
        if (viewRoot != null) {
            return viewRoot.getLongshotViewRoot().isConnected();
        }
        return false;
    }

    @Override // com.color.screenshot.IColorLongshotController
    public int getOverScrollMode(int overScrollMode) {
        return overScrollMode;
    }

    @Override // com.color.screenshot.IColorLongshotController
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent, int oldScrollY, boolean result) {
        int top = -maxOverScrollY;
        int bottom = maxOverScrollY + scrollRangeY;
        boolean clampedY = false;
        int newScrollY = scrollY + deltaY;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }
        if (newScrollY == oldScrollY) {
            return result;
        }
        int left = -maxOverScrollX;
        int right = maxOverScrollX + scrollRangeX;
        boolean clampedX = false;
        int newScrollX = scrollX + deltaX;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }
        this.mView.onLongshotOverScrolled(newScrollX, newScrollY, clampedX, clampedY);
        return clampedX || clampedY;
    }
}
