package com.color.widget;

import com.color.util.ColorLog;
import com.color.util.ColorLogKey.BottomMenu;
import com.color.widget.ColorBottomMenuView.ItemStateRunnable;

class ColorBottomMenuView$CheckForLongPress extends ItemStateRunnable {
    private int mOriginalWindowAttachCount = 0;
    final /* synthetic */ ColorBottomMenuView this$0;

    public ColorBottomMenuView$CheckForLongPress(ColorBottomMenuView this$0, int position) {
        this.this$0 = this$0;
        super(this$0, position);
    }

    public void run() {
        int position = getPosition();
        if (this.this$0.isItemPressedInternal(this.this$0.getDrawItem(position)) && this.this$0.getParent() != null && this.mOriginalWindowAttachCount == ColorBottomMenuView.-wrap0(this.this$0)) {
            ColorLog.d(BottomMenu.PRESS, this.this$0.mTagClass, "CheckForLongPress : run");
            if (this.this$0.performLongClick(position)) {
                ColorBottomMenuView.-set0(this.this$0, true);
            }
        }
        super.run();
    }

    public void rememberWindowAttachCount() {
        this.mOriginalWindowAttachCount = ColorBottomMenuView.-wrap0(this.this$0);
    }
}
