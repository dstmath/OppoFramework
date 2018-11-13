package com.color.widget;

import android.animation.Animator;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey.BottomMenu;
import com.color.widget.ColorBottomMenuCallback.Updater;

class ColorBottomMenuView$DirectUpdater implements Updater {
    final /* synthetic */ ColorBottomMenuView this$0;

    /* synthetic */ ColorBottomMenuView$DirectUpdater(ColorBottomMenuView this$0, ColorBottomMenuView$DirectUpdater -this1) {
        this(this$0);
    }

    private ColorBottomMenuView$DirectUpdater(ColorBottomMenuView this$0) {
        this.this$0 = this$0;
    }

    public Animator getUpdater(int currCount, int nextCount) {
        ColorLog.d(BottomMenu.ANIM, this.this$0.mTagClass, "----------------------------getDirectUpdater : ", Integer.valueOf(currCount), "=>", Integer.valueOf(nextCount));
        ColorBottomMenuView.-wrap3(this.this$0);
        return new ColorBottomMenuView$DirectAnimator(this.this$0, null);
    }

    public boolean visibleFirst() {
        return false;
    }
}
