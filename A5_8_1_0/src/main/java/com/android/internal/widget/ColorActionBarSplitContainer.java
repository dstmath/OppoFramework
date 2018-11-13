package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;

public class ColorActionBarSplitContainer extends ActionBarContainer {
    protected final Class<?> mTagClass;

    public ColorActionBarSplitContainer(Context context) {
        this(context, null);
    }

    public ColorActionBarSplitContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTagClass = getClass();
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isOppoStyle()) {
            int tempHeight = this.mHeight;
            this.mHeight = -1;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            this.mHeight = tempHeight;
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
