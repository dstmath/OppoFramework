package com.color.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColorToastLayout extends LinearLayout {
    private static final boolean DBG = false;
    private static final String TAG = "ColorToastLayout";
    private final int mTextColor;
    private final Rect mTextPadding;
    private final float mTextSize;

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
        setId(201458937);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        TextView textView = (TextView) findViewById(16908299);
        textView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
        textView.setGravity(17);
        textView.setPadding(this.mTextPadding.left, this.mTextPadding.top, this.mTextPadding.right, this.mTextPadding.bottom);
        textView.setTextAppearance(this.mContext, 201523219);
        textView.setTextColor(this.mTextColor);
        textView.setTextSize(0, this.mTextSize);
    }
}
