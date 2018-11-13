package com.color.widget;

import android.view.ViewConfiguration;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey.BottomMenu;
import com.color.widget.ColorBottomMenuView.DrawItem;
import com.color.widget.ColorBottomMenuView.ItemStateRunnable;

class ColorBottomMenuView$CheckForTap extends ItemStateRunnable {
    final /* synthetic */ ColorBottomMenuView this$0;

    public ColorBottomMenuView$CheckForTap(ColorBottomMenuView this$0, int position) {
        this.this$0 = this$0;
        super(this$0, position);
    }

    public void run() {
        int position = getPosition();
        DrawItem drawItem = this.this$0.getDrawItem(position);
        if (drawItem != null) {
            ColorLog.d(BottomMenu.PRESS, this.this$0.mTagClass, "CheckForTap : run");
            ColorBottomMenuView.-wrap2(this.this$0, drawItem, false);
            this.this$0.setItemPressedInternal(drawItem, true, true);
            ColorBottomMenuView.-wrap1(this.this$0, ViewConfiguration.getTapTimeout(), position);
        }
        super.run();
    }
}
