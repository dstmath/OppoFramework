package com.color.widget;

import android.os.Bundle;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ColorRecyclerViewAccessibilityDelegate extends AccessibilityDelegate {
    final AccessibilityDelegate mItemDelegate = new AccessibilityDelegate() {
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            if (!ColorRecyclerViewAccessibilityDelegate.this.shouldIgnore() && ColorRecyclerViewAccessibilityDelegate.this.mRecyclerView.getLayoutManager() != null) {
                ColorRecyclerViewAccessibilityDelegate.this.mRecyclerView.getLayoutManager().onInitializeAccessibilityNodeInfoForItem(host, info);
            }
        }

        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (super.performAccessibilityAction(host, action, args)) {
                return true;
            }
            if (ColorRecyclerViewAccessibilityDelegate.this.shouldIgnore() || ColorRecyclerViewAccessibilityDelegate.this.mRecyclerView.getLayoutManager() == null) {
                return false;
            }
            return ColorRecyclerViewAccessibilityDelegate.this.mRecyclerView.getLayoutManager().performAccessibilityActionForItem(host, action, args);
        }
    };
    final ColorRecyclerView mRecyclerView;

    public ColorRecyclerViewAccessibilityDelegate(ColorRecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
    }

    private boolean shouldIgnore() {
        return this.mRecyclerView.hasPendingAdapterUpdates();
    }

    public boolean performAccessibilityAction(View host, int action, Bundle args) {
        if (super.performAccessibilityAction(host, action, args)) {
            return true;
        }
        if (shouldIgnore() || this.mRecyclerView.getLayoutManager() == null) {
            return false;
        }
        return this.mRecyclerView.getLayoutManager().performAccessibilityAction(action, args);
    }

    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        info.setClassName(ColorRecyclerView.class.getName());
        if (!shouldIgnore() && this.mRecyclerView.getLayoutManager() != null) {
            this.mRecyclerView.getLayoutManager().onInitializeAccessibilityNodeInfo(info);
        }
    }

    public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(host, event);
        event.setClassName(ColorRecyclerView.class.getName());
        if ((host instanceof ColorRecyclerView) && (shouldIgnore() ^ 1) != 0) {
            ColorRecyclerView rv = (ColorRecyclerView) host;
            if (rv.getLayoutManager() != null) {
                rv.getLayoutManager().onInitializeAccessibilityEvent(event);
            }
        }
    }

    AccessibilityDelegate getItemDelegate() {
        return this.mItemDelegate;
    }
}
