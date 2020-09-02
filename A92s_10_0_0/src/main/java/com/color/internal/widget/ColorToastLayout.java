package com.color.internal.widget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColorToastLayout extends LinearLayout {
    private static final boolean DBG = false;
    private static final int HORIZONTAL_MARGIN_DELTA = 8;
    private static final int LAYOUT_MIN_HEIGHT_DP = 34;
    private static final float PIXEL_OFFSET = 0.5f;
    private static final String TAG = "ColorToastLayout";
    private static final float TEXT_ADD_SPACING = 0.0f;
    private static final float TEXT_MULTI_SPACING = 1.2f;
    private static final int TOAST_MARGIN_LEFT = 24;
    private final int mTextColor;
    private final Rect mTextPadding;
    private final float mTextSize;
    private final Typeface mTypeface;

    public ColorToastLayout(Context context) {
        this(context, null);
    }

    public ColorToastLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTextPadding = new Rect();
        setBackgroundResource(201852146);
        this.mTextColor = getResources().getColor(201720857);
        this.mTextSize = getResources().getDimension(201655439);
        this.mTextPadding.left = getResources().getDimensionPixelSize(201655440);
        this.mTextPadding.top = getResources().getDimensionPixelSize(201655441);
        this.mTextPadding.right = getResources().getDimensionPixelSize(201655442);
        this.mTextPadding.bottom = getResources().getDimensionPixelSize(201655443);
        this.mTypeface = Typeface.create("sans-serif-medium", 0);
        setId(201458937);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        TextView textView = (TextView) findViewById(16908299);
        textView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
        textView.setGravity(17);
        textView.setPadding(this.mTextPadding.left, this.mTextPadding.top, this.mTextPadding.right, this.mTextPadding.bottom);
        textView.setTextColor(this.mTextColor);
        textView.setTextSize(0, this.mTextSize);
        Typeface typeface = this.mTypeface;
        if (typeface != null) {
            textView.setTypeface(typeface, 0);
        }
        textView.setMinHeight(dp2px(34));
        textView.setLineSpacing(0.0f, TEXT_MULTI_SPACING);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        int mode = View.MeasureSpec.getMode(widthMeasureSpec);
        Point displaySize = new Point();
        getContext().getDisplay().getRealSize(displaySize);
        int targetWidth = displaySize.x - (dp2px(24) * 2);
        if (size > targetWidth) {
            super.onMeasure(View.MeasureSpec.makeSafeMeasureSpec(targetWidth, mode), heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int dp2px(int dp) {
        return (int) ((getContext().getResources().getDisplayMetrics().density * ((float) dp)) + 0.5f);
    }
}
