package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import oppo.R;

public class ColorCloudServiceView extends ColorEmptyPage {
    private int mContentWidth;
    private int mDefaultColor;
    private Drawable mDefaultDrawable;
    private String mDefaultTextView;
    private int mLineDistance;
    private int mSecondLinePos;
    private int mTextMarginTop;
    private int mTextSize;
    private int mViewMarginTop;
    private int mWidth;

    public ColorCloudServiceView(Context context) {
        this(context, null);
    }

    public ColorCloudServiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393273);
    }

    public ColorCloudServiceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDefaultDrawable = null;
        this.mDefaultColor = 0;
        this.mDefaultTextView = null;
        this.mSecondLinePos = -1;
        this.mWidth = 0;
        this.mLineDistance = 0;
        this.mViewMarginTop = 0;
        this.mContentWidth = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorEmptyPage, defStyle, 0);
        this.mDefaultDrawable = a.getDrawable(0);
        this.mDefaultTextView = a.getString(4);
        this.mTextSize = a.getDimensionPixelSize(1, 0);
        this.mDefaultColor = a.getColor(2, 0);
        this.mTextMarginTop = a.getDimensionPixelSize(3, 0);
        this.mViewMarginTop = a.getDimensionPixelSize(5, 0);
        this.mLineDistance = getResources().getDimensionPixelSize(201655444);
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        initPaint();
        this.mContentWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(this.mContentWidth, this.mViewMarginTop + (this.mDrawable.getIntrinsicHeight() * 2));
    }

    public void initPaint() {
        super.initPaint();
        if (this.mTextView != null) {
            int textLength = this.mTextView.length();
            for (int i = 0; i < textLength; i++) {
                if (10 == this.mTextView.charAt(i)) {
                    this.mSecondLinePos = i;
                    return;
                }
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        int iconWidth = this.mDrawable.getIntrinsicWidth();
        int iconHeight = this.mDrawable.getIntrinsicHeight();
        int left = (this.mContentWidth - iconWidth) / 2;
        int top = this.mViewMarginTop;
        int bottom = top + iconHeight;
        this.mDrawable.setBounds(left, top, left + iconWidth, bottom);
        this.mDrawable.draw(canvas);
        FontMetricsInt fmi = this.mTextPaint.getFontMetricsInt();
        int textY;
        if (this.mSecondLinePos != -1) {
            int textLength = this.mTextView.length();
            String previousText = this.mTextView.substring(0, this.mSecondLinePos);
            textY = (this.mTextMarginTop + bottom) + (fmi.descent - fmi.ascent);
            canvas.drawText(previousText, (float) ((this.mContentWidth - ((int) this.mTextPaint.measureText(previousText))) / 2), (float) textY, this.mTextPaint);
            String rearText = this.mTextView.substring(this.mSecondLinePos + 1, textLength);
            canvas.drawText(rearText, (float) ((this.mContentWidth - ((int) this.mTextPaint.measureText(rearText))) / 2), (float) ((this.mLineDistance + textY) + (fmi.descent - fmi.ascent)), this.mTextPaint);
            return;
        }
        int textX = (this.mContentWidth - ((int) this.mTextPaint.measureText(this.mTextView))) / 2;
        textY = (this.mTextMarginTop + bottom) + (fmi.descent - fmi.ascent);
        canvas.drawText(this.mTextView, (float) textX, (float) textY, this.mTextPaint);
    }
}
