package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ProgressBar;
import com.color.util.ColorContextUtil;
import oppo.R;

public class ColorTransferProgress extends ColorLoadProgress {
    private static final float FULLANGLE = 360.0f;
    private static final float INITANGLE = 270.0f;
    private final boolean DEBUG;
    private final String TAG;
    private Context mContext;
    private Drawable mDrawable;
    private PaintFlagsDrawFilter mFilter;
    private Paint mPaint;
    private Path mPath;
    private int mStrokeWidth;

    public ColorTransferProgress(Context context) {
        this(context, null);
    }

    public ColorTransferProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 201393250);
    }

    public ColorTransferProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = "ColorDownAndUp";
        this.DEBUG = false;
        this.mFilter = null;
        this.mPath = null;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorLoadProgress, defStyle, 0);
        this.mStrokeWidth = a.getDimensionPixelOffset(6, getResources().getDimensionPixelSize(201655395));
        this.mDrawable = a.getDrawable(0);
        a.recycle();
        init();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mDrawable.getIntrinsicWidth(), this.mDrawable.getIntrinsicHeight());
    }

    private void init() {
        int paintColor = ColorContextUtil.getAttrColor(getContext(), 201392700);
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth((float) this.mStrokeWidth);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mPaint.setColor(paintColor);
        this.mFilter = new PaintFlagsDrawFilter(0, 3);
        this.mPath = new Path();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        Drawable buttonDrawable = this.mDrawable;
        float mScale = (((float) this.mProgress) * FULLANGLE) / ((float) this.mMax);
        if (this.mDrawable != null) {
            int verticalGravity = getGravity() & 112;
            int drawableHeight = buttonDrawable.getIntrinsicHeight();
            int drawableWidth = buttonDrawable.getIntrinsicWidth();
            switch (verticalGravity) {
                case 16:
                    top = (getHeight() - drawableHeight) / 2;
                    break;
                case 80:
                    top = getHeight() - drawableHeight;
                    break;
            }
            bottom = top + drawableHeight;
            left = isLayoutRtl() ? getWidth() - drawableWidth : 0;
            right = isLayoutRtl() ? getWidth() : drawableWidth;
            buttonDrawable.setBounds(left, top, right, bottom);
            buttonDrawable.draw(canvas);
        }
        int strokeWidthHalf = (this.mStrokeWidth + 1) / 2;
        Rect mRect = new Rect(left + strokeWidthHalf, top + strokeWidthHalf, right - strokeWidthHalf, bottom - strokeWidthHalf);
        this.mPath.reset();
        this.mPath.addArc(new RectF(mRect), INITANGLE, mScale);
        canvas.drawPath(this.mPath, this.mPaint);
    }

    public CharSequence getAccessibilityClassName() {
        return ProgressBar.class.getName();
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setItemCount(this.mMax);
        event.setCurrentItemIndex(this.mProgress);
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
    }
}
