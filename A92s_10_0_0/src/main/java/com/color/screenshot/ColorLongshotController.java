package com.color.screenshot;

public abstract class ColorLongshotController implements IColorLongshotController {
    final String mSource;
    final ColorLongshotViewBase mViewBase;

    public ColorLongshotController(ColorLongshotViewBase view, String source) {
        this.mViewBase = view;
        this.mSource = source;
    }

    @Override // com.color.screenshot.IColorLongshotController
    public boolean findInfo(ColorLongshotViewInfo info) {
        findUnsupported(info);
        return true;
    }

    private void findUnsupported(ColorLongshotViewInfo info) {
        ColorLongshotViewBase colorLongshotViewBase = this.mViewBase;
        if ((colorLongshotViewBase instanceof ColorLongshotUnsupported) && ((ColorLongshotUnsupported) colorLongshotViewBase).isLongshotUnsupported()) {
            info.setUnsupported();
        }
    }
}
