package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import oppo.R;

public class ColorEmptyPage extends View {
    private static final char NEW_LINE = '\n';
    private static final String TAG = "OppoEmptyBottle";
    private int mContentWidth;
    private int mDefaultColor;
    private Drawable mDefaultDrawable;
    private String mDefaultTextView;
    protected Drawable mDrawable;
    private int mNewLinePos;
    private int mTextMarginTop;
    protected TextPaint mTextPaint;
    private int mTextSize;
    protected String mTextView;
    private int mUserColor;
    private Drawable mUserDrawable;
    private String mUserTextView;
    private int mViewMarginTop;

    public ColorEmptyPage(Context context) {
        this(context, null);
    }

    public ColorEmptyPage(Context context, AttributeSet attrs) {
        this(context, attrs, 201393271);
    }

    public ColorEmptyPage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDefaultColor = -1;
        this.mUserColor = -1;
        this.mTextPaint = null;
        this.mDefaultTextView = null;
        this.mUserTextView = null;
        this.mTextView = null;
        this.mUserDrawable = null;
        this.mDefaultDrawable = null;
        this.mDrawable = null;
        this.mViewMarginTop = 0;
        this.mContentWidth = 0;
        this.mNewLinePos = -1;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorEmptyPage, defStyle, 0);
        this.mDefaultDrawable = a.getDrawable(0);
        this.mDefaultTextView = a.getString(4);
        this.mTextSize = a.getDimensionPixelSize(1, 0);
        this.mDefaultColor = a.getColor(2, 0);
        this.mTextMarginTop = a.getDimensionPixelSize(3, 0);
        this.mViewMarginTop = a.getDimensionPixelSize(5, 0);
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        initPaint();
        this.mContentWidth = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(this.mContentWidth, MeasureSpec.getSize(heightMeasureSpec));
    }

    protected void initPaint() {
        this.mTextPaint = new TextPaint(1);
        this.mTextPaint.setAntiAlias(true);
        if (this.mTextSize != 0) {
            this.mTextPaint.setTextSize((float) this.mTextSize);
        }
        int textColor = this.mUserColor;
        if (textColor == -1) {
            textColor = this.mDefaultColor;
        }
        this.mTextPaint.setColor(textColor);
        this.mDrawable = this.mUserDrawable;
        if (this.mDrawable == null) {
            this.mDrawable = this.mDefaultDrawable;
        }
        if (this.mTextView == null) {
            this.mTextView = this.mDefaultTextView;
        }
    }

    protected void onDraw(Canvas canvas) {
        int top = this.mViewMarginTop;
        int bottom = 0;
        if (this.mDrawable != null) {
            int iconWidth = this.mDrawable.getIntrinsicWidth();
            int left = (this.mContentWidth - iconWidth) / 2;
            bottom = top + this.mDrawable.getIntrinsicHeight();
            this.mDrawable.setBounds(left, top, left + iconWidth, bottom);
            this.mDrawable.draw(canvas);
        }
        if (this.mTextView != null && this.mTextPaint != null) {
            FontMetricsInt fmi = this.mTextPaint.getFontMetricsInt();
            int length = this.mTextView.length();
            int textY;
            if (this.mNewLinePos < 0) {
                int textX = (this.mContentWidth - ((int) this.mTextPaint.measureText(this.mTextView))) / 2;
                textY = (this.mTextMarginTop + bottom) + (fmi.descent - fmi.ascent);
                canvas.drawText(this.mTextView, (float) textX, (float) textY, this.mTextPaint);
                return;
            }
            String textLine1 = this.mTextView.substring(0, this.mNewLinePos);
            textY = (this.mTextMarginTop + bottom) + (fmi.descent - fmi.ascent);
            canvas.drawText(textLine1, (float) ((this.mContentWidth - ((int) this.mTextPaint.measureText(textLine1))) / 2), (float) textY, this.mTextPaint);
            String textLine2 = this.mTextView.substring(this.mNewLinePos + 1, length);
            canvas.drawText(textLine2, (float) ((this.mContentWidth - ((int) this.mTextPaint.measureText(textLine2))) / 2), (float) (textY + (fmi.descent - fmi.ascent)), this.mTextPaint);
        }
    }

    public void setImage(int resId) {
        setImage(getResources().getDrawable(resId));
    }

    public void setImage(Drawable d) {
        this.mUserDrawable = d;
    }

    public void setMessage(int id) {
        setMessage(getResources().getString(id));
    }

    public void setMessage(String msg) {
        this.mUserTextView = msg;
        if (msg != null) {
            this.mTextView = this.mUserTextView;
        }
        int newLinePos = getTextNewLinePos(this.mUserTextView);
        if (newLinePos >= 0) {
            this.mNewLinePos = newLinePos;
        }
        invalidate();
    }

    private int getTextNewLinePos(String text) {
        if (text != null) {
            int length = text.length();
            for (int i = 0; i < length; i++) {
                if (NEW_LINE == text.charAt(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void useDeepColorStyle(boolean deepColorStyle) {
        if (deepColorStyle) {
            Log.e(TAG, "Please don't use deepColorStyle");
        }
    }

    public void setTextColor(int color) {
        this.mUserColor = color;
    }
}
