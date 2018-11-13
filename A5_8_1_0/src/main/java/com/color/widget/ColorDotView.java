package com.color.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ColorDotView extends View {
    private Context mContext;
    private int mDotNumber;
    private int mDotSize;
    private int mHightlightDot;
    private Paint mPaint;
    private float mRadius;

    public ColorDotView(Context context) {
        super(context);
        this.mDotNumber = 1;
        this.mHightlightDot = 0;
        this.mDotSize = 0;
        this.mContext = context;
    }

    public ColorDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDotNumber = 1;
        this.mHightlightDot = 0;
        this.mDotSize = 0;
        this.mRadius = getResources().getDimension(201655401);
        this.mContext = context;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
    }

    public ColorDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mDotNumber = 1;
        this.mHightlightDot = 0;
        this.mDotSize = 0;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDotNumber > 1) {
            for (int i = 0; i < this.mDotNumber; i++) {
                if (this.mHightlightDot == i) {
                    this.mPaint.setColor(getResources().getColor(201720853));
                } else {
                    this.mPaint.setColor(getResources().getColor(201720852));
                }
                if (isLayoutRtl()) {
                    canvas.drawCircle(((((float) (getWidth() / 2)) + (((float) (this.mDotNumber * 2)) * this.mRadius)) - (this.mRadius * 2.0f)) - (((float) (i * 4)) * this.mRadius), (float) (getHeight() / 2), this.mRadius, this.mPaint);
                } else {
                    canvas.drawCircle(((((float) (getWidth() / 2)) - (((float) (this.mDotNumber * 2)) * this.mRadius)) + (this.mRadius * 2.0f)) + (((float) (i * 4)) * this.mRadius), (float) (getHeight() / 2), this.mRadius, this.mPaint);
                }
            }
        }
    }

    public void setDotNumber(int dotnumber) {
        this.mDotNumber = dotnumber;
    }

    public void setDotSize(int size) {
        this.mDotSize = size;
        setDotNumber(this.mContext.getResources().getConfiguration());
    }

    private void setDotNumber(Configuration cfg) {
        int pagerSize;
        if (cfg.orientation == 2) {
            pagerSize = 4;
        } else {
            pagerSize = 8;
        }
        if ((this.mContext instanceof Activity) && ((Activity) this.mContext).isInMultiWindowMode()) {
            pagerSize = 4;
        }
        this.mDotNumber = (int) Math.ceil(((double) this.mDotSize) / ((double) pagerSize));
    }

    public void setHightlightDot(int index) {
        this.mHightlightDot = index;
        invalidate();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mHightlightDot = 0;
        setDotNumber(newConfig);
    }
}
