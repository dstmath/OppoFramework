package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import oppo.R;

public class ColorHintRedDot extends View {
    public static final int NO_POINT_MODE = 0;
    public static final int POINT_ONLY_MODE = 1;
    public static final int POINT_WITH_NUM_MODE = 2;
    private int mBgColor;
    private Paint mBgPaint;
    private int mCircleMaxWidth;
    private int mCircleMinRadius;
    private int mCircleMinWidth;
    private int mCircleNormalRadius;
    private Context mContext;
    private int mIconWidth;
    private int mPointMode;
    private int mPointNumber;
    private int mTextColor;
    private TextPaint mTextPaint;
    private int mTextSize;

    public ColorHintRedDot(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public ColorHintRedDot(Context context, AttributeSet attrs) {
        this(context, attrs, 201393334);
        this.mContext = context;
    }

    public ColorHintRedDot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTextPaint = null;
        this.mBgPaint = new Paint();
        this.mCircleNormalRadius = 0;
        this.mCircleMinRadius = 0;
        this.mCircleMaxWidth = 0;
        this.mCircleMinWidth = 0;
        this.mIconWidth = 0;
        this.mPointMode = 0;
        this.mPointNumber = 0;
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorHintRedDot, defStyle, 0);
        this.mBgColor = a.getColor(2, 0);
        this.mTextColor = a.getColor(3, 0);
        this.mTextSize = a.getDimensionPixelSize(4, 0);
        this.mPointMode = a.getInteger(0, 0);
        this.mPointNumber = a.getInteger(1, 0);
        a.recycle();
        initRedPointResource();
    }

    public void initRedPointResource() {
        this.mTextPaint = new TextPaint(1);
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setColor(this.mTextColor);
        this.mTextPaint.setTextSize((float) this.mTextSize);
        this.mBgPaint.setAntiAlias(true);
        this.mBgPaint.setColor(this.mBgColor);
        this.mBgPaint.setStyle(Style.FILL);
        this.mCircleNormalRadius = (int) this.mContext.getResources().getDimension(201655518);
        this.mCircleMinRadius = (int) this.mContext.getResources().getDimension(201655519);
        this.mCircleMaxWidth = (int) this.mContext.getResources().getDimension(201655520);
        this.mCircleMinWidth = (int) this.mContext.getResources().getDimension(201655521);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getViewWidth(), getViewHeight());
    }

    private int getViewWidth() {
        switch (this.mPointMode) {
            case 0:
                return 0;
            case 1:
                return this.mCircleMinRadius * 2;
            case 2:
                return getBgWidth();
            default:
                return 0;
        }
    }

    private int getViewHeight() {
        switch (this.mPointMode) {
            case 0:
                return 0;
            case 1:
                return this.mCircleMinRadius * 2;
            case 2:
                return getBgHeight();
            default:
                return 0;
        }
    }

    private int getBgWidth() {
        if (this.mPointNumber > 0 && this.mPointNumber < 10) {
            return this.mCircleNormalRadius * 2;
        }
        if (this.mPointNumber < 10 || this.mPointNumber >= 100) {
            return this.mCircleMaxWidth;
        }
        return this.mCircleMinWidth;
    }

    private int getBgHeight() {
        if (this.mPointNumber <= 0 || this.mPointMode >= 10) {
            return this.mCircleNormalRadius * 2;
        }
        return this.mCircleNormalRadius * 2;
    }

    protected void onDraw(Canvas canvas) {
        drawRedPoint(canvas);
    }

    public void drawRedPoint(Canvas canvas) {
        switch (this.mPointMode) {
            case 1:
                drawPointOnly(canvas, this.mCircleMinRadius, this.mCircleMinRadius);
                return;
            case 2:
                drawPointWithNumber(canvas, 0, 0, this.mPointNumber);
                return;
            default:
                return;
        }
    }

    public void drawPointOnly(Canvas canvas, int x, int y) {
        canvas.drawCircle((float) x, (float) y, (float) this.mCircleMinRadius, this.mBgPaint);
    }

    public void drawPointWithNumber(Canvas canvas, int top, int left, int number) {
        if (number > 0) {
            String numSring = String.valueOf(number);
            RectF r = new RectF();
            r.left = (float) left;
            r.top = (float) top;
            r.bottom = r.top + ((float) (this.mCircleNormalRadius * 2));
            r.right = r.left + ((float) getBgWidth());
            if (number >= 100) {
                numSring = "99+";
            }
            canvas.drawRoundRect(r, (float) this.mCircleNormalRadius, (float) this.mCircleNormalRadius, this.mBgPaint);
            FontMetricsInt fmi = this.mTextPaint.getFontMetricsInt();
            canvas.drawText(numSring, (float) ((int) (r.left + (((r.right - r.left) - ((float) ((int) this.mTextPaint.measureText(numSring)))) / 2.0f))), (float) (((int) (((r.top + r.bottom) - ((float) fmi.ascent)) - ((float) fmi.descent))) / 2), this.mTextPaint);
        }
    }

    public void setPointNumber(int num) {
        this.mPointNumber = num;
        requestLayout();
    }

    public void setPointMode(int mode) {
        this.mPointMode = mode;
        requestLayout();
    }

    public int getPointMode() {
        return this.mPointMode;
    }

    public int getPointNumber() {
        return this.mPointNumber;
    }
}
