package com.color.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

public class ColorAppFullScreenLayoutLandscape extends LinearLayout {
    private Context mContext;
    private int mDefaultWidth;
    private Locale mLocale;

    public ColorAppFullScreenLayoutLandscape(Context context) {
        this(context, null);
    }

    public ColorAppFullScreenLayoutLandscape(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorAppFullScreenLayoutLandscape(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = null;
        this.mDefaultWidth = 96;
        this.mLocale = null;
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mLocale = context.getResources().getConfiguration().locale;
        boolean hasHeteroFeature = false;
        if (!(this.mContext == null || this.mContext.getPackageManager() == null)) {
            hasHeteroFeature = this.mContext.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        }
        this.mDefaultWidth = hasHeteroFeature ? 136 : 96;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View child = getChildAt(0);
        if (child != null) {
            child.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(this.mDefaultWidth, 1073741824));
            -wrap3(this.mDefaultWidth, child.getMeasuredWidth());
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        View child = getChildAt(0);
        if (child != null) {
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int centerTop = ((bottom - top) - childHeight) / 2;
            int centerLeft = ((right - left) - childWidth) / 2;
            child.layout(centerLeft, centerTop, centerLeft + childWidth, centerTop + childHeight);
            return;
        }
        super.onLayout(b, left, top, right, bottom);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig != null && newConfig.locale != this.mLocale) {
            this.mLocale = newConfig.locale;
            TextView textView = (TextView) findViewById(201458991);
            if (textView != null) {
                textView.setText(201590172);
            }
        }
    }
}
