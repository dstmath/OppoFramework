package com.color.widget;

import com.color.animation.ColorFakeAnimator;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey.BottomMenu;
import com.color.widget.ColorBottomMenuView.DrawItem;

class ColorBottomMenuView$DirectAnimator extends ColorFakeAnimator {
    final /* synthetic */ ColorBottomMenuView this$0;

    /* synthetic */ ColorBottomMenuView$DirectAnimator(ColorBottomMenuView this$0, ColorBottomMenuView$DirectAnimator -this1) {
        this(this$0);
    }

    private ColorBottomMenuView$DirectAnimator(ColorBottomMenuView this$0) {
        this.this$0 = this$0;
    }

    protected void onStart() {
        if (this.this$0.mNextItems.size() > 0) {
            ColorLog.d(BottomMenu.ANIM, this.this$0.mTagClass, "----------------------------DirectAnimator : onStart");
            for (DrawItem drawItem : this.this$0.mCurrItems) {
                drawItem.setIconY((float) drawItem.getIconMarginTop());
            }
            this.this$0.invalidate();
        }
    }
}
