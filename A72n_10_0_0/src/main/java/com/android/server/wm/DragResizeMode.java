package com.android.server.wm;

/* access modifiers changed from: package-private */
public class DragResizeMode {
    static final int DRAG_RESIZE_MODE_DOCKED_DIVIDER = 1;
    static final int DRAG_RESIZE_MODE_FREEFORM = 0;

    DragResizeMode() {
    }

    static boolean isModeAllowedForStack(TaskStack stack, int mode) {
        if (mode == 0) {
            return stack.getWindowingMode() == 5;
        }
        if (mode != 1) {
            return false;
        }
        return stack.inSplitScreenWindowingMode();
    }
}
