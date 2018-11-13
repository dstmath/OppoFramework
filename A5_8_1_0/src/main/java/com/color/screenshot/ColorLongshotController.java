package com.color.screenshot;

public abstract class ColorLongshotController {
    final String mSource;
    final ColorLongshotViewBase mViewBase;

    public ColorLongshotController(ColorLongshotViewBase view, String source) {
        this.mViewBase = view;
        this.mSource = source;
    }

    public boolean findInfo(ColorLongshotViewInfo info) {
        findUnsupported(info);
        return true;
    }

    private void findUnsupported(ColorLongshotViewInfo info) {
        if ((this.mViewBase instanceof ColorLongshotUnsupported) && this.mViewBase.isLongshotUnsupported()) {
            info.setUnsupported();
        }
    }
}
