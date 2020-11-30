package com.color.screenshot;

public class ColorLongshotCustomController extends ColorLongshotController {
    private final ColorScreenshotManager mScreenshotManager;

    public ColorLongshotCustomController(ColorLongshotViewBase view, String source) {
        super(view, source);
        this.mScreenshotManager = ColorLongshotUtils.getScreenshotManager(view.getContext());
    }

    public void onLongScroll() {
        if (!canLongshot()) {
            this.mScreenshotManager.notifyOverScroll(new ColorLongshotEvent(this.mViewBase.getClass().getName(), 0, true));
        }
    }

    private boolean canLongshot() {
        ColorLongshotViewBase view = this.mViewBase;
        int offset = view.computeLongScrollOffset();
        if (view.computeLongScrollRange() - view.computeLongScrollExtent() != 0 && offset > 0) {
            return true;
        }
        return false;
    }
}
