package com.android.server.wm;

public class ColorDummyActivityStackEx implements IColorActivityStackEx {
    final ActivityStack mStack;

    public ColorDummyActivityStackEx(ActivityStack stack) {
        this.mStack = stack;
    }

    @Override // com.android.server.wm.IColorActivityStackEx
    public void moveFreeformToBackLocked(boolean inFreeform) {
    }
}
