package com.color.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import com.android.internal.widget.ColorActionBarView;
import com.color.util.ColorViewUtil;

public class ColorActionBarUpContainerLayout extends LinearLayout {
    private static final boolean DBG = false;
    private static final String TAG = "ColorActionBarUpContainerLayout";
    private View mChildExpandedHome;
    private View mChildHome;
    private View mChildTitle;

    public ColorActionBarUpContainerLayout(Context context) {
        this(context, null);
    }

    public ColorActionBarUpContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mChildHome = null;
        this.mChildExpandedHome = null;
        this.mChildTitle = null;
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(2);
        }
        setContentDescription(null);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (isOppoStyle()) {
            findChildViews();
            layoutAlignLeft(this.mChildHome, l, t, r, b);
            layoutAlignLeft(this.mChildExpandedHome, l, t, r, b);
            if (this.mChildTitle != null) {
                boolean childHomeVisible = (this.mChildHome == null || this.mChildHome.getVisibility() != 0) ? DBG : true;
                boolean isMainActionBar = DBG;
                int homeWidth = childHomeVisible ? this.mChildHome.getWidth() : 0;
                if (getParent() instanceof ColorActionBarView) {
                    if (((ColorActionBarView) getParent()).getMainActionBar()) {
                        isMainActionBar = true;
                    } else {
                        isMainActionBar = DBG;
                    }
                }
                Log.d(TAG, "onLayout, display isMainActionBar = " + isMainActionBar + ", childHomeVisible = " + childHomeVisible + ", homeWidth = " + homeWidth + ", mChildHome.getRight = " + this.mChildHome.getRight() + ", l = " + l + ", r = " + r);
                if (childHomeVisible || isMainActionBar) {
                    boolean isLayoutRtl = isLayoutRtl();
                    layoutAlignLeft(this.mChildTitle, isLayoutRtl ? l : homeWidth, t, isLayoutRtl ? r - homeWidth : r, b);
                } else {
                    layoutCenterHorizontal(this.mChildTitle, l, t, r, b);
                }
            }
        }
    }

    private void findChildViews() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getId() == 201458689) {
                this.mChildTitle = child;
            } else if (child.getId() == 201458692) {
                this.mChildHome = child;
            } else if (child.getId() == 201458693) {
                this.mChildExpandedHome = child;
            }
        }
    }

    private void layoutAlignLeft(View child, int l, int t, int r, int b) {
        if (child != null) {
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int left = isLayoutRtl() ? r - width : l;
            int top = t + ((b - height) / 2);
            child.layout(left, top, left + width, top + height);
        }
    }

    private void layoutCenterHorizontal(View child, int l, int t, int r, int b) {
        if (child != null) {
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (width == 0 && height == 0) {
                child.measure(makeUnspecifiedMeasureSpec(), makeUnspecifiedMeasureSpec());
                width = child.getMeasuredWidth();
                height = child.getMeasuredHeight();
            }
            int left = l + ((r - width) / 2);
            int top = t + ((b - height) / 2);
            child.layout(left, top, left + width, top + height);
        }
    }

    private int makeUnspecifiedMeasureSpec() {
        return ColorViewUtil.makeUnspecifiedMeasureSpec();
    }
}
