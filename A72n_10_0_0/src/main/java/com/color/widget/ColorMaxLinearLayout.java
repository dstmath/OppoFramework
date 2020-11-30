package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import com.oppo.internal.R;

public class ColorMaxLinearLayout extends LinearLayout {
    private int mLandscapeMaxHeight;
    private int mPortraitMaxHeight;

    public ColorMaxLinearLayout(Context context) {
        super(context);
    }

    public ColorMaxLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorMaxLinearLayout);
        this.mPortraitMaxHeight = typedArray.getDimensionPixelSize(0, 0);
        this.mLandscapeMaxHeight = typedArray.getDimensionPixelSize(1, 0);
        typedArray.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(width, 1073741824);
        int maxHeight = getMaxHeight();
        if (height > maxHeight) {
            super.onMeasure(widthMeasureSpec2, View.MeasureSpec.makeMeasureSpec(maxHeight, 1073741824));
        }
    }

    private int getMaxHeight() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        return metrics.widthPixels < metrics.heightPixels ? this.mPortraitMaxHeight : this.mLandscapeMaxHeight;
    }
}
