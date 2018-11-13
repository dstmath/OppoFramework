package com.oppo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import oppo.R;

public class OppoWeightedLinearLayout extends LinearLayout {
    private Context mContext;
    private float mMajorWeight;
    private float mMinorWeight;

    public OppoWeightedLinearLayout(Context context) {
        super(context);
    }

    public OppoWeightedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OppoWeightedLinearLayout);
        this.mMajorWeight = a.getFloat(0, 0.0f);
        this.mMinorWeight = a.getFloat(1, 0.0f);
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight;
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        boolean isPortrait = screenWidth < metrics.heightPixels;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        boolean measure = false;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, 1073741824);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, 1073741824);
        if (isPortrait) {
            maxHeight = this.mContext.getResources().getDimensionPixelOffset(201655298);
        } else {
            maxHeight = this.mContext.getResources().getDimensionPixelOffset(201655371);
        }
        if (height > maxHeight) {
            height = maxHeight;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, 1073741824);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        float widthWeight = isPortrait ? this.mMinorWeight : this.mMajorWeight;
        if (widthMode == Integer.MIN_VALUE && widthWeight > 0.0f && ((float) width) < ((float) screenWidth) * widthWeight) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (((float) screenWidth) * widthWeight), 1073741824);
            measure = true;
        }
        if (measure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
