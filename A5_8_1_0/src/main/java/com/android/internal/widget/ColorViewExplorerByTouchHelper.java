package com.android.internal.widget;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.IntArray;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ColorViewExplorerByTouchHelper extends ExploreByTouchHelper {
    private static final String VIEW_LOG_TAG = "ColorViewTouchHelper";
    private ColorViewTalkBalkInteraction mColorViewTalkBalkInteraction = null;
    private View mHostView = null;
    private final Rect mTempRect = new Rect();

    public interface ColorViewTalkBalkInteraction {
        CharSequence getClassName();

        int getCurrentPosition();

        int getDisablePosition();

        void getItemBounds(int i, Rect rect);

        int getItemCounts();

        CharSequence getItemDescription(int i);

        int getVirtualViewAt(float f, float f2);

        void performAction(int i, int i2, boolean z);
    }

    public ColorViewExplorerByTouchHelper(View host) {
        super(host);
        this.mHostView = host;
    }

    public void setFocusedVirtualView(int virtualViewId) {
        getAccessibilityNodeProvider(this.mHostView).performAction(virtualViewId, 64, null);
    }

    public void clearFocusedVirtualView() {
        int focusedVirtualView = getFocusedVirtualView();
        if (focusedVirtualView != Integer.MIN_VALUE) {
            getAccessibilityNodeProvider(this.mHostView).performAction(focusedVirtualView, 128, null);
        }
    }

    protected int getVirtualViewAt(float x, float y) {
        int position = this.mColorViewTalkBalkInteraction.getVirtualViewAt(x, y);
        if (position >= 0) {
            return position;
        }
        return Integer.MIN_VALUE;
    }

    protected void getVisibleVirtualViews(IntArray virtualViewIds) {
        for (int day = 0; day < this.mColorViewTalkBalkInteraction.getItemCounts(); day++) {
            virtualViewIds.add(day);
        }
    }

    protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
        event.setContentDescription(this.mColorViewTalkBalkInteraction.getItemDescription(virtualViewId));
    }

    protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
        getItemBounds(virtualViewId, this.mTempRect);
        node.setContentDescription(this.mColorViewTalkBalkInteraction.getItemDescription(virtualViewId));
        node.setBoundsInParent(this.mTempRect);
        if (this.mColorViewTalkBalkInteraction.getClassName() != null) {
            node.setClassName(this.mColorViewTalkBalkInteraction.getClassName());
        }
        node.addAction(16);
        if (virtualViewId == this.mColorViewTalkBalkInteraction.getCurrentPosition()) {
            node.setSelected(true);
        }
        if (virtualViewId == this.mColorViewTalkBalkInteraction.getDisablePosition()) {
            node.setEnabled(false);
        }
    }

    protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
        switch (action) {
            case 16:
                this.mColorViewTalkBalkInteraction.performAction(virtualViewId, 16, false);
                return true;
            default:
                return false;
        }
    }

    private void getItemBounds(int position, Rect rect) {
        if (position >= 0 && position < this.mColorViewTalkBalkInteraction.getItemCounts()) {
            this.mColorViewTalkBalkInteraction.getItemBounds(position, rect);
        }
    }

    public void setColorViewTalkBalkInteraction(ColorViewTalkBalkInteraction colorViewTalkBalkInteraction) {
        this.mColorViewTalkBalkInteraction = colorViewTalkBalkInteraction;
    }
}
