package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

class ColorBottomMenuView$1 extends AnimatorListenerAdapter {
    final /* synthetic */ ColorBottomMenuView this$0;

    ColorBottomMenuView$1(ColorBottomMenuView this$0) {
        this.this$0 = this$0;
    }

    public void onAnimationEnd(Animator animation) {
        ColorBottomMenuView.-set1(this.this$0, null);
    }
}
